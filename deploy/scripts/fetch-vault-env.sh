#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_ENV_FILE="${ROOT_DIR}/compose/.env.api"
RUNTIME_ENV_FILE="${ROOT_DIR}/compose/.env.runtime"

if [[ ! -f "${BASE_ENV_FILE}" ]]; then
  echo ".env.api file is missing at ${BASE_ENV_FILE}" >&2
  exit 1
fi

if ! command -v oci >/dev/null 2>&1; then
  echo "OCI CLI is required. Please install OCI CLI on this server." >&2
  exit 1
fi

set -a
source "${BASE_ENV_FILE}"
set +a

if [[ -z "${OCI_VAULT_ID:-}" ]]; then
  echo "OCI_VAULT_ID is missing in ${BASE_ENV_FILE}" >&2
  exit 1
fi

AUTH_MODE="${OCI_CLI_AUTH:-instance_principal}"

declare -a OCI_CMD=(oci)
if [[ -n "${OCI_CONFIG_FILE:-}" ]]; then
  OCI_CMD+=(--config-file "${OCI_CONFIG_FILE}")
fi
if [[ -n "${OCI_CLI_PROFILE:-}" ]]; then
  OCI_CMD+=(--profile "${OCI_CLI_PROFILE}")
fi
if [[ -n "${OCI_REGION:-}" ]]; then
  OCI_CMD+=(--region "${OCI_REGION}")
fi
OCI_CMD+=(--auth "${AUTH_MODE}")

declare -a REQUIRED_SECRETS=(
  "JWT_SECRET_KEY"
  "GOOGLE_CLIENT_SECRET"
  "ADMIN_TOKEN"
  "MYSQL_USERNAME"
  "MYSQL_PASSWORD"
  "OCI_EMAIL_SMTP_USERNAME"
  "OCI_EMAIL_SMTP_PASSWORD"
  "OPEN_AI_API_KEY"
  "OPEN_AI_ASSISTANT_ID"
  "OPEN_AI_THREAD_ID"
  "SLACK_WEBHOOK_URL"
)

declare -a OPTIONAL_SECRETS=(
  "OCI_EMAIL_SMTP_HOST"
  "OCI_EMAIL_SMTP_PORT"
  "OCI_EMAIL_FROM_NAME"
  "OCI_EMAIL_FROM_ADDRESS"
)

declare -a ALL_SECRET_KEYS=("${REQUIRED_SECRETS[@]}" "${OPTIONAL_SECRETS[@]}")

is_secret_key() {
  local candidate="$1"
  local secret_key
  for secret_key in "${ALL_SECRET_KEYS[@]}"; do
    if [[ "${candidate}" == "${secret_key}" ]]; then
      return 0
    fi
  done
  return 1
}

fetch_secret_value() {
  local secret_name="$1"
  local encoded
  local decoded

  encoded="$(
    "${OCI_CMD[@]}" secrets secret-bundle get-secret-bundle-by-name \
      --secret-name "${secret_name}" \
      --vault-id "${OCI_VAULT_ID}" \
      --query 'data."secret-bundle-content".content' \
      --raw-output
  )" || return 1

  if [[ -z "${encoded}" || "${encoded}" == "null" ]]; then
    return 1
  fi

  if decoded="$(printf '%s' "${encoded}" | base64 --decode 2>/dev/null)"; then
    :
  elif decoded="$(printf '%s' "${encoded}" | base64 -d 2>/dev/null)"; then
    :
  else
    return 1
  fi

  printf '%s' "${decoded}"
}

write_secret() {
  local secret_name="$1"
  local secret_value="$2"

  if [[ "${secret_value}" == *$'\n'* ]]; then
    echo "Secret '${secret_name}' contains a newline, which is not supported for .env format." >&2
    exit 1
  fi

  printf '%s=%s\n' "${secret_name}" "${secret_value}" >> "${RUNTIME_ENV_FILE}"
}

: > "${RUNTIME_ENV_FILE}"
while IFS= read -r line || [[ -n "${line}" ]]; do
  if [[ -z "${line}" || "${line}" =~ ^[[:space:]]*# ]]; then
    printf '%s\n' "${line}" >> "${RUNTIME_ENV_FILE}"
    continue
  fi

  key="${line%%=*}"
  key="${key#"${key%%[![:space:]]*}"}"
  key="${key%"${key##*[![:space:]]}"}"

  if is_secret_key "${key}"; then
    continue
  fi

  printf '%s\n' "${line}" >> "${RUNTIME_ENV_FILE}"
done < "${BASE_ENV_FILE}"

for secret_name in "${REQUIRED_SECRETS[@]}"; do
  if ! secret_value="$(fetch_secret_value "${secret_name}")"; then
    echo "Failed to fetch required secret '${secret_name}' from OCI Vault '${OCI_VAULT_ID}'." >&2
    exit 1
  fi
  write_secret "${secret_name}" "${secret_value}"
done

for secret_name in "${OPTIONAL_SECRETS[@]}"; do
  if secret_value="$(fetch_secret_value "${secret_name}" 2>/dev/null)"; then
    write_secret "${secret_name}" "${secret_value}"
  fi
done

chmod 600 "${RUNTIME_ENV_FILE}"
echo "Vault secrets synced to ${RUNTIME_ENV_FILE}"

