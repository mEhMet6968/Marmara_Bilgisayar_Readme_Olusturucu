#!/bin/bash
# Marmara Readme Düzenleyici GUI'sini başlatır (Linux/macOS).
# Gereksinim: JDK 21+ kurulu olmalı (Maven kurulu olması GEREKMEZ, mvnw otomatik indirir).
cd "$(dirname "$0")"
./mvnw -q javafx:run
