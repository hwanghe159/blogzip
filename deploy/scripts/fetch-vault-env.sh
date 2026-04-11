#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_ENV_FILE="${ROOT_DIR}/compose/.env.runtime"

resolve_oci_cli() {
  if [[ -n "${OCI_CLI_BIN:-}" ]]; then
    if [[ -x "${OCI_CLI_BIN}" ]]; then
      printf '%s' "${OCI_CLI_BIN}"
      return 0
    fi
    echo "OCI_CLI_BIN is set but not executable: ${OCI_CLI_BIN}" >&2
    exit 1
  fi

  if command -v oci >/dev/null 2>&1; then
    command -v oci
    return 0
  fi

  if [[ -x "${HOME}/bin/oci" ]]; then
    printf '%s' "${HOME}/bin/oci"
    return 0
  fi

  echo "OCI CLI is required but was not found in PATH or ${HOME}/bin/oci." >&2
  echo "Install once on the VM: bash -c \"\$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)\" -- --accept-all-defaults" >&2
  exit 1
}

OCI_BIN="$(resolve_oci_cli)"

require_env() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "${value}" ]]; then
    echo "${name} is required." >&2
    exit 1
  fi
}

require_env "OCI_VAULT_ID"
require_env "OCI_REGION"

MYSQL_PORT_VALUE="${MYSQL_PORT:-3306}"

AUTH_MODE="${OCI_CLI_AUTH:-instance_principal}"

declare -a OCI_CMD=("${OCI_BIN}")
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
  "MYSQL_HOST"
  "MYSQL_USERNAME"
  "MYSQL_PASSWORD"
  "OCI_EMAIL_SMTP_USERNAME"
  "OCI_EMAIL_SMTP_PASSWORD"
  "OPEN_AI_API_KEY"
  "SLACK_WEBHOOK_URL"
)

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

write_env_pair() {
  local key="$1"
  local value="$2"

  if [[ "${value}" == *$'\n'* ]]; then
    echo "Value for '${key}' contains a newline, which is not supported for .env format." >&2
    exit 1
  fi

  printf '%s=%s\n' "${key}" "${value}" >> "${RUNTIME_ENV_FILE}"
}

: > "${RUNTIME_ENV_FILE}"
write_env_pair "MYSQL_PORT" "${MYSQL_PORT_VALUE}"
write_env_pair "MYSQL_DATABASE" "blogzip"
write_env_pair "OCI_REGION" "${OCI_REGION}"

for secret_name in "${REQUIRED_SECRETS[@]}"; do
  if ! secret_value="$(fetch_secret_value "${secret_name}")"; then
    echo "Failed to fetch required secret '${secret_name}' from OCI Vault '${OCI_VAULT_ID}'." >&2
    exit 1
  fi
  write_env_pair "${secret_name}" "${secret_value}"
done

chmod 600 "${RUNTIME_ENV_FILE}"
echo "Vault secrets synced to ${RUNTIME_ENV_FILE}"
