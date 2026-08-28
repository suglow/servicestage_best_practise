#!/usr/bin/env bash
set -euo pipefail

umask 027

current_dir=$(cd "$(dirname "$0")" && pwd)
cd "$current_dir"

mkdir -p "$current_dir/log" "$current_dir/data"

echo "Starting Local CSE (Service Center :30100, Config Center :30110, dashboard :30103)..."
exec "$current_dir/cse" >> "$current_dir/log/startup.log" 2>&1
