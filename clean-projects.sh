#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
readonly PROJECT_NAMES=(
  "spring-cloud-huawei-nacos"
  "spring-cloud-huawei-servicecomb"
  "sermant-cse-demo"
  "mesher-python-sidecar-demo"
)

dry_run=false

usage() {
  cat <<'EOF'
用法：./clean-projects.sh [--dry-run] [--help]

清理以下工程中的编译生成件和临时文件：
  - spring-cloud-huawei-nacos
  - spring-cloud-huawei-servicecomb
  - sermant-cse-demo
  - mesher-python-sidecar-demo

清理内容：
  - target/、__pycache__/、.pytest_cache/ 目录
  - *.class、*.pyc、*.tmp、*.temp、*.log 文件
  - .flattened-pom.xml 文件

选项：
  --dry-run  仅显示将被清理的路径，不删除任何内容
  -h, --help 显示帮助
EOF
}

for arg in "$@"; do
  case "$arg" in
    --dry-run)
      dry_run=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      printf '错误：未知参数 %q\n' "$arg" >&2
      usage >&2
      exit 2
      ;;
  esac
done

project_roots=()
for project_name in "${PROJECT_NAMES[@]}"; do
  project_root="${SCRIPT_DIR}/${project_name}"
  if [[ ! -d "$project_root" ]]; then
    printf '错误：目标工程目录不存在：%s\n' "$project_root" >&2
    exit 1
  fi

  canonical_root="$(cd -- "$project_root" && pwd -P)"
  expected_root="${SCRIPT_DIR}/${project_name}"
  if [[ "$canonical_root" != "$expected_root" ]]; then
    printf '错误：目标工程路径校验失败：%s\n' "$project_root" >&2
    exit 1
  fi
  project_roots+=("$canonical_root")
done

temporary_dirs=()
temporary_files=()

for project_root in "${project_roots[@]}"; do
  while IFS= read -r -d '' path; do
    temporary_dirs+=("$path")
  done < <(
    find "$project_root" \
      -type d \( \
        -name target -o \
        -name __pycache__ -o \
        -name .pytest_cache \
      \) -prune -print0
  )

  while IFS= read -r -d '' path; do
    temporary_files+=("$path")
  done < <(
    find "$project_root" \
      -type d \( \
        -name target -o \
        -name __pycache__ -o \
        -name .pytest_cache \
      \) -prune -o \
      -type f \( \
        -name '*.class' -o \
        -name '*.pyc' -o \
        -name '*.tmp' -o \
        -name '*.temp' -o \
        -name '*.log' -o \
        -name '.flattened-pom.xml' \
      \) -print0
  )
done

if ((${#temporary_dirs[@]} == 0 && ${#temporary_files[@]} == 0)); then
  printf '没有发现需要清理的内容。\n'
  exit 0
fi

if [[ "$dry_run" == true ]]; then
  printf '[预览] 将清理以下内容：\n'
else
  printf '正在清理以下内容：\n'
fi

for path in "${temporary_dirs[@]}" "${temporary_files[@]}"; do
  relative_path="${path#"${SCRIPT_DIR}/"}"
  printf '  %s\n' "$relative_path"
done

printf '\n共发现 %d 个临时目录、%d 个临时文件。\n' \
  "${#temporary_dirs[@]}" "${#temporary_files[@]}"

if [[ "$dry_run" == true ]]; then
  printf '预览完成，未删除任何内容。\n'
  exit 0
fi

for path in "${temporary_dirs[@]}"; do
  rm -rf -- "$path"
done

for path in "${temporary_files[@]}"; do
  rm -f -- "$path"
done

printf '清理完成。\n'
