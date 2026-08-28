#!/usr/bin/env bash
set -e

CURRENT_DIR=$(cd $(dirname $0); pwd)

echo "Stopping ServiceComb CSE..."
pkill -f "${CURRENT_DIR}/cse" || true

echo "ServiceComb CSE stopped."
