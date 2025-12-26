package org.example.orchestrator_layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.data_access_layer.Dbmanager;
import org.example.service.IndexService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Recommendation Orchestrator
 * 基于用户兴趣和论文向量计算相似度，生成个性化推荐博客
 */
public class RecommendationOrchestrator {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String GENERATE_BLOG_API_URL = "http://localhost:8080/api/papers";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Dbmanager dbManager;
    private final IndexService indexService;

    public RecommendationOrchestrator() {
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)  // 生成博客需要较长时间
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.dbManager = new Dbmanager();

        // 初始化向量索引服务
        try {
            this.indexService = new IndexService();
            System.out.println("✅ IndexService initialized for recommendations");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize IndexService: " + e.getMessage());
            throw new RuntimeException("Failed to initialize IndexService", e);
        }
    }

    /**
     * 生成所有用户的个性化推荐
     */
    public void generateRecommendations() {
        try {
            System.out.println("🎯 Starting Recommendation Orchestrator...");

            // 获取所有用户
            List<Map<String, Object>> users = dbManager.getAllUsers(1, 1000); // 获取所有用户
            System.out.println("📊 Found " + users.size() + " users to process");

            // 获取所有已建立索引的论文
            List<Map<String, Object>> indexedPapers = getIndexedPapers();
            System.out.println("📄 Found " + indexedPapers.size() + " indexed papers");

            if (indexedPapers.isEmpty()) {
                System.out.println("⚠️ No indexed papers found. Please run Fetch Orchestrator first.");
                return;
            }

            // 为每个用户生成推荐
            for (Map<String, Object> user : users) {
                try {
                    int userId = (Integer) user.get("user_id");
                    String username = (String) user.get("username");
                    String interest = (String) user.get("interest");

                    System.out.println("👤 Processing user: " + username + " (ID: " + userId + ")");

                    if (interest == null || interest.trim().isEmpty()) {
                        System.out.println("   ⚠️ User " + username + " has no interest specified, skipping...");
                        continue;
                    }

                    // 生成用户兴趣的向量
                    List<Double> userInterestVector = generateInterestVector(interest);

                    // 计算与所有论文的相似度，获取top3
                    List<PaperSimilarity> topSimilarPapers = findTopSimilarPapers(userInterestVector, indexedPapers, 3);

                    // 为top3论文生成推荐博客
                    for (PaperSimilarity paperSim : topSimilarPapers) {
                        try {
                            generateRecommendationBlog(userId, paperSim.paperId, paperSim.similarity);
                            // 添加延迟避免API限流
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            System.err.println("   ❌ Failed to generate blog for user " + userId + ", paper " + paperSim.paperId + ": " + e.getMessage());
                        }
                    }

                    System.out.println("   ✅ Completed recommendations for user: " + username);

                } catch (Exception e) {
                    System.err.println("   ❌ Error processing user " + user.get("username") + ": " + e.getMessage());
                }
            }

            System.out.println("🎉 Recommendation Orchestrator completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in generateRecommendations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成用户兴趣的向量表示
     * @param interest 用户兴趣字符串
     * @return 向量列表
     */
    private List<Double> generateInterestVector(String interest) throws IOException {
        System.out.println("   🔄 Generating vector for interest: " + interest);

        // 使用IndexService生成向量（复用相同的逻辑）
        // 由于IndexService是为论文设计的，我们需要临时创建一个模拟的论文向量
        // 这里直接调用Gemini API为兴趣文本生成向量

        return indexService.generateEmbedding(interest);
    }

    /**
     * 获取所有已建立索引的论文及其向量
     * @return 论文信息列表，包含向量数据
     */
    private List<Map<String, Object>> getIndexedPapers() throws Exception {
        List<Map<String, Object>> indexedPapers = new ArrayList<>();

        // 获取所有论文
        List<Map<String, Object>> allPapers = dbManager.getAllPapers(1, 10000); // 获取所有论文

        for (Map<String, Object> paper : allPapers) {
            int paperId = (Integer) paper.get("paper_id");

            // 检查是否已建立索引
            if (dbManager.isPaperIndexed(paperId)) {
                // 获取向量数据
                Map<String, Object> embeddingData = dbManager.getPaperEmbedding(paperId);
                if (embeddingData != null) {
                    // 将向量数据添加到论文信息中
                    paper.put("embedding", embeddingData);
                    indexedPapers.add(paper);
                }
            }
        }

        return indexedPapers;
    }

    /**
     * 计算向量之间的余弦相似度
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 相似度分数 (0-1之间，1表示完全相似)
     */
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0; // 避免除零错误
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 解析存储的向量JSON字符串
     * @param embeddingJson JSON字符串
     * @return 向量列表
     */
    private List<Double> parseEmbeddingJson(String embeddingJson) throws IOException {
        List<Double> embedding = new ArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(embeddingJson);

        if (jsonNode.isArray()) {
            for (JsonNode value : jsonNode) {
                embedding.add(value.asDouble());
            }
        }

        return embedding;
    }

    /**
     * 查找最相似的top N篇论文
     * @param userVector 用户兴趣向量
     * @param papers 论文列表
     * @param topN 返回的top N数量
     * @return 最相似论文列表，按相似度降序排列
     */
    private List<PaperSimilarity> findTopSimilarPapers(List<Double> userVector,
                                                      List<Map<String, Object>> papers,
                                                      int topN) throws IOException {
        List<PaperSimilarity> similarities = new ArrayList<>();

        for (Map<String, Object> paper : papers) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> embeddingData = (Map<String, Object>) paper.get("embedding");
                String embeddingJson = (String) embeddingData.get("embedding");

                List<Double> paperVector = parseEmbeddingJson(embeddingJson);
                double similarity = cosineSimilarity(userVector, paperVector);

                int paperId = (Integer) paper.get("paper_id");
                String title = (String) paper.get("title");

                similarities.add(new PaperSimilarity(paperId, title, similarity));

            } catch (Exception e) {
                System.err.println("   ⚠️ Error calculating similarity for paper " + paper.get("paper_id") + ": " + e.getMessage());
            }
        }

        // 按相似度降序排序
        similarities.sort((a, b) -> Double.compare(b.similarity, a.similarity));

        // 返回top N
        return similarities.subList(0, Math.min(topN, similarities.size()));
    }

    /**
     * 为指定用户和论文生成推荐博客
     * @param userId 用户ID
     * @param paperId 论文ID
     * @param similarity 相似度分数
     */
    private void generateRecommendationBlog(int userId, int paperId, double similarity) throws IOException {
        System.out.println("   📝 Generating blog for user " + userId + ", paper " + paperId +
                          " (similarity: " + String.format("%.3f", similarity) + ")");

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", userId);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 调用generate-blog API
        String url = GENERATE_BLOG_API_URL + "/" + paperId + "/generate-blog";
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("   ✅ Blog generated successfully for user " + userId + ", paper " + paperId);

                // 解析响应获取recommendation_id
                JsonNode responseJson = objectMapper.readTree(responseBody);
                if (responseJson.has("data") && responseJson.get("data").has("recommendation_id")) {
                    int recommendationId = responseJson.get("data").get("recommendation_id").asInt();
                    System.out.println("   📋 Created recommendation with ID: " + recommendationId);
                }
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("   ❌ Failed to generate blog for user " + userId + ", paper " + paperId +
                                 ": HTTP " + response.code());
                System.err.println("   Error details: " + errorBody);
            }
        }
    }

    /**
     * 论文相似度内部类
     */
    private static class PaperSimilarity {
        int paperId;
        String title;
        double similarity;

        PaperSimilarity(int paperId, String title, double similarity) {
            this.paperId = paperId;
            this.title = title;
            this.similarity = similarity;
        }
    }

    /**
     * 主方法 - 运行Recommendation Orchestrator
     */
    public static void main(String[] args) {
        System.out.println("🎯 Starting Recommendation Orchestrator...");
        System.out.println("🧠 Generating personalized recommendations based on user interests...");

        RecommendationOrchestrator orchestrator = new RecommendationOrchestrator();
        orchestrator.generateRecommendations();

        System.out.println("🎉 Recommendation Orchestrator completed!");
    }
}
