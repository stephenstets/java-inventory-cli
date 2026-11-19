@echo off
REM Build and run the inventory CLI (Windows).
javac -d out src\*.java
if errorlevel 1 exit /b 1
java -cp out Main
