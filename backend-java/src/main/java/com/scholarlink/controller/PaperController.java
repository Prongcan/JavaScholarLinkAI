package com.scholarlink.controller;

import com.scholarlink.dto.ApiResponse;
import com.scholarlink.entity.Paper;
import com.scholarlink.repository.PaperRepository;
import com.scholarlink.service.PaperFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/papers")
public class PaperController {
    
    @Autowired
    private PaperRepository paperRepository;
    
    @Autowired
    private PaperFetchService paperFetchService;
    
    @PostMapping("/fetch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchPapers(
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            Integer maxResults = request != null && request.containsKey("max_results") 
                    ? (Integer) request.get("max_results") 
                    : null;
            
            log.info("开始抓取论文，max_results={}", maxResults);
            
            // 1. 调用服务获取论文
            List<Map<String, Object>> papers = paperFetchService.fetchPapers(maxResults);
            
            if (papers.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("fetched_count", 0);
                data.put("saved_count", 0);
                data.put("failed_count", 0);
                data.put("papers", List.of());
                
                return ResponseEntity.ok(ApiResponse.success(
                        "该时间窗口内没有找到新论文",
                        data
                ));
            }
            
            log.info("成功抓取 {} 篇论文，开始保存到数据库...", papers.size());
            
            // 2. 保存到数据库
            int savedCount = 0;
            int failedCount = 0;
            List<Map<String, Object>> savedPapers = new java.util.ArrayList<>();
            
            for (Map<String, Object> paperData : papers) {
                try {
                    // 检查论文是否已存在
                    String title = (String) paperData.get("title");
                    if (paperRepository.findByTitle(title).isPresent()) {
                        log.debug("论文 {} 已存在，跳过", title != null && title.length() > 50 
                                ? title.substring(0, 50) + "..." : title);
                        continue;
                    }
                    
                    // 准备数据
                    Paper paper = new Paper();
                    paper.setTitle(title);
                    
                    @SuppressWarnings("unchecked")
                    List<String> authors = (List<String>) paperData.get("authors");
                    if (authors != null && !authors.isEmpty()) {
                        paper.setAuthor(String.join(", ", authors));
                    }
                    
                    paper.setAbstractText((String) paperData.get("abstract"));
                    paper.setPdfUrl((String) paperData.get("pdf_url"));
                    
                    // 保存
                    Paper saved = paperRepository.save(paper);
                    savedCount++;
                    
                    Map<String, Object> savedPaperInfo = new HashMap<>();
                    savedPaperInfo.put("paper_id", saved.getPaperId());
                    savedPaperInfo.put("title", saved.getTitle());
                    savedPapers.add(savedPaperInfo);
                    
                    log.debug("成功保存论文: {}", title != null && title.length() > 50 
                            ? title.substring(0, 50) + "..." : title);
                    
                } catch (Exception e) {
                    failedCount++;
                    log.error("保存论文失败: {}", e.getMessage());
                }
            }
            
            log.info("论文保存完成: 抓取 {} 篇, 保存 {} 篇, 失败 {} 篇", 
                    papers.size(), savedCount, failedCount);
            
            Map<String, Object> data = new HashMap<>();
            data.put("fetched_count", papers.size());
            data.put("saved_count", savedCount);
            data.put("failed_count", failedCount);
            data.put("papers", savedPapers.stream().limit(10).collect(Collectors.toList()));
            
            return ResponseEntity.ok(ApiResponse.success(
                    "成功抓取并保存 " + savedCount + " 篇论文",
                    data
            ));
            
        } catch (Exception e) {
            log.error("抓取论文失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("抓取论文失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listPapers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int page_size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, page_size);
            Page<Paper> paperPage = paperRepository.findAllByOrderByPaperIdDesc(pageable);
            
            Map<String, Object> data = new HashMap<>();
            data.put("papers", paperPage.getContent());
            
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", page);
            pagination.put("page_size", page_size);
            pagination.put("total", paperPage.getTotalElements());
            pagination.put("total_pages", paperPage.getTotalPages());
            data.put("pagination", pagination);
            
            return ResponseEntity.ok(ApiResponse.success("获取论文列表成功", data));
            
        } catch (Exception e) {
            log.error("获取论文列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取论文列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{paperId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaper(@PathVariable Integer paperId) {
        try {
            Paper paper = paperRepository.findById(paperId)
                    .orElse(null);
            
            if (paper == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("论文不存在 (paper_id=" + paperId + ")"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("paper", paper);
            
            return ResponseEntity.ok(ApiResponse.success("获取论文详情成功", data));
            
        } catch (Exception e) {
            log.error("获取论文详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取论文详情失败: " + e.getMessage()));
        }
    }
}

