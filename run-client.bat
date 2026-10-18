@echo off
rem =========================================================================
rem NutriGuide - Java Swing Desktop Client Launcher
rem Connects to localhost backend at http://127.0.0.1:8081.
rem =========================================================================

echo ========================================================================
echo   NutriGuide Desktop Client (Java Swing)
echo ========================================================================
echo Connecting to localhost backend at http://127.0.0.1:8081...
echo Ensure run-backend.bat is running in a separate window.
echo.

if exist mvnw.cmd (
    call mvnw.cmd exec:java -Dexec.mainClass="com.nutriguide.gui.NutriGuideGUI"
) else (
    call mvn exec:java -Dexec.mainClass="com.nutriguide.gui.NutriGuideGUI"
)

if errorlevel 1 (
    echo.
    echo ========================================================================
    echo [ERROR] Desktop client failed to start or terminated with an error.
    echo Please review the error message above before closing this window.
    echo ========================================================================
    pause
)
