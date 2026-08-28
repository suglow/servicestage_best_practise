#!/bin/sh
# ============================================================
# KIE 初始化脚本 — 推送灰度路由规则 + 配置演示
# 等待 KIE 就绪 → 清空旧配置 → 推送各类规则
# ============================================================

KIE_URL="${KIE_URL:-http://local-cse:30110}"
APPLICATION_NAME="${CAS_APPLICATION_NAME:-demo-application}"
MAX_RETRY=30
RETRY_INTERVAL=2
LABELS_JSON="{\"app\":\"${APPLICATION_NAME}\",\"service\":\"kie-config-demo\",\"environment\":\"\"}"
GATEWAY_LABELS_JSON="{\"app\":\"${APPLICATION_NAME}\",\"service\":\"gateway\",\"environment\":\"\"}"
FAULT_LABELS_JSON="{\"app\":\"${APPLICATION_NAME}\",\"service\":\"fault-injection-consumer\",\"environment\":\"\"}"
READY=0

echo "[kie-init] 等待 KIE 就绪: $KIE_URL/v1/health ..."
for i in $(seq 1 $MAX_RETRY); do
    if curl -sf "$KIE_URL/v1/health" > /dev/null 2>&1; then
        echo "[kie-init] KIE 已就绪"
        READY=1
        break
    fi
    echo "[kie-init] 等待中... ($i/$MAX_RETRY)"
    sleep $RETRY_INTERVAL
done

if [ "$READY" -ne 1 ]; then
    echo "[kie-init] ERROR: KIE 未在超时时间内就绪"
    exit 1
fi

# ============================================================
# 清空已有规则（幂等）
# ============================================================
echo "[kie-init] 清空已有配置 ..."
EXISTING=$(curl -sf "$KIE_URL/v1/default/kie/kv" 2>/dev/null)
IDS_FILE=$(mktemp)
trap 'rm -f "$IDS_FILE"' EXIT
if echo "$EXISTING" | python3 -c "
import sys,json
ids = [i['id'] for i in json.load(sys.stdin)['data']]
for id in ids: print(id)
" 2>/dev/null > "$IDS_FILE"; then
    DELETE_FAILED=0
    while read id; do
        if [ -n "$id" ] && ! curl -sf -X DELETE "$KIE_URL/v1/default/kie/kv/$id" > /dev/null; then
            echo "[kie-init] ERROR: 删除配置失败 id=$id" >&2
            DELETE_FAILED=1
        fi
    done < "$IDS_FILE"
    if [ "$DELETE_FAILED" -ne 0 ]; then
        exit 1
    fi
    echo "[kie-init] 已清空 $(wc -l < "$IDS_FILE" | tr -d ' ') 条配置"
else
    echo "[kie-init] KIE 为空，跳过清空"
fi

python3 -c "
import json, subprocess

KIE = '$KIE_URL/v1/default/kie/kv'
push_ok = True

def push(key, value, value_type, labels_str='$LABELS_JSON', desc=''):
    global push_ok
    labels = json.loads(labels_str)
    body = json.dumps({
        'key': key,
        'value': value,
        'labels': labels,
        'value_type': value_type,
        'status': 'enabled'
    })
    r = subprocess.run(['curl', '-sf', '-X', 'POST', KIE,
        '-H', 'Content-Type: application/json', '-d', body],
        capture_output=True, text=True)
    ok = r.returncode == 0
    if not ok:
        push_ok = False
    print(f\"[kie-init] {'✅' if ok else '❌'} {desc} (key={key}, type={value_type})\")
    return ok

# ─── ① 灰度路由规则 — governance.yaml (yaml + block scalar)
#       使用 YAML block scalar (|) 嵌入路由规则
#       fileSource 触发 YamlPropertiesFactoryBean 扁平化
#       servicecomb.routeRule.product-service 保持为完整 String，避免规则误作用于 order-service
push('governance.yaml', '''servicecomb:
  routeRule:
    product-service: |
      - precedence: 2
        match:
          headers:
            X-Gray-Tag:
              exact: gray
        route:
          - weight: 30
            tags:
              version: 1.0.0
          - weight: 70
            tags:
              version: 2.0.0
      - precedence: 1
        match: {}
        route:
          - weight: 100
            tags:
              version: 1.0.0''',
     'yaml', '$GATEWAY_LABELS_JSON', 'governance.yaml (yaml + block scalar 路由)')

# ─── ② 故障注入规则 — fault-injection-consumer ───
push('governance-fault.yaml', '''servicecomb:
  matchGroup:
    callNormalOperation: |
      matches:
        - apiPath:
            exact: /api/call
          serviceName: fault-injection-normal-provider
    callErrorOperation: |
      matches:
        - apiPath:
            exact: /api/error
          serviceName: fault-injection-error-provider
  faultInjection:
    callNormalOperation: |
      type: abort
      percentage: 50
      fallbackType: ThrowException
      forceClosed: false
    callErrorOperation: |
      type: abort
      percentage: 100
      fallbackType: ThrowException
      forceClosed: false''',
     'yaml', '$FAULT_LABELS_JSON', 'governance-fault.yaml (故障注入)')

# ─── ③ 简单配置 — text 类型 ───
push('demo.conf.text-message', 'hello-from-kie-text-type',
     'text', desc='text 类型 (简单)')

# ─── ④ 简单配置 — yaml 类型（演示差异：Map 无法读取） ───
push('demo.conf.yaml-message', 'message: hello-from-kie-yaml-type',
     'yaml', desc='yaml 类型 (简单, 演示Map差异)')

# ─── ⑤ 简单配置 — properties 类型 ───
push('demo.conf.props-message', 'demo.conf.props-message=hello-from-kie-props-type',
     'properties', desc='properties 类型 (简单)')

# ─── ⑥ 多属性业务配置 — properties 类型（真实项目推荐） ───
push('app.business.config', '''app.tenant-id=tenant-001
app.notification.email.enabled=true
app.notification.email.host=smtp.company.com
app.notification.email.port=587
app.notification.email.username=noreply@company.com
app.notification.email.from-address=noreply@company.com
app.notification.sms.enabled=true
app.notification.sms.provider=aliyun
app.notification.sms.api-key=replace-with-your-api-key
app.notification.sms.region=cn-shanghai
app.notification.webhook.enabled=false
app.notification.webhook.url=https://hooks.company.com/events
app.notification.webhook.secret=replace-with-your-webhook-secret
app.notification.webhook.retry-count=5
app.limit.qps=500
app.limit.burst-size=100
app.limit.timeout-ms=5000
app.feature.gray-release-enabled=true
app.feature.new-ui-enabled=false
app.feature.dark-mode-enabled=true''',
     'properties', desc='业务配置 19属性 (properties)')

if not push_ok:
    raise SystemExit('[kie-init] ERROR: 至少一条配置推送失败')
"

# ============================================================
# 验证
# ============================================================
echo ""
echo "[kie-init] 验证 KIE 数据..."
curl -sf "$KIE_URL/v1/default/kie/kv" | python3 -c '
import sys, json
data = json.load(sys.stdin)
print("[kie-init] KIE total={}".format(data["total"]))
for item in data["data"]:
    print("[kie-init]   {:>10}  key={}".format(item["value_type"], item["key"]))
'

echo ""
echo "[kie-init] ✅ 完成"
