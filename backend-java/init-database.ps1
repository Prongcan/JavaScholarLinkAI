# Database Initialization Script for ScholarLink AI
# Usage: .\init-database.ps1

param(
    [string]$DbHost = "localhost",
    [int]$Port = 3306,
    [string]$User = "root",
    [string]$Password = "Sehun19940412",
    [string]$Database = "scholarlink_ai"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ScholarLink AI Database Initialization" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Host: $DbHost" -ForegroundColor Yellow
Write-Host "Port: $Port" -ForegroundColor Yellow
Write-Host "User: $User" -ForegroundColor Yellow
Write-Host "Database: $Database" -ForegroundColor Yellow
Write-Host ""

# Check if MySQL is available
$mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue
if (-not $mysqlPath) {
    Write-Host "[ERROR] mysql command not found" -ForegroundColor Red
    Write-Host "Please ensure MySQL is installed and added to PATH" -ForegroundColor Yellow
    Write-Host "Or use full path, e.g.: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -ForegroundColor Yellow
    exit 1
}

$schemaFile = Join-Path $PSScriptRoot "src\main\resources\db\schema.sql"
if (-not (Test-Path $schemaFile)) {
    Write-Host "[ERROR] SQL script not found: $schemaFile" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Executing SQL script..." -ForegroundColor Green

try {
    # Set MySQL password environment variable
    $env:MYSQL_PWD = $Password
    
    # Execute MySQL command with source file
    $mysqlArgs = "-h$DbHost", "-P$Port", "-u$User", "-e", "source $schemaFile"
    & $mysqlPath.Source $mysqlArgs
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] Database initialization completed!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Verify database:" -ForegroundColor Cyan
        Write-Host "  mysql -u $User -p -e 'USE $Database; SHOW TABLES;'" -ForegroundColor Gray
    } else {
        Write-Host "[ERROR] Database initialization failed (exit code: $LASTEXITCODE)" -ForegroundColor Red
        Write-Host ""
        Write-Host "Trying alternative method..." -ForegroundColor Yellow
        # Alternative: read SQL content and execute
        $sqlContent = Get-Content $schemaFile -Raw
        $tempFile = [System.IO.Path]::GetTempFileName()
        $sqlContent | Out-File -FilePath $tempFile -Encoding UTF8
        $mysqlArgs2 = "-h$DbHost", "-P$Port", "-u$User", "-e", "source $tempFile"
        & $mysqlPath.Source $mysqlArgs2
        Remove-Item $tempFile -ErrorAction SilentlyContinue
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[SUCCESS] Database initialization completed (alternative method)!" -ForegroundColor Green
        } else {
            Write-Host "[ERROR] Both methods failed" -ForegroundColor Red
            Write-Host ""
            Write-Host "Manual execution method:" -ForegroundColor Yellow
            Write-Host "  1. Open MySQL command line: mysql -u $User -p" -ForegroundColor Gray
            Write-Host "  2. Execute: source $schemaFile" -ForegroundColor Gray
            Write-Host "  Or copy and paste the content of schema.sql" -ForegroundColor Gray
            exit 1
        }
    }
} catch {
    Write-Host "[ERROR] Execution failed: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Manual execution method:" -ForegroundColor Yellow
    Write-Host "  1. Open MySQL command line: mysql -u $User -p" -ForegroundColor Gray
    Write-Host "  2. Execute: source $schemaFile" -ForegroundColor Gray
    Write-Host "  Or copy and paste the content of schema.sql" -ForegroundColor Gray
    exit 1
} finally {
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}
