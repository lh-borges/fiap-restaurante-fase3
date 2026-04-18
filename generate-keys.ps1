# Gera par de chaves RSA 2048-bit para assinar e verificar JWTs.
# Execute uma vez antes de iniciar a aplicação pela primeira vez.
# ATENCAO: Nao commite privateKey.pem em repositórios públicos.

$keysDir = "src\main\resources\keys"
if (-not (Test-Path $keysDir)) {
    New-Item -ItemType Directory -Path $keysDir | Out-Null
}

$openssl = Get-Command openssl -ErrorAction SilentlyContinue
if (-not $openssl) {
    Write-Error "OpenSSL nao encontrado. Instale via 'winget install ShiningLight.OpenSSL' ou use o OpenSSL incluido no Git for Windows."
    exit 1
}

Write-Host "Gerando chave privada RSA 2048-bit (PKCS8)..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$keysDir\privateKey.pem"

Write-Host "Extraindo chave publica..."
openssl rsa -in "$keysDir\privateKey.pem" -pubout -out "$keysDir\publicKey.pem" 2>$null

Write-Host ""
Write-Host "Chaves RSA geradas com sucesso em '$keysDir':"
Write-Host "  - privateKey.pem  (chave privada — mantenha em segredo)"
Write-Host "  - publicKey.pem   (chave publica — pode ser compartilhada)"
