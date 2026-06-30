@echo off
echo =================================================
echo   Compiling Asset Management System
echo =================================================

if not exist bin (
    mkdir bin
    echo Created 'bin/' directory.
)

echo Compiling source files...
javac -d bin -cp "lib/*;src" src/com/assetmanager/database/*.java src/com/assetmanager/exception/*.java src/com/assetmanager/model/*.java src/com/assetmanager/dao/*.java src/com/assetmanager/service/*.java src/com/assetmanager/util/*.java src/com/assetmanager/main/*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation complete. Compiled classes saved in bin/
) else (
    echo [ERROR] Compilation failed.
)
pause
