#!/usr/bin/env python3
"""
Adiciona a pasta "5. Roteiro do Video" na collection Postman, com requests
numerados na ordem EXATA dos passos demonstrados no docs/roteiro-video.md.

Cada request eh uma copia adaptada dos existentes em "1. Autenticacao",
"2. Pedidos" e do schema GraphQL do restaurante-service (cozinha).
As outras pastas (1, 2, 3, 4) ficam intactas.

Como rodar:
    docker run --rm -v <repo>:/work -w /work/docs/build-pdf python:3.12-slim \\
        python add_roteiro_folder.py
"""

import json
from pathlib import Path

COLLECTION = (
    Path(__file__).resolve().parents[1]
    / "fiap-fase-3-restaurante.postman_collection.json"
)


def _request_login(label: str, conta_email_var: str, conta_senha_var: str) -> dict:
    """Login (USUARIO ou DONO) com script que salva o token em {{token}}."""
    return {
        "name": label,
        "event": [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        "var json = pm.response.json();",
                        "var token = json && json.data && json.data.login ? json.data.login.token : null;",
                        "if (token) {",
                        "    pm.collectionVariables.set('token', token);",
                        "    console.log('Token JWT salvo.');",
                        "} else {",
                        "    console.warn('Token nao encontrado:', JSON.stringify(json));",
                        "}",
                    ],
                },
            }
        ],
        "request": {
            "method": "POST",
            "url": "{{authUrl}}",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "mutation Login($input: LoginInput!) {\n"
                        "  login(input: $input) {\n"
                        "    token\n"
                        "    tipoToken\n"
                        "    expiraEmSegundos\n"
                        "    usuario { id nome email perfil }\n"
                        "  }\n"
                        "}"
                    ),
                    "variables": (
                        "{\n"
                        f'  "input": {{\n'
                        f'    "email": "{{{{{conta_email_var}}}}}",\n'
                        f'    "senha": "{{{{{conta_senha_var}}}}}"\n'
                        f"  }}\n"
                        "}"
                    ),
                },
            },
        },
        "response": [],
    }


def _request_criar_pedido(label: str) -> dict:
    """Cria um pedido com 2 itens fixos, salva pedidoId em variavel."""
    return {
        "name": label,
        "event": [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        "var json = pm.response.json();",
                        "var pedido = json && json.data ? json.data.criarPedido : null;",
                        "if (pedido && pedido.id) {",
                        "    pm.collectionVariables.set('pedidoId', pedido.id);",
                        "    console.log('pedidoId salvo:', pedido.id, '| valorTotal:', pedido.valorTotal);",
                        "} else {",
                        "    console.warn('pedidoId nao encontrado:', JSON.stringify(json));",
                        "}",
                    ],
                },
            }
        ],
        "request": {
            "method": "POST",
            "url": "{{pedidoUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "mutation CriarPedido($input: CriarPedidoInput!) {\n"
                        "  criarPedido(input: $input) {\n"
                        "    id\n    clienteId\n    restauranteId\n    valorTotal\n    status\n"
                        "    itens { produtoId nome quantidade preco subtotal }\n"
                        "    createdAt\n  }\n}"
                    ),
                    "variables": (
                        "{\n"
                        '  "input": {\n'
                        '    "restauranteId": "{{restauranteId}}",\n'
                        '    "itens": [\n'
                        "      {\n"
                        '        "produtoId": "00000000-0000-4000-8000-000000000001",\n'
                        '        "nome": "X-Burger",\n'
                        '        "quantidade": 2,\n'
                        '        "preco": "25.90"\n'
                        "      },\n"
                        "      {\n"
                        '        "produtoId": "00000000-0000-4000-8000-000000000004",\n'
                        '        "nome": "Refrigerante",\n'
                        '        "quantidade": 1,\n'
                        '        "preco": "7.50"\n'
                        "      }\n"
                        "    ]\n"
                        "  }\n"
                        "}"
                    ),
                },
            },
        },
        "response": [],
    }


def _request_confirmar(label: str) -> dict:
    return {
        "name": label,
        "request": {
            "method": "POST",
            "url": "{{pedidoUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "mutation ConfirmarPedido($pedidoId: ID!) {\n"
                        "  confirmarPedido(pedidoId: $pedidoId) {\n"
                        "    id status valorTotal updatedAt\n  }\n}"
                    ),
                    "variables": '{\n  "pedidoId": "{{pedidoId}}"\n}',
                },
            },
        },
        "response": [],
    }


def _request_pedido_por_id(label: str) -> dict:
    return {
        "name": label,
        "request": {
            "method": "POST",
            "url": "{{pedidoUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "query PedidoPorId($pedidoId: ID!) {\n"
                        "  pedidoPorId(pedidoId: $pedidoId) {\n"
                        "    id status valorTotal pagamentoId motivoPendencia\n"
                        "    itens { produtoId nome quantidade preco subtotal }\n"
                        "    createdAt updatedAt\n  }\n}"
                    ),
                    "variables": '{\n  "pedidoId": "{{pedidoId}}"\n}',
                },
            },
        },
        "response": [],
    }


def _request_fila_cozinha(label: str) -> dict:
    """Lista a fila da cozinha; salva o pedidoCozinhaId do pedido corrente."""
    return {
        "name": label,
        "event": [
            {
                "listen": "test",
                "script": {
                    "type": "text/javascript",
                    "exec": [
                        "var json = pm.response.json();",
                        "var fila = json && json.data ? json.data.filaCozinha : null;",
                        "if (!fila || !fila.length) {",
                        "    console.warn('Fila vazia. Verifique se o passo 04 ja chegou em PAGO. Resposta:', JSON.stringify(json));",
                        "    return;",
                        "}",
                        "var alvo = pm.collectionVariables.get('pedidoId');",
                        "var item = fila.find(function(p) { return p.pedidoId === alvo; }) || fila[0];",
                        "pm.collectionVariables.set('pedidoCozinhaId', item.id);",
                        "console.log('pedidoCozinhaId salvo:', item.id, '| status:', item.status, '| pedidoId:', item.pedidoId);",
                    ],
                },
            }
        ],
        "request": {
            "method": "POST",
            "url": "{{cozinhaUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "query FilaCozinha {\n"
                        "  filaCozinha {\n"
                        "    id pedidoId restauranteId status createdAt\n"
                        "    itens { produtoId nome quantidade }\n"
                        "  }\n"
                        "}"
                    ),
                    "variables": "{}",
                },
            },
        },
        "response": [],
    }


def _request_iniciar_preparo(label: str) -> dict:
    return {
        "name": label,
        "request": {
            "method": "POST",
            "url": "{{cozinhaUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "mutation IniciarPreparo($pedidoCozinhaId: ID!) {\n"
                        "  iniciarPreparo(pedidoCozinhaId: $pedidoCozinhaId) {\n"
                        "    id pedidoId status iniciadoEm updatedAt\n"
                        "  }\n"
                        "}"
                    ),
                    "variables": '{\n  "pedidoCozinhaId": "{{pedidoCozinhaId}}"\n}',
                },
            },
        },
        "response": [],
    }


def _request_marcar_pronto(label: str) -> dict:
    return {
        "name": label,
        "request": {
            "method": "POST",
            "url": "{{cozinhaUrl}}",
            "header": [
                {"key": "Content-Type", "value": "application/json"},
                {"key": "Authorization", "value": "Bearer {{token}}"},
            ],
            "body": {
                "mode": "graphql",
                "graphql": {
                    "query": (
                        "mutation MarcarComoPronto($pedidoCozinhaId: ID!) {\n"
                        "  marcarComoPronto(pedidoCozinhaId: $pedidoCozinhaId) {\n"
                        "    id pedidoId status finalizadoEm updatedAt\n"
                        "  }\n"
                        "}"
                    ),
                    "variables": '{\n  "pedidoCozinhaId": "{{pedidoCozinhaId}}"\n}',
                },
            },
        },
        "response": [],
    }


def build_roteiro_folder() -> dict:
    """A pasta `5. Roteiro do Video` na ordem exata do roteiro."""
    return {
        "name": "5. Roteiro do Video",
        "description": (
            "Sequencia EXATA das acoes do roteiro de gravacao "
            "(docs/roteiro-video.md). Execute os requests em ordem: o passo "
            "N depende do que o passo N-1 deixou em {{token}}, {{pedidoId}} "
            "e {{pedidoCozinhaId}}.\n\n"
            "Numeracao casa com a referencia no roteiro: \"Postman -> 5. "
            "Roteiro do Video -> 03 - Confirmar Pedido (bloco 3.3)\".\n\n"
            "Estrutura:\n"
            "  - Passos 01-04: bloco 3 do roteiro (happy path Postman)\n"
            "  - Passos 05-10: bloco 3.5 — fluxo da cozinha (DONO_RESTAURANTE)\n"
            "  - Passos 11-14: bloco 4 — demo de resiliencia (gateway off -> reprocesso)\n\n"
            "Pre-requisitos: subir tudo com `docker compose up -d --build` e "
            "ativar o environment `fiap-fase-3-restaurante`."
        ),
        "item": [
            # ---- BLOCO 3: HAPPY PATH ----
            {
                **_request_login(
                    "01 - Login como Usuario (bloco 3.1)",
                    "usuarioEmail",
                    "usuarioSenha",
                ),
                "description": (
                    "ROTEIRO BLOCO 3.1 — Login. O JWT eh salvo automaticamente "
                    "em {{token}}; usado em todos os requests subsequentes."
                ),
            },
            {
                **_request_criar_pedido("02 - Criar Pedido (bloco 3.2)"),
                "description": (
                    "ROTEIRO BLOCO 3.2 — Cria o pedido com 2 itens. {{pedidoId}} "
                    "fica salvo. Repare na response: o servidor calculou o valorTotal."
                ),
            },
            {
                **_request_confirmar("03 - Confirmar Pedido (bloco 3.3)"),
                "description": (
                    "ROTEIRO BLOCO 3.3 — Confirma o pedido. Publica pedido.criado "
                    "no Kafka. Alt-tab para Kafka UI (:8085) e mostre a mensagem chegando."
                ),
            },
            {
                **_request_pedido_por_id(
                    "04 - Pedido por ID (espera PAGO - bloco 3.4)"
                ),
                "description": (
                    "ROTEIRO BLOCO 3.4 — Aguarde ~5s apos o passo 03 e rode este. "
                    "Status esperado: PAGO. Tudo automatico, sem intervencao."
                ),
            },
            # ---- BLOCO 3.5: FLUXO DA COZINHA (DONO_RESTAURANTE) ----
            {
                **_request_login(
                    "05 - Login como Dono (bloco 3.5 - cozinha)",
                    "donoEmail",
                    "donoSenha",
                ),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Loga como DONO_RESTAURANTE. O {{token}} "
                    "passa a ter o perfil necessario para chamar as queries/mutations "
                    "do restaurante-service (`:8084`)."
                ),
            },
            {
                **_request_fila_cozinha("06 - Fila da Cozinha (bloco 3.5)"),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Lista os pedidos na cozinha. O test script "
                    "localiza o item cujo pedidoId == {{pedidoId}} e salva seu id "
                    "em {{pedidoCozinhaId}} para os proximos passos. Status esperado "
                    "do item: RECEBIDO."
                ),
            },
            {
                **_request_iniciar_preparo("07 - Iniciar Preparo (bloco 3.5)"),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Marca o pedido como EM_PREPARO. Publica "
                    "`pedido.em-preparo` no Kafka; o restaurante-pedido consome e "
                    "atualiza o agregado principal."
                ),
            },
            {
                **_request_marcar_pronto("08 - Marcar como Pronto (bloco 3.5)"),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Marca o pedido como PRONTO. Publica "
                    "`pedido.pronto` no Kafka; o restaurante-pedido consome e "
                    "reflete o status final."
                ),
            },
            {
                **_request_login(
                    "09 - Re-login como Usuario (bloco 3.5 - voltar p/ consulta)",
                    "usuarioEmail",
                    "usuarioSenha",
                ),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Volta o {{token}} para USUARIO antes da "
                    "consulta final ao pedido original (a query `pedidoPorId` exige "
                    "que o cliente seja o dono do pedido)."
                ),
            },
            {
                **_request_pedido_por_id(
                    "10 - Pedido por ID (espera PRONTO - fim do bloco 3.5)"
                ),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Consulta o pedido original. Status "
                    "esperado: PRONTO. Prova que os 4 microsservicos conversaram "
                    "via Kafka sem nenhuma chamada sincrona entre eles."
                ),
            },
            # ---- BLOCO 4: DEMO DE RESILIENCIA ----
            {
                **_request_criar_pedido("11 - Criar Pedido (gateway off - bloco 4.2)"),
                "description": (
                    "ROTEIRO BLOCO 4.2 — Cria novo pedido. Pre-requisito: "
                    "`docker stop procpag` ja foi executado no terminal."
                ),
            },
            {
                **_request_confirmar("12 - Confirmar Pedido (gateway off - bloco 4.2)"),
                "description": (
                    "ROTEIRO BLOCO 4.2 — Confirma o pedido com o gateway fora. "
                    "O pagamento-service vai tentar 3x (Retry), abrir o Circuit "
                    "Breaker apos varias falhas e cair no Fallback. Acompanhe em "
                    "`docker logs -f pagamento-service` no terminal."
                ),
            },
            {
                **_request_pedido_por_id(
                    "13 - Pedido por ID (espera PENDENTE_PAGAMENTO - bloco 4.3)"
                ),
                "description": (
                    "ROTEIRO BLOCO 4.3 — Aguarde ~10-15s apos o passo 12. Status "
                    "esperado: PENDENTE_PAGAMENTO. O cliente nao recebeu erro — "
                    "o sistema absorveu a falha."
                ),
            },
            {
                **_request_pedido_por_id(
                    "14 - Pedido por ID (espera PAGO apos reprocesso - bloco 4.4)"
                ),
                "description": (
                    "ROTEIRO BLOCO 4.4 — Pre-requisito: `docker start procpag` ja "
                    "foi executado, e voce esperou ~30s para o worker @Scheduled "
                    "rodar. Status esperado: PAGO. Recuperacao automatica completa."
                ),
            },
        ],
    }


def main() -> None:
    data = json.loads(COLLECTION.read_text(encoding="utf-8"))

    # Remove versao anterior da pasta "5. Roteiro do Video" se existir
    data["item"] = [
        folder
        for folder in data["item"]
        if folder.get("name") != "5. Roteiro do Video"
    ]

    # Adiciona a nova pasta (sera a 5a)
    data["item"].append(build_roteiro_folder())

    # Salva com indentacao consistente (2 espacos, como ja estava)
    COLLECTION.write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    print(f"Atualizado: {COLLECTION}")
    print(f"Pastas finais: {[f['name'] for f in data['item']]}")
    print(
        f"Pasta 5 - Roteiro do Video: "
        f"{len(data['item'][-1]['item'])} requests"
    )
    for req in data["item"][-1]["item"]:
        print(f"  - {req['name']}")


if __name__ == "__main__":
    main()
