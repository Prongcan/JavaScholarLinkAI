# API 测试脚本
# 测试所有 API 端点

$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始测试 API 端点" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ========== 1. 测试 Papers API ==========
Write-Host "1. 测试 Papers API" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 1.1 获取论文列表
Write-Host "1.1 GET /api/papers/list" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/list" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 1.2 添加论文
Write-Host "1.2 POST /api/papers/fetch" -ForegroundColor Green
$paperData = @{
    title = "Test Paper - Machine Learning"
    author = "Test Author"
    abstract = "This is a test paper about machine learning"
    pdf_url = "http://example.com/test.pdf"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/fetch" -Method POST -Body $paperData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
    
    # 解析返回的 paper_id
    $result = $response.Content | ConvertFrom-Json
    $paperId = $result.data.paper_id
    Write-Host "创建的论文 ID: $paperId" -ForegroundColor Cyan
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    $paperId = 1  # 使用默认值继续测试
}
Write-Host ""

# 1.3 获取论文详情
Write-Host "1.3 GET /api/papers/$paperId" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/$paperId" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# ========== 2. 测试 Users API ==========
Write-Host "2. 测试 Users API" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 2.1 用户注册
Write-Host "2.1 POST /api/users/register" -ForegroundColor Green
$userData = @{
    username = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')"
    password = "123456"
    interest = "Artificial Intelligence"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $userData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
    
    # 解析返回的 user_id
    $result = $response.Content | ConvertFrom-Json
    $userId = $result.data.user_id
    Write-Host "创建的用户 ID: $userId" -ForegroundColor Cyan
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    $userId = 1  # 使用默认值继续测试
}
Write-Host ""

# 2.2 获取用户列表
Write-Host "2.2 GET /api/users/list" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/list" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2.3 获取用户信息
Write-Host "2.3 GET /api/users/$userId" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2.4 获取用户兴趣
Write-Host "2.4 GET /api/users/$userId/interest" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId/interest" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2.5 更新用户兴趣
Write-Host "2.5 PUT /api/users/$userId/interest" -ForegroundColor Green
$interestData = @{
    interest = "Deep Learning, Neural Networks"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId/interest" -Method PUT -Body $interestData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2.6 删除用户（可选，注释掉以避免删除测试数据）
Write-Host "2.6 DELETE /api/users/$userId (跳过，保留测试数据)" -ForegroundColor Gray
# try {
#     $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId" -Method DELETE -UseBasicParsing
#     Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
#     Write-Host "响应: $($response.Content)" -ForegroundColor Gray
# } catch {
#     Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
# }
Write-Host ""

# ========== 3. 测试错误情况 ==========
Write-Host "3. 测试错误情况" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 3.1 获取不存在的论文
Write-Host "3.1 GET /api/papers/99999 (不存在的论文)" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/99999" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "状态码: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 3.2 注册重复用户名
Write-Host "3.2 POST /api/users/register (重复用户名)" -ForegroundColor Green
$duplicateUserData = @{
    username = "testuser_duplicate"
    password = "123456"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $duplicateUserData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "第一次注册 - 状态码: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "第一次注册失败: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $duplicateUserData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "第二次注册 - 状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "第二次注册 - 状态码: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "API 测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

