# 测试文件说明

## 📁 测试文件列表

### 1. ScholarLinkAiApplicationTests.java
**应用上下文测试**

- 测试 Spring Boot 应用上下文是否能正常加载
- 验证所有 Bean 是否正确配置

**运行方法**:
```bash
cd backend-java
.\mvnw.cmd test -Dtest=ScholarLinkAiApplicationTests
```

### 2. Controller 测试

#### HelloControllerTest.java
**测试 Hello API 端点**

- 测试 GET /api/hello/
- 测试 GET /api/hello/{name}
- 测试 POST /api/hello/post
- 测试 GET /api/hello/status

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=HelloControllerTest
```

#### PaperControllerTest.java
**测试论文 API 完整工作流**

- 测试数据库连接
- 测试表结构
- 测试抓取并保存论文
- 测试查询功能
- 测试分页功能

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=PaperControllerTest
```

#### UserControllerTest.java
**测试用户管理 API**

- 测试用户注册
- 测试用户查询
- 测试兴趣更新
- 测试用户列表
- 测试用户删除

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=UserControllerTest
```

### 3. Repository 测试

#### PaperRepositoryTest.java
**测试论文数据访问层**

- 测试 PaperRepository 基本功能
- 测试保存和查询
- 测试分页查询
- 测试按标题查找

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=PaperRepositoryTest
```

#### UserRepositoryTest.java
**测试用户数据访问层**

- 测试 UserRepository 基本功能
- 测试保存和查询
- 测试按用户名查找
- 测试用户名唯一性检查

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=UserRepositoryTest
```

### 4. Service 测试

#### PaperFetchServiceTest.java
**测试论文抓取服务**

- 测试从 arXiv 抓取论文
- 验证时间窗口设置
- 测试去重机制

**运行方法**:
```bash
.\mvnw.cmd test -Dtest=PaperFetchServiceTest
```

## 🚀 运行所有测试

```powershell
# 进入项目目录
cd backend-java

# 运行所有测试
.\mvnw.cmd test

# 运行特定包的测试
.\mvnw.cmd test -Dtest=com.scholarlink.controller.*

# 运行特定类的测试
.\mvnw.cmd test -Dtest=UserControllerTest

# 跳过集成测试，只运行单元测试
.\mvnw.cmd test -Dtest=*Test -DfailIfNoTests=false
```

## 📋 测试前提条件

1. **数据库已创建**
   - 数据库名: `scholarlink_ai`
   - 表: `papers`, `users`, `recommendations`
   - 测试使用独立的测试配置（`application-test.yml`）

2. **Maven Wrapper 已配置**
   - 使用 `.\mvnw.cmd` 运行测试（Windows）
   - 或使用全局 Maven: `mvn test`

3. **测试配置文件**
   - `src/test/resources/application-test.yml` 已配置
   - 使用独立的测试数据库或 H2 内存数据库

4. **JDK 17+**
   - 确保已安装 JDK 17 或更高版本

## ✅ 测试检查清单

- [ ] 数据库连接成功
- [ ] papers 表存在且结构正确
- [ ] users 表存在且结构正确
- [ ] 能够成功抓取论文
- [ ] 能够保存论文到数据库
- [ ] 能够注册和管理用户
- [ ] API 端点正常工作
- [ ] Repository 层功能正常
- [ ] Service 层业务逻辑正确

## 🧪 测试类型说明

### 单元测试 (Unit Tests)
- **@WebMvcTest**: 只加载 Web 层，用于测试 Controller
- **@DataJpaTest**: 只加载 JPA 层，用于测试 Repository
- **@MockBean**: 用于模拟依赖

### 集成测试 (Integration Tests)
- **@SpringBootTest**: 加载完整的 Spring 上下文
- **@AutoConfigureMockMvc**: 自动配置 MockMvc
- **@Transactional**: 测试后自动回滚数据

### 测试配置
- **@ActiveProfiles("test")**: 使用测试配置文件
- **application-test.yml**: 测试环境配置

## 🔧 故障排查

### 问题：Maven 命令未找到
```powershell
# 使用 Maven Wrapper（推荐）
.\mvnw.cmd test

# 或安装 Maven 后使用
mvn test
```

### 问题：数据库连接失败
- 检查 MySQL 服务是否启动
- 检查 `application-test.yml` 中的数据库配置
- 验证用户权限
- 确保测试数据库已创建

### 问题：表不存在
```sql
-- 运行初始化脚本
-- 参考 backend-java/src/main/resources/db/schema.sql
```

### 问题：测试失败 - 端口被占用
```yaml
# 在 application-test.yml 中修改端口
server:
  port: 0  # 随机端口
```

### 问题：测试数据污染
- 使用 `@Transactional` 注解，测试后自动回滚
- 在 `@BeforeEach` 中清理测试数据
- 使用独立的测试数据库

## 📊 测试覆盖率

查看测试覆盖率报告：

```powershell
# 生成测试覆盖率报告（需要 JaCoCo 插件）
.\mvnw.cmd clean test jacoco:report

# 查看报告
# 报告位置: target/site/jacoco/index.html
```

## 🎯 最佳实践

1. **测试命名**: 使用描述性的测试方法名
   ```java
   @Test
   void testSaveUser() { ... }
   ```

2. **测试隔离**: 每个测试应该独立，不依赖其他测试

3. **使用断言**: 使用 JUnit 5 的断言方法
   ```java
   assertEquals(expected, actual);
   assertTrue(condition);
   assertNotNull(object);
   ```

4. **Mock 外部依赖**: 使用 `@MockBean` 模拟外部服务

5. **清理测试数据**: 在 `@BeforeEach` 或 `@AfterEach` 中清理

## 📝 添加新测试

1. 在对应的测试目录创建测试类
2. 使用适当的测试注解
3. 编写测试方法
4. 运行测试验证

示例：
```java
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testMyEndpoint() throws Exception {
        mockMvc.perform(get("/api/my-endpoint"))
                .andExpect(status().isOk());
    }
}
```

