# 完整 API 测试脚本
# 测试所有 API 端点

$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始测试所有 API 端点" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ========== 1. Papers API 测试 ==========
Write-Host "1. Papers API 测试" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 1.1 获取论文列表（已测试，跳过）
Write-Host "1.1 GET /api/papers/list (已测试)" -ForegroundColor Gray
Write-Host ""

# 1.2 添加论文
Write-Host "1.2 POST /api/papers/fetch - 添加论文" -ForegroundColor Green
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
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
    $paperId = 1  # 使用默认值继续测试
}
Write-Host ""

# 1.3 获取论文详情
Write-Host "1.3 GET /api/papers/$paperId - 获取论文详情" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/$paperId" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}
Write-Host ""

# ========== 2. Users API 测试 ==========
Write-Host "2. Users API 测试" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 2.1 获取用户列表（已测试，跳过）
Write-Host "2.1 GET /api/users/list (已测试)" -ForegroundColor Gray
Write-Host ""

# 2.2 用户注册
Write-Host "2.2 POST /api/users/register - 用户注册" -ForegroundColor Green
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'
$userData = @{
    username = "testuser_$timestamp"
    password = "123456"
    interest = "Artificial Intelligence, Machine Learning"
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
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
    $userId = 1  # 使用默认值继续测试
}
Write-Host ""

# 2.3 获取用户信息
Write-Host "2.3 GET /api/users/$userId - 获取用户信息" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}
Write-Host ""

# 2.4 获取用户兴趣
Write-Host "2.4 GET /api/users/$userId/interest - 获取用户兴趣" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId/interest" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}
Write-Host ""

# 2.5 更新用户兴趣
Write-Host "2.5 PUT /api/users/$userId/interest - 更新用户兴趣" -ForegroundColor Green
$interestData = @{
    interest = "Deep Learning, Neural Networks, Computer Vision"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId/interest" -Method PUT -Body $interestData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应内容: $responseBody" -ForegroundColor Red
    }
}
Write-Host ""

# 2.6 验证更新后的兴趣
Write-Host "2.6 GET /api/users/$userId/interest - 验证更新后的兴趣" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId/interest" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2.7 删除用户（可选，注释掉以避免删除测试数据）
Write-Host "2.7 DELETE /api/users/$userId - 删除用户 (跳过，保留测试数据)" -ForegroundColor Gray
# try {
#     $response = Invoke-WebRequest -Uri "$baseUrl/api/users/$userId" -Method DELETE -UseBasicParsing
#     Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
#     Write-Host "响应: $($response.Content)" -ForegroundColor Gray
# } catch {
#     Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
# }
Write-Host ""

# ========== 3. 错误情况测试 ==========
Write-Host "3. 错误情况测试" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# 3.1 获取不存在的论文
Write-Host "3.1 GET /api/papers/99999 - 获取不存在的论文" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/99999" -Method GET -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "状态码: $statusCode" -ForegroundColor Yellow
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $responseBody = $reader.ReadToEnd()
    Write-Host "响应内容: $responseBody" -ForegroundColor Gray
}
Write-Host ""

# 3.2 注册重复用户名
Write-Host "3.2 POST /api/users/register - 注册重复用户名" -ForegroundColor Green
$duplicateUserData = @{
    username = "duplicate_user"
    password = "123456"
} | ConvertTo-Json

# 先注册一次
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $duplicateUserData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "第一次注册 - 状态码: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "第一次注册失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 再次注册相同用户名
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/users/register" -Method POST -Body $duplicateUserData -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "第二次注册 - 状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应: $($response.Content)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "第二次注册 - 状态码: $statusCode" -ForegroundColor Yellow
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $responseBody = $reader.ReadToEnd()
    Write-Host "响应内容: $responseBody" -ForegroundColor Gray
}
Write-Host ""

# 3.3 无效的 JSON 格式
Write-Host "3.3 POST /api/papers/fetch - 无效的 JSON 格式" -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/papers/fetch" -Method POST -Body "invalid json" -ContentType "application/json; charset=UTF-8" -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "状态码: $statusCode" -ForegroundColor Yellow
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $responseBody = $reader.ReadToEnd()
    Write-Host "响应内容: $responseBody" -ForegroundColor Gray
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "所有 API 测试完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

