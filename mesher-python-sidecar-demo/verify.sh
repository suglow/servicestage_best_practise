#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

DEMO_HOST=${DEMO_HOST:-127.0.0.1}
SC_PORT=${CSE_SC_PORT:-30100}
MESHER_PORT=${MESHER_PORT:-30101}
PYTHON_PORT=${PYTHON_PORT:-9000}
PROVIDER_PORT=${JAVA_PROVIDER_PORT:-7001}
CONSUMER_PORT=${JAVA_CONSUMER_PORT:-7002}
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
  --tc NUMBER  run one test case (1-6)
  --help       show this help
EOF
}

list_cases() {
    printf '%s\n' \
        'TC-1  infrastructure health and running services' \
        'TC-2  three services registered and Mesher endpoint advertised' \
        'TC-3  Java provider API contract' \
        'TC-4  Flask and Mesher proxy health' \
        'TC-5  Python outbound call through Mesher' \
        'TC-6  Java consumer inbound call through Mesher'
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
    for service in local-cse java-provider python-app java-consumer; do
        check "$service container is running" "$service container is not running" \
            bash -c "docker compose ps --status running --services | grep -qx '$service'"
    done
}

tc2() {
    local registry_url="http://${DEMO_HOST}:${SC_PORT}/v4/default/registry/microservices"
    for service in python-app java-provider java-consumer; do
        check "$service is registered" "$service is not registered" python3 -c \
            'import json,sys,urllib.request; data=json.load(urllib.request.urlopen(sys.argv[1])); sys.exit(0 if any(item.get("serviceName")==sys.argv[2] for item in data.get("services",[])) else 1)' \
            "$registry_url" "$service"
    done
    check 'python-app advertises Mesher port 30101' 'python-app does not advertise Mesher port 30101' python3 -c \
        'import json,sys,urllib.request; base=sys.argv[1]; services=json.load(urllib.request.urlopen(base+"/registry/microservices")).get("services",[]); service=next(item for item in services if item.get("serviceName")=="python-app"); instances=json.load(urllib.request.urlopen(base+"/registry/microservices/"+service["serviceId"]+"/instances")).get("instances",[]); endpoints=[endpoint for instance in instances for endpoint in instance.get("endpoints",[])]; sys.exit(0 if any(str(endpoint).endswith(":30101") for endpoint in endpoints) else 1)' \
        "http://${DEMO_HOST}:${SC_PORT}/v4/default"
}

tc3() {
    check 'provider endpoint becomes ready' 'provider endpoint did not become ready' wait_for "http://${DEMO_HOST}:${PROVIDER_PORT}/api/users" 30
    check 'provider returns three demo users' 'provider response contract failed' bash -c \
        "curl -fsS 'http://${DEMO_HOST}:${PROVIDER_PORT}/api/users' | python3 -c 'import json,sys; data=json.load(sys.stdin); assert data[\"service\"]==\"java-provider\" and len(data[\"users\"])==3'"
}

tc4() {
    check 'python health endpoint' 'python health endpoint failed' curl -fsS "http://${DEMO_HOST}:${PYTHON_PORT}/health"
    check 'Mesher proxy port accepts connections' 'Mesher proxy port is unavailable' python3 -c \
        'import socket,sys; connection=socket.create_connection((sys.argv[1],int(sys.argv[2])),3); connection.close()' "$DEMO_HOST" "$MESHER_PORT"
}

tc5() {
    check 'python outbound endpoint becomes ready' 'python outbound endpoint did not become ready' wait_for "http://${DEMO_HOST}:${PYTHON_PORT}/show" 30
    check 'python calls java-provider through Mesher' 'python to Mesher to provider call failed' bash -c \
        "curl -fsS 'http://${DEMO_HOST}:${PYTHON_PORT}/show' | python3 -c 'import json,sys; data=json.load(sys.stdin); result=data[\"java_provider_response\"]; assert result[\"status\"]==200 and result[\"body\"][\"service\"]==\"java-provider\"'"
}

tc6() {
    local response successful=0
    check 'consumer endpoint becomes ready' 'consumer endpoint did not become ready' wait_for "http://${DEMO_HOST}:${CONSUMER_PORT}/show" 30
    for _ in 1 2 3; do
        response=$(curl -fsS "http://${DEMO_HOST}:${CONSUMER_PORT}/show" 2>/dev/null || true)
        if printf '%s' "$response" | python3 -c 'import json,sys; data=json.load(sys.stdin); python=data["python_app_response"]; assert python["service"]=="python-app" and python["java_provider_response"]["status"]==200' 2>/dev/null; then
            successful=$((successful + 1))
        fi
    done
    if [[ "$successful" -eq 3 ]]; then PASS=$((PASS + 1)); green 'full Java to Mesher to Python to Mesher to Java path succeeds 3/3'; else FAIL=$((FAIL + 1)); red "full call path succeeded ${successful}/3"; fi
}

case "${1:-}" in
    --help|-h) usage; exit 0 ;;
    --list) list_cases; exit 0 ;;
    --tc)
        case "${2:-}" in
            1|2|3|4|5|6) SELECTED=$2 ;;
            *) echo 'error: --tc expects a number from 1 to 6' >&2; exit 2 ;;
        esac
        ;;
    '') SELECTED=all ;;
    *) echo "error: unknown argument $1" >&2; usage >&2; exit 2 ;;
esac

printf '\nMesher Python Sidecar Demo verification\n\n'
for test_case in 1 2 3 4 5 6; do
    if [[ "$SELECTED" == all || "$SELECTED" == "$test_case" ]]; then
        printf '[TC-%s] %s\n' "$test_case" "$(list_cases | sed -n "${test_case}p" | cut -d' ' -f3-)"
        "tc${test_case}"
        printf '\n'
    fi
done

printf 'Result: %d passed, %d failed\n' "$PASS" "$FAIL"
((FAIL == 0))
