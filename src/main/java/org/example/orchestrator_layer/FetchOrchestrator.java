package org.example.orchestrator_layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.data_access_layer.Dbmanager;
import org.example.service.IndexService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Fetch Orchestrator
 * 负责从arXiv获取论文信息并调用后端API进行处理
 */
public class FetchOrchestrator {
    private static final String ARXIV_API_BASE = "http://export.arxiv.org/api/query";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String PAPERS_API_URL = "http://localhost:8080/api/papers/fetch";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Dbmanager dbManager;
    private final IndexService indexService;

    public FetchOrchestrator() {
        // 配置HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.dbManager = new Dbmanager();

        // 初始化向量索引服务
        try {
            this.indexService = new IndexService();
            System.out.println("✅ IndexService initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize IndexService: " + e.getMessage());
            throw new RuntimeException("Failed to initialize IndexService", e);
        }
    }

    /**
     * 初始化文档集 - 获取昨天到今天的所有arXiv论文
     */
    public void initDocset() {
        try {
            // 获取昨天到今天的时间范围
            LocalDate today = LocalDate.now();
            LocalDate Days4Ago = today.minusDays(4);
            LocalDate Days5Ago = today.minusDays(5);

            String startTime = Days5Ago.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0000";
            String endTime = Days4Ago.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0000";

            System.out.println("Fetching arXiv papers from " + Days5Ago + " to " + Days4Ago);

            // 获取arXiv论文ID列表
            List<String> arxivIds = fetchArxivIds(startTime, endTime);

            System.out.println("Found " + arxivIds.size() + " papers to process");

            // 逐个处理论文
            for (String arxivId : arxivIds) {
                try {
                    processPaper(arxivId);
                    // 添加延迟避免API限流
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Failed to process paper " + arxivId + ": " + e.getMessage());
                }
            }

            System.out.println("Fetch orchestrator completed successfully!");

        } catch (Exception e) {
            System.err.println("Error in initDocset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从arXiv API获取论文ID列表
     * @param startTime 开始时间 (yyyyMMddHHmm格式)
     * @param endTime 结束时间 (yyyyMMddHHmm格式)
     * @return arXiv ID列表
     */
    private List<String> fetchArxivIds(String startTime, String endTime) throws IOException {
        List<String> arxivIds = new ArrayList<>();

        // arXiv搜索查询：计算机科学类别，指定时间范围
        String query = "cat:cs.*";
        String dateQuery = "submittedDate:[" + startTime + " TO " + endTime + "]";

        // 构建完整查询
        String fullQuery = query + " AND " + dateQuery;

        // arXiv API URL
        String url = ARXIV_API_BASE + "?search_query=" + java.net.URLEncoder.encode(fullQuery, "UTF-8")
                   + "&start=0&max_results=5&sortBy=submittedDate&sortOrder=descending";

        System.out.println("Fetching from arXiv API: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("arXiv API error: " + response.code());
            }

            String responseBody = response.body().string();
            arxivIds = parseArxivResponse(responseBody);

            System.out.println("Successfully fetched " + arxivIds.size() + " paper IDs from arXiv");
        }

        return arxivIds;
    }

    /**
     * 解析arXiv XML响应获取论文ID
     * @param xmlResponse arXiv API响应XML
     * @return arXiv ID列表
     */
    private List<String> parseArxivResponse(String xmlResponse) {
        List<String> arxivIds = new ArrayList<>();

        // 简单的XML解析，提取id标签中的arXiv ID
        String[] entries = xmlResponse.split("<entry>");
        for (String entry : entries) {
            if (entry.contains("<id>") && entry.contains("arxiv.org/abs/")) {
                int idStart = entry.indexOf("<id>") + 4;
                int idEnd = entry.indexOf("</id>", idStart);
                if (idStart > 0 && idEnd > idStart) {
                    String fullUrl = entry.substring(idStart, idEnd);
                    // 提取arXiv ID (URL最后一部分)
                    String arxivId = fullUrl.substring(fullUrl.lastIndexOf("/") + 1);
                    if (!arxivId.isEmpty()) {
                        arxivIds.add(arxivId);
                    }
                }
            }
        }

        return arxivIds;
    }

    /**
     * 处理单个论文 - 调用后端API
     * @param arxivId arXiv论文ID
     */
    private void processPaper(String arxivId) throws IOException {
        System.out.println("Processing paper: " + arxivId);

        // 构建请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("arxiv_id", arxivId);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 调用Papers API
        Request request = new Request.Builder()
                .url(PAPERS_API_URL)
                .post(RequestBody.create(jsonBody, JSON))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("✅ Successfully processed paper: " + arxivId);

                // 解析响应获取paper_id
                JsonNode responseJson = objectMapper.readTree(responseBody);
                if (responseJson.has("data") && responseJson.get("data").has("paper_id")) {
                    int paperId = responseJson.get("data").get("paper_id").asInt();
                    System.out.println("   → Created paper with ID: " + paperId);

                    // 为论文创建向量索引
                    try {
                        System.out.println("   🔄 Creating vector embedding for paper ID: " + paperId);
                        boolean embeddingSuccess = indexService.indexPaper(paperId);
                        if (embeddingSuccess) {
                            System.out.println("   ✅ Vector embedding created successfully for paper ID: " + paperId);
                        } else {
                            System.err.println("   ❌ Failed to create vector embedding for paper ID: " + paperId);
                        }
                    } catch (Exception e) {
                        System.err.println("   ❌ Error creating embedding for paper ID " + paperId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("   ❌ Invalid response format: missing paper_id");
                }
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("❌ Failed to process paper " + arxivId + ": HTTP " + response.code());
                System.err.println("   Error: " + errorBody);
            }
        }
    }

    /**
     * 主方法 - 运行Fetch Orchestrator
     */
    public static void main(String[] args) {
        System.out.println("🚀 Starting Fetch Orchestrator...");
        System.out.println("📅 Fetching papers from yesterday to today...");

        FetchOrchestrator orchestrator = new FetchOrchestrator();
        orchestrator.initDocset();

        System.out.println("✨ Fetch Orchestrator completed!");
    }
}
