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

crontab /app/crontab

echo "[scheduler] installed crontab:"
crontab -l
echo "[scheduler] cron daemon started (TZ=${TZ:-Asia/Seoul})"

exec cron -f -L 15
