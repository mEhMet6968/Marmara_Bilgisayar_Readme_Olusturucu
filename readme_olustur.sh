#!/bin/bash
# Başsız (GUI'siz) README üretimi: json_dosyalari klasörünü argüman olarak verin.
# Örnek: ./readme_olustur.sh ../Marmara_Bilgisayar_Muhendisligi_Arsiv/json_dosyalari
cd "$(dirname "$0")"
./mvnw -q exec:java -Dexec.mainClass=edu.marmara.readme.cli.ReadmeOlusturCli -Dexec.args="$1"
