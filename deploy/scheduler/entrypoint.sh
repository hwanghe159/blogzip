#!/usr/bin/env bash

set -euo pipefail

if [[ ! -f /app/crontab ]]; then
  echo "Scheduler crontab file not found: /app/crontab" >&2
  exit 1
fi

if [[ -n "${TZ:-}" && -f "/usr/share/zoneinfo/${TZ}" ]]; then
  ln -snf "/usr/share/zoneinfo/${TZ}" /etc/localtime
  echo "${TZ}" > /etc/timezone
fi

# Debian cron jobs in this image use /usr/bin/java in crontab.
# Temurin base image may install java outside /usr/bin, so ensure a stable path.
if [[ ! -x /usr/bin/java ]]; then
  JAVA_BIN="$(command -v java || true)"
  if [[ -z "${JAVA_BIN}" ]]; then
    echo "java executable not found in PATH" >&2
    exit 1
  fi
  ln -s "${JAVA_BIN}" /usr/bin/java
  echo "[scheduler] linked java binary: /usr/bin/java -> ${JAVA_BIN}"
fi

crontab /app/crontab

echo "[scheduler] installed crontab:"
crontab -l
echo "[scheduler] cron daemon started (TZ=${TZ:-Asia/Seoul})"

exec cron -f -L 15
