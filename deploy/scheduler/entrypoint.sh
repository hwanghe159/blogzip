#!/usr/bin/env bash

set -euo pipefail

if [[ ! -f /app/crontab ]]; then
  echo "Scheduler crontab file not found: /app/crontab" >&2
  exit 1
fi

CRON_ENV_FILE="/app/cron.env"

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

# Cron은 컨테이너 프로세스 환경변수를 그대로 물려받지 않을 수 있으므로,
# 현재 환경변수를 별도 파일로 보존해 배치 실행 스크립트에서 source 한다.
: > "${CRON_ENV_FILE}"
while IFS= read -r line; do
  key="${line%%=*}"
  value="${line#*=}"
  if [[ ! "${key}" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]; then
    continue
  fi
  printf 'export %s=%q\n' "${key}" "${value}" >> "${CRON_ENV_FILE}"
done < <(printenv)
chmod 600 "${CRON_ENV_FILE}"
echo "[scheduler] wrote cron env file: ${CRON_ENV_FILE}"

crontab /app/crontab

echo "[scheduler] installed crontab:"
crontab -l
echo "[scheduler] cron daemon started (TZ=${TZ:-Asia/Seoul})"

exec cron -f -L 15
