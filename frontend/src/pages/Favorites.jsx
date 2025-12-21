import React from 'react'
import { Link } from 'react-router-dom'
import './Favorites.css'

const Favorites = ({ isLoggedIn }) => {
  if (!isLoggedIn) {
    return (
      <div className="favorites-container">
        <div className="login-prompt">
          <h2>我的收藏</h2>
          <p>请先登录以查看您的收藏论文</p>
          <Link to="/login" className="btn-login">
            立即登录
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="favorites-container">
      <div className="favorites-header">
        <h1>我的收藏</h1>
        <p>您收藏的论文和文章</p>
      </div>
      
      <div className="favorites-content">
        <div className="empty-state">
          <div className="empty-icon">📚</div>
          <h3>暂无收藏</h3>
          <p>开始探索并收藏您感兴趣的论文吧！</p>
          <Link to="/explore" className="btn-primary">
            去探索
          </Link>
        </div>
      </div>
    </div>
  )
}

export default Favorites
