@echo off
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b 1
javac --release 17 -encoding UTF-8 -cp "bin;lib\*" -d bin src\test\java\com\engine\SearchEngineTest.java
if errorlevel 1 exit /b 1
java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" com.engine.SearchEngineTest
