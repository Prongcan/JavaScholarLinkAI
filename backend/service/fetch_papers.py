"""
Paper Fetching Service
从 arXiv 抓取前两天到前一天的 CS 类论文元数据
"""

import arxiv
import time
from datetime import datetime, timedelta
from typing import List, Dict, Optional
import logging

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class PaperFetchService:
    """
    论文抓取服务类
    负责从 arXiv 获取前两天到前一天的论文元数据
    """
    
    def __init__(self):
        """初始化 arXiv 客户端"""
        self.client = arxiv.Client()
        self.arxiv_pool = set()  # 用于去重的论文 ID 集合
    
    def fetch_papers(self, max_results: Optional[int] = None) -> List[Dict]:
        """
        抓取前两天到前一天的论文
        
        时间窗口：[当前时间 - 2天, 当前时间 - 1天]
        
        Args:
            max_results: 最大返回结果数，None 表示不限制
            
        Returns:
            论文元数据列表
        """
        # 计算时间窗口：前两天到前一天
        current_time = datetime.utcnow()
        end_time = current_time - timedelta(days=1)    # 前一天
        start_time = current_time - timedelta(days=2)  # 前两天
        
        # 格式化时间为 arXiv API 要求的格式: YYYYMMDDHHMMSS
        start_time_str = start_time.strftime("%Y%m%d%H%M%S")
        end_time_str = end_time.strftime("%Y%m%d%H%M%S")
        
        logger.info(f"开始抓取时间窗口: [{start_time_str} TO {end_time_str}]")
        logger.info(f"时间范围: {start_time.strftime('%Y-%m-%d %H:%M:%S')} 到 {end_time.strftime('%Y-%m-%d %H:%M:%S')} (UTC)")
        
        # 构建查询
        query = f"cat:cs.* AND submittedDate:[{start_time_str} TO {end_time_str}]"
        
        search = arxiv.Search(
            query=query,
            max_results=max_results,
            sort_by=arxiv.SortCriterion.SubmittedDate
        )
        
        logger.info(f"正在从 arXiv 抓取 CS 类论文...")
        
        try:
            # 获取搜索结果
            results = self.client.results(search)
            results_list = list(results)
            
            logger.info(f"成功搜索! 共找到 {len(results_list)} 篇论文")
            
            # 解析论文数据
            papers = self._parse_papers(results_list)
            
            logger.info(f"成功解析 {len(papers)} 篇论文元数据")
            
            return papers
            
        except Exception as e:
            logger.error(f"抓取论文时出错: {str(e)}")
            raise
    
    def _parse_papers(self, results_list: List) -> List[Dict]:
        """
        解析论文结果列表，提取元数据
        
        Args:
            results_list: arXiv API 返回的结果列表
            
        Returns:
            解析后的论文元数据列表
        """
        papers = []
        failed_count = 0
        
        for idx, result in enumerate(results_list):
            try:
                # 提取 arxiv_id (去掉版本号)
                arxiv_id = result.entry_id.split('/abs/')[-1].split('v')[0]
                
                # 去重检查
                if arxiv_id in self.arxiv_pool:
                    logger.debug(f"论文 {arxiv_id} 已存在，跳过")
                    continue
                
                # 提取元数据
                paper_data = {
                    'arxiv_id': arxiv_id,
                    'title': result.title,
                    'authors': [author.name for author in result.authors],
                    'categories': result.categories,
                    'primary_category': result.primary_category,
                    'published_date': result.published.strftime("%Y-%m-%d %H:%M:%S"),
                    'updated_date': result.updated.strftime("%Y-%m-%d %H:%M:%S"),
                    'abstract': result.summary,
                    'pdf_url': result.pdf_url,
                    'entry_url': result.entry_id,
                    'comment': result.comment if hasattr(result, 'comment') else None,
                    'journal_ref': result.journal_ref if hasattr(result, 'journal_ref') else None,
                }
                
                papers.append(paper_data)
                self.arxiv_pool.add(arxiv_id)
                
                # arXiv API 限流：每3秒最多1个请求
                # 为了安全，在处理每篇论文后稍微延迟
                if (idx + 1) % 10 == 0:  # 每处理10篇论文延迟一次
                    time.sleep(3)
                    logger.debug(f"已处理 {idx + 1} 篇论文...")
                
            except Exception as e:
                failed_count += 1
                logger.error(f"解析论文时出错 (索引 {idx}): {str(e)}")
                # 记录失败的论文ID（如果可以获取）
                try:
                    failed_id = result.entry_id if hasattr(result, 'entry_id') else 'Unknown'
                    logger.error(f"失败的论文ID: {failed_id}")
                except:
                    pass
                continue
        
        if failed_count > 0:
            logger.warning(f"共有 {failed_count} 篇论文解析失败")
        
        return papers


# 使用示例
if __name__ == "__main__":
    # 创建服务实例
    service = PaperFetchService()
    
    # 抓取前两天到前一天的论文
    papers = service.fetch_papers(max_results=None)
    
    # 打印结果
    print(f"\n成功抓取 {len(papers)} 篇论文")
    if papers:
        print("\n第一篇论文示例:")
        first_paper = papers[0]
        print(f"ID: {first_paper['arxiv_id']}")
        print(f"标题: {first_paper['title']}")
        print(f"作者: {', '.join(first_paper['authors'][:3])}...")
        print(f"发布日期: {first_paper['published_date']}")
        print(f"PDF链接: {first_paper['pdf_url']}")
