# ====================================================
# KIE Init — 推送灰度路由规则到 KIE Config Center
# 基于 Alpine + curl + python3
# ====================================================
FROM alpine:3.20

RUN apk add --no-cache curl python3

WORKDIR /init
COPY init-route-rules.sh .

ENTRYPOINT ["/bin/sh", "/init/init-route-rules.sh"]
