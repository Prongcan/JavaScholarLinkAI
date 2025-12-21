# IntelliJ IDEA 运行指南

## 📋 前置要求

1. **IntelliJ IDEA** (推荐 2022.1 或更高版本)
2. **JDK 17+** (项目使用 Java 17)
3. **MySQL 8.0+** (数据库服务)

## 🚀 在 IntelliJ 中打开项目

### 方法 1：直接打开项目（推荐）

1. 打开 IntelliJ IDEA
2. 选择 `File` → `Open`
3. 选择 `backend-java` 目录（不是整个项目根目录）
4. 选择 "Open as Project"

### 方法 2：从现有源导入

1. 打开 IntelliJ IDEA
2. 选择 `File` → `New` → `Project from Existing Sources`
3. 选择 `backend-java` 目录
4. 选择 "Import project from external model" → "Maven"
5. 点击 `Next` 完成导入

## ⚙️ 配置项目

### 1. 配置 JDK

1. 打开 `File` → `Project Structure` (Ctrl+Alt+Shift+S)
2. 在 `Project` 标签页：
   - 设置 `Project SDK` 为 JDK 17 或更高
   - 设置 `Project language level` 为 17
3. 在 `Modules` 标签页：
   - 确认 `Language level` 为 17
4. 点击 `OK` 保存

### 2. 配置 Maven

1. 打开 `File` → `Settings` (Ctrl+Alt+S)
2. 导航到 `Build, Execution, Deployment` → `Build Tools` → `Maven`
3. 配置：
   - **Maven home path**: 使用项目自带的 Maven Wrapper 或系统 Maven
   - **User settings file**: 使用默认或自定义
   - **Local repository**: 使用默认
4. 点击 `Apply` 和 `OK`

### 3. 导入 Maven 项目

1. 打开右侧的 `Maven` 工具窗口（View → Tool Windows → Maven）
2. 点击刷新按钮（Reload All Maven Projects）或右键项目 → `Maven` → `Reload Project`
3. 等待依赖下载完成

## 🗄️ 配置数据库

### 1. 确保数据库已创建

```sql
-- 在 MySQL 中执行
CREATE DATABASE IF NOT EXISTS scholarlink_ai;
```

或使用提供的脚本：
```powershell
cd backend-java
.\init-database.ps1
```

### 2. 检查数据库配置

编辑 `src/main/resources/application.yml`，确认数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scholarlink_ai?...
    username: root
    password: your_password
```

## ▶️ 运行项目

### 方法 1：使用主类运行（推荐）

1. 打开 `src/main/java/com/scholarlink/ScholarLinkAiApplication.java`
2. 右键点击文件或 `main` 方法
3. 选择 `Run 'ScholarLinkAiApplication'`
4. 或使用快捷键：`Shift+F10`

### 方法 2：使用运行配置

1. 点击右上角的运行配置下拉菜单
2. 选择 `Edit Configurations...`
3. 点击 `+` → `Spring Boot`
4. 配置：
   - **Name**: ScholarLink AI Backend
   - **Main class**: `com.scholarlink.ScholarLinkAiApplication`
   - **Working directory**: `$MODULE_DIR$`
5. 点击 `OK`，然后点击运行按钮

### 方法 3：使用 Maven 运行

1. 打开右侧 `Maven` 工具窗口
2. 展开 `scholarlink-ai-backend` → `Plugins` → `spring-boot`
3. 双击 `spring-boot:run`

## 🧪 运行测试

### 运行所有测试

1. 右键点击 `src/test` 目录
2. 选择 `Run 'All Tests'`

或使用 Maven：
1. 在 Maven 工具窗口中
2. 展开 `Lifecycle`
3. 双击 `test`

### 运行单个测试

1. 打开测试文件（如 `UserControllerTest.java`）
2. 右键点击测试类或测试方法
3. 选择 `Run 'UserControllerTest'` 或 `Run 'testMethodName()'`

## 🔍 调试

### 设置断点

1. 在代码行号左侧点击，设置断点（红色圆点）
2. 右键点击主类或测试类
3. 选择 `Debug 'ScholarLinkAiApplication'` 或按 `Shift+F9`

### 调试配置

1. 点击运行配置下拉菜单 → `Edit Configurations...`
2. 选择运行配置
3. 在 `VM options` 中可以添加 JVM 参数：
   ```
   -Xmx512m -Xms256m
   ```

## 📝 常见问题

### 问题 1：Maven 依赖下载失败

**解决方案**：
1. 检查网络连接
2. 配置 Maven 镜像（在 `settings.xml` 中）：
   ```xml
   <mirrors>
     <mirror>
       <id>aliyun</id>
       <mirrorOf>central</mirrorOf>
       <url>https://maven.aliyun.com/repository/public</url>
     </mirror>
   </mirrors>
   ```
3. 在 Maven 工具窗口中点击刷新按钮

### 问题 2：JDK 版本不匹配

**解决方案**：
1. 确认安装了 JDK 17+
2. 在 `Project Structure` 中配置正确的 JDK
3. 在 `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Java Compiler` 中设置 `Project bytecode version` 为 17

### 问题 3：数据库连接失败

**解决方案**：
1. 确认 MySQL 服务已启动
2. 检查 `application.yml` 中的数据库配置
3. 确认数据库用户有足够权限
4. 检查防火墙设置

### 问题 4：端口被占用

**解决方案**：
1. 修改 `application.yml` 中的端口：
   ```yaml
   server:
     port: 3002  # 改为其他端口
   ```
2. 或关闭占用 3001 端口的进程

### 问题 5：找不到主类

**解决方案**：
1. 确认项目已正确导入为 Maven 项目
2. 在 Maven 工具窗口中点击刷新
3. 执行 `File` → `Invalidate Caches / Restart...`

## 🎯 推荐设置

### 代码格式化

1. `File` → `Settings` → `Editor` → `Code Style` → `Java`
2. 可以导入 Google 或 Sun 代码风格

### 自动导入

1. `File` → `Settings` → `Editor` → `General` → `Auto Import`
2. 勾选：
   - ✅ Add unambiguous imports on the fly
   - ✅ Optimize imports on the fly

### Lombok 插件

如果使用 Lombok（项目已使用），需要安装插件：

1. `File` → `Settings` → `Plugins`
2. 搜索 "Lombok"
3. 安装并重启 IntelliJ

### 代码检查

1. `File` → `Settings` → `Editor` → `Inspections`
2. 可以启用/禁用特定的代码检查规则

## ✅ 验证运行成功

项目启动成功后，你应该看到：

1. **控制台输出**：
   ```
   Started ScholarLinkAiApplication in X.XXX seconds
   ```

2. **访问测试**：
   - API 根路径: http://localhost:3001/
   - Swagger 文档: http://localhost:3001/docs
   - API 文档: http://localhost:3001/api-docs

3. **测试端点**：
   ```bash
   curl http://localhost:3001/api/hello/
   ```

## 📚 相关文档

- [README.md](README.md) - 项目总体说明
- [USERS_API_使用说明.md](USERS_API_使用说明.md) - 用户 API 文档
- [PAPERS_API_简化版说明.md](PAPERS_API_简化版说明.md) - 论文 API 文档
- [src/test/README.md](src/test/README.md) - 测试说明

## 🎉 完成！

现在你可以在 IntelliJ IDEA 中愉快地开发和调试项目了！

