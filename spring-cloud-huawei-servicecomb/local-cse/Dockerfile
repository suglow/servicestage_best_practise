# ====================================================
# ServiceComb Local CSE Docker
# 包含 Service Center (:30100) + Config Center (:30110)
# 基于华为 CSE 2.1.8 官方包
# ====================================================
FROM ubuntu:22.04

LABEL maintainer="demo"
LABEL description="ServiceComb Local CSE - Service Center + Config Center"

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --shell /bin/bash cse

WORKDIR /local-cse

# 复制 CSE zip 包并解压（压缩包内容直接位于根层级）
COPY Local-CSE-2.1.8-linux-amd64.zip .
RUN unzip Local-CSE-2.1.8-linux-amd64.zip && \
    rm Local-CSE-2.1.8-linux-amd64.zip

# 复制修正后的启动脚本（覆盖 zip 包内的原始脚本）
COPY start.sh .
COPY stop.sh .

# 修正所有文件权限
RUN chown -R cse:cse /local-cse && \
    chmod 755 cse start.sh stop.sh && \
    chmod 755 conf/

# ServiceComb SC:30100, KIE:30110, Frontend:30103
EXPOSE 30100 30110 30103

HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=15s \
    CMD curl -f http://localhost:30100/health || exit 1

USER cse
ENTRYPOINT ["./start.sh"]
