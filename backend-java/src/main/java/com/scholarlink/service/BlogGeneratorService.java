package com.scholarlink.service;

import com.scholarlink.config.OpenAIConfig;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BlogGeneratorService {
    
    private final OpenAiService openAiService;
    private final String model;
    private final String language;
    private final int timeout;
    private final WebClient webClient;
    
    @Autowired
    public BlogGeneratorService(OpenAIConfig openAIConfig) {
        this.model = openAIConfig.getChatModel();
        this.language = openAIConfig.getLanguage();
        this.timeout = openAIConfig.getTimeout();
        
        if (openAIConfig.getBaseUrl() != null && !openAIConfig.getBaseUrl().isEmpty()) {
            this.openAiService = new OpenAiService(openAIConfig.getApiKey(),
                    java.time.Duration.ofSeconds(timeout));
        } else {
            this.openAiService = new OpenAiService(openAIConfig.getApiKey(),
                    java.time.Duration.ofSeconds(timeout));
        }
        
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * 从PDF URL生成博客
     */
    public String generateFromPdfUrl(String pdfUrl) {
        try {
            // 1. 下载PDF
            byte[] pdfBytes = downloadPdf(pdfUrl);
            
            // 2. 提取文本
            String text = extractTextFromPdf(pdfBytes);
            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("未能从该PDF中提取到文本");
            }
            
            // 3. 分段处理
            List<String> chunks = chunkText(text, 8000);
            
            // 4. 逐段提取要点
            List<String> points = new ArrayList<>();
            for (String chunk : chunks) {
                String point = extractKeyPoints(chunk);
                points.add(point);
            }
            
            String merged = String.join("\n\n", points);
            
            // 5. 生成最终博客
            String blog = generateFinalBlog(merged, pdfUrl);
            
            return blog;
            
        } catch (Exception e) {
            log.error("生成博客失败", e);
            throw new RuntimeException("生成博客失败: " + e.getMessage(), e);
        }
    }
    
    private byte[] downloadPdf(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .block(java.time.Duration.ofSeconds(timeout));
    }
    
    private String extractTextFromPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            log.error("提取PDF文本失败", e);
            throw new RuntimeException("提取PDF文本失败: " + e.getMessage(), e);
        }
    }
    
    private List<String> chunkText(String text, int size) {
        text = text.trim();
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> chunks = new ArrayList<>();
        int i = 0;
        int n = text.length();
        
        while (i < n) {
            int j = Math.min(i + size, n);
            chunks.add(text.substring(i, j));
            i = j;
        }
        
        return chunks;
    }
    
    private String extractKeyPoints(String chunk) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt()));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), getChunkPrompt(chunk)));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(0.3)
                .build();
        
        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
        
        return response != null ? response.trim() : "";
    }
    
    private String generateFinalBlog(String merged, String pdfUrl) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt()));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), getFinalPrompt(merged, pdfUrl)));
        
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(0.4)
                .build();
        
        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
        
        return response != null ? response.trim() : "";
    }
    
    private String getSystemPrompt() {
        if (language.toLowerCase().startsWith("zh")) {
            return "你是一名资深的学术科普写作者，擅长将论文内容转化为通俗易懂、结构清晰的技术博客。" +
                   "请严格按照要求输出高质量、可直接发布的 Markdown 中文博客。";
        }
        return "You are a senior technical writer who turns academic papers into clear, engaging blog posts." +
               "Output high-quality, publish-ready Markdown.";
    }
    
    private String getChunkPrompt(String chunk) {
        if (language.toLowerCase().startsWith("zh")) {
            return "请阅读以下论文片段，提取核心要点（中文）：\n\n" +
                   chunk + "\n\n" +
                   "请输出：\n- 关键术语\n- 主要方法或思想\n- 实验或评估要点\n- 结论/启示\n（用紧凑的要点列表，勿超过200字）";
        }
        return "Read the following paper chunk and extract key points (English):\n\n" +
               chunk + "\n\n" +
               "Output bullet points: terms, methods/ideas, experiments/evaluations, conclusions (<= 120 words).";
    }
    
    private String getFinalPrompt(String merged, String pdfUrl) {
        if (language.toLowerCase().startsWith("zh")) {
            return String.format("""
                    根据以下要点，撰写一篇结构完整的中文技术博客（Markdown）：
                    原文链接：%s
                    
                    要点汇总：
                    %s
                    
                    写作要求：
                    - 面向有一定基础的工程师与研究生
                    - 采用通俗、准确、具体的语言，不要空话
                    - 必须包含以下章节（使用二级与三级标题组织）：
                      1. 摘要与核心贡献
                      2. 背景与动机
                      3. 方法原理（文本化解释关键公式/流程）
                      4. 实验设置与结果解读（定量/定性，对比方法）
                      5. 局限性与潜在改进
                      6. 应用场景与实践建议
                      7. 相关工作与对比
                      8. TL;DR（3-5条要点）
                    - 输出纯 Markdown
                    """, pdfUrl, merged);
        }
        return String.format("""
                Based on the following points, write a well-structured technical blog post in Markdown.
                Link: %s
                
                Key points:
                %s
                
                Sections: Summary, Background, Method, Experiments, Limitations, Applications, Related Work, TL;DR (3-5 bullets)
                """, pdfUrl, merged);
    }
}

