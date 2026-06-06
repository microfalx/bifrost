@echo off
setlocal

set "SCRIPT_DIR=%~dp0"

if "%BIFROST_HOME%"=="" (
    for %%I in ("%SCRIPT_DIR%..") do set "BIFROST_HOME=%%~fI"
)

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java executable not found in PATH.
    echo Please install Java and ensure java.exe is available on the PATH.
    exit /b 1
)

cd /d "%BIFROST_HOME%"
if errorlevel 1 (
    echo ERROR: Unable to change directory to %BIFROST_HOME%
    exit /b 1
)

java -jar "%BIFROST_HOME%\lib\bifrost.jar" %*

endlocal