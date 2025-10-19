#!/bin/bash
set -euo pipefail
cd /var/app/staging
chmod +x ./gradlew || true
./gradlew clean bootJar -x test
JAR="$(ls -1 build/libs/*.jar | head -n 1)"
cp "$JAR" app.jar
