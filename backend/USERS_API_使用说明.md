# Users API 使用说明

## 📋 概述

Users API 提供了完整的用户管理功能，使用 `DbManager` 直接操作 MySQL 数据库。

## 📊 数据库表结构

```sql
users 表：
- user_id: INT (主键, 自增)
- username: VARCHAR (用户名, 唯一)
- password: VARCHAR (密码, SHA256 哈希)
- interest: VARCHAR (用户兴趣)
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
  "timestamp": "2024-11-27T19:30:00",
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

### 2. GET /api/users/<user_id>
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

### 3. PUT /api/users/<user_id>/interest
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

### 4. GET /api/users/<user_id>/interest
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

### 6. DELETE /api/users/<user_id>
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

## 🐍 Python 使用示例

```python
import requests

BASE_URL = "http://localhost:3001/api"

# 1. 用户注册
print("1. 注册用户...")
response = requests.post(
    f"{BASE_URL}/users/register",
    json={
        "username": "test_user",
        "password": "test123",
        "interest": "Machine Learning, Deep Learning"
    }
)
result = response.json()
print(f"✅ {result['message']}")
user_id = result['data']['user_id']

# 2. 获取用户信息
print("\n2. 获取用户信息...")
response = requests.get(f"{BASE_URL}/users/{user_id}")
user = response.json()['data']['user']
print(f"用户名: {user['username']}")
print(f"兴趣: {user['interest']}")

# 3. 更新兴趣
print("\n3. 更新用户兴趣...")
response = requests.put(
    f"{BASE_URL}/users/{user_id}/interest",
    json={"interest": "Computer Vision, NLP, AI"}
)
result = response.json()
print(f"✅ {result['message']}")

# 4. 获取用户列表
print("\n4. 获取用户列表...")
response = requests.get(f"{BASE_URL}/users/list?page=1&page_size=5")
data = response.json()['data']
print(f"总用户数: {data['pagination']['total']}")
for user in data['users']:
    print(f"  - [{user['user_id']}] {user['username']}")
```

## 🔒 密码处理

当前使用 **SHA256** 哈希存储密码（简化版本）。

**生产环境建议**：
- 使用 `bcrypt` 或 `argon2` 进行密码哈希
- 添加盐值（salt）
- 实现密码强度验证

```python
# 当前实现（简单）
import hashlib
password_hash = hashlib.sha256(password.encode()).hexdigest()

# 生产环境推荐
import bcrypt
password_hash = bcrypt.hashpw(password.encode(), bcrypt.gensalt())
```

## 🧪 测试

### 运行测试脚本

```bash
# 激活虚拟环境
.\.venv\Scripts\Activate.ps1

# 运行用户 API 测试
python backend/test/test_users_api.py
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

1. 启动服务：`python backend/app.py`
2. 访问：http://localhost:3001/docs/
3. 找到 `users` 分组
4. 测试各个端点

## 📊 数据库操作

所有 API 都使用 `DbManager` 进行数据库操作：

```python
from service.dbmanager import DbManager

db = DbManager()

# 查询单个用户
user = db.query_one(
    "SELECT * FROM users WHERE user_id = %s",
    (user_id,)
)

# 插入用户
result = db.execute(
    "INSERT INTO users (username, password, interest) VALUES (%s, %s, %s)",
    (username, password_hash, interest)
)

# 更新兴趣
result = db.execute(
    "UPDATE users SET interest = %s WHERE user_id = %s",
    (interest, user_id)
)

# 查询所有用户
users = db.query_all(
    "SELECT user_id, username, interest FROM users LIMIT %s",
    (20,)
)
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
   - 生产环境请使用 bcrypt

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

```python
# 用户登录示例（待实现）
@users_ns.route('/login')
class UserLogin(Resource):
    def post(self):
        # 验证用户名和密码
        # 生成 JWT token
        # 返回认证信息
        pass
```

## 📁 文件结构

```
backend/
├── api_router/
│   ├── users_routes.py          ← 用户 API（新增）
│   ├── papers_routes.py         ← 论文 API
│   └── hello_routes.py
├── service/
│   ├── dbmanager.py            ← 数据库管理器
│   └── fetch_papers.py
├── test/
│   ├── test_users_api.py       ← 用户 API 测试（新增）
│   ├── test_papers_api.py
│   └── test_fetch_papers.py
├── app.py                      ← 主应用（已更新）
└── USERS_API_使用说明.md       ← 本文档（新增）
```

## ✅ 完成！

现在你可以：
1. 启动服务：`python backend/app.py`
2. 运行测试：`python backend/test/test_users_api.py`
3. 访问文档：http://localhost:3001/docs/
4. 开始使用用户管理 API！🎉

