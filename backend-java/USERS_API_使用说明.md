# Users API 使用说明

## 📋 概述

Users API 提供了完整的用户管理功能，使用 Spring Data JPA 操作 MySQL 数据库。

## 📊 数据库表结构

```sql
users 表：
- user_id: INT (主键, 自增)
- username: VARCHAR (用户名, 唯一)
- password: VARCHAR (密码, SHA256 哈希)
- interest: TEXT (用户兴趣)
```

## 🚀 API 端点

### 1. POST /api/users/register
**用户注册**

**请求体**:
```json
{
  "username": "zhang_san",
  "password": "password123",
  "interest": "Machine Learning, NLP, Computer Vision"
}
```

**响应示例**:
```json
{
  "message": "用户注册成功",
  "status": "success",
  "timestamp": "2024-12-21T17:30:00",
  "data": {
    "user_id": 1,
    "username": "zhang_san",
    "interest": "Machine Learning, NLP, Computer Vision"
  }
}
```

**curl 示例**:
```bash
curl -X POST http://localhost:3001/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhang_san",
    "password": "password123",
    "interest": "Machine Learning, NLP"
  }'
```

### 2. GET /api/users/{userId}
**获取用户信息**

**响应示例**:
```json
{
  "message": "获取用户信息成功",
  "status": "success",
  "data": {
    "user": {
      "user_id": 1,
      "username": "zhang_san",
      "interest": "Machine Learning, NLP, Computer Vision"
    }
  }
}
```

**curl 示例**:
```bash
curl http://localhost:3001/api/users/1
```

### 3. PUT /api/users/{userId}/interest
**更新用户兴趣**

**请求体**:
```json
{
  "interest": "Deep Learning, Reinforcement Learning, AI"
}
```

**响应示例**:
```json
{
  "message": "用户兴趣更新成功",
  "status": "success",
  "data": {
    "user_id": 1,
    "interest": "Deep Learning, Reinforcement Learning, AI",
    "updated_rows": 1
  }
}
```

**curl 示例**:
```bash
curl -X PUT http://localhost:3001/api/users/1/interest \
  -H "Content-Type: application/json" \
  -d '{
    "interest": "Deep Learning, AI"
  }'
```

### 4. GET /api/users/{userId}/interest
**获取用户兴趣**

**响应示例**:
```json
{
  "message": "获取用户兴趣成功",
  "status": "success",
  "data": {
    "user_id": 1,
    "username": "zhang_san",
    "interest": "Deep Learning, AI"
  }
}
```

**curl 示例**:
```bash
curl http://localhost:3001/api/users/1/interest
```

### 5. GET /api/users/list
**获取用户列表**

**查询参数**:
- `page`: 页码（默认 1）
- `page_size`: 每页数量（默认 20）

**响应示例**:
```json
{
  "message": "获取用户列表成功",
  "status": "success",
  "data": {
    "users": [
      {
        "user_id": 1,
        "username": "zhang_san",
        "interest": "Machine Learning, NLP"
      },
      {
        "user_id": 2,
        "username": "li_si",
        "interest": "Computer Vision"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total": 2,
      "total_pages": 1
    }
  }
}
```

**curl 示例**:
```bash
# 第一页
curl http://localhost:3001/api/users/list

# 第二页，每页 10 条
curl "http://localhost:3001/api/users/list?page=2&page_size=10"
```

### 6. DELETE /api/users/{userId}
**删除用户**

**响应示例**:
```json
{
  "message": "用户删除成功",
  "status": "success",
  "data": {
    "deleted_user_id": 1,
    "deleted_rows": 1
  }
}
```

**curl 示例**:
```bash
curl -X DELETE http://localhost:3001/api/users/1
```

## ☕ Java 使用示例

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

RestTemplate restTemplate = new RestTemplate();
String baseUrl = "http://localhost:3001/api";

// 1. 用户注册
System.out.println("1. 注册用户...");
Map<String, String> registerRequest = new HashMap<>();
registerRequest.put("username", "test_user");
registerRequest.put("password", "test123");
registerRequest.put("interest", "Machine Learning, Deep Learning");

HttpHeaders headers = new HttpHeaders();
headers.set("Content-Type", "application/json");
HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(registerRequest, headers);

ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
    baseUrl + "/users/register",
    registerEntity,
    Map.class
);
Map<String, Object> registerData = (Map<String, Object>) registerResponse.getBody().get("data");
Integer userId = (Integer) registerData.get("user_id");
System.out.println("✅ 用户注册成功，ID: " + userId);

// 2. 获取用户信息
System.out.println("\n2. 获取用户信息...");
ResponseEntity<Map> getUserResponse = restTemplate.getForEntity(
    baseUrl + "/users/" + userId,
    Map.class
);
Map<String, Object> userData = (Map<String, Object>) 
    ((Map<String, Object>) getUserResponse.getBody().get("data")).get("user");
System.out.println("用户名: " + userData.get("username"));
System.out.println("兴趣: " + userData.get("interest"));

// 3. 更新兴趣
System.out.println("\n3. 更新用户兴趣...");
Map<String, String> interestRequest = new HashMap<>();
interestRequest.put("interest", "Computer Vision, NLP, AI");
HttpEntity<Map<String, String>> interestEntity = new HttpEntity<>(interestRequest, headers);

restTemplate.put(baseUrl + "/users/" + userId + "/interest", interestEntity);
System.out.println("✅ 兴趣更新成功");

// 4. 获取用户列表
System.out.println("\n4. 获取用户列表...");
ResponseEntity<Map> listResponse = restTemplate.getForEntity(
    baseUrl + "/users/list?page=1&page_size=5",
    Map.class
);
Map<String, Object> listData = (Map<String, Object>) listResponse.getBody().get("data");
Map<String, Object> pagination = (Map<String, Object>) listData.get("pagination");
System.out.println("总用户数: " + pagination.get("total"));
```

## 🔒 密码处理

当前使用 **SHA256** 哈希存储密码（简化版本）。

**生产环境建议**：
- 使用 `BCryptPasswordEncoder` 进行密码哈希
- 添加盐值（salt）
- 实现密码强度验证

```java
// 当前实现（简单）
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
// ... 转换为十六进制字符串

// 生产环境推荐（Spring Security）
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(password);
```

## 🧪 测试

### 运行测试脚本

```powershell
# 进入项目目录
cd backend-java

# 运行用户 API 测试
.\mvnw.cmd test -Dtest=UserControllerTest

# 运行 Repository 测试
.\mvnw.cmd test -Dtest=UserRepositoryTest
```

测试内容：
- ✅ 数据库连接
- ✅ 表结构验证
- ✅ 用户注册
- ✅ 查询用户
- ✅ 更新兴趣
- ✅ 用户列表
- ✅ 数据清理

### 在 Swagger UI 中测试

1. 启动服务：`.\mvnw.cmd spring-boot:run`
2. 访问：http://localhost:3001/docs/
3. 找到 `users` 分组
4. 测试各个端点

## 📊 数据库操作

所有 API 都使用 Spring Data JPA Repository 进行数据库操作：

```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<User> findAllByOrderByUserIdDesc(Pageable pageable);
}
```

**使用示例**:
```java
@Autowired
private UserRepository userRepository;

// 查询单个用户
Optional<User> user = userRepository.findById(userId);

// 插入用户
User newUser = new User();
newUser.setUsername("test");
newUser.setPassword("hashed");
userRepository.save(newUser);

// 更新兴趣
User user = userRepository.findById(userId).orElse(null);
if (user != null) {
    user.setInterest("New Interest");
    userRepository.save(user);
}

// 查询所有用户（分页）
Pageable pageable = PageRequest.of(0, 20);
Page<User> users = userRepository.findAllByOrderByUserIdDesc(pageable);
```

## 🔄 完整工作流示例

### 场景：用户注册并设置兴趣

```bash
# 1. 注册用户
curl -X POST http://localhost:3001/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "research_student",
    "password": "secure123",
    "interest": "Natural Language Processing"
  }'

# 返回：{"data": {"user_id": 1, ...}}

# 2. 查看用户信息
curl http://localhost:3001/api/users/1

# 3. 更新研究兴趣
curl -X PUT http://localhost:3001/api/users/1/interest \
  -H "Content-Type: application/json" \
  -d '{
    "interest": "NLP, LLM, Prompt Engineering, RAG"
  }'

# 4. 验证更新
curl http://localhost:3001/api/users/1/interest
```

## ⚠️ 注意事项

1. **用户名唯一性**：
   - 系统会自动检查用户名是否已存在
   - 重复注册会返回 409 错误

2. **密码安全**：
   - 当前使用 SHA256（演示用）
   - 生产环境请使用 BCrypt

3. **数据验证**：
   - 用户名和密码不能为空
   - API 会进行基本验证

4. **外键约束**：
   - 删除用户会级联删除相关推荐数据（如果配置了外键）

## 🎯 下一步

### 与推荐系统集成

用户兴趣可以用于：
1. 根据兴趣推荐论文
2. 个性化内容展示
3. 智能匹配研究方向

### 添加认证功能

```java
// 用户登录示例（待实现）
@PostMapping("/login")
public ResponseEntity<ApiResponse<Map<String, Object>>> login(
        @RequestBody Map<String, String> request) {
    // 验证用户名和密码
    // 生成 JWT token
    // 返回认证信息
}
```

## 📁 文件结构

```
backend-java/
├── src/main/java/com/scholarlink/
│   ├── controller/
│   │   └── UserController.java          ← 用户 API
│   ├── entity/
│   │   └── User.java                    ← 用户实体
│   ├── repository/
│   │   └── UserRepository.java         ← 数据访问层
│   └── service/
│       └── (相关服务)
├── src/test/java/com/scholarlink/
│   ├── controller/
│   │   └── UserControllerTest.java     ← 用户 API 测试
│   └── repository/
│       └── UserRepositoryTest.java      ← Repository 测试
└── USERS_API_使用说明.md                ← 本文档
```

## ✅ 完成！

现在你可以：
1. 启动服务：`.\mvnw.cmd spring-boot:run`
2. 运行测试：`.\mvnw.cmd test -Dtest=UserControllerTest`
3. 访问文档：http://localhost:3001/docs/
4. 开始使用用户管理 API！🎉

