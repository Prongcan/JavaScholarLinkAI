package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "searchApi", value = "/api/search")
@Tag(name = "搜索管理", description = "论文搜索相关的 API 操作")
public class SearchApi extends HttpServlet {
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();

        // 获取搜索查询参数
        String query = request.getParameter("q");

        // 创建成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "搜索成功");

        Map<String, Object> data = new HashMap<>();
        data.put("query", query != null ? query : "");
        data.put("results_count", 0); // 暂时返回0个结果
        result.put("data", data);

        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();

        // 创建成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "搜索成功");

        Map<String, Object> data = new HashMap<>();
        data.put("results_count", 0); // 暂时返回0个结果
        result.put("data", data);

        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
}