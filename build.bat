@echo off
REM Projeyi derler ve calistirilabilir "fat jar" uretir (Windows).
REM Gereksinim: JDK 21+.
cd /d "%~dp0"
call mvnw.cmd -q clean package -DskipTests
echo Derleme tamamlandi: target\marmara-readme-araci-0.1.0.jar
echo Calistirmak icin: java -jar target\marmara-readme-araci-0.1.0.jar
pause
