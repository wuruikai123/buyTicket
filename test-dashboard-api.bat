@echo off
echo Testing Dashboard API...
echo.

echo 1. Testing backend health:
curl -X GET http://localhost:8080/api/v1/admin/statistics/dashboard
echo.
echo.

echo 2. Testing with formatted output:
curl -X GET http://localhost:8080/api/v1/admin/statistics/dashboard -H "Content-Type: application/json"
echo.
echo.

pause
