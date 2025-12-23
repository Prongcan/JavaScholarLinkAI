package org.example.javafinal;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "mockApiServlet", value = "/api/mock")
public class MockApiServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, String> jsonResponse = new HashMap<>();

        System.out.println("--- MockApiServlet doGet: START ---");

        try {
            String action = request.getParameter("action");
            System.out.println("Received 'action' parameter: " + action);

            if (action != null) {
                action = action.trim();
                System.out.println("Action after trimming: '" + action + "'");
                String message;
                response.setStatus(HttpServletResponse.SC_OK);

                switch (action) {
                    case "readPaper":
                        message = "阅读论文成功";
                        jsonResponse.put("status", "success");
                        break;
                    case "uploadPaper":
                        message = "上传论文成功";
                        jsonResponse.put("status", "success");
                        break;
                    case "searchPaper":
                        message = "搜索论文成功";
                        jsonResponse.put("status", "success");
                        break;
                    case "deletePaper":
                        message = "删除论文成功";
                        jsonResponse.put("status", "success");
                        break;
                    default:
                        message = "未知操作: '" + action + "'";
                        jsonResponse.put("status", "error");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        break;
                }
                jsonResponse.put("message", message);
                System.out.println("IF block processed. Status: " + jsonResponse.get("status") + ", Message: " + jsonResponse.get("message"));

            } else {
                System.out.println("ELSE block processed (action is null).");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "缺少 'action' 参数");
            }

            String jsonString = objectMapper.writeValueAsString(jsonResponse);
            System.out.println("Final JSON to be sent: " + jsonString);
            out.print(jsonString);

        } catch (Exception e) {
            System.out.println("ERROR in servlet: " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println(sw.toString());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "服务器内部错误，请查看详情。");
            jsonResponse.put("error_details", sw.toString());
            out.print(objectMapper.writeValueAsString(jsonResponse));
        } finally {
            System.out.println("--- MockApiServlet doGet: END ---");
            out.flush();
        }
    }
}
