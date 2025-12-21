package com.scholarlink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAIConfig {
    private String apiKey;
    private String baseUrl;
    private String embeddingModel = "text-embedding-3-small";
    private String chatModel = "gpt-4o-mini";
    private Integer timeout = 60;
    private String language = "zh";
}

