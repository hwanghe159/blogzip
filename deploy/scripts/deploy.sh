#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/compose/docker-compose.prod.yml"
DEPLOY_ENV_FILE="${ROOT_DIR}/compose/.env.deploy"
RUNTIME_ENV_FILE="${ROOT_DIR}/compose/.env.runtime"

if [[ ! -f "${DEPLOY_ENV_FILE}" ]]; then
  echo ".env.deploy file is missing at ${DEPLOY_ENV_FILE}" >&2
  exit 1
fi

"${ROOT_DIR}/scripts/fetch-vault-env.sh"

if [[ ! -f "${RUNTIME_ENV_FILE}" ]]; then
  echo ".env.runtime file is missing at ${RUNTIME_ENV_FILE}" >&2
  exit 1
fi

set -a
source "${DEPLOY_ENV_FILE}"
set +a

if [[ -z "${API_IMAGE:-}" || -z "${NGINX_IMAGE:-}" || -z "${BATCH_IMAGE:-}" || -z "${CRAWLER_IMAGE:-}" || -z "${SCHEDULER_IMAGE:-}" ]]; then
  echo "API_IMAGE, NGINX_IMAGE, BATCH_IMAGE, CRAWLER_IMAGE, SCHEDULER_IMAGE must be set in ${DEPLOY_ENV_FILE}" >&2
  exit 1
fi

docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" pull
docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" up -d --remove-orphans
docker image prune -f
