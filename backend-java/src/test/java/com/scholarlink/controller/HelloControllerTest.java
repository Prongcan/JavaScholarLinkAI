package com.scholarlink.controller;

import com.scholarlink.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloController.class)
@Import(AppConfig.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testHelloWithName() throws Exception {
        mockMvc.perform(get("/api/hello/TestUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("TestUser"));
    }

    @Test
    void testHelloPost() throws Exception {
        String requestBody = "{\"name\":\"TestUser\",\"message\":\"Hello from test\"}";
        
        mockMvc.perform(post("/api/hello/post")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("TestUser"));
    }

    @Test
    void testHelloStatus() throws Exception {
        mockMvc.perform(get("/api/hello/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}

