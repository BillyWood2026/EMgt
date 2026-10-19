@echo off
REM stop-all.bat - Stop all EMgtv2 services
cd /d D:\EMgtv2
powershell -NoProfile -ExecutionPolicy Bypass -File "D:\EMgtv2\stop-all.ps1"
pause
