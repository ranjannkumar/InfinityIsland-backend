#!/bin/bash
set -euo pipefail
cd /var/app/staging
chmod +x ./gradlew || true
./gradlew clean bootJar -x test
