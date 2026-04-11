#!/usr/bin/env bash

set -euo pipefail

if [[ -f /app/cron.env ]]; then
  set -a
  source /app/cron.env
  set +a
fi

exec /usr/bin/java -jar /app/app.jar "$@"
