# 测试文件说明

## 📁 测试文件列表

### 1. test_fetch_papers.py
**测试论文抓取服务**

- 测试从 arXiv 抓取论文
- 验证时间窗口设置
- 测试去重机制

**运行方法**:
```bash
python backend/test/test_fetch_papers.py
```

### 2. test_papers_api.py
**测试论文 API 完整工作流**

- 测试数据库连接
- 测试表结构
- 测试抓取并保存论文
- 测试查询功能

**运行方法**:
```bash
python backend/test/test_papers_api.py
```

### 3. test_users_api.py
**测试用户管理 API**

- 测试用户注册
- 测试用户查询
- 测试兴趣更新
- 测试用户列表

**运行方法**:
```bash
python backend/test/test_users_api.py
```

### 4. test_db_manager.py
**测试数据库管理器**

- 测试 DbManager 基本功能
- 测试连接池
- 测试事务处理

**运行方法**:
```bash
python backend/test/test_db_manager.py
```

## 🚀 运行所有测试

```bash
# 激活虚拟环境
.\.venv\Scripts\Activate.ps1

# 运行所有测试
python backend/test/test_db_manager.py
python backend/test/test_fetch_papers.py
python backend/test/test_papers_api.py
python backend/test/test_users_api.py
```

## 📋 测试前提条件

1. **数据库已创建**
   - 数据库名: `scholarlink_ai`
   - 表: `papers`, `users`, `recommendations`

2. **虚拟环境已激活**
   ```bash
   .\.venv\Scripts\Activate.ps1
   ```

3. **依赖已安装**
   ```bash
   pip install -r backend/requirements.txt
   ```

4. **数据库配置正确**
   - 检查 `backend/config.py`
   - 或配置 `.env` 文件

## ✅ 测试检查清单

- [ ] 数据库连接成功
- [ ] papers 表存在且结构正确
- [ ] users 表存在且结构正确
- [ ] 能够成功抓取论文
- [ ] 能够保存论文到数据库
- [ ] 能够注册和管理用户
- [ ] API 端点正常工作

## 🔧 故障排查

### 问题：ModuleNotFoundError
```bash
# 确保从项目根目录运行
cd F:\31\软件工程\project\SE-Program-ScholarLinkAI
python backend/test/test_xxx.py
```

### 问题：数据库连接失败
- 检查 MySQL 服务是否启动
- 检查数据库配置
- 验证用户权限

### 问题：表不存在
```bash
# 运行初始化脚本
python backend/service/init_db.py
```

