package com.scholarlink.service;

import com.scholarlink.config.OpenAIConfig;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmbeddingService {
    
    private final OpenAiService openAiService;
    private final String model;
    
    @Autowired
    public EmbeddingService(OpenAIConfig openAIConfig) {
        this.model = openAIConfig.getEmbeddingModel();
        
        // 配置代理（如果需要）
        if (openAIConfig.getBaseUrl() != null && !openAIConfig.getBaseUrl().isEmpty()) {
            this.openAiService = new OpenAiService(openAIConfig.getApiKey(), 
                    java.time.Duration.ofSeconds(openAIConfig.getTimeout()));
        } else {
            this.openAiService = new OpenAiService(openAIConfig.getApiKey(),
                    java.time.Duration.ofSeconds(openAIConfig.getTimeout()));
        }
    }
    
    /**
     * 生成单个文本的嵌入向量
     */
    public List<Double> embedText(String text, boolean normalize) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(List.of(text.trim()))
                    .build();
            
            List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
            
            if (embeddings.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<Double> vector = embeddings.get(0).getEmbedding().stream()
                    .map(f -> f != null ? f.doubleValue() : 0.0)
                    .collect(Collectors.toList());
            
            if (normalize) {
                vector = normalizeL2(vector);
            }
            
            return vector;
            
        } catch (Exception e) {
            log.error("生成嵌入向量失败", e);
            throw new RuntimeException("生成嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量生成文本嵌入向量
     */
    public List<List<Double>> embedTexts(List<String> texts, boolean normalize, int batchSize) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<List<Double>> results = new ArrayList<>();
        int start = 0;
        int n = texts.size();
        
        while (start < n) {
            int end = Math.min(start + batchSize, n);
            List<String> batch = texts.subList(start, end).stream()
                    .map(t -> t != null ? t.trim() : " ")
                    .collect(Collectors.toList());
            
            try {
                EmbeddingRequest request = EmbeddingRequest.builder()
                        .model(model)
                        .input(batch)
                        .build();
                
                List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
                
                List<List<Double>> batchResults = embeddings.stream()
                        .map(e -> {
                            List<Double> vec = e.getEmbedding().stream()
                                    .map(f -> f != null ? f.doubleValue() : 0.0)
                                    .collect(Collectors.toList());
                            return normalize ? normalizeL2(vec) : vec;
                        })
                        .collect(Collectors.toList());
                
                results.addAll(batchResults);
                
            } catch (Exception e) {
                log.error("批量生成嵌入向量失败", e);
                // 为失败的批次添加空向量
                for (int i = 0; i < batch.size(); i++) {
                    results.add(new ArrayList<>());
                }
            }
            
            start = end;
        }
        
        return results;
    }
    
    /**
     * L2归一化
     */
    private List<Double> normalizeL2(List<Double> vector) {
        double sum = vector.stream().mapToDouble(v -> v * v).sum();
        if (sum <= 0) {
            return vector;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return vector;
        }
        return vector.stream().map(v -> v / norm).collect(Collectors.toList());
    }
}

