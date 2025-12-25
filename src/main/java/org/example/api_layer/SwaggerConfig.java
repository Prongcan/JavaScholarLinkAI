package org.example.api_layer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

@OpenAPIDefinition(
    info = @Info(
        title = "JavaFinal API",
        version = "1.0",
        description = "JavaFinal 项目后端 API 文档",
        contact = @Contact(
            name = "开发团队",
            email = "dev@example.com"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = @Server(
        url = "http://localhost:8080",
        description = "本地开发服务器"
    )
)
@WebListener
public class SwaggerConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.setProperty("swagger.scanner.basePackage", "org.example.javafinal");
        System.setProperty("swagger.scanner.resourcePackage", "org.example.javafinal");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 清理工作
    }
}
