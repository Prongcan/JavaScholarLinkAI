package org.example.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 论文抓取服务
 * 从arXiv API抓取论文信息
 */
public class PaperFetchService {
    private static final String ARXIV_API_BASE = "http://export.arxiv.org/api/query";
    private final OkHttpClient httpClient;

    public PaperFetchService() {
        this.httpClient = new OkHttpClient();
    }

    /**
     * 根据arXiv ID抓取论文信息
     * @param arxivId arXiv论文ID（如：2301.00001）
     * @return 包含论文信息的Map（title, author, abstract, pdf_url）
     * @throws IOException 如果API调用失败
     */
    public Map<String, String> fetchPaperByArxivId(String arxivId) throws IOException {
        String url = ARXIV_API_BASE + "?id_list=" + arxivId;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return parseArxivResponse(responseBody);
        }
    }

    /**
     * 解析arXiv API的XML响应
     */
    private Map<String, String> parseArxivResponse(String xmlResponse) throws IOException {
        Map<String, String> paperInfo = new HashMap<>();
        
        // 使用正则表达式提取entry标签内的内容
        Pattern entryPattern = Pattern.compile("<entry>(.*?)</entry>", Pattern.DOTALL);
        Matcher entryMatcher = entryPattern.matcher(xmlResponse);

        if (entryMatcher.find()) {
            String entryXml = entryMatcher.group(1);

            // 提取标题（在entry标签内）
            String title = extractXmlValue(entryXml, "<title>", "</title>");
            if (title != null) {
                title = title.replaceAll("\\s+", " ").trim();
                paperInfo.put("title", title);
            }

            // 提取作者
            String authors = extractAuthors(entryXml);
            paperInfo.put("author", authors);

            // 提取摘要
            String summary = extractXmlValue(entryXml, "<summary>", "</summary>");
            if (summary != null) {
                summary = summary.replaceAll("\\s+", " ").trim();
                paperInfo.put("abstract", summary);
            }

            // 提取PDF URL
            String pdfUrl = extractXmlValue(entryXml, "<id>", "</id>");
            if (pdfUrl != null && pdfUrl.contains("arxiv.org")) {
                pdfUrl = pdfUrl.replace("/abs/", "/pdf/") + ".pdf";
                paperInfo.put("pdf_url", pdfUrl);
            }
        }
        
        return paperInfo;
    }

    /**
     * 从XML中提取指定标签的值
     */
    private String extractXmlValue(String xml, String startTag, String endTag) {
        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) {
            return null;
        }
        startIndex += startTag.length();
        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) {
            return null;
        }
        return xml.substring(startIndex, endIndex).trim();
    }

    /**
     * 提取作者列表
     */
    private String extractAuthors(String entryXml) {
        Pattern authorPattern = Pattern.compile("<name>(.*?)</name>");
        Matcher authorMatcher = authorPattern.matcher(entryXml);
        
        StringBuilder authors = new StringBuilder();
        while (authorMatcher.find()) {
            if (authors.length() > 0) {
                authors.append(", ");
            }
            authors.append(authorMatcher.group(1).trim());
        }
        
        return authors.length() > 0 ? authors.toString() : "Unknown";
    }
}

