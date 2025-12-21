package com.scholarlink.controller;

import com.scholarlink.config.AppConfig;
import com.scholarlink.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hello")
public class HelloController {
    
    @Autowired
    private AppConfig appConfig;
    
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hello() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "ScholarLink AI Backend");
        data.put("version", appConfig.getApi().getVersion());
        data.put("description", "这是一个基于 Spring Boot 的 Java 后端服务");
        
        return ResponseEntity.ok(ApiResponse.success(
                "Hello World! 欢迎使用 ScholarLink AI API",
                data
        ));
    }
    
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> helloName(@PathVariable String name) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("service", "ScholarLink AI Backend");
        data.put("version", appConfig.getApi().getVersion());
        
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + name + "! 欢迎使用 ScholarLink AI API",
                data
        ));
    }
    
    @PostMapping("/post")
    public ResponseEntity<ApiResponse<Map<String, Object>>> helloPost(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String message = request.get("message");
        
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请提供 name 参数"));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("custom_message", message);
        data.put("service", "ScholarLink AI Backend");
        data.put("version", appConfig.getApi().getVersion());
        
        String responseMessage = "Hello " + name + "! " + 
                (message != null && !message.isEmpty() ? message : "欢迎使用 ScholarLink AI API");
        
        return ResponseEntity.ok(ApiResponse.success(responseMessage, data));
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("GET /api/hello/", "基础 Hello World");
        endpoints.put("GET /api/hello/{name}", "带参数的 Hello");
        endpoints.put("POST /api/hello/post", "POST 请求的 Hello");
        endpoints.put("GET /api/hello/status", "API 状态检查");
        
        Map<String, Object> data = new HashMap<>();
        data.put("endpoints", endpoints);
        
        return ResponseEntity.ok(ApiResponse.success("Hello API 运行正常", data));
    }
}

