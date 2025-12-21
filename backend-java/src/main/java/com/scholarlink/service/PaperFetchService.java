package com.scholarlink.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaperFetchService {
    
    private final WebClient webClient;
    
    public PaperFetchService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://export.arxiv.org/api/query")
                .build();
    }
    
    /**
     * 从arXiv抓取前两天到前一天的CS类论文
     */
    public List<Map<String, Object>> fetchPapers(Integer maxResults) {
        try {
            // 计算时间窗口：前两天到前一天
            LocalDate now = LocalDate.now();
            LocalDate endDate = now.minusDays(1);
            LocalDate startDate = now.minusDays(2);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String startTimeStr = startDate.atStartOfDay().format(formatter);
            String endTimeStr = endDate.atTime(23, 59, 59).format(formatter);
            
            log.info("开始抓取时间窗口: [{} TO {}]", startTimeStr, endTimeStr);
            
            // 构建查询
            String query = String.format("cat:cs.* AND submittedDate:[%s TO %s]", startTimeStr, endTimeStr);
            
            // 调用arXiv API
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("search_query", query)
                            .queryParam("start", 0)
                            .queryParam("max_results", maxResults != null ? maxResults : 100)
                            .queryParam("sortBy", "submittedDate")
                            .queryParam("sortOrder", "descending")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response == null) {
                log.warn("arXiv API返回空响应");
                return new ArrayList<>();
            }
            
            // 解析Atom XML响应
            List<Map<String, Object>> papers = parseArxivResponse(response);
            
            log.info("成功抓取 {} 篇论文", papers.size());
            return papers;
            
        } catch (Exception e) {
            log.error("抓取论文失败", e);
            throw new RuntimeException("抓取论文失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析arXiv API返回的Atom XML
     * 这里简化处理，实际应该使用XML解析器
     */
    private List<Map<String, Object>> parseArxivResponse(String xmlResponse) {
        List<Map<String, Object>> papers = new ArrayList<>();
        
        try {
            // 简单的XML解析（实际项目中应使用JAXB或DOM解析器）
            // 这里使用正则表达式简单提取（不推荐，但为了快速实现）
            String[] entries = xmlResponse.split("<entry>");
            
            for (int i = 1; i < entries.length; i++) {
                String entry = entries[i];
                try {
                    Map<String, Object> paper = new java.util.HashMap<>();
                    
                    // 提取ID
                    String id = extractXmlTag(entry, "id");
                    if (id != null && id.contains("arxiv.org/abs/")) {
                        String arxivId = id.substring(id.lastIndexOf('/') + 1).split("v")[0];
                        paper.put("arxiv_id", arxivId);
                    }
                    
                    // 提取标题
                    paper.put("title", extractXmlTag(entry, "title"));
                    
                    // 提取作者
                    List<String> authors = new ArrayList<>();
                    String[] authorTags = entry.split("<author>");
                    for (int j = 1; j < authorTags.length; j++) {
                        String name = extractXmlTag(authorTags[j], "name");
                        if (name != null) {
                            authors.add(name);
                        }
                    }
                    paper.put("authors", authors);
                    
                    // 提取摘要
                    paper.put("abstract", extractXmlTag(entry, "summary"));
                    
                    // 提取PDF URL
                    String pdfUrl = extractXmlTag(entry, "link", "type", "application/pdf");
                    if (pdfUrl == null) {
                        // 如果没有找到，尝试从id构建
                        if (id != null && id.contains("arxiv.org")) {
                            pdfUrl = id.replace("/abs/", "/pdf/") + ".pdf";
                        }
                    }
                    paper.put("pdf_url", pdfUrl);
                    
                    // 提取发布日期
                    String published = extractXmlTag(entry, "published");
                    paper.put("published_date", published);
                    
                    papers.add(paper);
                    
                } catch (Exception e) {
                    log.warn("解析论文条目失败: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("解析arXiv响应失败", e);
        }
        
        return papers;
    }
    
    private String extractXmlTag(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int start = xml.indexOf(startTag);
        if (start == -1) return null;
        start += startTag.length();
        int end = xml.indexOf(endTag, start);
        if (end == -1) return null;
        return xml.substring(start, end).trim().replaceAll("<!\\[CDATA\\[|\\]\\]>", "");
    }
    
    private String extractXmlTag(String xml, String tagName, String attrName, String attrValue) {
        // 查找包含特定属性的标签
        String pattern = "<" + tagName + "[^>]*" + attrName + "=\"" + attrValue + "\"[^>]*>";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(xml);
        if (m.find()) {
            int start = m.end();
            String endTag = "</" + tagName + ">";
            int end = xml.indexOf(endTag, start);
            if (end != -1) {
                return xml.substring(start, end).trim();
            }
        }
        return null;
    }
}

