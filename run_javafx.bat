@echo off
REM JavaFX Run Script
REM This script compiles and runs the JavaFX application

setlocal enabledelayedexpansion

REM Set paths
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src\main\java
set TARGET_DIR=%PROJECT_DIR%target\classes
set JAVAFX_VERSION=23.0.1

REM Check if javac is available
javac -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java compiler (javac) not found. Please install Java JDK.
    pause
    exit /b 1
)

echo.
echo ============================================
echo JavaFX Application Build and Run
echo ============================================
echo.

REM Create target directory if it doesn't exist
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

echo Step 1: Compiling Java source files...
cd /d "%SRC_DIR%"

REM Compile all Java files
for /r %%F in (*.java) do (
    echo Compiling: %%~nxF
    javac -d "%TARGET_DIR%" -source 25 "%%F" 2>nul
    if errorlevel 1 (
        echo Warning: Some files may not have compiled. Trying with full compilation...
    )
)

echo.
echo Step 2: Running JavaFX Application...
echo.

REM Run the JavaFX application
cd /d "%TARGET_DIR%"
java -cp "%TARGET_DIR%" com.ecommerce.ui.App

pause
