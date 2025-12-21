package com.scholarlink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarlink.entity.User;
import com.scholarlink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser() throws Exception {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", "testuser");
        userData.put("password", "testpass123");
        userData.put("interest", "Machine Learning");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testRegisterUserDuplicate() throws Exception {
        // 先创建一个用户
        User user = new User();
        user.setUsername("duplicate");
        user.setPassword("hashed");
        userRepository.save(user);

        // 尝试注册同名用户
        Map<String, String> userData = new HashMap<>();
        userData.put("username", "duplicate");
        userData.put("password", "testpass");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetUser() throws Exception {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashed");
        user.setInterest("AI");
        User saved = userRepository.save(user);

        mockMvc.perform(get("/api/users/" + saved.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    void testUpdateInterest() throws Exception {
        // 创建测试用户
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("hashed");
        userRepository.save(user);

        Map<String, String> updateData = new HashMap<>();
        updateData.put("interest", "Deep Learning");

        mockMvc.perform(put("/api/users/" + user.getUserId() + "/interest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.interest").value("Deep Learning"));
    }

    @Test
    void testGetUserList() throws Exception {
        // 创建几个测试用户
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setPassword("pass" + i);
            userRepository.save(user);
        }

        mockMvc.perform(get("/api/users/list?page=1&page_size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.users").isArray())
                .andExpect(jsonPath("$.data.pagination.total").value(5));
    }
}

