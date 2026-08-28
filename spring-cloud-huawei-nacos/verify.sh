#!/bin/bash

set -uo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0
WARN=0
SELECTED_TC=""
DEMO_HOST=${DEMO_HOST:-localhost}
NACOS_NAMESPACE=${NACOS_NAMESPACE:-dev}
HTTP_BASE="http://${DEMO_HOST}"

usage() {
  cat <<'USAGE'
用法: bash verify.sh [--tc N]

  不带参数       运行全部测试用例
  --tc N         运行指定用例：1, 2, 2F, 3, 3G, 4-10
  --list         列出可用测试组
  --help, -h     查看帮助

环境变量:
  DEMO_HOST      服务地址，默认 localhost
  NACOS_NAMESPACE  Nacos namespace，默认 dev
USAGE
}

list_cases() {
  cat <<'CASES'
TC-1   Nacos 健康、配置与注册发现
TC-2   RestTemplate 服务调用
TC-2F  OpenFeign 服务调用
TC-3   Consumer 灰度路由
TC-3G  Gateway 灰度路由
TC-4   Provider 故障端点
TC-5   流量限流
TC-6   Gateway 基础路由
TC-7   重试治理
TC-8   实例隔离
TC-9   舱壁隔离
TC-10  Provider 熔断
CASES
}

normalize_case() {
  printf '%s' "$1" | tr '[:lower:]' '[:upper:]' | sed 's/^TC-//'
}

if [ "$#" -gt 0 ]; then
  case "$1" in
    --help|-h)
      usage
      exit 0
      ;;
    --list)
      list_cases
      exit 0
      ;;
    --tc)
      if [ "$#" -ne 2 ]; then
        echo "错误: --tc 需要且仅需要一个用例编号" >&2
        usage >&2
        exit 2
      fi
      SELECTED_TC=$(normalize_case "$2")
      ;;
    *)
      echo "错误: 未知参数 $1" >&2
      usage >&2
      exit 2
      ;;
  esac
fi

case "$SELECTED_TC" in
  ""|1|2|2F|3|3G|4|5|6|7|8|9|10) ;;
  *)
    echo "错误: 未知测试用例 ${SELECTED_TC}" >&2
    list_cases >&2
    exit 2
    ;;
esac

should_run() {
  [ -z "$SELECTED_TC" ] || [ "$SELECTED_TC" = "$1" ]
}

pass() {
  printf "  ${GREEN}✅ PASS${NC} %s\n" "$1"
  PASS=$((PASS + 1))
}

fail() {
  printf "  ${RED}❌ FAIL${NC} %s\n" "$1"
  FAIL=$((FAIL + 1))
}

warn() {
  printf "  ${YELLOW}⚠️  WARN${NC} %s\n" "$1"
  WARN=$((WARN + 1))
}

section() {
  printf "\n==========================================\n%s\n==========================================\n" "$1"
}

wait_for_url() {
  label=$1
  url=$2
  attempt=1
  while [ "$attempt" -le 30 ]; do
    if curl -fsS --max-time 3 "$url" >/dev/null 2>&1; then
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 2
  done
  fail "${label} 未就绪: ${url}"
  return 1
}

wait_for_http_server() {
  label=$1
  url=$2
  attempt=1
  while [ "$attempt" -le 30 ]; do
    if curl -sS --max-time 3 -o /dev/null "$url" 2>/dev/null; then
      return 0
    fi
    attempt=$((attempt + 1))
    sleep 2
  done
  fail "${label} 未就绪: ${url}"
  return 1
}

extract_port() {
  grep -oE '"port"[[:space:]]*:[[:space:]]*"?[0-9]+"?' \
    | head -1 \
    | grep -oE '[0-9]+' || true
}

assert_contains() {
  body=$1
  pattern=$2
  message=$3
  if printf '%s' "$body" | grep -qE "$pattern"; then
    pass "$message"
  else
    fail "$message"
  fi
}

assert_route() {
  label=$1
  url=$2
  expected_port=$3
  header=${4:-}
  attempts=${5:-10}
  matched=0
  index=1

  while [ "$index" -le "$attempts" ]; do
    if [ -n "$header" ]; then
      response=$(curl -sS --max-time 10 -H "$header" "$url" 2>/dev/null || true)
    else
      response=$(curl -sS --max-time 10 "$url" 2>/dev/null || true)
    fi
    port=$(printf '%s' "$response" | extract_port)
    [ "$port" = "$expected_port" ] && matched=$((matched + 1))
    index=$((index + 1))
  done

  if [ "$matched" -eq "$attempts" ]; then
    pass "${label}: ${matched}/${attempts} 命中端口 ${expected_port}"
  else
    fail "${label}: ${matched}/${attempts} 命中端口 ${expected_port}"
  fi
}

test_tc1() {
  section "TC-1: Nacos 配置与注册发现"
  wait_for_url "Nacos" "${HTTP_BASE}:8848/nacos/v1/console/health/readiness" || return
  pass "TC-1.1 Nacos 健康检查"

  config=$(curl -fsSG "${HTTP_BASE}:8848/nacos/v1/cs/configs" \
    --data-urlencode "dataId=gray-routing.yaml" \
    --data-urlencode "group=CORE_GROUP" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" 2>/dev/null || true)
  assert_contains "$config" 'product-service' "TC-1.2 灰度配置已发布"

  instances=$(curl -fsSG "${HTTP_BASE}:8848/nacos/v1/ns/instance/list" \
    --data-urlencode "serviceName=product-service" \
    --data-urlencode "groupName=CORE_GROUP" \
    --data-urlencode "namespaceId=${NACOS_NAMESPACE}" 2>/dev/null || true)
  if printf '%s' "$instances" | grep -qE '"port"[[:space:]]*:[[:space:]]*8081' \
    && printf '%s' "$instances" | grep -qE '"port"[[:space:]]*:[[:space:]]*8083'; then
    pass "TC-1.3 product-service 双版本实例已注册"
  else
    fail "TC-1.3 product-service 双版本实例已注册"
  fi

  wait_for_url "product-service v1" "${HTTP_BASE}:8081/api/products" \
    && pass "TC-1.4 product-service v1 可访问"
  wait_for_url "product-service v2" "${HTTP_BASE}:8083/api/products" \
    && pass "TC-1.5 product-service v2 可访问"
}

test_tc2() {
  section "TC-2: RestTemplate 服务调用"
  wait_for_url "order-service" "${HTTP_BASE}:8082/actuator/health" || return
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8082/api/orders?productId=1" 2>/dev/null || true)
  assert_contains "$response" '"orderId"' "TC-2.1 创建订单"
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8082/api/orders/products" 2>/dev/null || true)
  assert_contains "$response" '"products"' "TC-2.2 查询商品列表"
  assert_route "TC-2.3 默认路由" "${HTTP_BASE}:8082/api/orders?productId=1" 8081
}

test_tc2f() {
  section "TC-2F: OpenFeign 服务调用"
  wait_for_url "order-service-feign" "${HTTP_BASE}:8084/api/orders-fg/products" || return
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8084/api/orders-fg?productId=1" 2>/dev/null || true)
  assert_contains "$response" '"orderId"' "TC-2F.1 创建订单"
  assert_route "TC-2F.2 默认路由" "${HTTP_BASE}:8084/api/orders-fg/products" 8081
  assert_route "TC-2F.3 灰度路由" "${HTTP_BASE}:8084/api/orders-fg/products" 8083 "X-Gray-Tag: gray"
}

test_tc3() {
  section "TC-3: Consumer 灰度路由"
  wait_for_url "order-service" "${HTTP_BASE}:8082/actuator/health" || return
  assert_route "TC-3.1 默认流量" "${HTTP_BASE}:8082/api/orders?productId=1" 8081
  assert_route "TC-3.2 灰度流量" "${HTTP_BASE}:8082/api/orders?productId=1" 8083 "X-Gray-Tag: gray"
  v1=$(curl -fsS --max-time 10 "${HTTP_BASE}:8081/api/products" 2>/dev/null || true)
  v2=$(curl -fsS --max-time 10 "${HTTP_BASE}:8083/api/products" 2>/dev/null || true)
  if printf '%s' "$v1" | grep -qE '"version"[[:space:]]*:[[:space:]]*"1.0.0"' \
    && printf '%s' "$v2" | grep -qE '"version"[[:space:]]*:[[:space:]]*"2.0.0"'; then
    pass "TC-3.3 商品服务双版本共存"
  else
    fail "TC-3.3 商品服务双版本共存"
  fi
}

test_tc3g() {
  section "TC-3G: Gateway 灰度路由"
  wait_for_url "gateway" "${HTTP_BASE}:8080/api/products" || return
  assert_route "TC-3G.1 默认流量" "${HTTP_BASE}:8080/api/products" 8081
  assert_route "TC-3G.2 灰度流量" "${HTTP_BASE}:8080/api/products" 8083 "X-Gray-Tag: gray"
  response=$(curl -fsS --max-time 10 -H "X-Gray-Tag: gray" \
    "${HTTP_BASE}:8080/api/orders?productId=1" 2>/dev/null || true)
  assert_contains "$response" '"port"[[:space:]]*:[[:space:]]*"?8083"?' "TC-3G.3 全链路灰度"
}

test_tc4() {
  section "TC-4: Provider 故障端点"
  wait_for_url "product-service" "${HTTP_BASE}:8081/api/products" || return
  ok=0
  failed=0
  for _ in 1 2 3 4 5 6; do
    code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 5 \
      "${HTTP_BASE}:8081/api/products/test/error" 2>/dev/null || printf '000')
    [ "$code" = "200" ] && ok=$((ok + 1))
    [ "$code" != "200" ] && [ "$code" != "000" ] && failed=$((failed + 1))
  done
  if [ "$ok" -gt 0 ] && [ "$failed" -gt 0 ]; then
    pass "TC-4.1 错误端点同时产生成功和失败响应"
  else
    fail "TC-4.1 错误端点响应分布异常: success=${ok}, failed=${failed}"
  fi

  duration=$(curl -sS -o /dev/null -w '%{time_total}' --max-time 5 \
    "${HTTP_BASE}:8081/api/products/test/slow" 2>/dev/null || printf '0')
  seconds=${duration%%.*}
  if [ "${seconds:-0}" -ge 1 ]; then
    pass "TC-4.2 慢调用端点产生延迟"
  else
    fail "TC-4.2 慢调用端点未产生预期延迟"
  fi
}

test_tc5() {
  section "TC-5: 流量限流"
  wait_for_url "order-service" "${HTTP_BASE}:8082/actuator/health" || return
  sleep 6
  ok=0
  limited=0
  for _ in 1 2 3 4 5 6 7 8; do
    code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 5 \
      "${HTTP_BASE}:8082/api/orders/test/rate-limit" 2>/dev/null || printf '000')
    [ "$code" = "200" ] && ok=$((ok + 1))
    [ "$code" = "429" ] && limited=$((limited + 1))
  done
  if [ "$ok" -gt 0 ] && [ "$limited" -gt 0 ]; then
    pass "TC-5.1 限流生效: 200=${ok}, 429=${limited}"
  else
    fail "TC-5.1 限流未按预期生效: 200=${ok}, 429=${limited}"
  fi
}

test_tc6() {
  section "TC-6: Gateway 基础路由"
  wait_for_url "gateway" "${HTTP_BASE}:8080/api/products" || return
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8080/api/products/1" 2>/dev/null || true)
  assert_contains "$response" '"product"' "TC-6.1 路由到 product-service"
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8080/api/orders?productId=1" 2>/dev/null || true)
  assert_contains "$response" '"orderId"' "TC-6.2 路由到 order-service"
  code=$(curl -sS -o /dev/null -w '%{http_code}' --max-time 5 \
    "${HTTP_BASE}:8080/api/unknown" 2>/dev/null || printf '000')
  [ "$code" = "404" ] && pass "TC-6.3 未知路径返回 404" || fail "TC-6.3 未知路径返回 ${code}"
}

test_boolean_endpoints() {
  base_url=$1
  prefix=$2
  shift 2
  for endpoint in "$@"; do
    response=$(curl -fsS --max-time 90 "${base_url}/${endpoint}" 2>/dev/null || true)
    if [ "$response" = "true" ]; then
      pass "${prefix} ${endpoint}"
    else
      fail "${prefix} ${endpoint}: ${response:-no response}"
    fi
  done
}

test_tc7() {
  section "TC-7: 重试治理"
  wait_for_http_server "retry-provider" "${HTTP_BASE}:8090/" || return
  wait_for_http_server "retry-consumer" "${HTTP_BASE}:8091/" || return
  for endpoint in no-retry retry status-retry retry-service-name on-same-retry-one on-same-retry-two; do
    response=$(curl -fsS --max-time 90 "${HTTP_BASE}:8091/${endpoint}" 2>/dev/null || true)
    assert_contains "$response" '^请求结果符合策略要求$' "TC-7 ${endpoint}"
  done
}

test_tc8() {
  section "TC-8: 实例隔离"
  wait_for_http_server "isolation-provider" "${HTTP_BASE}:8092/" || return
  wait_for_http_server "isolation-consumer" "${HTTP_BASE}:8093/" || return
  test_boolean_endpoints "${HTTP_BASE}:8093" "TC-8" \
    low-minimum-calls up-minimum-calls \
    low-failed-percent up-failed-percent \
    low-slow-call-percent up-slow-call-percent \
    force-closed force-open force-open-service \
    error-code-404 error-code-500
}

test_tc9() {
  section "TC-9: 舱壁隔离"
  wait_for_http_server "bulkhead-provider" "${HTTP_BASE}:8094/" || return
  wait_for_http_server "bulkhead-consumer" "${HTTP_BASE}:8095/" || return
  test_boolean_endpoints "${HTTP_BASE}:8095" "TC-9" \
    bulk-rate-limiting bulk-no-rate-limiting bulk-service-name
}

test_tc10() {
  section "TC-10: Provider 熔断"
  wait_for_http_server "circuit-breaker-provider" "${HTTP_BASE}:8096/" || return
  wait_for_http_server "circuit-breaker-consumer" "${HTTP_BASE}:8097/" || return
  response=$(curl -fsS --max-time 10 "${HTTP_BASE}:8097/api/consumer/circuit/normal" 2>/dev/null || true)
  assert_contains "$response" '"success"[[:space:]]*:[[:space:]]*true' "TC-10.1 正常调用成功"
  assert_contains "$response" '"providerCalls"[[:space:]]*:[[:space:]]*1' "TC-10.2 正常调用进入 Provider"

  response=$(curl -fsS --max-time 30 "${HTTP_BASE}:8097/api/consumer/circuit/error-rate" 2>/dev/null || true)
  assert_contains "$response" '"preparationReady"[[:space:]]*:[[:space:]]*true' "TC-10.3 错误率场景状态已恢复"
  assert_contains "$response" '"circuitOpen"[[:space:]]*:[[:space:]]*true' "TC-10.4 错误率触发熔断"
  assert_contains "$response" '"rejectedCalls"[[:space:]]*:[[:space:]]*[1-9][0-9]*' "TC-10.5 错误率场景出现短路请求"

  response=$(curl -fsS --max-time 30 "${HTTP_BASE}:8097/api/consumer/circuit/slow-call" 2>/dev/null || true)
  assert_contains "$response" '"preparationReady"[[:space:]]*:[[:space:]]*true' "TC-10.6 慢调用场景状态已恢复"
  assert_contains "$response" '"circuitOpen"[[:space:]]*:[[:space:]]*true' "TC-10.7 慢调用触发熔断"
  assert_contains "$response" '"rejectedCalls"[[:space:]]*:[[:space:]]*[1-9][0-9]*' "TC-10.8 慢调用场景出现短路请求"
}

printf "==========================================\n"
printf "  Spring Cloud Huawei Nacos Demo 验证\n"
printf "  Host: %s  Namespace: %s\n" "$DEMO_HOST" "$NACOS_NAMESPACE"
printf "==========================================\n"

should_run 1 && test_tc1
should_run 2 && test_tc2
should_run 2F && test_tc2f
should_run 3 && test_tc3
should_run 3G && test_tc3g
should_run 4 && test_tc4
should_run 5 && test_tc5
should_run 6 && test_tc6
should_run 7 && test_tc7
should_run 8 && test_tc8
should_run 9 && test_tc9
should_run 10 && test_tc10

section "测试结果汇总"
printf "  ${GREEN}PASS: %d${NC}\n" "$PASS"
printf "  ${RED}FAIL: %d${NC}\n" "$FAIL"
printf "  ${YELLOW}WARN: %d${NC}\n" "$WARN"
printf "  总计: %d\n" "$((PASS + FAIL + WARN))"

[ "$FAIL" -eq 0 ]
