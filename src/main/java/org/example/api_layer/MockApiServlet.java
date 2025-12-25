package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "mockApiServlet", value = "/api/mock")
@Tag(name = "模拟接口", description = "用于测试的模拟 API 接口")
public class MockApiServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Operation(
        summary = "模拟接口",
        description = "根据不同的 action 参数执行相应的模拟操作",
        parameters = {
            @Parameter(
                name = "action",
                description = "操作类型",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = {"readPaper", "uploadPaper", "searchPaper", "deletePaper"}
                )
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "操作成功",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object")
                )
            ),
            @ApiResponse(responseCode = "400", description = "缺少 action 参数或未知操作"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
        }
    )
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
