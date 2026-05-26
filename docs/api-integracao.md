# Guia de Integração — API do FIAP Restaurante

Este documento descreve **como integrar com o sistema FIAP
Restaurante**. Cobre o contrato de cada operação dos 4 microsserviços:
URL, autenticação, payload, response e erros possíveis.

> **Resumo em uma frase:** todos os 4 microsserviços expõem uma
> **API GraphQL** no endpoint `POST /graphql`, autenticada via
> JWT no header `Authorization: Bearer <token>` (exceto o `login`
> e `cadastrarUsuario` do `usuario-autenticacao`).

## Índice

1. [Visão geral](#1-visão-geral)
2. [Autenticação (`usuario-autenticacao` :8081)](#2-autenticação)
3. [Pedidos (`restaurante-pedido` :8082)](#3-pedidos)
4. [Pagamentos (`pagamento-service` :8083)](#4-pagamentos)
5. [Cozinha (`restaurante-service` :8084)](#5-cozinha)
6. [Fluxo recomendado de integração](#6-fluxo-recomendado-de-integração)
7. [Códigos de erro](#7-códigos-de-erro)
8. [Como descobrir o schema em runtime](#8-como-descobrir-o-schema-em-runtime)

---

## 1. Visão geral

| Serviço | Porta host | Endpoint GraphQL | Autenticação |
|---|---|---|---|
| `usuario-autenticacao` | `8081` | `POST http://localhost:8081/graphql` | **Não** para `cadastrarUsuario` e `login`; **JWT** para `me` |
| `restaurante-pedido` | `8082` | `POST http://localhost:8082/graphql` | **JWT** (perfil `USUARIO` ou `DONO_RESTAURANTE`) |
| `pagamento-service` | `8083` | `POST http://localhost:8083/graphql` | **JWT** |
| `restaurante-service` | `8084` | `POST http://localhost:8084/graphql` | **JWT** com perfil `DONO_RESTAURANTE` |

### Como uma chamada GraphQL é estruturada

GraphQL usa **um único endpoint** com `POST`. O cliente envia um JSON com 2 campos:

```json
{
  "query": "<a query ou mutation>",
  "variables": { "...": "..." }
}
```

E recebe um JSON com 2 campos:

```json
{
  "data":  { "...": "..." },
  "errors": [ /* só aparece em caso de erro */ ]
}
```

**Headers obrigatórios:**

| Header | Valor | Quando |
|---|---|---|
| `Content-Type` | `application/json` | Sempre |
| `Authorization` | `Bearer <jwt>` | Em toda chamada exceto `cadastrarUsuario` e `login` |

### Console interativo (GraphiQL)

Cada serviço tem GraphiQL embutido — ideal para explorar o schema, montar queries e testar. Abra no navegador:

- `http://localhost:8081/graphiql` — autenticação
- `http://localhost:8082/graphiql` — pedidos
- `http://localhost:8083/graphiql` — pagamentos
- `http://localhost:8084/graphiql` — cozinha

Para usar com JWT no GraphiQL, cole o header em **Request Headers** (canto inferior):

```json
{ "Authorization": "Bearer <token>" }
```

---

## 2. Autenticação

Base: `POST http://localhost:8081/graphql`

### 2.1 Cadastrar usuário

Cria uma nova conta. **Não exige autenticação.**

**Body:**
```json
{
  "query": "mutation Cadastrar($input: CadastrarUsuarioInput!) { cadastrarUsuario(input: $input) { id nome email perfil criadoEm } }",
  "variables": {
    "input": {
      "nome": "Maria Silva",
      "email": "maria@fiap.com",
      "senha": "minha-senha-segura",
      "perfil": "USUARIO"
    }
  }
}
```

**Campos de entrada:**

| Campo | Tipo | Obrigatório | Notas |
|---|---|---|---|
| `nome` | String | sim | nome completo |
| `email` | String | sim | único; usado no login |
| `senha` | String | sim | será hasheada com BCrypt |
| `perfil` | enum `PerfilUsuario` | sim | `USUARIO` (cliente) ou `DONO_RESTAURANTE` (admin) |

**Response (200 OK):**
```json
{
  "data": {
    "cadastrarUsuario": {
      "id": "11111111-1111-4111-8111-111111111111",
      "nome": "Maria Silva",
      "email": "maria@fiap.com",
      "perfil": "USUARIO",
      "criadoEm": "2026-05-25T12:00:00Z"
    }
  }
}
```

**Erros possíveis:**

| Cenário | `errors[].extensions.classification` |
|---|---|
| E-mail já cadastrado | `BAD_REQUEST` |
| Validação (nome vazio, e-mail inválido, senha curta) | `BAD_REQUEST` |

### 2.2 Login

Autentica e retorna o JWT.

**Body:**
```json
{
  "query": "mutation Login($input: LoginInput!) { login(input: $input) { token tipoToken expiraEmSegundos usuario { id nome perfil } } }",
  "variables": {
    "input": {
      "email": "maria@fiap.com",
      "senha": "minha-senha-segura"
    }
  }
}
```

**Response (200 OK):**
```json
{
  "data": {
    "login": {
      "token": "eyJraWQiOiJ...",
      "tipoToken": "Bearer",
      "expiraEmSegundos": 3600,
      "usuario": {
        "id": "11111111-1111-4111-8111-111111111111",
        "nome": "Maria Silva",
        "perfil": "USUARIO"
      }
    }
  }
}
```

**O `token`** vai no header `Authorization: Bearer <token>` de todas as requisições subsequentes para os outros 3 serviços. Validade: 1 hora.

**Estrutura interna do JWT** (informativa — você não precisa decodificar):
```json
{
  "iss": "fiap-restaurante",
  "sub": "<UUID do usuário>",   // <-- é daqui que sai o clienteId
  "email": "maria@fiap.com",
  "nome": "Maria Silva",
  "groups": ["USUARIO"],         // <-- usado para @PreAuthorize
  "iat": 1716...,
  "exp": 1716...
}
```

### 2.3 Consultar usuário corrente (`me`)

Retorna os dados do usuário do JWT. **Requer autenticação.**

**Body:**
```json
{
  "query": "{ me { id nome email perfil } }"
}
```

**Headers:**
```
Authorization: Bearer <token>
```

---

## 3. Pedidos

Base: `POST http://localhost:8082/graphql`

**Todas as operações requerem JWT** (`USUARIO` ou `DONO_RESTAURANTE`).

### 3.1 Criar pedido

Cria um pedido no status `CRIADO` e calcula o `valorTotal` automaticamente.

**Importante:** o `clienteId` **NÃO** é enviado pelo cliente — é extraído do JWT (`subject`). Isso é parte do requisito 5.2 da spec.

**Body:**
```json
{
  "query": "mutation Criar($input: CriarPedidoInput!) { criarPedido(input: $input) { id status valorTotal itens { produtoId nome quantidade preco subtotal } } }",
  "variables": {
    "input": {
      "restauranteId": "22222222-2222-4222-8222-222222222222",
      "itens": [
        {
          "produtoId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
          "nome": "X-Burger",
          "quantidade": 2,
          "preco": "25.90"
        },
        {
          "produtoId": "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
          "nome": "Batata frita G",
          "quantidade": 1,
          "preco": "15.00"
        }
      ]
    }
  }
}
```

**Notas:**
- `preco` é serializado como **string** (scalar `BigDecimal` — preserva precisão monetária)
- `subtotal` por item (`preco × quantidade`) e `valorTotal` (soma dos subtotais) são calculados pelo servidor

**Response (200 OK):**
```json
{
  "data": {
    "criarPedido": {
      "id": "33333333-3333-4333-8333-333333333333",
      "status": "CRIADO",
      "valorTotal": "66.80",
      "itens": [
        {
          "produtoId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
          "nome": "X-Burger",
          "quantidade": 2,
          "preco": "25.90",
          "subtotal": "51.80"
        },
        {
          "produtoId": "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb",
          "nome": "Batata frita G",
          "quantidade": 1,
          "preco": "15.00",
          "subtotal": "15.00"
        }
      ]
    }
  }
}
```

### 3.2 Confirmar pedido

Transita `CRIADO → CONFIRMADO` e **publica o evento `pedido.criado` no Kafka** — disparando o fluxo de pagamento.

**Body:**
```json
{
  "query": "mutation Confirmar($id: ID!) { confirmarPedido(pedidoId: $id) { id status updatedAt } }",
  "variables": {
    "id": "33333333-3333-4333-8333-333333333333"
  }
}
```

**Response (200 OK):**
```json
{
  "data": {
    "confirmarPedido": {
      "id": "33333333-3333-4333-8333-333333333333",
      "status": "CONFIRMADO",
      "updatedAt": "2026-05-25T12:00:05Z"
    }
  }
}
```

**O que acontece em background:**
1. Evento `pedido.criado` é publicado no Kafka
2. O `pagamento-service` consome e chama o procpag
3. Em ~5 segundos, o status do pedido transita para `PAGO` (ou `PENDENTE_PAGAMENTO` se o gateway estiver fora)
4. Após `PAGO`, evento `pedido.pronto-para-cozinha` é publicado
5. O `restaurante-service` consome e cria o `PedidoCozinha`

### 3.3 Consultar pedido por ID

**Importante:** verifica `ownership` — só retorna se o pedido pertencer ao cliente do JWT. Pedido de outro cliente retorna `null` (mesma resposta que "não encontrado", para não vazar a existência do recurso).

**Body:**
```json
{
  "query": "query PedidoPorId($id: ID!) { pedidoPorId(pedidoId: $id) { id status valorTotal pagamentoId motivoPendencia createdAt updatedAt itens { nome quantidade preco subtotal } } }",
  "variables": {
    "id": "33333333-3333-4333-8333-333333333333"
  }
}
```

**Response — pedido seu, status `PAGO`:**
```json
{
  "data": {
    "pedidoPorId": {
      "id": "33333333-3333-4333-8333-333333333333",
      "status": "PAGO",
      "valorTotal": "66.80",
      "pagamentoId": "44444444-4444-4444-8444-444444444444",
      "motivoPendencia": null,
      "createdAt": "2026-05-25T12:00:00Z",
      "updatedAt": "2026-05-25T12:00:05Z",
      "itens": [ /* ... */ ]
    }
  }
}
```

**Response — pedido de outro cliente OU inexistente:**
```json
{ "data": { "pedidoPorId": null } }
```

**Valores possíveis de `status`:** `CRIADO`, `CONFIRMADO`, `PAGO`, `PENDENTE_PAGAMENTO`, `EM_PREPARO`, `PRONTO`, `CANCELADO`.

### 3.4 Listar meus pedidos

Lista todos os pedidos do cliente autenticado (filtrado por `clienteId` do JWT), do mais recente para o mais antigo.

**Body:**
```json
{
  "query": "{ meusPedidos { id status valorTotal createdAt } }"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "meusPedidos": [
      { "id": "...", "status": "PRONTO", "valorTotal": "66.80", "createdAt": "..." },
      { "id": "...", "status": "PAGO", "valorTotal": "32.00", "createdAt": "..." }
    ]
  }
}
```

---

## 4. Pagamentos

Base: `POST http://localhost:8083/graphql`

**Todas as operações requerem JWT.** Note que **não há mutation aqui** — o pagamento é orquestrado por eventos Kafka (publicados pelo `restaurante-pedido` ao confirmar). Este serviço expõe apenas consultas de leitura.

### 4.1 Consultar pagamento por pedido

Retorna o registro de pagamento associado a um `pedidoId`.

**Body:**
```json
{
  "query": "query Pagamento($pedidoId: ID!) { pagamentoPorPedido(pedidoId: $pedidoId) { id status valor tentativas motivoFalha createdAt updatedAt } }",
  "variables": {
    "pedidoId": "33333333-3333-4333-8333-333333333333"
  }
}
```

**Response — pagamento aprovado:**
```json
{
  "data": {
    "pagamentoPorPedido": {
      "id": "44444444-4444-4444-8444-444444444444",
      "status": "APROVADO",
      "valor": 66.80,
      "tentativas": 1,
      "motivoFalha": null,
      "createdAt": "2026-05-25T12:00:03Z",
      "updatedAt": "2026-05-25T12:00:05Z"
    }
  }
}
```

**Response — pagamento pendente:**
```json
{
  "data": {
    "pagamentoPorPedido": {
      "id": "44444444-4444-4444-8444-444444444444",
      "status": "PENDENTE",
      "valor": 66.80,
      "tentativas": 3,
      "motivoFalha": "Gateway externo indisponível após retries",
      "createdAt": "2026-05-25T12:00:03Z",
      "updatedAt": "2026-05-25T12:00:15Z"
    }
  }
}
```

**Valores possíveis de `status`:** `PENDENTE`, `APROVADO`, `RECUSADO`.

### 4.2 Listar pagamentos pendentes

Lista todos os pagamentos no status `PENDENTE`. Útil para dashboards operacionais e para verificar o estado da fila de reprocessamento.

**Body:**
```json
{
  "query": "{ pagamentosPendentes { id pedidoId valor tentativas motivoFalha createdAt } }"
}
```

---

## 5. Cozinha

Base: `POST http://localhost:8084/graphql`

**Todas as operações requerem JWT com perfil `DONO_RESTAURANTE`.** Usuários com perfil `USUARIO` recebem `FORBIDDEN`.

### 5.1 Consultar fila

Lista pedidos na fila da cozinha, opcionalmente filtrados por status. Ordenados do mais antigo para o mais recente (FIFO de preparo).

**Body — todos os pedidos:**
```json
{
  "query": "query Fila($status: String) { filaCozinha(status: $status) { id pedidoId status createdAt itens { nome quantidade } } }",
  "variables": {
    "status": null
  }
}
```

**Body — só os em preparo:**
```json
{
  "variables": { "status": "EM_PREPARO" }
}
```

**Response (200 OK):**
```json
{
  "data": {
    "filaCozinha": [
      {
        "id": "55555555-5555-4555-8555-555555555555",
        "pedidoId": "33333333-3333-4333-8333-333333333333",
        "status": "RECEBIDO",
        "createdAt": "2026-05-25T12:00:08Z",
        "itens": [
          { "nome": "X-Burger", "quantidade": 2 },
          { "nome": "Batata frita G", "quantidade": 1 }
        ]
      }
    ]
  }
}
```

**Valores possíveis de `status`:** `RECEBIDO`, `EM_PREPARO`, `PRONTO`. Status inválido retorna `BAD_REQUEST`.

### 5.2 Consultar pedido da cozinha por ID

**Body:**
```json
{
  "query": "query($id: ID!) { pedidoCozinhaPorId(pedidoCozinhaId: $id) { id pedidoId status iniciadoEm finalizadoEm itens { produtoId nome quantidade } } }",
  "variables": {
    "id": "55555555-5555-4555-8555-555555555555"
  }
}
```

### 5.3 Iniciar preparo

Transita `RECEBIDO → EM_PREPARO` e **publica o evento `pedido.em-preparo` no Kafka**. O `restaurante-pedido` consome e atualiza o status do `Pedido` principal.

**Body:**
```json
{
  "query": "mutation($id: ID!) { iniciarPreparo(pedidoCozinhaId: $id) { id status iniciadoEm } }",
  "variables": {
    "id": "55555555-5555-4555-8555-555555555555"
  }
}
```

**Response (200 OK):**
```json
{
  "data": {
    "iniciarPreparo": {
      "id": "55555555-5555-4555-8555-555555555555",
      "status": "EM_PREPARO",
      "iniciadoEm": "2026-05-25T12:01:00Z"
    }
  }
}
```

**Erros:**
- Pedido não está em `RECEBIDO` → `BAD_REQUEST` ("pedido nao pode iniciar preparo no status XXXX")
- Pedido inexistente → `NOT_FOUND`

### 5.4 Marcar como pronto

Transita `EM_PREPARO → PRONTO` e **publica o evento `pedido.pronto` no Kafka**.

**Body:**
```json
{
  "query": "mutation($id: ID!) { marcarComoPronto(pedidoCozinhaId: $id) { id status finalizadoEm } }",
  "variables": {
    "id": "55555555-5555-4555-8555-555555555555"
  }
}
```

**Erros:**
- Pedido não está em `EM_PREPARO` → `BAD_REQUEST`
- Pedido inexistente → `NOT_FOUND`

---

## 6. Fluxo recomendado de integração

Para um cliente novo integrar do zero, esta é a sequência completa:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Cadastrar (apenas se conta não existe)                   │
│    POST :8081/graphql  mutation cadastrarUsuario(...)       │
│    (sem JWT)                                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Login                                                    │
│    POST :8081/graphql  mutation login(...)                  │
│    → guarda o token retornado                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Criar pedido                                             │
│    POST :8082/graphql  mutation criarPedido(...)            │
│    Authorization: Bearer <token>                            │
│    → guarda o pedidoId; status: CRIADO                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Confirmar pedido                                         │
│    POST :8082/graphql  mutation confirmarPedido(pedidoId)   │
│    status: CONFIRMADO; pedido.criado publicado no Kafka     │
└─────────────────────────────────────────────────────────────┘
                            ↓ (~5 segundos depois, em background)
┌─────────────────────────────────────────────────────────────┐
│ 5. Polling do status                                        │
│    POST :8082/graphql  query pedidoPorId(...)               │
│    Resultado esperado: PAGO (ou PENDENTE_PAGAMENTO)         │
└─────────────────────────────────────────────────────────────┘
                            ↓ (se for um cliente do tipo "dono")
┌─────────────────────────────────────────────────────────────┐
│ 6. Cozinha (perfil DONO_RESTAURANTE)                        │
│    POST :8084/graphql                                       │
│    a) query filaCozinha → lista pedidos RECEBIDO            │
│    b) mutation iniciarPreparo                               │
│    c) mutation marcarComoPronto                             │
└─────────────────────────────────────────────────────────────┘
```

### Exemplo end-to-end via `curl`

```bash
# 1) Login
TOKEN=$(curl -s -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { login(input: { email: \"usuario@fiap.com\", senha: \"usuario123\" }) { token } }"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['login']['token'])")

echo "Token: $TOKEN"

# 2) Criar pedido
PEDIDO_ID=$(curl -s -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "query": "mutation($i: CriarPedidoInput!) { criarPedido(input: $i) { id } }",
    "variables": {
      "i": {
        "restauranteId": "22222222-2222-4222-8222-222222222222",
        "itens": [{
          "produtoId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
          "nome": "X-Burger", "quantidade": 1, "preco": "25.90"
        }]
      }
    }
  }' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['criarPedido']['id'])")

# 3) Confirmar
curl -s -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"query\":\"mutation { confirmarPedido(pedidoId: \\\"$PEDIDO_ID\\\") { id status } }\"}"

# 4) Aguardar ~5s e consultar status
sleep 5
curl -s -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"query\":\"{ pedidoPorId(pedidoId: \\\"$PEDIDO_ID\\\") { status } }\"}"
# Esperado: {"data":{"pedidoPorId":{"status":"PAGO"}}}
```

---

## 7. Códigos de erro

GraphQL não usa códigos HTTP convencionais para erros de negócio — a resposta vem em `200 OK` com um array `errors`. A **classificação** do erro fica em `errors[].extensions.classification`:

| `classification` | Significado | HTTP equivalente |
|---|---|---|
| `BAD_REQUEST` | Erro de validação, dados inválidos, transição de estado proibida | 400 |
| `UNAUTHORIZED` | JWT ausente, expirado ou inválido | 401 |
| `FORBIDDEN` | JWT válido, mas perfil insuficiente (ex.: `USUARIO` tentando mutation da cozinha) | 403 |
| `NOT_FOUND` | Recurso inexistente (ex.: `iniciarPreparo` de um id que não existe na fila) | 404 |
| `INTERNAL_ERROR` | Erro inesperado no servidor (raro; logado para diagnóstico) | 500 |

**Exemplo de resposta com erro:**
```json
{
  "errors": [
    {
      "message": "pedido nao pode iniciar preparo no status EM_PREPARO",
      "extensions": { "classification": "BAD_REQUEST" }
    }
  ],
  "data": { "iniciarPreparo": null }
}
```

> **Status HTTP do payload:** o `restaurante-pedido` tem um adicional `GraphQlHttpStatusConfig` que traduz a classification de erro para o status HTTP de fato (400, 403, 404, 500) — facilita integração com clientes REST tradicionais. Os outros 3 serviços retornam sempre `200 OK` com o erro no body (padrão GraphQL puro).

---

## 8. Como descobrir o schema em runtime

GraphQL é **introspectivo** — o servidor publica o schema completo para clientes:

**Via GraphiQL:** abra o navegador em `http://localhost:8082/graphiql` e clique em **"Docs"** no canto superior direito. Você vê todos os tipos, queries, mutations, com tipagem e descrição.

**Via curl (introspection query):**
```bash
curl -s -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ __schema { types { name kind description } } }"}'
```

**Via OpenAPI (apenas no `restaurante-pedido`):**

O `restaurante-pedido` também publica um contrato OpenAPI/Swagger documentando o endpoint `POST /graphql`:

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- JSON: `http://localhost:8082/v3/api-docs`

Útil quando o cliente usa ferramentas que não falam GraphQL (Postman REST, geração de stubs).

---

## Referência rápida — cheatsheet

| Quero... | Endpoint | Operação | Auth |
|---|---|---|---|
| Cadastrar conta | `:8081/graphql` | `mutation cadastrarUsuario` | — |
| Logar e obter JWT | `:8081/graphql` | `mutation login` | — |
| Ver dados do meu usuário | `:8081/graphql` | `query me` | JWT |
| Criar pedido | `:8082/graphql` | `mutation criarPedido` | JWT |
| Confirmar pedido | `:8082/graphql` | `mutation confirmarPedido` | JWT |
| Consultar 1 pedido (com ownership) | `:8082/graphql` | `query pedidoPorId` | JWT |
| Listar meus pedidos | `:8082/graphql` | `query meusPedidos` | JWT |
| Status do pagamento de 1 pedido | `:8083/graphql` | `query pagamentoPorPedido` | JWT |
| Listar pagamentos pendentes | `:8083/graphql` | `query pagamentosPendentes` | JWT |
| Ver fila da cozinha | `:8084/graphql` | `query filaCozinha` | JWT (DONO_RESTAURANTE) |
| Iniciar preparo | `:8084/graphql` | `mutation iniciarPreparo` | JWT (DONO_RESTAURANTE) |
| Marcar como pronto | `:8084/graphql` | `mutation marcarComoPronto` | JWT (DONO_RESTAURANTE) |
