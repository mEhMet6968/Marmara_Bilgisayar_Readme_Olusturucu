@echo off
REM Bassiz (GUI'siz) README uretimi: json_dosyalari klasorunu argumen olarak verin.
REM Ornek: readme_olustur.bat ..\Marmara_Bilgisayar_Muhendisligi_Arsiv\json_dosyalari
cd /d "%~dp0"
call mvnw.cmd -q exec:java -Dexec.mainClass=edu.marmara.readme.cli.ReadmeOlusturCli -Dexec.args="%1"
pause
