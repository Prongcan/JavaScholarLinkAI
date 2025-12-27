<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>论文管理系统</title>
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Arial', sans-serif;
            background: #ffffff;
            min-height: 100vh;
            color: #333;
            position: relative;
        }

        /* 雪花动画 */
        .snowflake {
            position: absolute;
            color: #fff;
            font-size: 20px;
            user-select: none;
            pointer-events: none;
            animation: snowfall linear infinite;
        }

        .snowflake:nth-child(odd) {
            animation-duration: 8s;
        }

        .snowflake:nth-child(even) {
            animation-duration: 12s;
        }

        @keyframes snowfall {
            0% {
                transform: translateY(-100vh) rotate(0deg);
                opacity: 1;
            }
            100% {
                transform: translateY(100vh) rotate(360deg);
                opacity: 0;
            }
        }

        /* 顶部导航栏 */
        .navbar {
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 50%, #a569bd 100%);
            color: white;
            padding: 1rem;
            box-shadow: 0 4px 8px rgba(142, 68, 173, 0.3);
            border-bottom: 3px solid #f39c12;
            position: relative;
        }

        .navbar::before {
            content: '🎄';
            position: absolute;
            left: 20px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 1.5rem;
        }

        .navbar-container {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .navbar h1 {
            margin: 0;
            font-size: 1.8rem;
            font-weight: bold;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
            background: linear-gradient(45deg, #fff, #f39c12, #e74c3c);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            position: relative;
        }

        .navbar h1::before {
            content: '🎓';
            margin-right: 10px;
            font-size: 1.5rem;
        }

        .navbar-tabs {
            display: flex;
            gap: 2rem;
        }

        .tab-btn {
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            border: 2px solid #fff;
            color: white;
            padding: 0.5rem 1rem;
            cursor: pointer;
            border-radius: 25px;
            transition: all 0.3s ease;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
            position: relative;
            overflow: hidden;
        }

        .tab-btn::before {
            content: '';
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: linear-gradient(45deg, transparent, rgba(255,255,255,0.2), transparent);
            transform: rotate(45deg);
            transition: all 0.3s;
            opacity: 0;
        }

        .tab-btn:hover::before {
            opacity: 1;
            animation: shine 0.5s ease-in-out;
        }

        .tab-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(142, 68, 173, 0.3);
        }

        .tab-btn.active {
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(142, 68, 173, 0.4);
        }

        @keyframes shine {
            0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
            100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
        }

        /* 搜索区域 */
        .search-section {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 2rem;
            margin: 2rem auto;
            border-radius: 20px;
            box-shadow: 0 8px 32px rgba(142, 68, 173, 0.15);
            border: 2px solid rgba(142, 68, 173, 0.2);
            position: relative;
            overflow: hidden;
        }

        .search-section::before {
            content: '🔍';
            position: absolute;
            right: -20px;
            top: -20px;
            font-size: 4rem;
            opacity: 0.1;
            transform: rotate(15deg);
        }

        .search-container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .search-bar {
            display: flex;
            gap: 1rem;
            align-items: center;
        }

        #search-input {
            flex: 1;
            padding: 0.75rem 1rem;
            border: 2px solid #8e44ad;
            border-radius: 25px;
            font-size: 1rem;
            background: rgba(255,255,255,0.9);
            transition: all 0.3s ease;
        }

        #search-input:focus {
            outline: none;
            border-color: #f39c12;
            box-shadow: 0 0 10px rgba(243, 156, 18, 0.3);
            background: white;
        }

        .search-btn {
            padding: 0.75rem 1.5rem;
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            color: white;
            border: none;
            border-radius: 25px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
            position: relative;
            overflow: hidden;
        }

        .search-btn::before {
            content: '🔍';
            margin-right: 5px;
        }

        .search-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(142, 68, 173, 0.4);
        }

        /* 内容区域 */
        .content {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 2rem;
            min-height: calc(100vh - 200px);
        }

        /* 论文卡片 */
        .papers-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 2rem;
            margin-bottom: 2rem;
            min-height: 200px; /* 设置最小高度避免内容切换时的抖动 */
            transition: opacity 0.3s ease; /* 添加平滑过渡 */
        }

        .paper-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(142, 68, 173, 0.15);
            overflow: hidden;
            transition: all 0.3s ease;
            border: 2px solid rgba(142, 68, 173, 0.2);
            position: relative;
        }

        .paper-card::before {
            content: '📚';
            position: absolute;
            top: 10px;
            right: 10px;
            font-size: 1.5rem;
            opacity: 0.7;
        }

        .paper-card:hover {
            transform: translateY(-8px) scale(1.02);
            box-shadow: 0 15px 40px rgba(142, 68, 173, 0.25);
            border-color: #f39c12;
            cursor: pointer;
        }

        .paper-header {
            padding: 1.5rem;
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            border-bottom: 2px solid #f39c12;
            color: white;
        }

        .paper-title {
            font-size: 1.25rem;
            font-weight: bold;
            margin-bottom: 0.5rem;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
        }

        .paper-author {
            color: #ecf0f1;
            font-size: 0.9rem;
            opacity: 0.9;
        }

        .paper-author::before {
            content: '👤';
            margin-right: 5px;
        }

        .paper-content {
            padding: 1.5rem;
        }

        .paper-abstract {
            color: #555;
            line-height: 1.6;
            margin-bottom: 1rem;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .paper-actions {
            display: flex;
            gap: 0.5rem;
        }

        .action-btn {
            padding: 0.5rem 1rem;
            border: 2px solid #8e44ad;
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            color: white;
            text-decoration: none;
            border-radius: 20px;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
            position: relative;
            overflow: hidden;
        }

        .action-btn::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }

        .action-btn:hover::before {
            left: 100%;
        }

        .action-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(142, 68, 173, 0.4);
        }

        /* 分页 */
        .pagination {
            display: flex;
            justify-content: center;
            gap: 0.5rem;
            margin-top: 2rem;
        }

        .page-btn {
            padding: 0.5rem 1rem;
            border: 2px solid #8e44ad;
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            color: white;
            text-decoration: none;
            border-radius: 25px;
            transition: all 0.3s ease;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
            position: relative;
            overflow: hidden;
        }

        .page-btn::before {
            content: '📄';
            margin-right: 3px;
            opacity: 0.8;
        }

        .page-btn:hover {
            transform: translateY(-3px) scale(1.1);
            box-shadow: 0 6px 20px rgba(142, 68, 173, 0.4);
        }

        .page-btn.active {
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            border-color: #8e44ad;
            transform: translateY(-3px) scale(1.1);
            box-shadow: 0 6px 20px rgba(142, 68, 173, 0.4);
        }

        /* 模态框 */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.7);
            backdrop-filter: blur(5px);
            z-index: 1000;
        }

        .modal-content {
            background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
            margin: 10% auto;
            padding: 2rem;
            border-radius: 20px;
            width: 90%;
            max-width: 500px;
            box-shadow: 0 15px 40px rgba(142, 68, 173, 0.3);
            border: 2px solid #8e44ad;
            position: relative;
            overflow: hidden;
        }

        .modal-content::before {
            content: '🎅';
            position: absolute;
            top: -30px;
            right: -30px;
            font-size: 3rem;
            opacity: 0.1;
            transform: rotate(15deg);
        }

        .modal-header {
            margin-bottom: 1.5rem;
        }

        .modal-title {
            font-size: 1.5rem;
            margin-bottom: 1rem;
        }

        .close-btn {
            float: right;
            font-size: 2rem;
            cursor: pointer;
            color: #8e44ad;
            transition: all 0.3s ease;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .close-btn:hover {
            color: white;
            background-color: #8e44ad;
            transform: rotate(90deg);
        }

        .form-group {
            margin-bottom: 1rem;
        }

        .form-label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: bold;
        }

        .form-input {
            width: 100%;
            padding: 0.75rem;
            border: 2px solid #8e44ad;
            border-radius: 25px;
            font-size: 1rem;
            background: rgba(255,255,255,0.9);
            transition: all 0.3s ease;
        }

        .form-input:focus {
            outline: none;
            border-color: #f39c12;
            box-shadow: 0 0 10px rgba(243, 156, 18, 0.3);
            background: white;
        }

        .submit-btn {
            width: 100%;
            padding: 0.75rem;
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            color: white;
            border: none;
            border-radius: 25px;
            font-size: 1rem;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
            position: relative;
            overflow: hidden;
        }

        .submit-btn::before {
            content: '🚀';
            margin-right: 5px;
        }

        .submit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(142, 68, 173, 0.4);
        }

        /* 加载状态 */
        .loading {
            text-align: center;
            padding: 3rem;
            color: #2c3e50;
            font-size: 1.2rem;
            font-weight: bold;
        }

        .loading::after {
            content: '🎄 正在加载中... 🎄';
            display: block;
            margin-top: 1rem;
            animation: pulse 1.5s infinite;
        }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }

        /* 错误消息 */
        .error-message {
            background: linear-gradient(135deg, #8e44ad 0%, #9b59b6 100%);
            color: white;
            padding: 1rem;
            border-radius: 15px;
            margin-bottom: 1rem;
            display: none;
            border: 2px solid #fff;
            box-shadow: 0 4px 15px rgba(142, 68, 173, 0.3);
            position: relative;
            overflow: hidden;
        }

        .error-message::before {
            content: '⚠️';
            margin-right: 10px;
            font-size: 1.2rem;
        }

        /* 用户信息显示 */
        .user-info {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            padding: 2rem;
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(142, 68, 173, 0.15);
            border: 2px solid rgba(142, 68, 173, 0.2);
        }

        .user-info h3 {
            margin-bottom: 1rem;
            color: #2c3e50;
        }

        .user-detail {
            margin-bottom: 0.5rem;
        }

        .user-label {
            font-weight: bold;
        }

        /* 响应式设计 */
        @media (max-width: 768px) {
            .navbar-container {
                flex-direction: column;
                gap: 1rem;
            }

            .navbar-tabs {
                gap: 1rem;
            }

            .papers-grid {
                grid-template-columns: 1fr;
            }

            .search-bar {
                flex-direction: column;
            }

            #search-input {
                margin-bottom: 1rem;
            }
        }
    </style>
</head>
<body>
    <!-- 顶部导航栏 -->
    <nav class="navbar">
        <div class="navbar-container">
            <h1>ScholarLinkAI</h1>
            <div class="navbar-tabs">
                <button class="tab-btn active" onclick="showTab('papers', event)">推荐论文</button>
                <button class="tab-btn" onclick="showTab('profile', event)">个人信息</button>
                <button class="tab-btn" onclick="showTab('login', event)">登录</button>
            </div>
        </div>
    </nav>

    <!-- 搜索区域 -->
    <section class="search-section">
        <div class="search-container">
            <div class="search-bar">
                <input type="text" id="search-input" placeholder="搜索论文..." onkeypress="handleSearchKeyPress(event)">
                <button class="search-btn" onclick="searchPapers()">搜索</button>
            </div>
        </div>
    </section>

    <!-- 主要内容区域 -->
    <div class="content">
            <!-- 调试信息（开发环境下显示） -->
        <div id="debug-info" style="position: fixed; bottom: 10px; right: 10px; background: rgba(0,0,0,0.8); color: white; padding: 10px; border-radius: 4px; font-size: 12px; max-width: 300px; display: none;">
            <strong>调试信息:</strong><br>
            <span id="debug-current-tab">当前标签页: papers</span><br>
            <span id="debug-user-status">用户状态: 未登录</span><br>
            <button onclick="document.getElementById('debug-info').style.display='none'" style="margin-top: 5px; font-size: 10px;">隐藏</button>
        </div>

        <!-- 错误消息 -->
        <div id="error-message" class="error-message"></div>

        <!-- 论文列表标签页 -->
        <div id="papers-tab" class="tab-content">
            <div id="papers-loading" class="loading">正在加载论文...</div>
            <div id="papers-grid" class="papers-grid"></div>
            <div id="pagination" class="pagination" style="display: none;"></div>
        </div>

        <!-- 个人信息标签页 -->
        <div id="profile-tab" class="tab-content" style="display: none;">
            <div id="profile-loading" class="loading">正在加载用户信息...</div>
            <div id="profile-content" class="user-info" style="display: none;">
                <h3>个人信息</h3>
                <div class="user-detail">
                    <span class="user-label">用户名：</span>
                    <span id="user-name">-</span>
                </div>
                <div class="user-detail">
                    <label for="interest-input" class="user-label">兴趣：</label>
                    <input type="text" id="interest-input" class="form-input" style="margin-top: 5px; margin-bottom: 10px;">
                    <button class="action-btn" onclick="saveInterests()">保存兴趣</button>
                </div>
                <div class="user-detail">
                    <label for="frequency-select" class="user-label">推荐频率：</label>
                    <select id="frequency-select" class="form-input" style="margin-top: 5px; margin-bottom: 10px;">
                        <option value="1">每小时一次</option>
                        <option value="6">每6小时一次</option>
                        <option value="12">每12小时一次</option>
                        <option value="24">每天一次</option>
                    </select>
                    <button class="action-btn" onclick="saveFrequency()">保存频率</button>
                </div>
            </div>
        </div>

        <!-- 登录标签页 -->
        <div id="login-tab" class="tab-content" style="display: none;">
            <div class="user-info">
                <h3>用户登录</h3>
                <form id="login-form">
                    <div class="form-group">
                        <label class="form-label" for="login-username">用户名</label>
                        <input type="text" id="login-username" class="form-input" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="login-password">密码</label>
                        <input type="password" id="login-password" class="form-input" required>
                    </div>
                    <button type="submit" class="submit-btn">登录</button>
                </form>
                <div style="margin-top: 1rem; text-align: center;">
                    <a href="#" onclick="showRegisterModal()">还没有账号？注册</a>
                </div>
            </div>
        </div>
    </div>

    <!-- 注册模态框 -->
    <div id="register-modal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <span class="close-btn" onclick="closeRegisterModal()">&times;</span>
                <h2 class="modal-title">用户注册</h2>
            </div>
            <form id="register-form">
                <div class="form-group">
                    <label class="form-label" for="register-username">用户名</label>
                    <input type="text" id="register-username" class="form-input" required>
                </div>
                <div class="form-group">
                    <label class="form-label" for="register-password">密码</label>
                    <input type="password" id="register-password" class="form-input" required>
                </div>
                <div class="form-group">
                    <label class="form-label" for="register-interest">兴趣</label>
                    <input type="text" id="register-interest" class="form-input" placeholder="如：机器学习、数据挖掘等">
                </div>
                <button type="submit" class="submit-btn">注册</button>
            </form>
        </div>
    </div>

    <!-- 博客详情模态框 -->
    <div id="blog-detail-modal" class="modal">
        <div class="modal-content" style="max-width: 800px;">
            <div class="modal-header">
                <span class="close-btn" onclick="closeBlogDetailModal()">&times;</span>
                <h2 id="blog-title-full" class="modal-title">博客详情</h2>
            </div>
            <div id="blog-content-full" class="paper-content" style="max-height: 70vh; overflow-y: auto;">
                <!-- 博客内容将在这里被渲染 -->
            </div>
        </div>
    </div>

    <script>
        // 全局变量
        let currentPage = 1;
        let currentTab = 'papers';
        let currentUser = null;

        // 更新调试信息
        function updateDebugInfo() {
            const debugTab = document.getElementById('debug-current-tab');
            const debugUser = document.getElementById('debug-user-status');

            if (debugTab) debugTab.textContent = '当前标签页: ' + currentTab;
            if (debugUser) debugUser.textContent = '用户状态: ' + (currentUser ? '已登录 (' + currentUser.username + ')' : '未登录');
        }

        // 创建雪花效果
        function createSnowflakes() {
            const snowContainer = document.body;
            const snowflakes = ['❄', '❅', '❆', '✼', '✽'];

            for (let i = 0; i < 50; i++) {
                const snowflake = document.createElement('div');
                snowflake.className = 'snowflake';
                snowflake.textContent = snowflakes[Math.floor(Math.random() * snowflakes.length)];
                snowflake.style.left = Math.random() * 100 + '%';
                snowflake.style.animationDelay = Math.random() * 10 + 's';
                snowflake.style.fontSize = (Math.random() * 10 + 10) + 'px';
                snowContainer.appendChild(snowflake);
            }
        }

        // 页面加载完成后的初始化
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Page loaded, initializing...');
            // 创建圣诞雪花效果
            createSnowflakes();
            // 显示调试信息（开发环境下）
            document.getElementById('debug-info').style.display = 'block';
            // 初始化默认标签页状态
            showTab('papers');
        });

        // 标签页切换
        function showTab(tabName, event) {
            console.log('Switching to tab:', tabName);

            // 隐藏所有标签页
            document.querySelectorAll('.tab-content').forEach(tab => {
                tab.style.display = 'none';
            });

            // 移除所有标签按钮的active类
            document.querySelectorAll('.tab-btn').forEach(btn => {
                btn.classList.remove('active');
            });

            // 显示选中的标签页
            const targetTab = document.getElementById(tabName + '-tab');
            if (targetTab) {
                targetTab.style.display = 'block';
                console.log('Tab displayed:', tabName + '-tab');
            } else {
                console.error('Tab not found:', tabName + '-tab');
            }

            // 添加active类到当前按钮
            if (event && event.target) {
                event.target.classList.add('active');
            } else {
                // 如果没有event参数，找到对应的按钮
                const buttons = document.querySelectorAll('.tab-btn');
                buttons.forEach(btn => {
                    if (btn.onclick && btn.onclick.toString().includes(tabName)) {
                        btn.classList.add('active');
                    }
                });
            }

            currentTab = tabName;
            updateDebugInfo();

            // 根据标签页加载相应内容
            if (tabName === 'papers') {
                loadPapers();
            } else if (tabName === 'profile') {
                loadUserProfile();
            }
        }

        // 加载推荐博客列表
        async function loadPapers(page = 1) {
            const loadingElement = document.getElementById('papers-loading');
            const gridElement = document.getElementById('papers-grid');

            // 使用淡入淡出效果减少抖动
            if (!currentUser) {
                loadingElement.style.display = 'none';
                showError('请先登录以查看推荐内容');
                showTab('login');
                return;
            }

            console.log('Loading recommendations for user:', currentUser.user_id);

            // 开始加载时保持现有内容可见，loading元素淡入
            loadingElement.style.display = 'block';
            loadingElement.style.opacity = '0.7';

            try {
                const url = 'api/papers/recommendations?userId=' + currentUser.user_id;
                const response = await fetch(url);

                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }

                const data = await response.json();

                // 成功获取数据后，先隐藏loading，再更新内容
                loadingElement.style.display = 'none';

                if (data.status === 'success') {
                    displayPapers(data.data); // Renamed for consistency, handles recommendations
                } else {
                    showError('加载推荐失败：' + data.message);
                }
            } catch (error) {
                loadingElement.style.display = 'none';
                showError('网络错误，请稍后重试: ' + error.message);
                console.error('Error loading recommendations:', error);
            }
        }

        // 显示推荐博客卡片
        function displayPapers(recommendations) {
            const gridElement = document.getElementById('papers-grid');

            // 先设置透明度为0，准备淡入效果
            gridElement.style.opacity = '0';

            if (!recommendations || recommendations.length === 0) {
                gridElement.innerHTML = '<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: #7f8c8d;"><h3>暂无推荐内容</h3><p>系统还没有为您生成任何推荐。</p></div>';
                // 淡入显示
                setTimeout(() => {
                    gridElement.style.opacity = '1';
                }, 50);
                return;
            }

            // 创建文档片段来批量添加元素，提高性能
            const fragment = document.createDocumentFragment();

            recommendations.forEach(rec => {
                const paperCard = document.createElement('div');
                paperCard.className = 'paper-card';
                paperCard.style.opacity = '0';
                paperCard.style.transform = 'translateY(20px)';
                paperCard.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

                const title = rec.paper_title || '无标题';
                const author = rec.paper_author || '未知来源';
                const blogContent = rec.blog || '';
                const summary = blogContent.substring(0, 150) + (blogContent.length > 150 ? '...' : '');

                // Store full content in data attributes for the modal
                paperCard.setAttribute('data-title', title);
                paperCard.setAttribute('data-blog', blogContent);
                paperCard.onclick = function() { showBlogDetail(this); };

                paperCard.innerHTML = `
                    <div class="paper-header">
                        <div class="paper-title">` + escapeHtml(title) + `</div>
                        <div class="paper-author">作者：` + escapeHtml(author) + `</div>
                    </div>
                    <div class="paper-content">
                        <div class="paper-abstract">` + marked.parse(summary) + `</div>
                    </div>
                `;

                fragment.appendChild(paperCard);
            });

            // 清空并添加新内容
            gridElement.innerHTML = '';
            gridElement.appendChild(fragment);

            // 淡入显示网格
            setTimeout(() => {
                gridElement.style.opacity = '1';

                // 依次显示每个卡片
                const cards = gridElement.querySelectorAll('.paper-card');
                cards.forEach((card, index) => {
                    setTimeout(() => {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0)';
                    }, index * 100); // 每个卡片延迟100ms显示
                });
            }, 50);
        }

        // 更新分页 - 第一页不显示分页
        function updatePagination(currentPage, pageSize, total) {
            // 第一页不显示分页，直接隐藏分页区域
            const paginationElement = document.getElementById('pagination');
            paginationElement.style.display = 'none';
        }

        // 搜索论文
        async function searchPapers() {
            const query = document.getElementById('search-input').value.trim();

            if (!query) {
                showError('请输入搜索关键词');
                return;
            }

            // 显示加载状态
            const papersGrid = document.getElementById('papers-grid');
            const papersLoading = document.getElementById('papers-loading');

            papersLoading.style.display = 'block';
            papersGrid.innerHTML = '';

            try {
                const response = await fetch('api/search?q=' + encodeURIComponent(query));
                const data = await response.json();

                papersLoading.style.display = 'none';

                if (data.status === 'success') {
                    if (data.data.results_count > 0) {
                        displaySearchResults(data.data.results);
                        showError('搜索成功！找到 ' + data.data.results_count + ' 个结果', false);
                    } else {
                        papersGrid.innerHTML = '<div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: #7f8c8d;"><h3>未找到相关论文</h3><p>尝试使用其他关键词搜索</p></div>';
                        showError('未找到匹配的论文', false);
                    }
                } else {
                    showError('搜索失败：' + data.message);
                }
            } catch (error) {
                papersLoading.style.display = 'none';
                showError('搜索时发生网络错误');
                console.error('Error searching papers:', error);
            }
        }

        // 显示搜索结果
        function displaySearchResults(results) {
            const gridElement = document.getElementById('papers-grid');
            gridElement.innerHTML = '';

            results.forEach(result => {
                const paperCard = document.createElement('div');
                paperCard.className = 'paper-card';

                const title = result.title || '无标题';
                const author = result.author || '未知来源';
                const abstractText = result.abstract || '';
                const summary = abstractText.substring(0, 150) + (abstractText.length > 150 ? '...' : '');
                const pdfUrl = result.pdf_url || '#';

                paperCard.innerHTML = `
                    <div class="paper-header">
                        <div class="paper-title">` + escapeHtml(title) + `</div>
                        <div class="paper-author">作者：` + escapeHtml(author) + `</div>
                    </div>
                    <div class="paper-content">
                        <div class="paper-abstract">` + marked.parse(summary) + `</div>
                        <div class="paper-actions">
                            <a href="#" class="action-btn" onclick="viewPaperPdf('` + pdfUrl.replace(/'/g, "\\'") + `')">查看详情</a>
                        </div>
                    </div>
                `;

                gridElement.appendChild(paperCard);
            });
        }

        // 查看论文PDF，在新标签页中打开
        function viewPaperPdf(pdfUrl) {
            if (pdfUrl && pdfUrl !== '#') {
                window.open(pdfUrl, '_blank');
            } else {
                showError('该论文暂无PDF链接');
            }
        }

        // 处理搜索输入框的回车事件
        function handleSearchKeyPress(event) {
            if (event.key === 'Enter') {
                searchPapers();
            }
        }

        // 加载用户信息
        async function loadUserProfile() {
            console.log('Loading user profile...');
            if (!currentUser) {
                console.log('No current user, showing login prompt');
                showError('请先登录');
                showTab('login');
                return;
            }

            const loadingElement = document.getElementById('profile-loading');
            const contentElement = document.getElementById('profile-content');

            if (!loadingElement || !contentElement) {
                console.error('Profile elements not found');
                return;
            }

            loadingElement.style.display = 'block';
            contentElement.style.display = 'none';

            try {
                const response = await fetch('api/users/' + currentUser.user_id);
                const data = await response.json();

                loadingElement.style.display = 'none';

                if (data.status === 'success') {
                    document.getElementById('user-name').textContent = data.data.username;
                    document.getElementById('interest-input').value = data.data.interest || ''; // Populate input field
                    document.getElementById('frequency-select').value = data.data.frequency || 24; // Populate frequency select
                    contentElement.style.display = 'block';
                    console.log('User profile loaded successfully');
                } else {
                    showError('加载用户信息失败：' + data.message);
                }
            } catch (error) {
                loadingElement.style.display = 'none';
                showError('网络错误，请稍后重试');
                console.error('Error loading user profile:', error);
            }
        }

        // 保存用户兴趣
        async function saveInterests() {
            if (!currentUser) {
                showError('请先登录再保存兴趣');
                return;
            }

            const interestInput = document.getElementById('interest-input');
            const newInterest = interestInput.value.trim();
            console.log('Saving new interest:', newInterest);

            try {
                const response = await fetch('api/users/' + currentUser.user_id + '/interest', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        interest: newInterest
                    })
                });

                const data = await response.json();

                if (data.status === 'success') {
                    showError('兴趣保存成功！', false);
                } else {
                    showError('保存失败：' + data.message);
                }
            } catch (error) {
                showError('网络错误，请稍后重试');
                console.error('Error saving interests:', error);
            }
        }

        async function saveFrequency() {
            if (!currentUser) {
                showError('请先登录再保存推荐频率');
                return;
            }

            const frequencySelect = document.getElementById('frequency-select');
            const newFrequency = parseInt(frequencySelect.value);
            console.log('Saving new frequency:', newFrequency);

            try {
                const response = await fetch('api/users/' + currentUser.user_id + '/frequency', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        frequency: newFrequency
                    })
                });

                const data = await response.json();

                if (data.status === 'success') {
                    showError('推荐频率保存成功！', false);
                } else {
                    showError('保存失败：' + data.message);
                }
            } catch (error) {
                showError('网络错误，请稍后重试');
                console.error('Error saving frequency:', error);
            }
        }

        // 查看论文详情
        function viewPaperDetail(paperId) {
            // 这里可以实现跳转到论文详情页或打开模态框
            alert('查看论文详情：' + paperId);
        }

        // 显示博客详情模态框
        function showBlogDetail(cardElement) {
            const title = cardElement.getAttribute('data-title');
            const blogContent = cardElement.getAttribute('data-blog');

            document.getElementById('blog-title-full').textContent = title;
            document.getElementById('blog-content-full').innerHTML = marked.parse(blogContent || '');
            document.getElementById('blog-detail-modal').style.display = 'block';
        }

        // 关闭博客详情模态框
        function closeBlogDetailModal() {
            document.getElementById('blog-detail-modal').style.display = 'none';
        }

        // 显示注册模态框
        function showRegisterModal() {
            document.getElementById('register-modal').style.display = 'block';
        }

        // 关闭注册模态框
        function closeRegisterModal() {
            document.getElementById('register-modal').style.display = 'none';
            document.getElementById('register-form').reset();
        }

        // 处理登录表单提交
        document.getElementById('login-form').addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;

            try {
                // 注意：这里应该调用实际的登录API，但根据现有API结构，可能需要先获取用户列表然后验证
                const response = await fetch('api/users/list');
                const data = await response.json();

                if (data.status === 'success') {
                    const user = data.data.users.find(u => u.username === username);
                    if (user) {
                        currentUser = user;
                        updateDebugInfo();
                        showError('登录成功！', false);
                        showTab('profile');
                    } else {
                        showError('用户名或密码错误');
                    }
                } else {
                    showError('登录失败：' + data.message);
                }
            } catch (error) {
                showError('网络错误，请稍后重试');
                console.error('Error logging in:', error);
            }
        });

        // 处理注册表单提交
        document.getElementById('register-form').addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('register-username').value;
            const password = document.getElementById('register-password').value;
            const interest = document.getElementById('register-interest').value;

            try {
                const response = await fetch('api/users/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        username: username,
                        password: password,
                        interest: interest
                    })
                });

                const data = await response.json();

                if (data.status === 'success') {
                    showError('注册成功！请登录', false);
                    closeRegisterModal();
                    showTab('login');
                } else {
                    showError('注册失败：' + data.message);
                }
            } catch (error) {
                showError('网络错误，请稍后重试');
                console.error('Error registering:', error);
            }
        });

        // 显示错误消息
        function showError(message, isError = true) {
            const errorElement = document.getElementById('error-message');
            errorElement.textContent = message;
            errorElement.style.backgroundColor = isError ? '#e74c3c' : '#27ae60';
            errorElement.style.display = 'block';

            // 3秒后自动隐藏
            setTimeout(() => {
                errorElement.style.display = 'none';
            }, 3000);
        }

        // HTML转义函数，防止XSS攻击
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        // 点击模态框外部关闭
        window.onclick = function(event) {
            const registerModal = document.getElementById('register-modal');
            const blogModal = document.getElementById('blog-detail-modal');
            if (event.target === registerModal) {
                closeRegisterModal();
            }
            if (event.target === blogModal) {
                closeBlogDetailModal();
            }
        }
    </script>
</body>
</html>
