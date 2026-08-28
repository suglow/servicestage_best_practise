#!/bin/bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

echo "[1/8] Shell syntax"
bash -n verify.sh check.sh
sh -n kie-init/init-route-rules.sh local-cse/start.sh local-cse/stop.sh

echo "[2/8] Verification CLI and documentation coverage"
bash verify.sh --help >/dev/null
case_list=$(bash verify.sh --list)
for test_case in TC-1 TC-2 TC-3 TC-4 TC-5 TC-6 TC-7 TC-8 TC-9 TC-10 TC-11; do
  printf '%s\n' "$case_list" | grep -q "^${test_case}[[:space:]]"
  grep -q "| ${test_case} |" docs/TEST_CASES.md
  grep -q "verify.sh --tc ${test_case#TC-}" docs/TUTORIAL_TESTCASE_MATRIX.md
done
if bash verify.sh --tc unknown >/dev/null 2>&1; then
  echo "verify.sh accepted an unknown test case." >&2
  exit 1
fi
if bash verify.sh --tc >/dev/null 2>&1; then
  echo "verify.sh accepted a missing test case value." >&2
  exit 1
fi

echo "[3/8] Compose profiles"
docker compose config --quiet
docker compose --profile config config --quiet
docker compose --profile governance config --quiet
docker compose --profile all config --quiet
test "$(docker compose config --services | wc -l)" -eq 5
test "$(docker compose --profile config config --services | wc -l)" -eq 7
test "$(docker compose --profile governance config --services | wc -l)" -eq 16
test "$(docker compose --profile all config --services | wc -l)" -eq 18

echo "[4/8] Application configuration"
for config in */src/main/resources/bootstrap.yml; do
  grep -q 'PAAS_CSE_SC_ENDPOINT' "$config"
  grep -q 'PAAS_CSE_CC_ENDPOINT' "$config"
done

echo "[5/8] Maven tests"
MAVEN_ARGS=(-B)
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then
  MAVEN_ARGS+=("-Dmaven.repo.local=${MAVEN_REPO_LOCAL}")
fi
mvn "${MAVEN_ARGS[@]}" test

echo "[6/8] Documentation links and index"
while IFS= read -r document; do
  while IFS= read -r target; do
    case "$target" in
      http://*|https://*|mailto:*|\#*|'') continue ;;
    esac
    target=${target%%#*}
    test -e "$(dirname "$document")/$target" || {
      echo "Broken Markdown link in ${document}: ${target}" >&2
      exit 1
    }
  done < <(sed -n 's/.*](\([^)]*\)).*/\1/p' "$document")
done < <(find . -path './.git' -prune -o -name '*.md' -print)

for document in ARCHITECTURE.md CIRCUIT_BREAKER_TEST.md GETTING_STARTED.md KIE_CONFIG.md TEST_CASES.md TUTORIAL.md TUTORIAL_TESTCASE_MATRIX.md; do
  grep -q "docs/${document}" README.md
done

if grep -qE '实际验证|✅[[:space:]]*PASS' docs/TEST_CASES.md; then
  echo "TEST_CASES.md contains a stale recorded result." >&2
  exit 1
fi

echo "[7/8] Documentation scope"
forbidden_pattern='na''cos|senti''nel|services''comb'
if git grep -niE "$forbidden_pattern" -- '*.md' '*.java' '*.xml' '*.yml' '*.yaml' '*.sh' 'Dockerfile'; then
  echo "Found descriptions outside this ServiceComb example's scope." >&2
  exit 1
fi

echo "[8/8] Repository hygiene"
tracked_artifacts=$(git ls-files | grep -E '(^|/)(target/|.*\.(class|log)$)' || true)
test -z "$tracked_artifacts"

echo "All checks passed."
