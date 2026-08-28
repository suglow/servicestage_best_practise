#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

echo '[1/7] Shell syntax'
bash -n verify.sh check.sh local-cse/start.sh local-cse/stop.sh

echo '[2/7] Compose syntax'
docker compose config --quiet

echo '[3/7] Verification CLI'
bash verify.sh --help >/dev/null
bash verify.sh --list | grep -q '^TC-1'
bash verify.sh --list | grep -q '^TC-3'
if bash verify.sh --tc 9 >/dev/null 2>&1; then
    echo 'verify.sh accepted an unknown test case.' >&2
    exit 1
fi

echo '[4/7] Maven tests'
MAVEN_ARGS=(-B)
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then MAVEN_ARGS+=("-Dmaven.repo.local=${MAVEN_REPO_LOCAL}"); fi
mvn "${MAVEN_ARGS[@]}" test

echo '[5/7] Documentation links'
while IFS= read -r document; do
    while IFS= read -r target; do
        case "$target" in http://*|https://*|mailto:*|'#'*) continue ;; esac
        target=${target%%#*}
        [[ -z "$target" ]] || test -e "$(dirname "$document")/$target"
    done < <(sed -n 's/.*](\([^)]*\)).*/\1/p' "$document")
done < <(find . -name '*.md' -not -path './.git/*' -print)

echo '[6/7] Scope and path hygiene'
if rg -n '/home/suglow|sermant-cse-demo/docker-compose|(^|/)(sch-consumer|sermant-product)(/|$)' \
    --glob '*.md' --glob '*.java' --glob '*.xml' --glob '*.yml' --glob '*.yaml' --glob '*.sh' --glob '!check.sh'; then
    echo 'Found stale source paths or module names.' >&2
    exit 1
fi

echo '[7/7] Repository hygiene'
# Maven is expected to create module target/ directories during this check.
# Build artifacts are forbidden everywhere else in the source tree.
test -z "$(find . -type f \( -name '*.class' -o -name '*.log' \) -not -path '*/target/*' -print)"
echo 'All checks passed.'
