"""
测试 Papers API 完整工作流
"""
import sys
import os

# 添加父目录到路径
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from service.fetch_papers import PaperFetchService
from service.dbmanager import DbManager


def test_fetch_and_save():
    """测试抓取论文并保存到数据库的完整流程"""
    print("=" * 80)
    print("测试 Papers API - 完整工作流")
    print("=" * 80)
    
    # 1. 测试数据库连接
    print("\n[步骤 1] 测试数据库连接...")
    db = DbManager()
    
    if not db.ping():
        print("❌ 数据库连接失败！请检查配置。")
        return False
    
    print("✅ 数据库连接成功")
    
    # 2. 检查表结构
    print("\n[步骤 2] 检查 papers 表结构...")
    try:
        columns = db.query_all("SHOW COLUMNS FROM papers")
        print(f"✅ papers 表存在，包含 {len(columns)} 个字段:")
        for col in columns:
            print(f"   - {col['Field']}: {col['Type']}")
        
        # 检查必要字段（匹配数据库设计：paper_id, title, author, abstract, pdf_url）
        required_fields = ['paper_id', 'title', 'author', 'abstract', 'pdf_url']
        existing_fields = {col['Field'] for col in columns}
        missing_fields = set(required_fields) - existing_fields
        
        if missing_fields:
            print(f"\n⚠️  缺少字段: {missing_fields}")
            print("请检查数据库表结构")
            return False
        
        print("✅ 所有必要字段都存在")
        
    except Exception as e:
        print(f"❌ 检查表结构失败: {e}")
        print("请运行: python backend/service/init_db.py")
        print("然后运行: python backend/service/migrate_papers_table.py")
        return False
    
    # 3. 测试抓取论文
    print("\n[步骤 3] 测试抓取论文...")
    print("正在抓取前两天到前一天的论文（最多 5 篇用于测试）...")
    
    try:
        fetch_service = PaperFetchService()
        papers = fetch_service.fetch_papers(max_results=5)
        
        if not papers:
            print("⚠️  未抓取到论文（可能该时间窗口内没有新论文）")
            print("这是正常的，继续测试其他功能...")
            papers = []
        else:
            print(f"✅ 成功抓取 {len(papers)} 篇论文")
            
            # 显示第一篇论文
            first_paper = papers[0]
            print(f"\n第一篇论文示例:")
            print(f"  ArXiv ID: {first_paper['arxiv_id']}")
            print(f"  标题: {first_paper['title'][:80]}...")
            print(f"  作者: {', '.join(first_paper['authors'][:3])}...")
            print(f"  分类: {first_paper['primary_category']}")
        
    except Exception as e:
        print(f"❌ 抓取论文失败: {e}")
        import traceback
        traceback.print_exc()
        return False
    
    # 4. 测试保存到数据库
    if papers:
        print("\n[步骤 4] 测试保存论文到数据库...")
        
        saved_count = 0
        failed_count = 0
        
        for paper in papers:
            try:
                # 检查是否已存在（通过标题）
                existing = db.query_one(
                    "SELECT paper_id FROM papers WHERE title = %s",
                    (paper['title'],)
                )
                
                if existing:
                    print(f"  ⏭️  论文 {paper['title'][:50]}... 已存在，跳过")
                    continue
                
                # 保存论文（只保存数据库设计要求的字段）
                authors_str = ', '.join(paper['authors'])
                
                result = db.execute(
                    """
                    INSERT INTO papers 
                    (title, author, abstract, pdf_url)
                    VALUES (%s, %s, %s, %s)
                    """,
                    (
                        paper['title'],
                        authors_str,
                        paper['abstract'],
                        paper['pdf_url']
                    )
                )
                
                saved_count += 1
                print(f"  ✅ 成功保存: {paper['title'][:50]}... (ID={result['lastrowid']})")
                
            except Exception as e:
                failed_count += 1
                print(f"  ❌ 保存失败 {paper.get('title', 'Unknown')[:30]}: {e}")
        
        print(f"\n保存结果: 成功 {saved_count} 篇, 失败 {failed_count} 篇")
    
    # 5. 测试查询论文
    print("\n[步骤 5] 测试查询论文...")
    
    try:
        # 查询最新的 3 篇论文（匹配数据库字段）
        recent_papers = db.query_all(
            """
            SELECT paper_id, title, author, abstract, pdf_url
            FROM papers 
            ORDER BY paper_id DESC 
            LIMIT 3
            """,
            ()
        )
        
        if recent_papers:
            print(f"✅ 成功查询到 {len(recent_papers)} 篇论文:")
            for paper in recent_papers:
                print(f"\n  论文 ID: {paper['paper_id']}")
                print(f"  标题: {paper['title'][:60]}...")
                print(f"  作者: {paper.get('author', 'N/A')[:50]}...")
        else:
            print("⚠️  数据库中暂无论文")
        
        # 统计信息
        stats = db.query_one("SELECT COUNT(*) as total FROM papers")
        print(f"\n数据库统计:")
        print(f"  总论文数: {stats['total']}")
        
    except Exception as e:
        print(f"❌ 查询失败: {e}")
        return False
    
    # 6. 测试按 paper_id 查询
    if recent_papers:
        print("\n[步骤 6] 测试按 paper_id 查询...")
        try:
            test_paper_id = recent_papers[0]['paper_id']
            paper = db.query_one(
                "SELECT * FROM papers WHERE paper_id = %s",
                (test_paper_id,)
            )
            
            if paper:
                print(f"✅ 成功查询到论文: ID={test_paper_id}")
                print(f"  完整字段数: {len(paper)}")
                print(f"  字段: {list(paper.keys())}")
            else:
                print(f"❌ 未找到论文: ID={test_paper_id}")
        except Exception as e:
            print(f"❌ 查询失败: {e}")
    
    print("\n" + "=" * 80)
    print("✅ 所有测试完成！")
    print("=" * 80)
    print("\n提示：")
    print("  1. 启动服务: python backend/app.py")
    print("  2. 访问 API 文档: http://localhost:3001/docs/")
    print("  3. 测试 fetch 接口:")
    print("     curl -X POST http://localhost:3001/api/papers/fetch \\")
    print("       -H 'Content-Type: application/json' \\")
    print("       -d '{\"max_results\": 10}'")
    print("  4. 查看论文列表:")
    print("     curl http://localhost:3001/api/papers/list")
    print("  5. 查看论文详情:")
    print("     curl http://localhost:3001/api/papers/1")
    
    return True


if __name__ == "__main__":
    try:
        success = test_fetch_and_save()
        exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n测试被中断")
        exit(1)
    except Exception as e:
        print(f"\n\n测试出错: {e}")
        import traceback
        traceback.print_exc()
        exit(1)

