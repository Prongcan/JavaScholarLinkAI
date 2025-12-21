import React, { useState, useEffect } from 'react'
import './Explore.css'

const Explore = () => {
  const [blogs, setBlogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [helloMessage, setHelloMessage] = useState('')

  // 获取 Hello World 消息
  useEffect(() => {
    fetch('http://127.0.0.1:3001/api/hello/')
      .then(response => response.json())
      .then(data => {
        setHelloMessage(data.message)
      })
      .catch(error => {
        console.error('Error fetching hello message:', error)
        setHelloMessage('无法从后端加载消息。')
      })
  }, [])

  // 模拟从 API 获取博客数据
  useEffect(() => {
    // 这里将来会替换为真实的 API 调用
    const mockBlogs = [
      {
        id: 1,
        title: "人工智能在学术研究中的应用",
        author: "张教授",
        date: "2024-01-15",
        summary: "探讨AI技术如何改变传统学术研究模式，提高研究效率和质量。",
        tags: ["AI", "学术研究", "技术"],
        readTime: "5分钟"
      },
      {
        id: 2,
        title: "机器学习算法优化研究",
        author: "李博士",
        date: "2024-01-14",
        summary: "深入分析各种机器学习算法的性能优化方法，为实际应用提供指导。",
        tags: ["机器学习", "算法优化", "研究"],
        readTime: "8分钟"
      },
      {
        id: 3,
        title: "深度学习在自然语言处理中的突破",
        author: "王研究员",
        date: "2024-01-13",
        summary: "介绍最新的深度学习技术在NLP领域的重要进展和应用案例。",
        tags: ["深度学习", "NLP", "技术突破"],
        readTime: "6分钟"
      }
    ]
    
    setTimeout(() => {
      setBlogs(mockBlogs)
      setLoading(false)
    }, 1000)
  }, [])

  const handleLoadMore = () => {
    // 模拟加载更多博客
    console.log('加载更多博客...')
  }

  if (loading) {
    return (
      <div className="explore-container">
        <div className="loading">
          <div className="loading-spinner"></div>
          <p>正在加载更多论文...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="explore-container">
      <div className="explore-header">
        <h1>探索论文</h1>
        <h2>{helloMessage}</h2>
        <p>发现最新的学术研究成果和前沿技术</p>
      </div>
      
      <div className="blogs-grid">
        {blogs.map(blog => (
          <div key={blog.id} className="blog-card">
            <div className="blog-header">
              <h3 className="blog-title">{blog.title}</h3>
              <div className="blog-meta">
                <span className="author">作者: {blog.author}</span>
                <span className="date">{blog.date}</span>
                <span className="read-time">{blog.readTime}</span>
              </div>
            </div>
            <p className="blog-summary">{blog.summary}</p>
            <div className="blog-tags">
              {blog.tags.map(tag => (
                <span key={tag} className="tag">{tag}</span>
              ))}
            </div>
            <div className="blog-actions">
              <button className="btn-primary">阅读全文</button>
              <button className="btn-secondary">收藏</button>
            </div>
          </div>
        ))}
      </div>
      
      <div className="load-more">
        <button className="btn-load-more" onClick={handleLoadMore}>
          加载更多论文...
        </button>
      </div>
    </div>
  )
}

export default Explore
