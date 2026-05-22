@echo off
title EduIDE Containerized Microservices Launchpad
echo ==========================================================
echo       KHOI DONG HE THONG MICROSERVICES DOCKER EDUIE        
echo ==========================================================
echo.

:: Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker check failed!
    echo Vui long dam bao rang Docker Desktop da duoc cai dat va dang chay.
    echo Tai ve tai: https://www.docker.com/products/docker-desktop/
    echo.
    pause
    exit /b 1
)

echo [+] Dang build va khoi dong cac container (backend, frontend)...
docker compose up --build -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Docker Compose failed to launch!
    echo Vui long kiem tra lai log loi ben tren.
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================================
echo [SUCCESS] Ca hai services dang chay hoan hao trong Docker!
echo - Frontend ^(Nginx Proxy^): http://localhost ^(Hoac http://localhost:5173^)
echo - Backend ^(Spring Boot^): http://localhost:8080
echo ==========================================================
echo.

timeout /t 5 >nul
start http://localhost
exit
