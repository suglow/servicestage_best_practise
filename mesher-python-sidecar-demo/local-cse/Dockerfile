FROM ubuntu:22.04

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --shell /bin/bash cse

WORKDIR /local-cse

COPY Local-CSE-2.1.8-linux-amd64.zip .
RUN unzip -q Local-CSE-2.1.8-linux-amd64.zip \
    && rm Local-CSE-2.1.8-linux-amd64.zip \
    && test -f /local-cse/cse

COPY start.sh stop.sh ./
RUN chown -R cse:cse /local-cse \
    && chmod 755 cse start.sh stop.sh conf

EXPOSE 30100 30110 30103

HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=15s \
    CMD curl -fsS http://127.0.0.1:30100/health || exit 1

USER cse
ENTRYPOINT ["./start.sh"]
