#!/usr/bin/env bash
set -euo pipefail

CURRENT_DIR=$(cd "$(dirname "$0")" && pwd)
pkill -f "$CURRENT_DIR/cse" || true
echo "ServiceComb CSE stopped."
