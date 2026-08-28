# Flask and Mesher intentionally run in one container to preserve the original demo architecture.
FROM mesher-sidecar-v181:local AS mesher

COPY python-app/chassis.yaml /opt/mesher/conf/chassis.yaml

FROM python:3.11-slim

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends iproute2 ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY --from=mesher /opt/mesher/mesher /opt/mesher/mesher
COPY --from=mesher /opt/mesher/conf /opt/mesher/conf
RUN mkdir -p /etc/mesher/conf \
    && cp /opt/mesher/conf/mesher.yaml /etc/mesher/conf/mesher.yaml

COPY python-app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY python-app/app.py python-app/chassis.yaml python-app/docker-entrypoint.sh ./
RUN chmod 755 /app/docker-entrypoint.sh

EXPOSE 9000 30101 30102

HEALTHCHECK --interval=5s --timeout=3s --retries=10 --start-period=15s \
    CMD python -c "import socket, urllib.request; urllib.request.urlopen('http://127.0.0.1:9000/health', timeout=2).read(); connection=socket.create_connection(('127.0.0.1', 30101), 2); connection.close()" || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
