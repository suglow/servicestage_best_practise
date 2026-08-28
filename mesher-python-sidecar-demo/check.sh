#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

echo '[1/8] Shell syntax'
bash -n check.sh verify.sh python-app/docker-entrypoint.sh local-cse/start.sh local-cse/stop.sh

echo '[2/8] Compose syntax'
docker compose config --quiet

echo '[3/8] Verification CLI'
bash verify.sh --help >/dev/null
bash verify.sh --list | grep -q '^TC-1'
bash verify.sh --list | grep -q '^TC-6'
if bash verify.sh --tc 7 >/dev/null 2>&1; then
    echo 'verify.sh accepted an unknown test case.' >&2
    exit 1
fi

echo '[4/8] Java 17 and Maven tests'
java -version 2>&1 | grep -q 'version "17\.'
MAVEN_ARGS=(-B)
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then MAVEN_ARGS+=("-Dmaven.repo.local=${MAVEN_REPO_LOCAL}"); fi
mvn "${MAVEN_ARGS[@]}" test

echo '[5/8] Python tests'
PYTHON_BIN=${PYTHON_BIN:-python3}
PYTHONPATH="python-app${PYTHONPATH:+:${PYTHONPATH}}" "$PYTHON_BIN" -m unittest discover -s python-app/tests -v

echo '[6/8] Documentation links'
while IFS= read -r document; do
    while IFS= read -r target; do
        case "$target" in http://*|https://*|mailto:*|'#'*) continue ;; esac
        target=${target%%#*}
        [[ -z "$target" ]] || test -e "$(dirname "$document")/$target"
    done < <(sed -n 's/.*](\([^)]*\)).*/\1/p' "$document")
done < <(find . -name '*.md' -not -path './.git/*' -print)

echo '[7/8] Source and dependency integrity'
printf '%s  %s\n' \
    '0f4270cc7b64699e45ae9bc32f43cc6f0cbdfc55dde99732b2767c4e1f6ca3f3' \
    'local-cse/Local-CSE-2.1.8-linux-amd64.zip' | sha256sum -c -
if rg -n '/home/suglow|mesher-source|docker-compose\.yaml|container_name:' \
    --glob '*.md' --glob '*.java' --glob '*.xml' --glob '*.yml' --glob '*.yaml' --glob '*.sh' --glob '!check.sh'; then
    echo 'Found stale source paths or names.' >&2
    exit 1
fi

echo '[8/8] Repository hygiene'
test ! -d .github
test ! -d .vscode
test -z "$(find . -type f \( -name '*.class' -o -name '*.log' \) -not -path '*/target/*' -print)"
echo 'All checks passed.'
