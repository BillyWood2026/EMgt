@echo off
REM =====================================================================================
REM start-all.bat - One-click launcher for EMgtv2 Platform
REM Double-click this file or run from CMD to start all services.
REM =====================================================================================
REM Options:
REM   start-all.bat              = Full build + start all
REM   start-all.bat -SkipBuild   = Start with existing JARs (no Maven build)
REM   start-all.bat -BuildOnly   = Build only, don't start services
REM   start-all.bat -SkipFrontend= Start backend only
REM =====================================================================================

cd /d D:\EMgtv2

echo.
echo ========================================
echo   EMgtv2 Platform - One-Click Startup
echo ========================================
echo.

REM Start the PowerShell script with execution policy bypass
powershell -NoProfile -ExecutionPolicy Bypass -File "D:\EMgtv2\start-all.ps1" %*

pause
