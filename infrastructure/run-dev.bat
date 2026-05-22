@echo off
title EduIDE Development Server Launchpad
echo ==========================================================
echo           KHOI DONG HE THONG PHAT TRIEN EDUIE             
echo ==========================================================
echo.

echo [+] Dang chuan bi khoi dong Spring Boot Backend (Port 8080)...
start "EduIDE Spring Boot Backend" cmd /k "cd ../backend && mvn spring-boot:run"

echo [+] Dang chuan bi khoi dong Vite React Frontend (Port 5173)...
start "EduIDE React Frontend" cmd /k "cd ../frontend && npm run dev"

echo.
echo ==========================================================
echo [SUCCESS] Ca hai may chu dang duoc khoi dong trong cua so moi!
echo he thong se tu dong mo ung dung tai dia chi: http://localhost:5173
echo ==========================================================
echo.

timeout /t 5 >nul
start http://localhost:5173
exit
