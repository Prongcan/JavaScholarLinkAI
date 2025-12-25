package org.example.api_layer;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@WebServlet(name = "helloWorldApi", value = "/api/hello")
@Tag(name = "Hello World", description = "简单的 Hello World API")
public class HelloWorldApi extends HttpServlet {

    @Override
    @Operation(
        summary = "获取 Hello World 消息",
        description = "返回一个简单的 Hello World 消息",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "成功响应",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "Hello World")
                )
            )
        }
    )
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print("Hello World");
        out.flush();
    }
}
