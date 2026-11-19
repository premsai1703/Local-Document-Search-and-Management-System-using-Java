@echo off
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b %errorlevel%
java --enable-native-access=ALL-UNNAMED,javafx.graphics --module-path "lib\javafx" --add-modules javafx.controls -cp "bin;lib\*" com.engine.Main %*
