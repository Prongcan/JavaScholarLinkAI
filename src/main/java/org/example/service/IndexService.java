package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.data_access_layer.Dbmanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

/**
 * 索引服务
 * 使用OpenAI Embeddings API将论文摘要转换为向量并存储到向量数据库
 */
public class IndexService {
    private static final String CONFIG_FILE = "vector.properties";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Dbmanager dbManager;

    private String apiUrl;
    private String model;
    private String apiKey;
    private boolean proxyEnabled;
    private String proxyHost;
    private int proxyPort;

    public IndexService() throws IOException {
        this.objectMapper = new ObjectMapper();
        this.dbManager = new Dbmanager();
        loadConfiguration();

        // 配置HTTP客户端
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        // 配置代理
        if (proxyEnabled) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, proxyPort));
            clientBuilder.proxy(proxy);
            System.out.println("IndexService: Proxy configured - " + proxyHost + ":" + proxyPort);
        }

        this.httpClient = clientBuilder.build();
    }

    /**
     * 加载配置文件
     */
    private void loadConfiguration() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Configuration file not found: " + CONFIG_FILE);
            }
            props.load(input);

            // 加载向量数据库配置
            String vectorDbUrl = props.getProperty("vectordb.url");
            String vectorDbUser = props.getProperty("vectordb.user");
            String vectorDbPassword = props.getProperty("vectordb.password");

            // 加载OpenAI配置
            this.apiUrl = props.getProperty("embeddings.api_url");
            this.model = props.getProperty("embeddings.model");

            // 优先从环境变量获取API密钥
            this.apiKey = System.getenv(props.getProperty("embeddings.api_key_env"));
            if (this.apiKey == null || this.apiKey.isEmpty()) {
                this.apiKey = props.getProperty("embeddings.api_key");
            }

            // 加载代理配置
            this.proxyEnabled = Boolean.parseBoolean(props.getProperty("proxy.enabled", "false"));
            this.proxyHost = props.getProperty("proxy.host", "127.0.0.1");
            this.proxyPort = Integer.parseInt(props.getProperty("proxy.port", "7897"));

            if (this.apiKey == null || this.apiKey.isEmpty()) {
                throw new IOException("Gemini API key is not configured. Please set the environment variable or configure in " + CONFIG_FILE);
            }

            System.out.println("IndexService: Configuration loaded successfully");
            System.out.println("IndexService: Using Gemini model: " + this.model);
            System.out.println("IndexService: Proxy enabled: " + this.proxyEnabled);

        } catch (IOException e) {
            System.err.println("IndexService: Failed to load configuration: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 为论文创建向量索引
     * @param paperId 论文ID
     * @return 是否成功
     * @throws IOException 如果API调用失败
     * @throws Exception 如果数据库操作失败
     */
    public boolean indexPaper(int paperId) throws Exception {
        // 获取论文信息
        Map<String, Object> paper = dbManager.getPaperById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("Paper not found with ID: " + paperId);
        }

        String abstractText = (String) paper.get("abstract");
        if (abstractText == null || abstractText.trim().isEmpty()) {
            throw new IllegalArgumentException("Paper has no abstract: " + paperId);
        }

        // 生成向量
        List<Double> embedding = generateEmbedding(abstractText);

        // 存储到向量数据库
        return storeEmbedding(paperId, embedding);
    }

    /**
     * 使用Gemini API生成文本向量
     * @param text 要转换为向量的文本
     * @return 向量列表
     * @throws IOException 如果API调用失败
     */
    public List<Double> generateEmbedding(String text) throws IOException {
        // 构建请求体 - Gemini格式
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "models/" + model);
        requestBody.put("content", Map.of("parts", new Object[]{Map.of("text", text)}));

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 构建请求 - 使用x-goog-api-key头
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(jsonBody, JSON))
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        // 发送请求
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Gemini API error (HTTP " + response.code() + "): " + errorBody);
            }

            String responseBody = response.body().string();
            return parseGeminiEmbeddingResponse(responseBody);
        }
    }

    /**
     * 解析Gemini Embeddings API响应
     * @param jsonResponse API响应JSON
     * @return 向量列表
     * @throws IOException 如果解析失败
     */
    private List<Double> parseGeminiEmbeddingResponse(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Gemini API响应格式: {"embedding": {"values": [float, float, ...]}}
        if (rootNode.has("embedding") && rootNode.get("embedding").has("values")) {
            JsonNode valuesNode = rootNode.get("embedding").get("values");
            if (valuesNode.isArray()) {
                // 将JsonNode转换为List<Double>
                List<Double> embedding = new java.util.ArrayList<>();
                for (JsonNode value : valuesNode) {
                    embedding.add(value.asDouble());
                }

                return embedding;
            }
        }

        throw new IOException("Invalid response format from Gemini Embeddings API: " + jsonResponse);
    }

    /**
     * 将向量存储到向量数据库
     * @param paperId 论文ID
     * @param embedding 向量数据
     * @return 是否成功
     * @throws Exception 如果数据库操作失败
     */
    private boolean storeEmbedding(int paperId, List<Double> embedding) throws Exception {
        // 将向量转换为JSON字符串存储
        String embeddingJson = objectMapper.writeValueAsString(embedding);

        // 存储到paper_embeddings表
        return dbManager.insertPaperEmbedding(paperId, embeddingJson, embedding.size());
    }

    /**
     * 检查论文是否已经建立索引
     * @param paperId 论文ID
     * @return 是否已建立索引
     * @throws Exception 如果数据库查询失败
     */
    public boolean isPaperIndexed(int paperId) throws Exception {
        return dbManager.isPaperIndexed(paperId);
    }

    /**
     * 批量索引论文
     * @param paperIds 论文ID列表
     * @return 成功索引的数量
     * @throws Exception 如果操作失败
     */
    public int batchIndexPapers(List<Integer> paperIds) throws Exception {
        int successCount = 0;

        for (Integer paperId : paperIds) {
            try {
                if (!isPaperIndexed(paperId)) {
                    if (indexPaper(paperId)) {
                        successCount++;
                        System.out.println("Successfully indexed paper: " + paperId);
                    }
                } else {
                    System.out.println("Paper already indexed: " + paperId);
                }
            } catch (Exception e) {
                System.err.println("Failed to index paper " + paperId + ": " + e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * 计算两个向量的余弦相似度
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 相似度分数 (0-1之间，1表示完全相同)
     */
    public double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0; // 避免除零错误
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 从JSON字符串解析向量
     * @param embeddingJson 向量JSON字符串
     * @return 向量列表
     * @throws IOException 如果解析失败
     */
    public List<Double> parseEmbeddingJson(String embeddingJson) throws IOException {
        return Arrays.asList(objectMapper.readValue(embeddingJson, Double[].class));
    }
}
