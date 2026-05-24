#!/usr/bin/env bash
#
# test-summary.sh
#
# Roda a suite de testes do fiap-restaurante e imprime, ao final, um
# resumo agregado com totais de todos os modulos.
#
# O Maven Surefire mostra "Tests run: N" por modulo, mas nao agrega entre
# modulos no Reactor Summary. Este script roda 'mvnw test' e depois le
# todos os XMLs em **/target/surefire-reports/TEST-*.xml, imprimindo
# totais por modulo e total agregado.
#
# O exit code do script reflete o resultado dos testes (0 verde, 1 com
# falhas/erros), para uso em CI.
#
# Uso:
#   ./scripts/test-summary.sh                # roda tudo e mostra agregado
#   ./scripts/test-summary.sh --quiet        # mvn com -q (saida reduzida)
#   ./scripts/test-summary.sh --skip-build   # so le XMLs de um build anterior
#   ./scripts/test-summary.sh -h | --help    # mostra esta ajuda
#
# Autor: Danilo Fernando

set -euo pipefail

# Navega para a raiz do projeto (este script vive em scripts/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

SKIP_BUILD=0
QUIET=0
for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=1 ;;
    --quiet|-q)   QUIET=1 ;;
    -h|--help)
      sed -n '3,22p' "$0" | sed 's/^# \{0,1\}//'
      exit 0
      ;;
    *)
      echo "Argumento desconhecido: $arg" >&2
      echo "Use -h para ajuda." >&2
      exit 2
      ;;
  esac
done

# Cores ANSI - so se stdout for terminal
if [ -t 1 ]; then
  C_YELLOW="\033[33m"; C_GREEN="\033[32m"; C_RED="\033[31m"
  C_CYAN="\033[36m";   C_RESET="\033[0m"
else
  C_YELLOW=""; C_GREEN=""; C_RED=""; C_CYAN=""; C_RESET=""
fi

# ---------------------------------------------------------------------------
# 1) Roda os testes (a menos que --skip-build)
# ---------------------------------------------------------------------------
EXIT_CODE=0
if [ "$SKIP_BUILD" -eq 0 ]; then
  printf "${C_CYAN}Rodando ./mvnw test ...${C_RESET}\n"
  if [ "$QUIET" -eq 1 ]; then
    ./mvnw -q test || EXIT_CODE=$?
  else
    ./mvnw test || EXIT_CODE=$?
  fi
fi

# ---------------------------------------------------------------------------
# 2) Agrega XMLs (sem xmlstarlet - usa grep/sed/awk para portabilidade)
# ---------------------------------------------------------------------------
mapfile -t XML_FILES < <(find . -path "*/target/surefire-reports/TEST-*.xml" 2>/dev/null | sort)

if [ "${#XML_FILES[@]}" -eq 0 ]; then
  printf "\n${C_YELLOW}Nenhum XML encontrado em **/target/surefire-reports/. Rode './mvnw test' primeiro.${C_RESET}\n"
  exit 1
fi

# Extrai o valor de um atributo da tag <testsuite ...> do XML
xml_attr() {
  # $1 = arquivo, $2 = nome do atributo
  grep -oE "<testsuite[^>]*" "$1" | grep -oE "$2=\"[^\"]+\"" | head -1 | sed -E "s/$2=\"([^\"]+)\"/\1/"
}

# Soma de floats via awk (bash nao tem float aritmetica)
fadd() {
  awk -v a="$1" -v b="$2" 'BEGIN{ printf "%.3f", a+b }'
}

declare -A MOD_TESTS MOD_FAIL MOD_ERR MOD_SKIP MOD_TIME

TOTAL_TESTS=0
TOTAL_FAIL=0
TOTAL_ERR=0
TOTAL_SKIP=0
TOTAL_TIME="0"

for xml in "${XML_FILES[@]}"; do
  # Nome do modulo = primeiro diretorio relativo (ex: ./pagamento/target/... -> "pagamento")
  mod="$(echo "$xml" | sed -E 's|^\./([^/]+)/.*|\1|')"

  t="$(xml_attr  "$xml" tests)";    t="${t:-0}"
  f="$(xml_attr  "$xml" failures)"; f="${f:-0}"
  e="$(xml_attr  "$xml" errors)";   e="${e:-0}"
  s="$(xml_attr  "$xml" skipped)";  s="${s:-0}"
  tm="$(xml_attr "$xml" time)";     tm="${tm:-0}"; tm="${tm//,/.}"

  MOD_TESTS["$mod"]=$(( ${MOD_TESTS["$mod"]:-0} + t ))
  MOD_FAIL["$mod"]=$((  ${MOD_FAIL["$mod"]:-0}  + f ))
  MOD_ERR["$mod"]=$((   ${MOD_ERR["$mod"]:-0}   + e ))
  MOD_SKIP["$mod"]=$((  ${MOD_SKIP["$mod"]:-0}  + s ))
  MOD_TIME["$mod"]="$(fadd "${MOD_TIME["$mod"]:-0}" "$tm")"

  TOTAL_TESTS=$(( TOTAL_TESTS + t ))
  TOTAL_FAIL=$((  TOTAL_FAIL  + f ))
  TOTAL_ERR=$((   TOTAL_ERR   + e ))
  TOTAL_SKIP=$((  TOTAL_SKIP  + s ))
  TOTAL_TIME="$(fadd "$TOTAL_TIME" "$tm")"
done

# ---------------------------------------------------------------------------
# 3) Imprime resumo
# ---------------------------------------------------------------------------
printf "\n${C_YELLOW}============================================================${C_RESET}\n"
printf   "${C_YELLOW} RESUMO POR MODULO${C_RESET}\n"
printf   "${C_YELLOW}============================================================${C_RESET}\n"

# Ordena os modulos alfabeticamente
for mod in $(printf "%s\n" "${!MOD_TESTS[@]}" | sort); do
  cor="$C_GREEN"
  if [ $(( ${MOD_FAIL["$mod"]:-0} + ${MOD_ERR["$mod"]:-0} )) -gt 0 ]; then
    cor="$C_RED"
  fi
  printf "${cor}  %-25s tests: %4d   falhas: %d   erros: %d   skip: %d   tempo: %6.2fs${C_RESET}\n" \
    "$mod" "${MOD_TESTS[$mod]}" "${MOD_FAIL[$mod]}" "${MOD_ERR[$mod]}" "${MOD_SKIP[$mod]}" "${MOD_TIME[$mod]}"
done

printf "\n${C_YELLOW}============================================================${C_RESET}\n"
printf   "${C_YELLOW} TOTAL AGREGADO${C_RESET}\n"
printf   "${C_YELLOW}============================================================${C_RESET}\n"

cor="$C_GREEN"
if [ $(( TOTAL_FAIL + TOTAL_ERR )) -gt 0 ]; then
  cor="$C_RED"
fi
printf "${cor}  Tests run:  %d${C_RESET}\n" "$TOTAL_TESTS"
printf "${cor}  Failures:   %d${C_RESET}\n" "$TOTAL_FAIL"
printf "${cor}  Errors:     %d${C_RESET}\n" "$TOTAL_ERR"
printf  "  Skipped:    %d\n"                "$TOTAL_SKIP"
printf  "  Tempo:      %ss\n\n"             "$TOTAL_TIME"

# Exit code: erro se ha falha/erro de teste; senao reflete o exit do mvn
if [ $(( TOTAL_FAIL + TOTAL_ERR )) -gt 0 ]; then
  exit 1
fi
exit $EXIT_CODE
