package com.scholarlink.controller;

import com.scholarlink.config.AppConfig;
import com.scholarlink.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {
    
    @Autowired
    private AppConfig appConfig;
    
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> index() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "欢迎使用 ScholarLink AI 后端服务");
        data.put("version", appConfig.getApi().getVersion());
        data.put("status", "running");
        data.put("environment", appConfig.getEnv());
        
        return ResponseEntity.ok(ApiResponse.success("服务运行正常", data));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "healthy");
        data.put("message", "服务运行正常");
        data.put("version", appConfig.getApi().getVersion());
        data.put("environment", appConfig.getEnv());
        
        return ResponseEntity.ok(ApiResponse.success("健康检查通过", data));
    }
}

