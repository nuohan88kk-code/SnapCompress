@echo off
setlocal ENABLEDELAYEDEXPANSION

set DIR=%~dp0
set CACHE_DIR=%DIR%\.gradle-dist
set GRADLE_VERSION=8.1.1
set DIST_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
set UNZIP_DIR=%CACHE_DIR%\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%UNZIP_DIR%\bin\gradle.bat

if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"

if not exist "%GRADLE_BIN%" (
  set ZIP=%CACHE_DIR%\gradle-%GRADLE_VERSION%.zip
  echo Downloading Gradle %GRADLE_VERSION% ...
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP%'"
  if exist "%UNZIP_DIR%" rmdir /s /q "%UNZIP_DIR%"
  powershell -NoProfile -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::ExtractToDirectory('%ZIP%','%CACHE_DIR%')"
)

call "%GRADLE_BIN%" -p "%DIR%" %*

endlocal
