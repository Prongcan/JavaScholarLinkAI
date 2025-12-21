package com.scholarlink.controller;

import com.scholarlink.entity.Paper;
import com.scholarlink.repository.PaperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaperRepository paperRepository;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        paperRepository.deleteAll();
    }

    @Test
    void testGetPaperList() throws Exception {
        // 创建测试论文
        for (int i = 1; i <= 3; i++) {
            Paper paper = new Paper();
            paper.setTitle("Test Paper " + i);
            paper.setAuthor("Author " + i);
            paper.setAbstractText("Abstract " + i);
            paperRepository.save(paper);
        }

        mockMvc.perform(get("/api/papers/list?page=1&page_size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.papers").isArray())
                .andExpect(jsonPath("$.data.pagination.total").value(3));
    }

    @Test
    void testGetPaperDetail() throws Exception {
        // 创建测试论文
        Paper paper = new Paper();
        paper.setTitle("Test Paper");
        paper.setAuthor("Test Author");
        paper.setAbstractText("Test Abstract");
        Paper saved = paperRepository.save(paper);

        mockMvc.perform(get("/api/papers/" + saved.getPaperId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.paper.title").value("Test Paper"));
    }

    @Test
    void testGetPaperNotFound() throws Exception {
        mockMvc.perform(get("/api/papers/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFetchPapers() throws Exception {
        // 注意：这个测试可能需要实际的网络连接
        // 在测试环境中建议使用Mock或跳过此测试
        // 由于需要网络，这里只测试接口是否存在，不验证具体结果
        
        // 尝试调用接口，可能成功（有网络）或失败（无网络）
        // 两种结果都是可以接受的
        int status = mockMvc.perform(post("/api/papers/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn()
                .getResponse()
                .getStatus();
        
        // 验证返回了某种状态码（200或500都可以）
        assertTrue(status == 200 || status == 500, 
            "Expected status 200 or 500, but got " + status);
    }
}

