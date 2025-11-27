# Papers API 使用说明（适配现有数据库）

## 📋 数据库结构

本 API 适配你现有的 MySQL 数据库结构：

```sql
-- papers 表
paper_id    INT (主键, 自增)
title       VARCHAR
author      VARCHAR  
abstract    TEXT
pdf_url     VARCHAR

-- users 表  
user_id     INT (主键, 自增)
username    VARCHAR
password    VARCHAR
interest    VARCHAR

-- recommendations 表
user_id     INT
paper_id    INT
blog        TEXT
```

## 🚀 快速开始

### 1. 确保数据库已创建

在 DBeaver 中确认 `scholarlink_ai` 数据库已创建，且包含 `papers` 表。

### 2. 配置数据库连接

编辑 `backend/config.py` 或创建 `.env` 文件：

```env
DATABASE_HOST=localhost
DATABASE_PORT=3306
DATABASE_NAME=scholarlink_ai
DATABASE_USER=root
DATABASE_PASSWORD=your_password
```

### 3. 安装依赖

```bash
pip install -r backend/requirements.txt
```

### 4. 启动服务

```bash
python backend/app.py
```

服务启动在 http://localhost:3001

### 5. 测试 API

```bash
# 抓取论文并保存（抓取 10 篇用于测试）
curl -X POST http://localhost:3001/api/papers/fetch \
  -H "Content-Type: application/json" \
  -d '{"max_results": 10}'

# 查看论文列表
curl http://localhost:3001/api/papers/list

# 查看论文详情（假设 paper_id=1）
curl http://localhost:3001/api/papers/1
```

## 📚 API 端点

### 1. POST /api/papers/fetch
抓取论文并保存到数据库

**请求体**:
```json
{
    "max_results": 10  // 可选，限制抓取数量
}
```

**功能**:
- 从 arXiv 抓取前两天到前一天的 CS 类论文
- 自动将论文保存到 papers 表
- 自动去重（通过标题检查）
- 只保存数据库设计中的字段：title, author, abstract, pdf_url

**响应示例**:
```json
{
    "message": "成功抓取并保存 10 篇论文",
    "status": "success",
    "data": {
        "fetched_count": 10,
        "saved_count": 10,
        "failed_count": 0,
        "papers": [
            {
                "paper_id": 1,
                "title": "Sample Paper Title"
            }
        ]
    }
}
```

### 2. GET /api/papers/list
获取论文列表

**查询参数**:
- `page`: 页码（默认 1）
- `page_size`: 每页数量（默认 20）

**示例**:
```bash
# 第一页，每页 20 条
curl http://localhost:3001/api/papers/list

# 第二页，每页 10 条  
curl "http://localhost:3001/api/papers/list?page=2&page_size=10"
```

**响应示例**:
```json
{
    "message": "获取论文列表成功",
    "status": "success",
    "data": {
        "papers": [
            {
                "paper_id": 1,
                "title": "Sample Paper",
                "author": "Author1, Author2",
                "abstract": "This is an abstract...",
                "pdf_url": "https://arxiv.org/pdf/2311.12345.pdf"
            }
        ],
        "pagination": {
            "page": 1,
            "page_size": 20,
            "total": 100,
            "total_pages": 5
        }
    }
}
```

### 3. GET /api/papers/<paper_id>
获取论文详情

**示例**:
```bash
curl http://localhost:3001/api/papers/1
```

**响应示例**:
```json
{
    "message": "获取论文详情成功",
    "status": "success",
    "data": {
        "paper": {
            "paper_id": 1,
            "title": "Sample Paper Title",
            "author": "Author1, Author2, Author3",
            "abstract": "Full abstract text...",
            "pdf_url": "https://arxiv.org/pdf/2311.12345.pdf"
        }
    }
}
```

## 🔄 工作流程

```
用户调用 POST /api/papers/fetch
         ↓
1. 调用 PaperFetchService.fetch_papers()
   - 从 arXiv API 抓取论文数据
   - 返回包含完整元数据的论文列表
         ↓
2. 对每篇论文处理：
   - 检查标题是否已存在（去重）
   - 将 authors 列表转为字符串（用逗号分隔）
   - 只提取需要的字段：title, author, abstract, pdf_url
         ↓
3. 使用 DbManager.execute() 插入数据库
   - INSERT INTO papers (title, author, abstract, pdf_url)
         ↓
返回统计结果（抓取数、保存数、失败数）
```

## 🐍 Python 使用示例

```python
import requests

BASE_URL = "http://localhost:3001/api"

# 1. 抓取并保存论文
print("正在抓取论文...")
response = requests.post(
    f"{BASE_URL}/papers/fetch",
    json={"max_results": 20}
)
result = response.json()
print(f"✅ {result['message']}")
print(f"   保存了 {result['data']['saved_count']} 篇论文")

# 2. 获取论文列表
print("\n获取论文列表...")
response = requests.get(f"{BASE_URL}/papers/list?page=1&page_size=5")
papers = response.json()['data']['papers']

print(f"共 {len(papers)} 篇论文:")
for paper in papers:
    print(f"  [{paper['paper_id']}] {paper['title']}")
    print(f"      作者: {paper['author']}")
    print(f"      PDF: {paper['pdf_url']}")

# 3. 获取论文详情
if papers:
    paper_id = papers[0]['paper_id']
    print(f"\n获取论文详情 (ID={paper_id})...")
    response = requests.get(f"{BASE_URL}/papers/{paper_id}")
    detail = response.json()['data']['paper']
    print(f"  标题: {detail['title']}")
    print(f"  摘要: {detail['abstract'][:100]}...")
```

## 🧪 测试

运行测试脚本：

```bash
python backend/test/test_papers_api.py
```

测试脚本会：
1. ✅ 检查数据库连接
2. ✅ 验证表结构
3. ✅ 抓取 5 篇论文
4. ✅ 保存到数据库
5. ✅ 查询验证

## 📊 数据映射

从 `fetch_papers` 返回的数据 → 数据库字段：

| fetch_papers 返回 | 数据库字段 | 转换说明 |
|------------------|-----------|---------|
| title | title | 直接保存 |
| authors (列表) | author | 用逗号分隔，转为字符串 |
| abstract | abstract | 直接保存 |
| pdf_url | pdf_url | 直接保存 |
| arxiv_id | - | 不保存（数据库设计中没有） |
| categories | - | 不保存 |
| published_date | - | 不保存 |
| ... | - | 其他字段不保存 |

## 📖 Swagger 文档

启动服务后访问：

```
http://localhost:3001/docs/
```

可以在 Swagger UI 中：
- 查看所有 API 端点
- 交互式测试 API
- 查看请求/响应模型

## ⚠️ 注意事项

1. **ArXiv API 限流**: 
   - arXiv 有请求频率限制
   - 建议单次不要抓取太多（max_results 控制在 50 以内）
   - 不要频繁调用 fetch 接口

2. **去重机制**:
   - 通过 `title` 字段检查是否已存在
   - 如果论文已存在，自动跳过

3. **时间窗口**:
   - 默认抓取前两天到前一天的论文
   - 这个时间窗口在 `PaperFetchService` 中定义

4. **字段限制**:
   - 只保存数据库设计中的字段
   - 其他元数据（如分类、发布日期）不保存

## 🔍 故障排查

### 问题：数据库连接失败
- 检查 MySQL 服务是否启动
- 检查 `config.py` 或 `.env` 中的数据库配置
- 确认数据库用户名和密码正确

### 问题：papers 表不存在
- 在 DBeaver 中手动创建表：
```sql
CREATE TABLE papers (
    paper_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(1000) NOT NULL,
    author VARCHAR(1000),
    abstract TEXT,
    pdf_url VARCHAR(512)
);
```

### 问题：抓取失败
- 检查网络连接
- 确认能访问 arXiv API
- 查看服务日志了解详细错误

## 🎉 完成！

现在你的 Papers API 已经完全适配现有数据库结构！

**核心功能**:
- ✅ 从 arXiv 抓取论文
- ✅ 自动保存到你的 papers 表
- ✅ 完全适配数据库设计（paper_id, title, author, abstract, pdf_url）
- ✅ RESTful API 接口
- ✅ 自动去重
- ✅ Swagger 文档

**一键抓取论文**:
```bash
curl -X POST http://localhost:3001/api/papers/fetch \
  -H "Content-Type: application/json" \
  -d '{"max_results": 50}'
```

