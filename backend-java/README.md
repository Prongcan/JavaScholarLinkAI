# ScholarLink AI Backend - Java版本

这是 ScholarLink AI 后端服务的 Java Spring Boot 实现，从 Python Flask 版本迁移而来。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **Java版本**: 17
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **API文档**: SpringDoc OpenAPI (Swagger UI)
- **构建工具**: Maven

## 项目结构

```
backend-java/
├── src/
│   ├── main/
│   │   ├── java/com/scholarlink/
│   │   │   ├── entity/          # 实体类
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── controller/     # 控制器层
│   │   │   ├── config/          # 配置类
│   │   │   ├── dto/             # 数据传输对象
│   │   │   └── ScholarLinkAiApplication.java
│   │   └── resources/
│   │       └── application.yml  # 配置文件
│   └── test/                    # 测试代码
└── pom.xml                      # Maven配置
```

## 功能特性

- ✅ 论文抓取（从arXiv）
- ✅ 论文管理（CRUD）
- ✅ 用户管理（注册、查询、更新兴趣）
- ✅ OpenAI Embedding服务
- ✅ 博客生成服务（从PDF生成技术博客）
- ✅ RESTful API（与Python版本API兼容）
- ✅ Swagger API文档

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6+（或使用项目自带的Maven Wrapper，无需安装）
- MySQL 8.0+
- OpenAI API Key（用于Embedding和博客生成）

### 配置

1. **数据库配置**

   编辑 `src/main/resources/application.yml`，修改数据库连接信息：

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/scholarlink_ai?...
       username: root
       password: your_password
   ```

2. **OpenAI配置**

   设置环境变量或编辑 `application.yml`：

   ```yaml
   openai:
     api-key: your_openai_api_key
   ```

   或使用环境变量：
   ```bash
   export OPENAI_API_KEY=your_api_key
   ```

### 运行

**推荐：使用Maven Wrapper（无需安装Maven）**

1. **首次使用需要下载wrapper jar**（如果自动下载失败，请手动下载）：
   
   Windows PowerShell:
   ```powershell
   cd backend-java
   # 首次运行会自动下载wrapper，如果失败请查看INSTALL_MAVEN.md
   .\mvnw.cmd clean install
   .\mvnw.cmd spring-boot:run
   ```

2. **打包运行**：

   ```powershell
   .\mvnw.cmd clean package
   java -jar target/scholarlink-ai-backend-1.0.0.jar
   ```

**或者：使用全局Maven（如果已安装）**

```bash
cd backend-java
mvn spring-boot:run
```

> 💡 **提示**：如果遇到 `mvn: 无法识别` 错误，请使用 `.\mvnw.cmd` 代替 `mvn`，或参考 `INSTALL_MAVEN.md` 安装Maven。

3. **访问服务**：

   - API根路径: http://localhost:3001/
   - Swagger文档: http://localhost:3001/docs
   - API文档: http://localhost:3001/api-docs

## API端点

### Hello API
- `GET /api/hello/` - Hello World
- `GET /api/hello/{name}` - 带参数的Hello
- `POST /api/hello/post` - POST请求的Hello
- `GET /api/hello/status` - API状态

### Papers API
- `POST /api/papers/fetch` - 抓取论文
- `GET /api/papers/list` - 获取论文列表（支持分页）
- `GET /api/papers/{paperId}` - 获取论文详情

### Users API
- `POST /api/users/register` - 用户注册
- `GET /api/users/{userId}` - 获取用户信息
- `GET /api/users/list` - 获取用户列表（支持分页）
- `GET /api/users/{userId}/interest` - 获取用户兴趣
- `PUT /api/users/{userId}/interest` - 更新用户兴趣
- `DELETE /api/users/{userId}` - 删除用户

## 与Python版本的差异

1. **框架差异**：
   - Python: Flask + Flask-RESTX
   - Java: Spring Boot + Spring Data JPA

2. **数据库访问**：
   - Python: PyMySQL + 原生SQL
   - Java: Spring Data JPA + Repository模式

3. **arXiv API解析**：
   - Python: 使用 `arxiv` 库
   - Java: 使用 WebClient 调用API并解析XML响应

4. **PDF处理**：
   - Python: pypdf
   - Java: Apache PDFBox

5. **OpenAI SDK**：
   - Python: openai (官方SDK)
   - Java: openai-gpt3-java (第三方SDK)

## 开发说明

### 添加新功能

1. 在 `entity/` 中定义实体类
2. 在 `repository/` 中创建Repository接口
3. 在 `service/` 中实现业务逻辑
4. 在 `controller/` 中创建REST端点

### 测试

```bash
mvn test
```

## 注意事项

1. **数据库表结构**：确保MySQL数据库已创建，表结构与Python版本一致
2. **API兼容性**：Java版本保持与Python版本的API接口兼容，前端无需修改
3. **性能**：Spring Boot默认使用内嵌Tomcat，生产环境建议使用外部Tomcat或容器化部署

## 迁移状态

- ✅ 基础框架搭建
- ✅ 实体类和Repository层
- ✅ Service层（论文抓取、用户管理、Embedding、博客生成）
- ✅ Controller层（所有API端点）
- ✅ 配置文件
- ⚠️ 测试代码（待补充）
- ⚠️ 错误处理优化（待完善）

## 许可证

与主项目保持一致。

