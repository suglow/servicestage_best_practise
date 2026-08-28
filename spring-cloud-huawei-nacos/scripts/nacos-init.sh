#!/bin/sh

set -eu

NACOS_URL=${NACOS_URL:-http://localhost:8848}
NACOS_NAMESPACE=${NACOS_NAMESPACE:-dev}
NACOS_GROUP=${NACOS_GROUP:-CORE_GROUP}
GRAY_CONFIG_FILE=${GRAY_CONFIG_FILE:-/config/gray-routing.yaml}
NAMESPACES_API="${NACOS_URL}/nacos/v1/console/namespaces"
CONFIG_API="${NACOS_URL}/nacos/v1/cs/configs"

wait_for_nacos() {
  max_attempts=30
  attempt=1

  echo "[nacos-init] Waiting for Nacos: ${NACOS_URL}"
  while [ "$attempt" -le "$max_attempts" ]; do
    if curl -fsS "${NACOS_URL}/nacos/v1/console/health/readiness" >/dev/null; then
      echo "[nacos-init] Nacos is ready"
      return 0
    fi
    echo "[nacos-init] Attempt ${attempt}/${max_attempts}"
    attempt=$((attempt + 1))
    sleep 2
  done

  echo "[nacos-init] Nacos did not become ready" >&2
  return 1
}

namespace_exists() {
  curl -fsS "$NAMESPACES_API" | grep -Eq "\"namespace(Id)?\"[[:space:]]*:[[:space:]]*\"$1\""
}

ensure_namespace() {
  namespace_id=$1
  namespace_name=$2
  description=$3

  if namespace_exists "$namespace_id"; then
    echo "[nacos-init] Namespace already exists: ${namespace_id}"
    return 0
  fi

  echo "[nacos-init] Creating namespace: ${namespace_id}"
  response=$(curl -fsS -X POST "$NAMESPACES_API" \
    --data-urlencode "namespaceName=${namespace_name}" \
    --data-urlencode "customNamespaceId=${namespace_id}" \
    --data-urlencode "namespaceText=${namespace_name}" \
    --data-urlencode "description=${description}")

  case "$response" in
    true|*'"code":200'*|*'"success":true'*) ;;
    *)
      echo "[nacos-init] Failed to create namespace ${namespace_id}: ${response}" >&2
      return 1
      ;;
  esac

  namespace_exists "$namespace_id"
  echo "[nacos-init] Namespace created: ${namespace_id}"
}

publish_gray_config() {
  if [ ! -s "$GRAY_CONFIG_FILE" ]; then
    echo "[nacos-init] Missing gray routing config: ${GRAY_CONFIG_FILE}" >&2
    return 1
  fi

  echo "[nacos-init] Publishing gray-routing.yaml to ${NACOS_NAMESPACE}/${NACOS_GROUP}"
  attempt=1
  published=false
  while [ "$attempt" -le 10 ]; do
    result=$(curl -sS -X POST "$CONFIG_API" \
      --data-urlencode "dataId=gray-routing.yaml" \
      --data-urlencode "group=${NACOS_GROUP}" \
      --data-urlencode "tenant=${NACOS_NAMESPACE}" \
      --data-urlencode "type=yaml" \
      --data-urlencode "content@${GRAY_CONFIG_FILE}" \
      --write-out '\n%{http_code}' || true)
    status=$(printf '%s\n' "$result" | tail -n 1)
    response=$(printf '%s\n' "$result" | sed '$d')
    if [ "$status" = "200" ] && [ "$response" = "true" ]; then
      published=true
      break
    fi
    echo "[nacos-init] Publish attempt ${attempt}/10 returned HTTP ${status:-unknown}"
    attempt=$((attempt + 1))
    sleep 1
  done

  if [ "$published" != "true" ]; then
    echo "[nacos-init] Failed to publish gray-routing.yaml: ${response:-no response}" >&2
    return 1
  fi

  attempt=1
  verified=false
  while [ "$attempt" -le 10 ]; do
    loaded=$(curl -sSG "$CONFIG_API" \
      --data-urlencode "dataId=gray-routing.yaml" \
      --data-urlencode "group=${NACOS_GROUP}" \
      --data-urlencode "tenant=${NACOS_NAMESPACE}" || true)
    if printf '%s\n' "$loaded" | grep -q "product-service"; then
      verified=true
      break
    fi
    echo "[nacos-init] Verify attempt ${attempt}/10 did not find the configuration"
    attempt=$((attempt + 1))
    sleep 1
  done

  if [ "$verified" != "true" ]; then
    echo "[nacos-init] Failed to verify gray-routing.yaml" >&2
    return 1
  fi
  echo "[nacos-init] gray-routing.yaml verified"
}

wait_for_nacos
ensure_namespace "$NACOS_NAMESPACE" "$NACOS_NAMESPACE" "Demo environment"
publish_gray_config

echo "[nacos-init] Initialization completed"
