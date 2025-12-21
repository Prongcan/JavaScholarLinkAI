# Maven Wrapper 下载脚本
# 如果 mvnw.cmd 无法自动下载wrapper jar，请运行此脚本

$wrapperDir = ".mvn\wrapper"
$wrapperJar = "$wrapperDir\maven-wrapper.jar"
$wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

Write-Host "正在下载 Maven Wrapper..." -ForegroundColor Green

# 创建目录
if (-not (Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    Write-Host "已创建目录: $wrapperDir" -ForegroundColor Yellow
}

# 下载文件
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($wrapperUrl, $wrapperJar)
    Write-Host "✓ 下载成功: $wrapperJar" -ForegroundColor Green
    Write-Host "现在可以使用 .\mvnw.cmd 命令了！" -ForegroundColor Green
} catch {
    Write-Host "✗ 下载失败: $_" -ForegroundColor Red
    Write-Host "请手动下载以下文件：" -ForegroundColor Yellow
    Write-Host "URL: $wrapperUrl" -ForegroundColor Cyan
    Write-Host "保存到: $wrapperJar" -ForegroundColor Cyan
    exit 1
}

