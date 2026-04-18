#!/usr/bin/env bash
# Gera par de chaves RSA 2048-bit para assinar e verificar JWTs.
# Execute uma vez antes de iniciar a aplicação pela primeira vez.
# ATENCAO: Nao commite privateKey.pem em repositórios públicos.
set -euo pipefail

KEYS_DIR="src/main/resources/keys"
mkdir -p "$KEYS_DIR"

echo "Gerando chave privada RSA 2048-bit (PKCS8)..."
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$KEYS_DIR/privateKey.pem"

echo "Extraindo chave pública..."
openssl rsa -in "$KEYS_DIR/privateKey.pem" -pubout -out "$KEYS_DIR/publicKey.pem" 2>/dev/null

echo ""
echo "Chaves RSA geradas com sucesso em '$KEYS_DIR':"
echo "  - privateKey.pem  (chave privada — mantenha em segredo)"
echo "  - publicKey.pem   (chave pública — pode ser compartilhada)"
