package org.example.api_layer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@WebServlet(name = "swaggerUIServlet", urlPatterns = {"/swagger-ui/*"})
public class SwaggerUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            path = "/index.html";
        }

        // 从 webjars 中获取 Swagger UI 文件
        String resourcePath = "/META-INF/resources/webjars/swagger-ui/4.15.5" + path;

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                // 设置正确的 Content-Type
                if (path.endsWith(".html")) {
                    response.setContentType("text/html");
                } else if (path.endsWith(".css")) {
                    response.setContentType("text/css");
                } else if (path.endsWith(".js")) {
                    response.setContentType("application/javascript");
                } else if (path.endsWith(".png")) {
                    response.setContentType("image/png");
                } else if (path.endsWith(".json")) {
                    response.setContentType("application/json");
                }

                response.setCharacterEncoding("UTF-8");

                try (OutputStream outputStream = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}
