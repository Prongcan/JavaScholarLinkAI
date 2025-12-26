package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.orchestrator_layer.FetchOrchestrator;
import org.example.orchestrator_layer.RecommendationOrchestrator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrator API
 * 提供对编排器的HTTP访问接口
 */
@WebServlet(name = "orchestratorApi", value = "/api/orchestrator/*")
@Tag(name = "编排器管理", description = "编排器相关的 API 操作")
public class OrchestratorApi extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try {
            if (pathInfo != null && pathInfo.equals("/fetch")) {
                // POST /api/orchestrator/fetch - 启动Fetch Orchestrator
                handleStartFetchOrchestrator(response, out);
            } else if (pathInfo != null && pathInfo.equals("/recommend")) {
                // POST /api/orchestrator/recommend - 启动Recommendation Orchestrator
                handleStartRecommendationOrchestrator(response, out);
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("OrchestratorApi error: " + e.getMessage());
            sendError(response, out, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * 启动Fetch Orchestrator
     */
    @Operation(
        summary = "启动Fetch Orchestrator",
        description = "异步启动Fetch Orchestrator，从arXiv获取最新的计算机科学论文，自动抓取论文信息并创建向量索引",
        responses = {
            @ApiResponse(responseCode = "200", description = "Orchestrator启动成功"),
            @ApiResponse(responseCode = "500", description = "Orchestrator启动失败")
        }
    )
    private void handleStartFetchOrchestrator(HttpServletResponse response,
                                             PrintWriter out) throws IOException {
        try {
            // 异步执行Fetch Orchestrator，避免阻塞HTTP响应
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("🔄 Starting Fetch Orchestrator via API...");
                    FetchOrchestrator orchestrator = new FetchOrchestrator();
                    orchestrator.initDocset();
                    System.out.println("✅ Fetch Orchestrator completed via API");
                } catch (Exception e) {
                    System.err.println("❌ Fetch Orchestrator failed via API: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // 立即返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Fetch Orchestrator started successfully");
            Map<String, Object> data = new HashMap<>();
            data.put("note", "Orchestrator is running asynchronously. Check server logs for progress.");
            result.put("data", data);

            out.print(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            sendError(response, out, 500, "Failed to start Fetch Orchestrator: " + e.getMessage());
        }

        out.flush();
    }

    /**
     * 启动Recommendation Orchestrator
     */
    @Operation(
        summary = "启动Recommendation Orchestrator",
        description = "异步启动Recommendation Orchestrator，基于用户兴趣计算论文相似度并生成个性化推荐博客",
        responses = {
            @ApiResponse(responseCode = "200", description = "Orchestrator启动成功"),
            @ApiResponse(responseCode = "500", description = "Orchestrator启动失败")
        }
    )
    private void handleStartRecommendationOrchestrator(HttpServletResponse response,
                                                       PrintWriter out) throws IOException {
        try {
            // 异步执行Recommendation Orchestrator，避免阻塞HTTP响应
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("🧠 Starting Recommendation Orchestrator via API...");
                    RecommendationOrchestrator orchestrator = new RecommendationOrchestrator();
                    orchestrator.generateRecommendations();
                    System.out.println("✅ Recommendation Orchestrator completed via API");
                } catch (Exception e) {
                    System.err.println("❌ Recommendation Orchestrator failed via API: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // 立即返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Recommendation Orchestrator started successfully");
            Map<String, Object> data = new HashMap<>();
            data.put("note", "Orchestrator is running asynchronously. Check server logs for progress.");
            result.put("data", data);

            out.print(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            sendError(response, out, 500, "Failed to start Recommendation Orchestrator: " + e.getMessage());
        }

        out.flush();
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out,
                          int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        out.print(objectMapper.writeValueAsString(error));
        out.flush();
    }
}
