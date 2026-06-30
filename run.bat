@echo off
echo =================================================
echo   Running Asset Management System (AMS)
echo =================================================

java -cp "bin;lib/*" com.assetmanager.main.AssetManagerApp

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Application terminated with exit code %ERRORLEVEL%
)
pause
