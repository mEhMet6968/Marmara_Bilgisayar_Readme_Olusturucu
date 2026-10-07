@echo off
REM Marmara Readme Duzenleyici GUI'sini baslatir (Windows).
REM Gereksinim: JDK 21+ kurulu olmali (Maven kurulu olmasi GEREKMEZ, mvnw.cmd otomatik indirir).
cd /d "%~dp0"
call mvnw.cmd -q javafx:run
pause
