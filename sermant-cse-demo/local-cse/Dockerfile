FROM ubuntu:22.04

LABEL description="ServiceComb Local CSE - Service Center and Config Center"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --shell /bin/bash cse

WORKDIR /local-cse
COPY Local-CSE-2.1.8-linux-amd64.zip .
RUN unzip Local-CSE-2.1.8-linux-amd64.zip \
    && rm Local-CSE-2.1.8-linux-amd64.zip \
    && chown -R cse:cse /local-cse

COPY start.sh stop.sh ./
RUN chown cse:cse start.sh stop.sh && chmod 755 start.sh stop.sh cse conf/

EXPOSE 30100 30110 30103
HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=15s \
    CMD curl -fsS http://localhost:30100/health || exit 1
USER cse
ENTRYPOINT ["./start.sh"]
