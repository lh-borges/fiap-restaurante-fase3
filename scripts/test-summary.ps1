<#
.SYNOPSIS
    Roda a suite de testes do fiap-restaurante e imprime, ao final, um
    resumo agregado com totais de todos os modulos.

.DESCRIPTION
    O Maven Surefire mostra "Tests run: N" por modulo, mas nao agrega entre
    modulos no Reactor Summary. Este script:
      1. Roda `./mvnw test` (a menos que -SkipBuild seja passado)
      2. Le todos os XMLs em **/target/surefire-reports/TEST-*.xml
      3. Imprime totais por modulo e total agregado

    O exit code do script reflete o resultado dos testes (0 verde, 1 com
    falhas/erros), para uso em CI.

.PARAMETER SkipBuild
    Pula a execucao do Maven; apenas le os XMLs ja existentes e imprime o
    resumo. Util quando o build acabou de rodar e voce so quer ver o total.

.PARAMETER Quiet
    Roda o Maven com -q (so warnings/erros do mvn aparecem).

.EXAMPLE
    .\scripts\test-summary.ps1
    Roda toda a suite e mostra o resumo agregado.

.EXAMPLE
    .\scripts\test-summary.ps1 -Quiet
    Idem, mas com saida do Maven reduzida.

.EXAMPLE
    .\scripts\test-summary.ps1 -SkipBuild
    Apenas le XMLs de um build anterior e imprime o resumo (sem rodar testes).

.NOTES
    Autor: Danilo Fernando

    Se a ExecutionPolicy bloquear, rode via:
      powershell -ExecutionPolicy Bypass -File .\scripts\test-summary.ps1
#>
[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'

# Navega para a raiz do projeto (este script vive em scripts/)
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# ---------------------------------------------------------------------------
# 1) Roda os testes (a menos que -SkipBuild)
# ---------------------------------------------------------------------------
$exitCode = 0
if (-not $SkipBuild) {
    Write-Host "Rodando ./mvnw test ..." -ForegroundColor Cyan
    if ($Quiet) {
        & .\mvnw.cmd -q test
    } else {
        & .\mvnw.cmd test
    }
    $exitCode = $LASTEXITCODE
}

# ---------------------------------------------------------------------------
# 2) Agrega XMLs de **/target/surefire-reports/TEST-*.xml
# ---------------------------------------------------------------------------
$total = @{ tests = 0; failures = 0; errors = 0; skipped = 0; time = 0.0 }
$porModulo = @{}

$xmls = Get-ChildItem -Recurse -Filter "TEST-*.xml" |
        Where-Object { $_.FullName -match "surefire-reports" }

if (-not $xmls -or $xmls.Count -eq 0) {
    Write-Host ""
    Write-Host "Nenhum XML encontrado em **/target/surefire-reports/. Rode './mvnw test' primeiro." -ForegroundColor Yellow
    exit 1
}

foreach ($xmlFile in $xmls) {
    [xml]$xml = Get-Content $xmlFile.FullName
    # Nome do modulo = primeiro diretorio relativo (ex: "pagamento", "shared")
    $modulo = ($xmlFile.FullName -split [regex]::Escape("$projectRoot\"))[1].Split("\")[0]
    if (-not $porModulo.ContainsKey($modulo)) {
        $porModulo[$modulo] = @{ tests = 0; failures = 0; errors = 0; skipped = 0; time = 0.0 }
    }
    $t  = [int]$xml.testsuite.tests
    $f  = [int]$xml.testsuite.failures
    $e  = [int]$xml.testsuite.errors
    $s  = [int]$xml.testsuite.skipped
    $tm = [double](([string]$xml.testsuite.time).Replace(',', '.'))

    $porModulo[$modulo].tests    += $t
    $porModulo[$modulo].failures += $f
    $porModulo[$modulo].errors   += $e
    $porModulo[$modulo].skipped  += $s
    $porModulo[$modulo].time     += $tm

    $total.tests    += $t
    $total.failures += $f
    $total.errors   += $e
    $total.skipped  += $s
    $total.time     += $tm
}

# ---------------------------------------------------------------------------
# 3) Imprime resumo
# ---------------------------------------------------------------------------
Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host " RESUMO POR MODULO" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow
foreach ($m in $porModulo.Keys | Sort-Object) {
    $v = $porModulo[$m]
    $corLinha = if ($v.failures + $v.errors -gt 0) { 'Red' } else { 'Green' }
    $linha = "  {0,-25} tests: {1,4}   falhas: {2}   erros: {3}   skip: {4}   tempo: {5,6:N2}s" -f `
             $m, $v.tests, $v.failures, $v.errors, $v.skipped, $v.time
    Write-Host $linha -ForegroundColor $corLinha
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host " TOTAL AGREGADO" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow
$corTotal = if ($total.failures + $total.errors -gt 0) { 'Red' } else { 'Green' }
Write-Host ("  Tests run:  {0}" -f $total.tests)    -ForegroundColor $corTotal
Write-Host ("  Failures:   {0}" -f $total.failures) -ForegroundColor $corTotal
Write-Host ("  Errors:     {0}" -f $total.errors)   -ForegroundColor $corTotal
Write-Host ("  Skipped:    {0}" -f $total.skipped)
Write-Host ("  Tempo:      {0:N2}s" -f $total.time)
Write-Host ""

# Exit code: erro se ha falha/erro de teste; senao reflete o exit do mvn
if ($total.failures -gt 0 -or $total.errors -gt 0) { exit 1 }
exit $exitCode
