package com.scholarlink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    
    private Api api = new Api();
    private String env;
    private Boolean debug;
    
    @Data
    public static class Api {
        private String title;
        private String version;
        private String description;
    }
}

