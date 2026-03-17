#!/usr/bin/env powershell

# 数据库同步脚本
# 从远程服务器同步数据库到本地

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "数据库同步工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置信息
$REMOTE_HOST = "47.121.192.245"
$REMOTE_PORT = "3306"
$REMOTE_USER = "root"
$REMOTE_DB = "buy_ticket"
$LOCAL_USER = "root"
$LOCAL_DB = "buy_ticket"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$BACKUP_FILE = "backup_$TIMESTAMP.sql"

Write-Host "步骤1: 从远程服务器导出数据库..." -ForegroundColor Yellow
Write-Host "远程服务器: $REMOTE_HOST" -ForegroundColor Gray
Write-Host "数据库名: $REMOTE_DB" -ForegroundColor Gray
Write-Host ""

# 提示输入远程密码
$REMOTE_PASSWORD = Read-Host "请输入远程 MySQL 密码"

# 导出远程数据库
try {
    Write-Host "正在导出数据库..." -ForegroundColor Gray
    $env:MYSQL_PWD = $REMOTE_PASSWORD
    mysqldump -h $REMOTE_HOST -P $REMOTE_PORT -u $REMOTE_USER $REMOTE_DB | Out-File -Encoding UTF8 $BACKUP_FILE
    Remove-Item env:MYSQL_PWD
    
    if ($LASTEXITCODE -ne 0) {
        throw "导出失败"
    }
    
    Write-Host "导出成功！文件: $BACKUP_FILE" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "错误: 导出失败！" -ForegroundColor Red
    Write-Host "请检查:" -ForegroundColor Red
    Write-Host "1. 远程服务器是否可访问" -ForegroundColor Red
    Write-Host "2. MySQL 用户名和密码是否正确" -ForegroundColor Red
    Write-Host "3. 数据库名是否正确" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    Read-Host "按 Enter 退出"
    exit 1
}

Write-Host "步骤2: 导入到本地数据库..." -ForegroundColor Yellow
Write-Host "本地数据库: $LOCAL_DB" -ForegroundColor Gray
Write-Host ""

# 提示输入本地密码
$LOCAL_PASSWORD = Read-Host "请输入本地 MySQL 密码"

# 导入到本地
try {
    Write-Host "正在导入数据库..." -ForegroundColor Gray
    $env:MYSQL_PWD = $LOCAL_PASSWORD
    Get-Content $BACKUP_FILE | mysql -u $LOCAL_USER $LOCAL_DB
    Remove-Item env:MYSQL_PWD
    
    if ($LASTEXITCODE -ne 0) {
        throw "导入失败"
    }
    
    Write-Host "导入成功！" -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "错误: 导入失败！" -ForegroundColor Red
    Write-Host "请检查:" -ForegroundColor Red
    Write-Host "1. 本地 MySQL 是否运行" -ForegroundColor Red
    Write-Host "2. 本地数据库 $LOCAL_DB 是否存在" -ForegroundColor Red
    Write-Host "3. 用户名和密码是否正确" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    Read-Host "按 Enter 退出"
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "同步完成！" -ForegroundColor Green
Write-Host "备份文件: $BACKUP_FILE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Read-Host "按 Enter 退出"
