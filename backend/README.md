# ScholarLink AI 后端服务

基于 Python Flask 的后端 API 服务。

## 功能特性

- 🚀 基于 Flask 框架
- 🌐 支持 CORS 跨域请求
- 📝 完整的错误处理
- 🔧 环境配置管理
- 📊 健康检查接口
- 🎯 RESTful API 设计
- 📚 自动生成 Swagger API 文档
- 🔍 可视化 API 测试界面

## 快速开始

### 1. 安装依赖

```bash
cd backend
pip install -r requirements.txt
```

### 2. 启动服务

```bash
python app.py
```

服务将在 `http://localhost:3001` 启动

### 3. 查看 API 文档

启动服务后，访问以下地址查看可视化 API 文档：

- **Swagger UI**: http://localhost:3001/docs/
- **API 根路径**: http://localhost:3001/

### 4. 测试 API

#### 基础 Hello World
```bash
curl http://localhost:3001/api/hello/
```

#### 带参数的 Hello
```bash
curl http://localhost:3001/api/hello/张三
```

#### POST 请求
```bash
curl -X POST http://localhost:3001/api/hello/post \
  -H "Content-Type: application/json" \
  -d '{"name": "李四", "message": "你好！"}'
```

#### 健康检查
```bash
curl http://localhost:3001/health
```

## API 接口

### 基础接口

- `GET /` - 服务信息
- `GET /health` - 健康检查
- `GET /docs/` - Swagger API 文档

### Hello API (新版本 - 带文档)

- `GET /api/hello/` - 基础 Hello World
- `GET /api/hello/<name>` - 带参数的 Hello
- `POST /api/hello/post` - POST 请求的 Hello
- `GET /api/hello/status` - API 状态检查

### Hello API (兼容版本)

- `GET /api/v1/hello` - 基础 Hello World
- `GET /api/v1/hello/<name>` - 带参数的 Hello
- `POST /api/v1/hello` - POST 请求的 Hello
- `GET /api/v1/hello/status` - API 状态检查

## 项目结构

```
backend/
├── app.py                 # 主应用文件
├── config.py             # 配置文件
├── requirements.txt      # Python 依赖
├── api_router/           # API 路由
│   ├── __init__.py
│   └── hello_routes.py   # Hello API 路由
├── entity/               # 数据模型 (待开发)
└── service/              # 业务逻辑 (待开发)
```

## 开发说明

- 使用 Flask 蓝图 (Blueprint) 组织路由
- 支持环境变量配置
- 包含完整的错误处理机制
- 支持 JSON 格式的请求和响应
