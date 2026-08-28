#!/bin/bash
#==============================================================================
# Spring Cloud Huawei ServiceComb Demo — 测试验证脚本
# 用法: bash verify.sh [选项]
#   bash verify.sh        # 运行全部测试用例
#   bash verify.sh --tc 1 # 只运行 TC-1
#   bash verify.sh --tc 3 # 只运行 TC-3
#   bash verify.sh --help # 查看帮助
#==============================================================================

set -uo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASS=0
FAIL=0
TOTAL=0
DEMO_HOST="${DEMO_HOST:-localhost}"
HTTP_BASE="http://${DEMO_HOST}"

pass() { echo -e "  ${GREEN}✅ PASS${NC} $1"; ((PASS++)) || true; ((TOTAL++)) || true; }
fail() { echo -e "  ${RED}❌ FAIL${NC} $1"; ((FAIL++)) || true; ((TOTAL++)) || true; }
warn() { echo -e "  ${YELLOW}⚠️  WARN${NC} $1"; ((TOTAL++)) || true; }
info() { echo -e "  ${NC}  $1"; }
usage() {
    echo "用法: bash verify.sh [--tc N]"
    echo "  不带参数     运行全部测试用例"
    echo "  --tc N       只运行指定用例（N 为 1-11）"
    echo "  --list       列出可用测试组"
    echo "  --help, -h   查看帮助"
}
list_cases() {
    cat <<'CASES'
TC-1   基础设施和注册发现
TC-2   OpenFeign 服务调用
TC-3   灰度发布
TC-4   Provider 故障端点
TC-5   Gateway 限流
TC-6   Gateway 基础路由
TC-7   重试治理
TC-8   实例隔离
TC-9   舱壁隔离
TC-10  Consumer 熔断
TC-11  故障注入
CASES
}
require_commands() {
    local missing=()
    local command_name
    for command_name in curl python3 grep head mktemp; do
        command -v "$command_name" >/dev/null 2>&1 || missing+=("$command_name")
    done
    if (( ${#missing[@]} > 0 )); then
        echo -e "${RED}❌ 缺少宿主机命令: ${missing[*]}${NC}" >&2
        exit 2
    fi
}
json_field() {
    local field=$1
    python3 -c "import json,sys; print(json.load(sys.stdin).get('$field', ''))" 2>/dev/null
}

wait_for_service() {
    local url=$1
    local name=$2
    local max_wait=${3:-30}
    local count=0
    while ! curl -sfm 2 "$url" > /dev/null 2>&1; do
        sleep 1
        ((count++))
        if ((count >= max_wait)); then
            echo -e "${RED}❌ ${name} 启动超时 (${max_wait}s)${NC}"
            return 1
        fi
    done
    return 0
}

wait_for_gateway_route_rule() {
    local url="${HTTP_BASE}:8080/api/products/version"
    local max_wait=${1:-90}
    local required_stable=5
    local stable=0
    local count=0
    local response

    while (( count < max_wait )); do
        response=$(curl -sfm 2 "$url" 2>/dev/null || true)
        if [[ "$response" == *'"version":"1.0.0"'* ]]; then
            ((stable++)) || true
            if (( stable >= required_stable )); then
                return 0
            fi
        else
            stable=0
        fi
        sleep 1
        ((count++)) || true
    done

    echo -e "${RED}❌ Gateway 默认路由规则未在 ${max_wait}s 内稳定生效${NC}"
    return 1
}

wait_for_test_services() {
    local tc_filter=${1:-all}
    local checks=()
    case "$tc_filter" in
        all) checks=(
            "${HTTP_BASE}:8081/api/products/1|product-service"
            "${HTTP_BASE}:8082/api/orders/products|order-service"
            "${HTTP_BASE}:8080/api/products/1|gateway"
            "${HTTP_BASE}:8091/no-retry|retry-consumer"
            "${HTTP_BASE}:8093/test-minimum-calls|isolation-consumer"
            "${HTTP_BASE}:8095/bulk-rate-limiting|bulkhead-consumer"
            "${HTTP_BASE}:8097/api/consumer/circuit/error50|circuit-breaker-consumer"
            "${HTTP_BASE}:8102/api/fault-test/preview|fault-injection-consumer"
        ) ;;
        1|2) checks=("${HTTP_BASE}:8082/api/orders/products|order-service") ;;
        3|5|6) checks=("${HTTP_BASE}:8080/api/products/1|gateway") ;;
        4) checks=("${HTTP_BASE}:8081/api/products/1|product-service") ;;
        7) checks=("${HTTP_BASE}:8091/no-retry|retry-consumer") ;;
        8) checks=("${HTTP_BASE}:8093/test-minimum-calls|isolation-consumer") ;;
        9) checks=("${HTTP_BASE}:8095/bulk-rate-limiting|bulkhead-consumer") ;;
        10) checks=("${HTTP_BASE}:8097/api/consumer/circuit/error50|circuit-breaker-consumer") ;;
        11) checks=("${HTTP_BASE}:8102/api/fault-test/preview|fault-injection-consumer") ;;
    esac

    info "等待用例所需服务就绪"
    local check url name
    for check in "${checks[@]}"; do
        url=${check%%|*}
        name=${check##*|}
        if ! wait_for_service "$url" "$name" 90; then
            exit 1
        fi
    done

    if [[ "$tc_filter" == "all" || "$tc_filter" == "3" ]]; then
        info "等待 Gateway 默认路由规则稳定生效"
        if ! wait_for_gateway_route_rule 90; then
            exit 1
        fi
    fi
}

#------------------------------------------------------------------------------
# TC-1: 基础设施
#------------------------------------------------------------------------------
run_tc1() {
    echo ""
    echo "=========================================="
    echo "TC-1: 基础设施和注册发现"
    echo "=========================================="

    # TC-1.1: SC 健康
    info "TC-1.1: ServiceComb SC 健康检查"
    if curl -sfm 5 "${HTTP_BASE}:30100/health" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('instances',[])[0].get('status','N/A'))" 2>/dev/null | grep -q "UP"; then
        pass "SC /health 返回 UP"
    else
        fail "SC /health 未返回 UP"
    fi

    # TC-1.1: KIE 健康
    info "TC-1.1: ServiceComb KIE 健康检查"
    if curl -sfm 5 "${HTTP_BASE}:30110/v1/health" | head -c 50 | grep -q "."; then
        pass "KIE /v1/health 可访问"
    else
        fail "KIE /v1/health 不可访问"
    fi

    # TC-1.2: 服务注册（通过业务调用验证）
    info "TC-1.2: 微服务注册验证（通过 Feign 调用）"
    if curl -sfm 5 "${HTTP_BASE}:8082/api/orders/products" 2>/dev/null | grep -q "product-service"; then
        pass "order-service 可调用 product-service（已注册）"
    else
        fail "order-service 无法调用 product-service"
    fi
}

#------------------------------------------------------------------------------
# TC-2: 服务间调用
#------------------------------------------------------------------------------
run_tc2() {
    echo ""
    echo "=========================================="
    echo "TC-2: 服务间调用"
    echo "=========================================="

    # TC-2.1
    info "TC-2.1: Feign 调用 product-service"
    local resp=$(curl -sfm 5 "${HTTP_BASE}:8082/api/orders/products" 2>/dev/null)
    if echo "$resp" | grep -q "product-service" && echo "$resp" | grep -q "products"; then
        pass "GET /api/orders/products 返回商品列表"
    else
        fail "GET /api/orders/products 未返回预期数据"
    fi

    # TC-2.2
    info "TC-2.2: 创建订单"
    local resp=$(curl -sfm 5 "${HTTP_BASE}:8082/api/orders?productId=1" 2>/dev/null)
    if echo "$resp" | grep -q "orderId" && echo "$resp" | grep -q "amount"; then
        pass "GET /api/orders 返回订单信息"
    else
        fail "GET /api/orders 未返回订单信息"
    fi
}

#------------------------------------------------------------------------------
# TC-3: ServiceComb 灰度发布
# 支持的灰度 Header：X-Gray-Tag: gray
#
# 路由规则（precedence 数字越大优先级越高）：
#   precedence=2: X-Gray-Tag:gray → 30% v1(30) + 70% v2(70)
#   precedence=1: (match: {}) → v1 (默认回退)
# ⚠️ 无 match 字段会被当作最高优先级！默认规则必须用 match: {}
#------------------------------------------------------------------------------
run_tc3() {
    echo ""
    echo "=========================================="
    echo "TC-3: 灰度发布"
    echo "=========================================="

    local GATEWAY=${HTTP_BASE}:8080
    local PROD_VERSION="$GATEWAY/api/products/version"

    # TC-3.1: 无灰度标签 → 默认路由 v1
    info "TC-3.1: 无灰度标签（默认 → v1.0.0）"
    local v=$(curl -sfm 5 "$PROD_VERSION" 2>/dev/null | grep -o '"version":"[^"]*"')
    if [[ "$v" == *"1.0.0"* ]]; then
        pass "TC-3.1: 无灰度标签 → v1.0.0"
    else
        fail "TC-3.1: 无灰度标签未路由到 v1.0.0（当前: $v）"
    fi

    # TC-3.2: X-Gray-Tag:gray → 30% v1 + 70% v2（权重路由）
    info "TC-3.2: X-Gray-Tag:gray 权重路由（30% v1 + 70% v2）"
    local gv1=0 gv2=0
    for i in {1..100}; do
        v=$(curl -sfm 3 -H "X-Gray-Tag: gray" "$PROD_VERSION" 2>/dev/null | grep -o '"version":"[^"]*"')
        if [[ "$v" == *"1.0.0"* ]]; then ((gv1++)) || true; fi
        if [[ "$v" == *"2.0.0"* ]]; then ((gv2++)) || true; fi
        sleep 0.1
    done
    if (( gv1 >= 20 && gv1 <= 40 && gv2 >= 60 && gv2 <= 80 )); then
        pass "TC-3.2: gray 权重路由（v1=${gv1}, v2=${gv2}）"
    else
        fail "TC-3.2: gray 权重路由偏离 30/70 容差范围（v1=${gv1}, v2=${gv2}）"
    fi

    # TC-3.3: 全链路灰度（Gateway → order-service → product-service）
    info "TC-3.3: 全链路灰度（Gateway → order-service → product-service）"
    local chain=$(curl -sfm 8 -H "X-Gray-Tag: gray" \
        "${HTTP_BASE}:8080/api/orders/version" 2>/dev/null | grep -o '"version":"[^"]*"' | head -1)
    if [[ "$chain" == *"2.0.0"* ]]; then
        pass "TC-3.3: 全链路灰度（order→product 调用路由到 v2.0.0）"
    else
        fail "TC-3.3: 全链路灰度未路由到 v2.0.0（结果: ${chain:-empty}）"
    fi

    # TC-3.4: 多版本共存（无标签走 v1，gray 走 30/70）
    info "TC-3.4: 多版本共存验证"
    sleep 1
    local pv1=$(curl -sfm 5 "$PROD_VERSION" 2>/dev/null)
    if [[ "$pv1" == *"1.0.0"* ]]; then
        pass "TC-3.4: 多版本共存（v1.0.0 + v2.0.0 均可访问）"
    else
        fail "TC-3.4: 多版本共存验证失败"
    fi

    # TC-3.5: 诊断接口与实际灰度规则一致
    info "TC-3.5: 灰度诊断接口"
    local diagnose=$(curl -sfm 5 -H "X-Gray-Tag: gray" \
        "${HTTP_BASE}:8080/gateway/gray-diagnose" 2>/dev/null)
    if [[ "$diagnose" == *'"matchesGrayRule":true'* ]] && \
       [[ "$diagnose" == *'"X-Gray-Tag":"gray"'* ]]; then
        pass "TC-3.5: 诊断接口识别 X-Gray-Tag=gray"
    else
        fail "TC-3.5: 诊断接口未反映当前灰度规则"
    fi
}

#------------------------------------------------------------------------------
# TC-4: Provider 故障端点
#------------------------------------------------------------------------------
run_tc4() {
    echo ""
    echo "=========================================="
    echo "TC-4: Provider 故障端点"
    echo "=========================================="

    info "TC-4.1: Provider 错误端点验证（HTTP 502 模式）"
    local err=0 ok=0
    for i in {1..6}; do
        local code=$(curl -sfm 3 "${HTTP_BASE}:8081/api/products/test/error" -w "%{http_code}" -o /dev/null 2>/dev/null)
        if [[ "$code" == "502" ]]; then ((err++)) || true; fi
        if [[ "$code" == "200" ]]; then ((ok++)) || true; fi
    done
    if (( err >= 2 && ok >= 2 )); then
        pass "Provider error 端点: ${ok}次200 + ${err}次502（50%错误率 HTTP 级）"
    else
        fail "Provider error 端点异常（200=${ok}, 502=${err}）"
    fi
    info "TC-4.1 说明: 该端点用于触发 Consumer 侧熔断（见 TC-10）"
}

#------------------------------------------------------------------------------
# TC-5: 限流
#------------------------------------------------------------------------------
run_tc5() {
    echo ""
    echo "=========================================="
    echo "TC-5: 限流"
    echo "=========================================="

    # 限流触发条件：1秒内超过 rate=20 的阈值
    info "TC-5.1: 网关限流（product-api rate=20，突发30次）"
    local tmp=$(mktemp)
    for i in {1..30}; do
        curl -s -o /dev/null -w "%{http_code}\n" "${HTTP_BASE}:8080/api/products/version" >> "$tmp" &
    done
    wait
    local ok=$(grep -c "^200$" "$tmp" 2>/dev/null || echo 0)
    local rate_limited=$(grep -c "^429$" "$tmp" 2>/dev/null || echo 0)
    rm -f "$tmp"
    if (( rate_limited >= 3 )); then
        pass "网关限流: ${ok}个200 + ${rate_limited}个429（限流生效）"
    else
        fail "网关限流未触发（ok=${ok}, 429=${rate_limited}）"
    fi
}

#------------------------------------------------------------------------------
# TC-6: 网关路由
#------------------------------------------------------------------------------
run_tc6() {
    echo ""
    echo "=========================================="
    echo "TC-6: 网关路由"
    echo "=========================================="

    info "TC-6.1: 网关 → product-service"
    local retry=0
    while ((retry < 3)); do
        if curl -sfm 8 "${HTTP_BASE}:8080/api/products/1" 2>/dev/null | grep -q "product"; then
            pass "GET /api/products/1 返回 product 数据"
            break
        fi
        ((retry++)) || true
        sleep 2
    done
    if ((retry >= 3)); then
        fail "GET /api/products/1 未返回 product 数据（重试3次均失败）"
    fi

    info "TC-6.2: 网关 → order-service"
    local retry=0
    while ((retry < 3)); do
        if curl -sfm 8 "${HTTP_BASE}:8080/api/orders/products" 2>/dev/null | grep -q "product"; then
            pass "GET /api/orders/products 返回数据"
            break
        fi
        ((retry++)) || true
        sleep 2
    done
    if ((retry >= 3)); then
        fail "GET /api/orders/products 未返回数据（重试3次均失败）"
    fi
}

#------------------------------------------------------------------------------
# TC-7: 重试治理
#------------------------------------------------------------------------------
run_tc7() {
    echo ""
    echo "=========================================="
    echo "TC-7: 重试治理"
    echo "=========================================="

    info "TC-7.1: 无重试策略"
    local resp=$(curl -sfm 5 "${HTTP_BASE}:8091/no-retry" 2>/dev/null)
    if echo "$resp" | grep -q "请求结果"; then
        pass "GET /no-retry 返回治理结果"
    else
        fail "GET /no-retry 无响应"
    fi

    info "TC-7.2: 重试策略（provider 返回 503，ServiceComb 重试生效）"
    local resp=$(curl -sfm 30 "${HTTP_BASE}:8091/retry" 2>/dev/null)
    if echo "$resp" | grep -q "符合策略要求"; then
        pass "GET /retry 返回符合策略要求（ServiceComb 重试生效）"
    else
        fail "GET /retry 返回不符合策略要求（resp=$resp）"
    fi
}

#------------------------------------------------------------------------------
# TC-8: 实例隔离
#------------------------------------------------------------------------------
run_tc8() {
    echo ""
    echo "=========================================="
    echo "TC-8: 实例隔离"
    echo "=========================================="

    info "TC-8.1: 实例隔离触发"
    local resp=$(curl -sfm 15 "${HTTP_BASE}:8093/test-minimum-calls" 2>/dev/null)
    if echo "$resp" | grep -q "符合策略要求"; then
        pass "GET /test-minimum-calls 返回符合策略要求"
    else
        fail "GET /test-minimum-calls 未返回预期结果"
    fi
}

#------------------------------------------------------------------------------
# TC-9: 舱壁隔离
#------------------------------------------------------------------------------
run_tc9() {
    echo ""
    echo "=========================================="
    echo "TC-9: 舱壁隔离"
    echo "=========================================="

    info "TC-9.1: 舱壁并发限制（maxConcurrentCalls=2, 10并发）"
    local resp=$(curl -sfm 20 "${HTTP_BASE}:8095/bulk-rate-limiting" 2>/dev/null)
    local requested=$(echo "$resp" | json_field requested)
    local success=$(echo "$resp" | json_field success)
    local rejected=$(echo "$resp" | json_field rejected)
    if [[ "$requested" == "10" ]] && (( success <= 2 && rejected >= 8 )); then
        pass "舱壁真实并发限制: requested=${requested}, success=${success}, rejected=${rejected}"
    else
        fail "舱壁并发断言失败（resp=${resp:-empty}）"
    fi
}

#------------------------------------------------------------------------------
# TC-10: Consumer 熔断
#------------------------------------------------------------------------------
run_tc10() {
    echo ""
    echo "=========================================="
    echo "TC-10: Consumer 熔断"
    echo "=========================================="

    info "TC-10.1: Consumer 熔断打开后停止调用 Provider"
    local provider_url="${CIRCUIT_PROVIDER_URL:-${HTTP_BASE}:8096}"
    local consumer_url="${CIRCUIT_CONSUMER_URL:-${HTTP_BASE}:8097}"
    curl -sfm 5 -X POST "${provider_url}/api/circuit/reset" >/dev/null 2>&1 || true
    for i in {1..12}; do
        curl -sm 8 -o /dev/null "${consumer_url}/api/consumer/circuit/provider-cb" 2>/dev/null || true
    done
    local provider_stats=$(curl -sfm 5 "${provider_url}/api/circuit/stats" 2>/dev/null)
    local provider_calls=$(echo "$provider_stats" | json_field error100)
    if [[ "$provider_calls" =~ ^[0-9]+$ ]] && (( provider_calls < 12 )); then
        pass "Consumer 熔断已生效: Provider实际调用=${provider_calls}/12，其余请求被拦截"
    else
        fail "未证明 Consumer 熔断阻止下游调用（Provider调用=${provider_calls:-unknown}/12）"
    fi
}

#------------------------------------------------------------------------------
# TC-11: 故障注入
#------------------------------------------------------------------------------
run_tc11() {
    echo ""
    echo "=========================================="
    echo "TC-11: 故障注入"
    echo "=========================================="

    info "TC-11.1: KIE 故障注入配置已加载"
    local preview=$(curl -sfm 8 "${HTTP_BASE}:8102/api/fault-test/preview" 2>/dev/null)
    if echo "$preview" | grep -q "callNormalOperation" &&
       echo "$preview" | grep -q "percentage: 50"; then
        pass "fault-injection-consumer 已加载 matchGroup 和 50% 故障注入规则"
    else
        fail "fault-injection-consumer 未加载预期的 KIE 故障注入规则"
    fi

    curl -sfm 5 "${HTTP_BASE}:8100/api/reset" >/dev/null 2>&1 || true
    curl -sfm 5 "${HTTP_BASE}:8101/api/reset" >/dev/null 2>&1 || true

    info "TC-11.2: 50%/100% 故障注入统计及 Provider 调用计数"
    local stats=$(curl -sfm 30 "${HTTP_BASE}:8102/api/fault-test/stats/50" 2>/dev/null)
    local normal_ok=$(echo "$stats" | sed -n 's/.*50% 注入预期): ok=\([0-9][0-9]*\).*/\1/p')
    local normal_fail=$(echo "$stats" | sed -n 's/.*50% 注入预期): ok=[0-9][0-9]* fail=\([0-9][0-9]*\).*/\1/p')
    local error_ok=$(echo "$stats" | sed -n 's/.*100% 注入预期): ok=\([0-9][0-9]*\).*/\1/p')
    local error_fail=$(echo "$stats" | sed -n 's/.*100% 注入预期): ok=[0-9][0-9]* fail=\([0-9][0-9]*\).*/\1/p')
    local normal_provider_stats=$(curl -sfm 5 "${HTTP_BASE}:8100/api/stats" 2>/dev/null)
    local error_provider_stats=$(curl -sfm 5 "${HTTP_BASE}:8101/api/stats" 2>/dev/null)
    local normal_provider_calls=$(echo "$normal_provider_stats" | json_field calls)
    local error_provider_calls=$(echo "$error_provider_stats" | json_field calls)

    if [[ -n "$normal_ok" && -n "$normal_fail" ]] &&
       (( normal_ok > 0 && normal_fail > 0 && normal_ok + normal_fail == 50 &&
          normal_provider_calls == normal_ok )); then
        pass "50% 注入已阻止下游调用: ok=${normal_ok}, injected=${normal_fail}, Provider调用=${normal_provider_calls}"
    else
        fail "正常 Provider 调用计数与注入结果不符（stats=${stats:-empty}, calls=${normal_provider_calls:-unknown}）"
    fi

    if [[ "$error_ok" == "0" && "$error_fail" == "50" && "$error_provider_calls" == "0" ]]; then
        pass "100% 注入已阻止全部下游调用: Provider调用=0/50"
    else
        fail "100% 注入未阻止下游调用（stats=${stats:-empty}, calls=${error_provider_calls:-unknown}）"
    fi
}

#------------------------------------------------------------------------------
# 主流程
#------------------------------------------------------------------------------
main() {
    local run_all=true
    local tc_filter=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            --tc)
                if (( $# < 2 )); then
                    echo "错误: --tc 缺少用例编号" >&2
                    usage >&2
                    exit 2
                fi
                if [[ ! "$2" =~ ^([1-9]|10|11)$ ]]; then
                    echo "错误: 未知测试用例 $2（支持 1-11）" >&2
                    usage >&2
                    exit 2
                fi
                run_all=false
                tc_filter="$2"
                shift 2
                ;;
            --help|-h)
                usage
                exit 0
                ;;
            --list)
                list_cases
                exit 0
                ;;
            *)
                echo "错误: 未知参数 $1" >&2
                usage >&2
                exit 2
                ;;
        esac
    done

    require_commands
    echo "=========================================="
    echo "  Spring Cloud Huawei ServiceComb Demo"
    echo "  测试验证脚本"
    echo "=========================================="

    if $run_all; then
        wait_for_test_services all
    else
        wait_for_test_services "$tc_filter"
    fi

    if $run_all; then
        run_tc1
        run_tc2
        run_tc3
        run_tc4
        run_tc5
        run_tc6
        run_tc7
        run_tc8
        run_tc9
        run_tc10
        run_tc11
    else
        case $tc_filter in
            1)  run_tc1 ;;
            2)  run_tc2 ;;
            3)  run_tc3 ;;
            4)  run_tc4 ;;
            5)  run_tc5 ;;
            6)  run_tc6 ;;
            7)  run_tc7 ;;
            8)  run_tc8 ;;
            9)  run_tc9 ;;
            10) run_tc10 ;;
            11) run_tc11 ;;
        esac
    fi

    echo ""
    echo "=========================================="
    echo "  测试结果汇总"
    echo "=========================================="
    echo -e "  ${GREEN}✅ PASS: $PASS${NC}"
    echo -e "  ${RED}❌ FAIL: $FAIL${NC}"
    echo -e "  ${YELLOW}⚠️  WARN: $((TOTAL - PASS - FAIL))${NC}"
    echo -e "  总计: $TOTAL"
    echo "=========================================="

    if (( FAIL > 0 )); then
        exit 1
    fi
}

main "$@"
