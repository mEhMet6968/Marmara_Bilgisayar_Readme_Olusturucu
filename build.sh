#!/bin/bash
# Projeyi derler ve çalıştırılabilir "fat jar" üretir (Linux/macOS).
# Gereksinim: JDK 21+.
cd "$(dirname "$0")"
./mvnw -q clean package -DskipTests
echo "Derleme tamamlandı: target/marmara-readme-araci-0.1.0.jar"
echo "Çalıştırmak için: java -jar target/marmara-readme-araci-0.1.0.jar"
