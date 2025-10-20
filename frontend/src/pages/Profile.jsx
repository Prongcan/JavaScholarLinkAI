import React from 'react'
import { Link } from 'react-router-dom'
import './Profile.css'

const Profile = ({ isLoggedIn }) => {
  if (!isLoggedIn) {
    return (
      <div className="profile-container">
        <div className="login-prompt">
          <h2>个人资料</h2>
          <p>请先登录以查看和管理您的个人资料</p>
          <Link to="/login" className="btn-login">
            立即登录
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="profile-container">
      <div className="profile-header">
        <h1>个人资料</h1>
        <p>管理您的账户信息和偏好设置</p>
      </div>
      
      <div className="profile-content">
        <div className="profile-card">
          <div className="profile-avatar">
            <div className="avatar-placeholder">👤</div>
          </div>
          <div className="profile-info">
            <h3>用户名</h3>
            <p>user@example.com</p>
            <div className="profile-stats">
              <div className="stat">
                <span className="stat-number">0</span>
                <span className="stat-label">收藏论文</span>
              </div>
              <div className="stat">
                <span className="stat-number">0</span>
                <span className="stat-label">阅读历史</span>
              </div>
            </div>
          </div>
        </div>
        
        <div className="profile-actions">
          <button className="btn-primary">编辑资料</button>
          <button className="btn-secondary">修改密码</button>
        </div>
      </div>
    </div>
  )
}

export default Profile
