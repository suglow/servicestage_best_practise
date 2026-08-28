#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

DEMO_HOST=${DEMO_HOST:-127.0.0.1}
SC_PORT=${CSE_SC_PORT:-30100}
ORDER_PORT=${ORDER_PORT:-8080}
PRODUCT_PORT=${PRODUCT_PORT:-8081}
PASS=0
FAIL=0

green() { printf '\033[32m  ✓ %s\033[0m\n' "$1"; }
red() { printf '\033[31m  ✗ %s\033[0m\n' "$1"; }
check() {
    local description=$1
    local failure_message=${2:-$description}
    shift 2
    if "$@"; then PASS=$((PASS + 1)); green "$description"; else FAIL=$((FAIL + 1)); red "$failure_message"; fi
}

usage() {
    cat <<'EOF'
Usage: bash verify.sh [--list | --tc NUMBER | --help]

  --list       list test cases
  --tc NUMBER  run one test case (1-3)
  --help       show this help
EOF
}

list_cases() {
    printf '%s\n' \
        'TC-1  infrastructure health and running services' \
        'TC-2  Sermant registration provenance in Service Center' \
        'TC-3  Spring Cloud Huawei consumer discovers and calls provider'
}

wait_for() {
    local url=$1 attempts=${2:-30} i
    for ((i = 1; i <= attempts; i++)); do
        if curl -fsS "$url" >/dev/null 2>&1; then return 0; fi
        sleep 2
    done
    return 1
}

tc1() {
    check 'docker compose is available' 'docker compose is unavailable' bash -c 'docker compose version >/dev/null 2>&1'
    check 'Service Center health endpoint' 'Service Center health endpoint failed' curl -fsS "http://${DEMO_HOST}:${SC_PORT}/health"
    check 'product-service container is running' 'product-service container is not running' bash -c 'docker compose ps --status running --services | grep -qx product-service'
    check 'order-service container is running' 'order-service container is not running' bash -c 'docker compose ps --status running --services | grep -qx order-service'
}

tc2() {
    local registry_url="http://${DEMO_HOST}:${SC_PORT}/v4/default/registry/microservices"
    check 'product-service is registered' 'product-service is not registered' python3 -c 'import json,sys,urllib.request; d=json.load(urllib.request.urlopen(sys.argv[1])); sys.exit(0 if any(s.get("serviceName")=="product-service" for s in d.get("services",[])) else 1)' "$registry_url"
    check 'product-service framework is Sermant' 'product-service framework is not Sermant' python3 -c 'import json,sys,urllib.request; d=json.load(urllib.request.urlopen(sys.argv[1])); sys.exit(0 if any(s.get("serviceName")=="product-service" and s.get("framework",{}).get("name")=="Sermant" for s in d.get("services",[])) else 1)' "$registry_url"
}

tc3() {
    local response
    check 'provider health endpoint' 'provider health endpoint failed' curl -fsS "http://${DEMO_HOST}:${PRODUCT_PORT}/api/products/1001"
    check 'consumer endpoint becomes ready' 'consumer endpoint did not become ready' wait_for "http://${DEMO_HOST}:${ORDER_PORT}/api/orders/products/1001" 30
    response=$(curl -fsS "http://${DEMO_HOST}:${ORDER_PORT}/api/orders/products/1001" 2>/dev/null || true)
    if printf '%s' "$response" | python3 -c 'import json,sys; d=json.load(sys.stdin); assert d["id"] == 1001 and d["source"] == "sermant-product"'; then
        PASS=$((PASS + 1)); green 'consumer service-discovery call returns provider data'
    else
        FAIL=$((FAIL + 1)); red "consumer service-discovery call failed: $response"
    fi
}

case "${1:-}" in
    --help|-h) usage; exit 0 ;;
    --list) list_cases; exit 0 ;;
    --tc)
        case "${2:-}" in
            1) SELECTED=1 ;;
            2) SELECTED=2 ;;
            3) SELECTED=3 ;;
            *) echo 'error: --tc expects 1, 2, or 3' >&2; exit 2 ;;
        esac
        ;;
    '') SELECTED=all ;;
    *) echo "error: unknown argument $1" >&2; usage >&2; exit 2 ;;
esac

printf '\nSermant CSE Demo verification\n\n'
if [[ "$SELECTED" == all || "$SELECTED" == 1 ]]; then
    printf '[TC-1] Infrastructure\n'; tc1; printf '\n'
fi
if [[ "$SELECTED" == all || "$SELECTED" == 2 ]]; then
    printf '[TC-2] Sermant registration\n'; tc2; printf '\n'
fi
if [[ "$SELECTED" == all || "$SELECTED" == 3 ]]; then
    printf '[TC-3] Service discovery call\n'; tc3; printf '\n'
fi

printf 'Result: %d passed, %d failed\n' "$PASS" "$FAIL"
((FAIL == 0))
