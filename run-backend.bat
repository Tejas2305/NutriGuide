@echo off
rem =========================================================================
rem NutriGuide - Localhost Backend Server Launcher
rem Runs strictly on 127.0.0.1:8081. Never prints credentials or secrets.
rem =========================================================================

echo ========================================================================
echo   NutriGuide Localhost Backend Service
echo ========================================================================
echo Binding strictly to: http://127.0.0.1:8081
echo Press Ctrl+C in this console window to gracefully stop the backend.
echo.

if exist mvnw.cmd (
    call mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.server.NutriGuideServer"
) else (
    call mvn exec:java -Dexec.mainClass="com.nutriguide.server.NutriGuideServer"
)

if errorlevel 1 (
    echo.
    echo ========================================================================
    echo [ERROR] Backend server failed to start or terminated with an error.
    echo Please review the error message above before closing this window.
    echo ========================================================================
    pause
)
