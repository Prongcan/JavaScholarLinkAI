package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.data_access_layer.Dbmanager;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "searchApi", value = "/api/search")
@Tag(name = "搜索管理", description = "论文搜索相关的 API 操作")
public class SearchApi extends HttpServlet {
    private ObjectMapper objectMapper;
    private Dbmanager dbManager;

    @Override
    public void init() {
        objectMapper = new ObjectMapper();
        dbManager = new Dbmanager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();

        try {
            // 获取搜索查询参数
            String query = request.getParameter("q");

            if (query == null || query.trim().isEmpty()) {
                // 如果没有查询参数，返回空结果
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "请输入搜索关键词");

                Map<String, Object> data = new HashMap<>();
                data.put("query", "");
                data.put("results_count", 0);
                data.put("results", new ArrayList<>());
                result.put("data", data);

                out.print(objectMapper.writeValueAsString(result));
                out.flush();
                return;
            }

            // 执行搜索
            List<Map<String, Object>> searchResults = searchPapers(query.trim());

            // 创建成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "搜索成功");

            Map<String, Object> data = new HashMap<>();
            data.put("query", query);
            data.put("results_count", searchResults.size());
            data.put("results", searchResults);
            result.put("data", data);

            out.print(objectMapper.writeValueAsString(result));
            out.flush();

        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", "搜索时发生错误: " + e.getMessage());

            out.print(objectMapper.writeValueAsString(errorResult));
            out.flush();
        }
    }

    /**
     * 执行论文搜索
     * 在论文标题和摘要中进行模糊匹配
     * @param query 搜索关键词
     * @return 搜索结果列表
     */
    private List<Map<String, Object>> searchPapers(String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        // 将查询字符串按空格分割，支持多关键词搜索
        String[] keywords = query.split("\\s+");

        // 构建SQL查询，支持在标题和摘要中搜索所有关键词
        StringBuilder sql = new StringBuilder(
            "SELECT paper_id, title, author, abstract, pdf_url FROM papers WHERE "
        );

        // 为每个关键词添加搜索条件
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append("(title LIKE ? OR abstract LIKE ?)");
        }

        sql.append(" ORDER BY paper_id DESC");

        try (var stmt = dbManager.getConnection().prepareStatement(sql.toString())) {
            // 设置参数，每个关键词在标题和摘要中各搜索一次
            int paramIndex = 1;
            for (String keyword : keywords) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(paramIndex++, searchPattern); // 标题搜索
                stmt.setString(paramIndex++, searchPattern); // 摘要搜索
            }

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> paper = new HashMap<>();
                    paper.put("paper_id", rs.getInt("paper_id"));
                    paper.put("title", rs.getString("title"));
                    paper.put("author", rs.getString("author"));
                    paper.put("abstract", rs.getString("abstract"));
                    paper.put("pdf_url", rs.getString("pdf_url"));

                    // 计算匹配度（简单版本：关键词出现次数）
                    String title = rs.getString("title").toLowerCase();
                    String abstractText = rs.getString("abstract").toLowerCase();
                    int matchScore = 0;

                    for (String keyword : keywords) {
                        String lowerKeyword = keyword.toLowerCase();
                        matchScore += countOccurrences(title, lowerKeyword) * 2; // 标题匹配权重更高
                        matchScore += countOccurrences(abstractText, lowerKeyword);
                    }

                    paper.put("match_score", matchScore);
                    results.add(paper);
                }
            }
        }

        // 按匹配度排序
        results.sort((a, b) -> Integer.compare((Integer)b.get("match_score"), (Integer)a.get("match_score")));

        return results;
    }

    /**
     * 计算字符串中某个子串出现的次数
     * @param text 原文
     * @param substring 子串
     * @return 出现次数
     */
    private int countOccurrences(String text, String substring) {
        if (text == null || substring == null || substring.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // POST方法暂时保持原有逻辑，可用于高级搜索
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = response.getWriter();

        // 创建成功响应
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "POST搜索暂未实现");

        Map<String, Object> data = new HashMap<>();
        data.put("results_count", 0);
        result.put("data", data);

        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
}