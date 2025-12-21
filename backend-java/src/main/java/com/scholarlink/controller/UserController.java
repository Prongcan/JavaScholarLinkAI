package com.scholarlink.controller;

import com.scholarlink.dto.ApiResponse;
import com.scholarlink.entity.User;
import com.scholarlink.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String interest = request.getOrDefault("interest", "");
            
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("用户名和密码不能为空"));
            }
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("用户名 " + username + " 已存在"));
            }
            
            // 密码加密
            String hashedPassword = hashPassword(password);
            
            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword);
            user.setInterest(interest);
            
            User saved = userRepository.save(user);
            
            log.info("用户注册成功: {} (ID={})", username, saved.getUserId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", saved.getUserId());
            data.put("username", saved.getUsername());
            data.put("interest", saved.getInterest());
            
            return ResponseEntity.ok(ApiResponse.success("用户注册成功", data));
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("用户注册失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUser(@PathVariable Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在 (user_id=" + userId + ")"));
            }
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("interest", user.getInterest());
            
            Map<String, Object> data = new HashMap<>();
            data.put("user", userData);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", data));
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{userId}/interest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInterest(@PathVariable Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在 (user_id=" + userId + ")"));
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", user.getUserId());
            data.put("username", user.getUsername());
            data.put("interest", user.getInterest());
            
            return ResponseEntity.ok(ApiResponse.success("获取用户兴趣成功", data));
            
        } catch (Exception e) {
            log.error("获取用户兴趣失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户兴趣失败: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{userId}/interest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateInterest(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request) {
        try {
            String interest = request.get("interest");
            
            if (interest == null || interest.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("兴趣字段不能为空"));
            }
            
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在 (user_id=" + userId + ")"));
            }
            
            user.setInterest(interest);
            userRepository.save(user);
            
            log.info("用户 {} 兴趣更新成功: {}", userId, interest);
            
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", userId);
            data.put("interest", interest);
            data.put("updated_rows", 1);
            
            return ResponseEntity.ok(ApiResponse.success("用户兴趣更新成功", data));
            
        } catch (Exception e) {
            log.error("更新用户兴趣失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新用户兴趣失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int page_size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, page_size);
            Page<User> userPage = userRepository.findAllByOrderByUserIdDesc(pageable);
            
            // 不返回密码
            Map<String, Object> data = new HashMap<>();
            data.put("users", userPage.getContent().stream().map(u -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", u.getUserId());
                userData.put("username", u.getUsername());
                userData.put("interest", u.getInterest());
                return userData;
            }).toList());
            
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", page);
            pagination.put("page_size", page_size);
            pagination.put("total", userPage.getTotalElements());
            pagination.put("total_pages", userPage.getTotalPages());
            data.put("pagination", pagination);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", data));
            
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteUser(@PathVariable Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("用户不存在 (user_id=" + userId + ")"));
            }
            
            userRepository.delete(user);
            
            log.info("用户 {} 已删除", userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("deleted_user_id", userId);
            data.put("deleted_rows", 1);
            
            return ResponseEntity.ok(ApiResponse.success("用户删除成功", data));
            
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除用户失败: " + e.getMessage()));
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}

