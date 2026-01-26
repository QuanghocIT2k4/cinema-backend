@echo off
echo ============================================
echo STARTING CINEMA BACKEND (LOCAL)
echo ============================================
echo.

echo [1/3] Starting MySQL Docker...
docker-compose up -d
timeout /t 5 /nobreak >nul

echo.
echo [2/3] Waiting for MySQL to be ready...
timeout /t 10 /nobreak >nul

echo.
echo [3/3] Starting Spring Boot Application...
echo.
call mvnw.cmd spring-boot:run

pause

