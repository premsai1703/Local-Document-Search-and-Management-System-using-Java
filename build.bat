@echo off
cd /d "%~dp0"
if not exist lib\javafx\javafx-controls-17.0.18-win.jar (
    echo JavaFX libraries are missing from lib\javafx.
    exit /b 1
)
if not exist bin mkdir bin
javac --release 17 -encoding UTF-8 --module-path "lib\javafx" --add-modules javafx.controls -cp "lib\*" -d bin src\main\java\com\engine\*.java
if errorlevel 1 exit /b 1
echo Build successful.
