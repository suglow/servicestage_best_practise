# ===================================================================
# 两段式构建 Apache ServiceComb Mesher v1.8.1
# 直接从 GitHub clone v1.8.1 源码（不把源码保存在工程里）
# 阶段 1: golang:1.23-alpine clone + 编译 mesher 二进制
# 阶段 2: alpine:3.19 运行时
# ===================================================================

# ============== Stage 1: Builder ==============
FROM golang:1.23-alpine AS builder

# 基础工具
RUN apk add --no-cache git make bash curl python3

WORKDIR /build

# 直接从 GitHub 克隆 v1.8.1 tag 源码（commit f35f82e）
ARG MESHER_VERSION=v1.8.1
ARG MESHER_COMMIT=f35f82e
RUN git clone --depth 1 --branch ${MESHER_VERSION} \
        https://github.com/apache/servicecomb-mesher.git /build/mesher \
    && test "$(git -C /build/mesher rev-parse --short HEAD)" = "${MESHER_COMMIT}"

# 应用 patch：start.sh 用 ip 命令替代 ifconfig（alpine-glibc 无 ifconfig）
# 用 Python 脚本做替换（比 sed 更可靠）
WORKDIR /build/mesher
RUN python3 << 'PYEOF'
with open('start.sh', 'r') as f:
    content = f.read()

# 替换 ifconfig 行为 ip + ifconfig fallback
old = 'listen_addr=$(ifconfig $net_name | grep -E \'inet\\W\' | grep -o -E [0-9]+.[0-9]+.[0-9]+.[0-9]+ | head -n 1)'
new = '''listen_addr=$(ip -o -4 addr show $net_name 2>/dev/null | grep -oE '[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+' | head -n 1)
if [ -z "$listen_addr" ]; then
  listen_addr=$(ifconfig $net_name 2>/dev/null | grep -E 'inet\\W' | grep -o -E [0-9]+.[0-9]+.[0-9]+.[0-9]+ | head -n 1)
fi'''

if old not in content:
    print('ERROR: pattern not found in start.sh')
    exit(1)

content = content.replace(old, new)
with open('start.sh', 'w') as f:
    f.write(content)
print('start.sh patched successfully')
PYEOF

# 下载依赖
RUN go mod download

# 编译 mesher 二进制（静态链接 + 减小体积）
RUN CGO_ENABLED=0 GOOS=linux go build \
    -ldflags="-s -w" \
    -o /out/mesher \
    ./cmd/mesher

# 复制运行时需要的文件到 /out
RUN mkdir -p /out/conf && \
    cp -r /build/mesher/conf/* /out/conf/ && \
    cp /build/mesher/start.sh /out/ && \
    cp /build/mesher/licenses/LICENSE /out/ && \
    cp /build/mesher/licenses/NOTICE /out/

# ============== Stage 2: Runtime ==============
FROM alpine:3.19

LABEL maintainer="mesher-python-sidecar-demo" \
      description="Apache ServiceComb Mesher v1.8.1 sidecar (two-stage build, source cloned from GitHub)" \
      version="1.8.1"

# 安装必要工具：iproute2（ip 命令）、bash、curl
RUN apk add --no-cache bash curl iproute2

# mesher 工作目录
WORKDIR /opt/mesher

# 从 builder 复制 mesher 二进制 + conf + 启动脚本
COPY --from=builder /out/mesher /opt/mesher/mesher
COPY --from=builder /out/conf/ /opt/mesher/conf/
COPY --from=builder /out/start.sh /opt/mesher/start.sh
COPY --from=builder /out/LICENSE /opt/mesher/LICENSE
COPY --from=builder /out/NOTICE /opt/mesher/NOTICE

# mesher 配置目录
RUN mkdir -p /etc/mesher/conf && \
    cp /opt/mesher/conf/mesher.yaml /etc/mesher/conf/mesher.yaml

# 启动脚本
RUN chmod +x /opt/mesher/start.sh

ENV CHASSIS_HOME=/opt/mesher/

# 入向 + 出向 + admin API
EXPOSE 30101 30102 40101

ENTRYPOINT ["sh", "/opt/mesher/start.sh"]
