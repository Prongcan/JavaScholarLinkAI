package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 博客生成服务
 * 使用DeepSeek API生成论文博客
 */
public class BlogGeneratorService {
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String apiKey; // Not final, can be assigned in constructor

    public BlogGeneratorService() {
        // 配置HTTP客户端，增加超时时间
        // DeepSeek API生成博客可能需要较长时间，所以设置较长的超时
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)      // 连接超时：30秒
                .readTimeout(120, TimeUnit.SECONDS)          // 读取超时：120秒（博客生成可能需要较长时间）
                .writeTimeout(30, TimeUnit.SECONDS)          // 写入超时：30秒
                .build();
        this.objectMapper = new ObjectMapper();
        loadApiKey();
    }
    
    /**
     * 从环境变量或配置文件加载API密钥
     */
    private void loadApiKey() {
        // Try environment variable first
        apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            return;
        }

        // If not found, try properties file
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("api.properties")) {
            if (input != null) {
                props.load(input);
                apiKey = props.getProperty("deepseek.api.key");
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load api.properties: " + e.getMessage());
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Warning: DeepSeek API key is not configured. Please set DEEPSEEK_API_KEY environment variable or create src/main/resources/api.properties file.");
        }
    }

    /**
     * 根据论文信息生成博客
     * @param paperTitle 论文标题
     * @param paperAuthor 论文作者
     * @param paperAbstract 论文摘要
     * @return 生成的博客内容（Markdown格式）
     * @throws IOException 如果API调用失败
     */
    public String generateBlog(String paperTitle, String paperAuthor, String paperAbstract) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("DeepSeek API key is not configured. Please set DEEPSEEK_API_KEY environment variable.");
        }

        // 构建提示词
        String prompt = buildPrompt(paperTitle, paperAuthor, paperAbstract);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        Request request = new Request.Builder()
                .url(DEEPSEEK_API_URL)
                .post(RequestBody.create(jsonBody, JSON))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = "Unknown error";
                String errorDetail = "";
                
                if (response.body() != null) {
                    errorBody = response.body().string();
                    // 尝试解析错误详情
                    try {
                        JsonNode errorNode = objectMapper.readTree(errorBody);
                        if (errorNode.has("error")) {
                            JsonNode error = errorNode.get("error");
                            if (error.has("message")) {
                                errorDetail = error.get("message").asText();
                            } else if (error.has("type")) {
                                errorDetail = error.get("type").asText();
                            }
                        }
                    } catch (Exception e) {
                        // 如果无法解析JSON，使用原始错误体
                        errorDetail = errorBody;
                    }
                }
                
                int statusCode = response.code();
                String errorMessage;
                
                // 根据HTTP状态码提供更友好的错误信息
                switch (statusCode) {
                    case 401:
                        errorMessage = "DeepSeek API认证失败：API密钥无效或已过期。请检查api.properties中的密钥是否正确。";
                        break;
                    case 402:
                    case 403:
                        errorMessage = "DeepSeek API访问被拒绝：可能是账户余额不足或权限不足。请检查DeepSeek账户余额和API权限。";
                        if (!errorDetail.isEmpty()) {
                            errorMessage += " 详细信息: " + errorDetail;
                        }
                        break;
                    case 429:
                        errorMessage = "DeepSeek API请求频率过高：请稍后再试。";
                        break;
                    case 500:
                    case 502:
                    case 503:
                        errorMessage = "DeepSeek API服务器错误：服务暂时不可用，请稍后再试。";
                        break;
                    default:
                        errorMessage = String.format("DeepSeek API错误 (HTTP %d): %s", statusCode, 
                            !errorDetail.isEmpty() ? errorDetail : errorBody);
                }
                
                System.err.println("DeepSeek API Error: HTTP " + statusCode + " - " + errorMessage);
                System.err.println("Response body: " + errorBody);
                throw new IOException(errorMessage);
            }

            String responseBody = response.body().string();
            return parseDeepSeekResponse(responseBody);
        }
    }

    /**
     * 构建生成博客的提示词
     */
    private String buildPrompt(String title, String author, String abstractText) {
        return String.format(
            "请为以下学术论文生成一篇详细的博客文章，使用Markdown格式。博客应该包括：\n" +
            "1. 论文简介和背景\n" +
            "2. 主要研究内容和贡献\n" +
            "3. 技术方法和创新点\n" +
            "4. 实验结果和发现\n" +
            "5. 总结和展望\n\n" +
            "论文信息：\n" +
            "标题：%s\n" +
            "作者：%s\n" +
            "摘要：%s\n\n" +
            "请生成一篇结构清晰、内容丰富的博客文章，使用Markdown格式。",
            title, author, abstractText
        );
    }

    /**
     * 解析DeepSeek API响应
     */
    private String parseDeepSeekResponse(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
            JsonNode choice = rootNode.get("choices").get(0);
            if (choice.has("message") && choice.get("message").has("content")) {
                return choice.get("message").get("content").asText();
            }
        }
        
        throw new IOException("Invalid response format from DeepSeek API");
    }
}

