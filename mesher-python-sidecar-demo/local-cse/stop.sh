#!/usr/bin/env bash
set -euo pipefail

current_dir=$(cd "$(dirname "$0")" && pwd)
pkill -f "$current_dir/cse" || true
