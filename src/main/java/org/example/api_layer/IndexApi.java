package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.example.service.IndexService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 索引API
 * 处理论文向量索引相关的操作
 */
@WebServlet(name = "indexApi", value = "/api/index/*")
@Tag(name = "索引管理", description = "论文向量索引相关的 API 操作")
public class IndexApi extends HttpServlet {
    private ObjectMapper objectMapper;
    private IndexService indexService;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        try {
            indexService = new IndexService();
            System.out.println("IndexApi: IndexService initialized successfully");
        } catch (Exception e) {
            System.err.println("IndexApi: Failed to initialize IndexService: " + e.getMessage());
            throw new ServletException("Failed to initialize IndexService", e);
        }
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
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/paper")) {
                // POST /api/index/paper - 为论文创建向量索引
                handleIndexPaper(request, response, out);
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("IndexApi error: " + e.getMessage());
            sendError(response, out, 500, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try {
            if (pathInfo != null && pathInfo.matches("/paper/\\d+/status")) {
                // GET /api/index/paper/{paperId}/status - 检查论文索引状态
                String paperIdStr = pathInfo.substring(7, pathInfo.indexOf("/status"));
                try {
                    int paperId = Integer.parseInt(paperIdStr);
                    handleCheckIndexStatus(paperId, response, out);
                } catch (NumberFormatException e) {
                    sendError(response, out, 400, "Invalid paper ID format");
                }
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("IndexApi error: " + e.getMessage());
            sendError(response, out, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * 处理论文索引请求
     */
    @Operation(
        summary = "为论文创建向量索引",
        description = "将指定论文的摘要转换为向量并存储到向量数据库中",
        requestBody = @RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object", example = "{\"paper_id\": 123}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "索引创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "404", description = "论文不存在"),
            @ApiResponse(responseCode = "409", description = "论文已建立索引"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
        }
    )
    private void handleIndexPaper(HttpServletRequest request, HttpServletResponse response,
                                  PrintWriter out) throws IOException {
        // 读取请求体
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            jsonBody.append(line);
        }

        Map<String, Object> requestData;
        try {
            requestData = objectMapper.readValue(jsonBody.toString(), Map.class);
        } catch (Exception e) {
            sendError(response, out, 400, "Invalid JSON format");
            return;
        }

        // 获取paper_id
        Integer paperId = null;
        if (requestData.containsKey("paper_id")) {
            Object paperIdObj = requestData.get("paper_id");
            if (paperIdObj instanceof Number) {
                paperId = ((Number) paperIdObj).intValue();
            } else if (paperIdObj instanceof String) {
                try {
                    paperId = Integer.parseInt((String) paperIdObj);
                } catch (NumberFormatException e) {
                    sendError(response, out, 400, "Invalid paper_id format");
                    return;
                }
            }
        }

        if (paperId == null) {
            sendError(response, out, 400, "paper_id is required");
            return;
        }

        try {
            // 检查是否已建立索引
            if (indexService.isPaperIndexed(paperId)) {
                sendError(response, out, 409, "Paper already indexed");
                return;
            }

            // 创建索引
            boolean success = indexService.indexPaper(paperId);

            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "论文索引创建成功");
                Map<String, Object> data = new HashMap<>();
                data.put("paper_id", paperId);
                result.put("data", data);

                out.print(objectMapper.writeValueAsString(result));
            } else {
                sendError(response, out, 500, "Failed to create paper index");
            }

        } catch (IllegalArgumentException e) {
            sendError(response, out, 404, e.getMessage());
        } catch (IOException e) {
            sendError(response, out, 500, "Failed to generate embedding: " + e.getMessage());
        } catch (Exception e) {
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }

        out.flush();
    }

    /**
     * 检查论文索引状态
     */
    @Operation(
        summary = "检查论文索引状态",
        description = "检查指定论文是否已建立向量索引",
        parameters = {
            @Parameter(name = "paperId", description = "论文ID", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
        }
    )
    private void handleCheckIndexStatus(int paperId, HttpServletResponse response,
                                       PrintWriter out) throws IOException {
        try {
            boolean isIndexed = indexService.isPaperIndexed(paperId);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "索引状态查询成功");
            Map<String, Object> data = new HashMap<>();
            data.put("paper_id", paperId);
            data.put("indexed", isIndexed);
            result.put("data", data);

            out.print(objectMapper.writeValueAsString(result));

        } catch (Exception e) {
            sendError(response, out, 500, "Database error: " + e.getMessage());
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
