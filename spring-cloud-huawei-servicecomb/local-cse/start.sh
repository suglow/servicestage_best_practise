#!/usr/bin/env bash
set -e

umask 027

CURRENT_DIR=$(cd $(dirname $0); pwd)
cd ${CURRENT_DIR}

mkdir -p ${CURRENT_DIR}/log
mkdir -p ${CURRENT_DIR}/data

log_file=${CURRENT_DIR}/log/startup.log

echo "Starting ServiceComb CSE..."
echo "SC (Service Center):  http://localhost:30100"
echo "KIE (Config Center):  http://localhost:30110"
echo "Frontend Dashboard:    http://localhost:30103"

exec ${CURRENT_DIR}/cse >> ${log_file} 2>&1
