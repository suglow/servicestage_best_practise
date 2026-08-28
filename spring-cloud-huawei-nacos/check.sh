#!/bin/bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$ROOT_DIR"

echo "[1/8] Shell syntax"
bash -n verify.sh check.sh
sh -n scripts/nacos-init.sh

echo "[2/8] Verification CLI and documentation coverage"
bash verify.sh --help >/dev/null
case_list=$(bash verify.sh --list)
for test_case in TC-1 TC-2 TC-2F TC-3 TC-3G TC-4 TC-5 TC-6 TC-7 TC-8 TC-9 TC-10; do
  printf '%s\n' "$case_list" | grep -q "^${test_case}[[:space:]]"
  grep -q "| ${test_case} |" docs/TEST_CASES.md
  grep -Eq -- "--tc ${test_case#TC-}([^[:alnum:]]|$)" docs/TUTORIAL_TESTCASE_MATRIX.md
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
docker compose --profile feign config --quiet
docker compose --profile governance config --quiet
docker compose --profile all config --quiet
test "$(docker compose config --services | wc -l)" -eq 6
test "$(docker compose --profile feign config --services | wc -l)" -eq 7
test "$(docker compose --profile governance config --services | wc -l)" -eq 14
test "$(docker compose --profile all config --services | wc -l)" -eq 15

echo "[4/8] Application configuration"
for config in */src/main/resources/application.yml; do
  grep -q 'server-addr:.*NACOS_SERVER_ADDR' "$config"
  grep -q 'namespace:.*NACOS_NAMESPACE' "$config"
done
for config in */src/main/resources/bootstrap.yaml; do
  grep -q 'server-addr:.*NACOS_SERVER_ADDR' "$config"
  grep -q 'namespace:.*NACOS_NAMESPACE' "$config"
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

for document in ARCHITECTURE.md CONFIGURATION.md TEST_CASES.md TROUBLESHOOTING.md TUTORIAL.md TUTORIAL_TESTCASE_MATRIX.md; do
  grep -q "docs/${document}" README.md
done

echo "[7/8] Documentation scope"
forbidden_pattern='Service''Comb|Senti''nel|services''comb'
if git grep -nE "$forbidden_pattern" -- '*.md' '*.java' '*.xml' '*.yml' '*.yaml' '*.sh' 'Dockerfile'; then
  echo "Found descriptions outside this Nacos example's scope." >&2
  exit 1
fi

echo "[8/8] Repository hygiene"
tracked_artifacts=$(git ls-files | grep -E '(^|/)(target/|.*\.(class|log)$)' || true)
test -z "$tracked_artifacts"

echo "All checks passed."
