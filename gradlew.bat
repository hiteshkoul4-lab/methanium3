@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off

set APP_HOME=%~dp0

if not defined JAVA_HOME (
    echo ERROR: JAVA_HOME is not set. Please install Java 25 and set JAVA_HOME.
    exit /b 1
)

set JAVACMD=%JAVA_HOME%\bin\java.exe
if not exist "%JAVACMD%" (
    echo ERROR: Java not found at %JAVACMD%
    exit /b 1
)

"%JAVACMD%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
