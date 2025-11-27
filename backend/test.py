"""
快速测试脚本 - 交互式验证
"""
from datetime import datetime, timedelta
from service.fetch_papers import PaperFetchService


def quick_test():
    print("=" * 60)
    print("论文抓取服务 - 快速测试")
    print("=" * 60)
    
    service = PaperFetchService()
    
    # 显示将要抓取的时间范围
    current_time = datetime.utcnow()
    start_time = current_time - timedelta(days=2)
    end_time = current_time - timedelta(days=1)
    
    print(f"\n[测试] 当前UTC时间: {current_time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"将抓取时间范围:")
    print(f"  开始: {start_time.strftime('%Y-%m-%d %H:%M:%S')} (前两天)")
    print(f"  结束: {end_time.strftime('%Y-%m-%d %H:%M:%S')} (前一天)")
    print("\n正在抓取论文...\n")
    
    try:
        # 直接调用 fetch_papers()，不需要传入日期参数
        papers = service.fetch_papers(max_results=None)
        
        print(f"\n✓ 成功抓取 {len(papers)} 篇论文")
        
        # 展示前3篇
        if papers:
            print("\n" + "=" * 60)
            print(f"前 {min(3, len(papers))} 篇论文预览:")
            print("=" * 60)
            
            for i, paper in enumerate(papers[:3], 1):
                print(f"\n【论文 {i}】")
                print(f"  ID: {paper['arxiv_id']}")
                print(f"  标题: {paper['title']}")
                print(f"  作者: {', '.join(paper['authors'][:3])}")
                if len(paper['authors']) > 3:
                    print(f"       (还有 {len(paper['authors']) - 3} 位作者)")
                print(f"  分类: {paper['primary_category']}")
                print(f"  发布时间: {paper['published_date']}")
                print(f"  更新时间: {paper['updated_date']}")
                print(f"  PDF: {paper['pdf_url']}")
                print(f"  摘要: {paper['abstract'][:100]}...")
        else:
            print("\n⚠ 该时间窗口内没有找到论文")
            print("这可能是因为：")
            print("  1. arXiv 数据更新有延迟")
            print("  2. 该时间段内确实没有新论文提交")
            print("  3. 网络连接问题")
        
        # 测试2: 验证去重
        print("\n" + "=" * 60)
        print("测试去重机制...")
        print("=" * 60)
        
        papers2 = service.fetch_papers(max_results=10)
        print(f"✓ 第二次抓取结果: {len(papers2)} 篇（应为0）")
        
        if len(papers2) == 0:
            print("✓ 去重机制工作正常！")
        else:
            print("⚠ 警告: 去重可能有问题")
            print(f"  意外获取到 {len(papers2)} 篇新论文")
        
    except Exception as e:
        print(f"\n✗ 测试失败: {str(e)}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    quick_test()
