@echo off
echo =================================================
echo   Running Asset Management System (Web Mode)
echo =================================================

java -cp "bin;lib/*" com.assetmanager.main.WebAppLauncher

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Application terminated with exit code %ERRORLEVEL%
)
pause
