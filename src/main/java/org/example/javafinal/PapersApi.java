package org.example.javafinal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "papersApi", value = "/api/papers/*")
public class PapersApi extends HttpServlet {
    private Dbmanager dbManager;
    private ObjectMapper objectMapper;
    
    @Override
    public void init() throws ServletException {
        super.init();
        dbManager = new Dbmanager();
        objectMapper = new ObjectMapper();
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
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // GET /api/papers/list - 获取论文列表
                handleListPapers(request, response, out);
            } else if (pathInfo.startsWith("/")) {
                // GET /api/papers/{paperId} - 获取论文详情
                String paperIdStr = pathInfo.substring(1);
                try {
                    int paperId = Integer.parseInt(paperIdStr);
                    handleGetPaper(paperId, response, out);
                } catch (NumberFormatException e) {
                    sendError(response, out, 400, "Invalid paper ID format");
                }
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
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
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/fetch")) {
                // POST /api/papers/fetch - 抓取论文
                handleFetchPaper(request, response, out);
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * 处理获取论文列表
     */
    private void handleListPapers(HttpServletRequest request, HttpServletResponse response, 
                                   PrintWriter out) throws SQLException, IOException {
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("page_size");
        
        int page = pageParam != null ? Integer.parseInt(pageParam) : 1;
        int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;
        
        List<Map<String, Object>> papers = dbManager.getAllPapers(page, pageSize);
        int total = dbManager.getPaperCount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "获取论文列表成功");
        Map<String, Object> data = new HashMap<>();
        data.put("papers", papers);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("total", total);
        result.put("data", data);
        
        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
    
    /**
     * 处理获取论文详情
     */
    private void handleGetPaper(int paperId, HttpServletResponse response, 
                                PrintWriter out) throws SQLException, IOException {
        Map<String, Object> paper = dbManager.getPaperById(paperId);
        
        if (paper == null) {
            sendError(response, out, 404, "Paper not found");
            return;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "获取论文详情成功");
        result.put("data", paper);
        
        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
    
    /**
     * 处理抓取论文
     */
    private void handleFetchPaper(HttpServletRequest request, HttpServletResponse response, 
                                  PrintWriter out) throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        
        Map<String, String> requestData;
        try {
            requestData = objectMapper.readValue(jsonBody.toString(), Map.class);
        } catch (Exception e) {
            sendError(response, out, 400, "Invalid JSON format");
            return;
        }
        
        String title = requestData.get("title");
        String author = requestData.get("author");
        String abstractText = requestData.get("abstract");
        String pdfUrl = requestData.get("pdf_url");
        
        if (title == null || title.isEmpty()) {
            sendError(response, out, 400, "Title is required");
            return;
        }
        
        int paperId = dbManager.insertPaper(
            title != null ? title : "",
            author != null ? author : "",
            abstractText != null ? abstractText : "",
            pdfUrl != null ? pdfUrl : ""
        );
        
        if (paperId > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "论文抓取成功");
            Map<String, Object> data = new HashMap<>();
            data.put("paper_id", paperId);
            result.put("data", data);
            
            out.print(objectMapper.writeValueAsString(result));
        } else {
            sendError(response, out, 500, "Failed to insert paper");
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
    
    @Override
    public void destroy() {
        super.destroy();
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}

