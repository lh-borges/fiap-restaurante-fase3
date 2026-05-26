#!/usr/bin/env python3
"""
Adiciona a pasta "5. Roteiro do Video" na collection Postman, com requests
numerados na ordem EXATA dos passos demonstrados no docs/roteiro-video.md.

Cada request eh uma copia adaptada dos existentes em "1. Autenticacao" e
"2. Pedidos". As outras pastas (1, 2, 3, 4) ficam intactas.

Como rodar:
    docker run --rm -v <repo>:/work -w /work/docs/build-pdf python:3.12-slim \\
        python add_roteiro_folder.py
"""

import json
from copy import deepcopy
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


def build_roteiro_folder() -> dict:
    """A pasta `5. Roteiro do Video` na ordem exata do roteiro."""
    return {
        "name": "5. Roteiro do Video",
        "description": (
            "Sequencia EXATA das acoes do roteiro de gravacao "
            "(docs/roteiro-video.md). Execute os requests em ordem: o passo "
            "N depende do que o passo N-1 deixou em {{token}} e {{pedidoId}}.\n\n"
            "Numeracao casa com a referencia no roteiro: \"Postman -> 5. "
            "Roteiro do Video -> 03 - Confirmar Pedido (happy path)\".\n\n"
            "Estrutura:\n"
            "  - Passos 01-04: bloco 3 do roteiro (happy path Postman)\n"
            "  - Passo 05: prepara token DONO para usar no GraphiQL :8084 (bloco 3.5)\n"
            "  - Passo 06: volta para USUARIO (bloco 4, demo resiliencia)\n"
            "  - Passos 07-10: demo de resiliencia (gateway off -> reprocesso)\n\n"
            "Cada request tem descricao curta indicando qual bloco do roteiro "
            "ele corresponde."
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
            # ---- BLOCO 3.5: PREPARAR TOKEN DONO P/ GRAPHIQL ----
            {
                **_request_login(
                    "05 - Login como Dono (token p/ GraphiQL :8084 - bloco 3.5)",
                    "donoEmail",
                    "donoSenha",
                ),
                "description": (
                    "ROTEIRO BLOCO 3.5 — Loga como DONO_RESTAURANTE para obter um "
                    "token com o perfil necessario. COPIE o token da response e cole "
                    "em Request Headers do GraphiQL `:8084/graphiql` no formato:\n\n"
                    '{ "Authorization": "Bearer <token>" }\n\n'
                    "No GraphiQL, rode `filaCozinha`, depois `iniciarPreparo` e "
                    "`marcarComoPronto`."
                ),
            },
            # ---- BLOCO 4: DEMO DE RESILIENCIA ----
            {
                **_request_login(
                    "06 - Re-login como Usuario (preparar bloco 4)",
                    "usuarioEmail",
                    "usuarioSenha",
                ),
                "description": (
                    "ROTEIRO BLOCO 4 — Antes da demo de resiliencia, volte o "
                    "{{token}} para USUARIO. O pedido a ser criado nos proximos "
                    "passos pertencera ao cliente comum (ownership check)."
                ),
            },
            {
                **_request_criar_pedido("07 - Criar Pedido (gateway off - bloco 4.2)"),
                "description": (
                    "ROTEIRO BLOCO 4.2 — Cria novo pedido. Pre-requisito: "
                    "`docker stop procpag` ja foi executado no terminal."
                ),
            },
            {
                **_request_confirmar("08 - Confirmar Pedido (gateway off - bloco 4.2)"),
                "description": (
                    "ROTEIRO BLOCO 4.2 — Confirma o pedido com o gateway fora. "
                    "O pagamento-service vai tentar 3x (Retry), abrir o Circuit "
                    "Breaker apos varias falhas e cair no Fallback. Acompanhe em "
                    "`docker logs -f pagamento-service` no terminal."
                ),
            },
            {
                **_request_pedido_por_id(
                    "09 - Pedido por ID (espera PENDENTE_PAGAMENTO - bloco 4.3)"
                ),
                "description": (
                    "ROTEIRO BLOCO 4.3 — Aguarde ~10-15s apos o passo 08. Status "
                    "esperado: PENDENTE_PAGAMENTO. O cliente nao recebeu erro — "
                    "o sistema absorveu a falha."
                ),
            },
            {
                **_request_pedido_por_id(
                    "10 - Pedido por ID (espera PAGO apos reprocesso - bloco 4.4)"
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


if __name__ == "__main__":
    main()
