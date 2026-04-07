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

if [[ -z "${BATCH_IMAGE:-}" ]]; then
  echo "BATCH_IMAGE is missing in ${DEPLOY_ENV_FILE}" >&2
  exit 1
fi

if [[ -z "${CRAWLER_IMAGE:-}" ]]; then
  echo "CRAWLER_IMAGE is missing in ${DEPLOY_ENV_FILE}" >&2
  exit 1
fi

if [[ -n "${REGISTRY_HOST:-}" && -n "${REGISTRY_USERNAME:-}" && -n "${REGISTRY_PASSWORD:-}" ]]; then
  echo "${REGISTRY_PASSWORD}" | docker login "${REGISTRY_HOST}" -u "${REGISTRY_USERNAME}" --password-stdin
fi

docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" pull batch
docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" --profile batch run --rm batch "$@"
