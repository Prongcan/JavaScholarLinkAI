$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "任务1 API测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试1: 从arXiv抓取论文
Write-Host "测试1: 从arXiv抓取论文" -ForegroundColor Yellow
Write-Host "端点: POST $baseUrl/api/papers/fetch" -ForegroundColor Gray
Write-Host ""

$fetchBody = '{"arxiv_id": "2301.00001"}'
$paperId = $null
$fetchSuccess = $false

try {
    Write-Host "正在发送请求..." -ForegroundColor Gray
    $fetchResponse = Invoke-RestMethod -Uri "$baseUrl/api/papers/fetch" -Method POST -ContentType "application/json" -Body $fetchBody -ErrorAction Stop

    Write-Host "论文抓取成功！" -ForegroundColor Green
    Write-Host ""
    $fetchResponse | ConvertTo-Json -Depth 10
    Write-Host ""

    $paperId = $fetchResponse.data.paper_id
    $fetchSuccess = $true
    Write-Host "抓取的论文ID: $paperId" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "论文抓取失败" -ForegroundColor Red
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
        Write-Host "详细: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# 如果论文抓取成功，继续测试博客生成
if ($fetchSuccess -and $paperId) {
    Write-Host "按 Enter 继续测试博客生成..." -ForegroundColor Yellow
    Read-Host | Out-Null

    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "测试2: 生成博客" -ForegroundColor Yellow
    Write-Host "端点: POST $baseUrl/api/papers/$paperId/generate-blog" -ForegroundColor Gray
    Write-Host ""

    $userId = 2  # 使用数据库中存在的user_id
    Write-Host "使用用户ID: $userId" -ForegroundColor Gray
    Write-Host "注意: 需要配置DeepSeek API密钥" -ForegroundColor Yellow
    Write-Host ""

    $blogBody = @"
{"user_id": $userId}
"@

    Write-Host "正在生成博客..." -ForegroundColor Gray

    try {
        $blogResponse = Invoke-RestMethod -Uri "$baseUrl/api/papers/$paperId/generate-blog" -Method POST -ContentType "application/json" -Body $blogBody -ErrorAction Stop

        Write-Host "博客生成成功！" -ForegroundColor Green
        Write-Host ""
        $blogResponse | ConvertTo-Json -Depth 10
        Write-Host ""

        $blogContent = $blogResponse.data.blog
        if ($blogContent) {
            Write-Host "博客内容预览 (前500字符):" -ForegroundColor Cyan
            if ($blogContent.Length -gt 500) {
                Write-Host $blogContent.Substring(0, 500) -ForegroundColor Gray
                Write-Host "..." -ForegroundColor Gray
            }
            else {
                Write-Host $blogContent -ForegroundColor Gray
            }
            Write-Host ""
        }
    }
    catch {
        Write-Host "博客生成失败" -ForegroundColor Red
        Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
        
        # 尝试获取详细的错误响应
        if ($_.Exception.Response) {
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorBody = $reader.ReadToEnd()
                $reader.Close()
                $errorStream.Close()
                
                if ($errorBody) {
                    Write-Host "`n服务器返回的错误信息:" -ForegroundColor Yellow
                    try {
                        $errorJson = $errorBody | ConvertFrom-Json
                        if ($errorJson.message) {
                            Write-Host "  消息: $($errorJson.message)" -ForegroundColor Red
                        }
                        if ($errorJson.status) {
                            Write-Host "  状态: $($errorJson.status)" -ForegroundColor Red
                        }
                        if ($errorJson -and -not $errorJson.message) {
                            Write-Host "  $errorBody" -ForegroundColor Red
                        }
                    } catch {
                        Write-Host "  $errorBody" -ForegroundColor Red
                    }
                }
            } catch {
                # 忽略解析错误
            }
        }
        
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            Write-Host "详细: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
