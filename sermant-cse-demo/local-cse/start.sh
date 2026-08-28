#!/usr/bin/env bash
set -euo pipefail

umask 027
CURRENT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$CURRENT_DIR"
mkdir -p log data

echo "Starting ServiceComb CSE..."
echo "SC (Service Center):  http://localhost:30100"
echo "KIE (Config Center):  http://localhost:30110"
echo "Frontend Dashboard:    http://localhost:30103"
exec "$CURRENT_DIR/cse" >> "$CURRENT_DIR/log/startup.log" 2>&1
