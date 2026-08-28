#!/bin/sh
set -eu

APP_PORT=${APP_PORT:-9000}
MESHER_CONF_DIR=/opt/mesher/conf

cp /app/chassis.yaml /tmp/chassis.yaml
cat > /tmp/microservice.yaml <<EOF
APPLICATION_ID: ${APP_ID:-demo-application}
service_description:
  name: ${SERVICE_NAME:-python-app}
  version: ${VERSION:-1.0.0}
  properties:
    allowCrossApp: true
EOF

cp -f /tmp/chassis.yaml "$MESHER_CONF_DIR/chassis.yaml"
cp -f /tmp/microservice.yaml "$MESHER_CONF_DIR/microservice.yaml"

net_name=$(ip -o -4 route show to default | awk '{print $5}')
listen_addr=$(ip -o -4 addr show "$net_name" 2>/dev/null | grep -oE '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' | head -n 1 || true)
if [ -z "$listen_addr" ]; then
    listen_addr=$(hostname -i 2>/dev/null | awk '{print $1}' || true)
fi
if [ -z "$listen_addr" ]; then
    listen_addr=127.0.0.1
fi
sed -i "s/listenAddress: [0-9.]*/listenAddress: $listen_addr/g" "$MESHER_CONF_DIR/chassis.yaml"

echo "Starting Mesher for ${SERVICE_NAME:-python-app} on ${listen_addr}..."
/opt/mesher/mesher \
    --config=/etc/mesher/conf/mesher.yaml \
    --service-ports="rest:$APP_PORT" &
MESHER_PID=$!

attempt=0
while [ "$attempt" -lt 30 ]; do
    if ! kill -0 "$MESHER_PID" 2>/dev/null; then
        echo "Mesher exited before its proxy port became ready." >&2
        wait "$MESHER_PID"
        exit 1
    fi
    if python -c "import socket; connection=socket.create_connection(('127.0.0.1', 30101), 1); connection.close()" >/dev/null 2>&1; then
        break
    fi
    attempt=$((attempt + 1))
    sleep 1
done

if [ "$attempt" -ge 30 ]; then
    echo "Timed out waiting for Mesher proxy port 30101." >&2
    kill "$MESHER_PID"
    exit 1
fi

export HTTP_PROXY=http://127.0.0.1:30101
export http_proxy=http://127.0.0.1:30101
export NO_PROXY=127.0.0.1,localhost,local-cse,service-center
export no_proxy=127.0.0.1,localhost,local-cse,service-center

echo "Mesher is ready; starting Flask on port ${APP_PORT}."
exec python /app/app.py
