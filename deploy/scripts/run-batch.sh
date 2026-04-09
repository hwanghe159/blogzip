#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/compose/docker-compose.prod.yml"
DEPLOY_ENV_FILE="${ROOT_DIR}/compose/.env.deploy"
RUNTIME_ENV_FILE="${ROOT_DIR}/compose/.env.runtime"
LOG_DIR="${ROOT_DIR}/logs/batch"

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

# run-batch는 batch/crawler만 사용하지만, compose 파일에 api/nginx 서비스가 존재해서
# 전체 구성 검증 시 API_IMAGE/NGINX_IMAGE가 비어 있으면 실패할 수 있다.
# 실제 pull/run 대상은 batch/crawler이므로 placeholder 값을 채워 검증만 통과시킨다.
export API_IMAGE="${API_IMAGE:-placeholder/api:latest}"
export NGINX_IMAGE="${NGINX_IMAGE:-placeholder/nginx:latest}"

if [[ -n "${REGISTRY_HOST:-}" && -n "${REGISTRY_USERNAME:-}" && -n "${REGISTRY_PASSWORD:-}" ]]; then
  echo "${REGISTRY_PASSWORD}" | docker login "${REGISTRY_HOST}" -u "${REGISTRY_USERNAME}" --password-stdin
fi

mkdir -p "${LOG_DIR}"

JOB_NAME="unknown"
for arg in "$@"; do
  if [[ "${arg}" == --spring.batch.job.name=* ]]; then
    JOB_NAME="${arg#*=}"
    break
  fi
done
SAFE_JOB_NAME="${JOB_NAME//[^a-zA-Z0-9._-]/_}"
TIMESTAMP="$(date '+%Y%m%d_%H%M%S')"
LOG_FILE="${LOG_DIR}/${TIMESTAMP}_${SAFE_JOB_NAME}.log"

echo "Batch log file: ${LOG_FILE}"

set +e
{
  docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" --profile batch pull batch
  docker compose --env-file "${DEPLOY_ENV_FILE}" -f "${COMPOSE_FILE}" --profile batch run --rm batch "$@"
} 2>&1 | tee "${LOG_FILE}"
RUN_EXIT_CODE="${PIPESTATUS[0]}"
set -e

if [[ "${RUN_EXIT_CODE}" -ne 0 ]]; then
  echo "Batch run failed. See log: ${LOG_FILE}" >&2
  exit "${RUN_EXIT_CODE}"
fi

echo "Batch run completed. See log: ${LOG_FILE}"
