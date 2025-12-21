"""
测试用户管理 API
"""
import sys
import os

# 添加父目录到路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from service.dbmanager import DbManager


def test_users_api():
    """测试用户管理 API 的数据库操作"""
    print("=" * 80)
    print("测试用户管理 API")
    print("=" * 80)
    
    db = DbManager()
    
    # 1. 测试数据库连接
    print("\n[步骤 1] 测试数据库连接...")
    if not db.ping():
        print("❌ 数据库连接失败！")
        return False
    print("✅ 数据库连接成功")
    
    # 2. 检查 users 表结构
    print("\n[步骤 2] 检查 users 表结构...")
    try:
        columns = db.query_all("SHOW COLUMNS FROM users")
        print(f"✅ users 表存在，包含 {len(columns)} 个字段:")
        for col in columns:
            print(f"   - {col['Field']}: {col['Type']}")
        
        # 检查必要字段
        required_fields = ['user_id', 'username', 'password', 'interest']
        existing_fields = {col['Field'] for col in columns}
        missing_fields = set(required_fields) - existing_fields
        
        if missing_fields:
            print(f"\n⚠️  缺少字段: {missing_fields}")
            return False
        
        print("✅ 所有必要字段都存在")
        
    except Exception as e:
        print(f"❌ 检查表结构失败: {e}")
        return False
    
    # 3. 测试用户注册（插入）
    print("\n[步骤 3] 测试用户注册...")
    try:
        # 检查测试用户是否已存在
        existing = db.query_one(
            "SELECT user_id FROM users WHERE username = %s",
            ('test_user_demo',)
        )
        
        if existing:
            print(f"   测试用户已存在，先删除...")
            db.execute("DELETE FROM users WHERE username = %s", ('test_user_demo',))
        
        # 插入测试用户
        import hashlib
        password_hash = hashlib.sha256('test123'.encode()).hexdigest()
        
        result = db.execute(
            """
            INSERT INTO users (username, password, interest)
            VALUES (%s, %s, %s)
            """,
            ('test_user_demo', password_hash, 'Machine Learning, NLP')
        )
        
        test_user_id = result['lastrowid']
        print(f"✅ 用户注册成功: test_user_demo (ID={test_user_id})")
        
    except Exception as e:
        print(f"❌ 用户注册失败: {e}")
        return False
    
    # 4. 测试查询用户
    print("\n[步骤 4] 测试查询用户...")
    try:
        user = db.query_one(
            "SELECT user_id, username, interest FROM users WHERE user_id = %s",
            (test_user_id,)
        )
        
        if user:
            print(f"✅ 查询成功:")
            print(f"   ID: {user['user_id']}")
            print(f"   用户名: {user['username']}")
            print(f"   兴趣: {user['interest']}")
        else:
            print(f"❌ 未找到用户")
            return False
            
    except Exception as e:
        print(f"❌ 查询用户失败: {e}")
        return False
    
    # 5. 测试更新兴趣
    print("\n[步骤 5] 测试更新用户兴趣...")
    try:
        new_interest = "Computer Vision, Deep Learning, AI"
        result = db.execute(
            "UPDATE users SET interest = %s WHERE user_id = %s",
            (new_interest, test_user_id)
        )
        
        print(f"✅ 兴趣更新成功，影响 {result['rowcount']} 行")
        
        # 验证更新
        updated_user = db.query_one(
            "SELECT interest FROM users WHERE user_id = %s",
            (test_user_id,)
        )
        print(f"   新兴趣: {updated_user['interest']}")
        
    except Exception as e:
        print(f"❌ 更新兴趣失败: {e}")
        return False
    
    # 6. 测试查询用户列表
    print("\n[步骤 6] 测试查询用户列表...")
    try:
        users = db.query_all(
            """
            SELECT user_id, username, interest
            FROM users
            ORDER BY user_id DESC
            LIMIT 5
            """,
            ()
        )
        
        print(f"✅ 查询到 {len(users)} 个用户:")
        for u in users:
            print(f"   [{u['user_id']}] {u['username']} - {u.get('interest', 'N/A')[:30]}...")
            
    except Exception as e:
        print(f"❌ 查询用户列表失败: {e}")
        return False
    
    # 7. 统计信息
    print("\n[步骤 7] 用户统计...")
    try:
        stats = db.query_one("SELECT COUNT(*) as total FROM users")
        print(f"   总用户数: {stats['total']}")
        
    except Exception as e:
        print(f"❌ 统计失败: {e}")
        return False
    
    # 8. 清理测试数据（可选）
    print("\n[步骤 8] 清理测试数据...")
    try:
        db.execute("DELETE FROM users WHERE username = %s", ('test_user_demo',))
        print("✅ 测试用户已删除")
    except Exception as e:
        print(f"⚠️  清理失败（不影响测试结果）: {e}")
    
    print("\n" + "=" * 80)
    print("✅ 所有测试完成！")
    print("=" * 80)
    print("\n接下来:")
    print("  1. 启动服务: python backend/app.py")
    print("  2. 访问 API 文档: http://localhost:3001/docs/")
    print("  3. 测试用户 API:")
    print("     - POST /api/users/register    - 用户注册")
    print("     - GET  /api/users/<id>        - 获取用户信息")
    print("     - PUT  /api/users/<id>/interest - 更新兴趣")
    print("     - GET  /api/users/list        - 用户列表")
    print()
    
    return True


if __name__ == "__main__":
    try:
        success = test_users_api()
        exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n测试被中断")
        exit(1)
    except Exception as e:
        print(f"\n\n测试出错: {e}")
        import traceback
        traceback.print_exc()
        exit(1)

