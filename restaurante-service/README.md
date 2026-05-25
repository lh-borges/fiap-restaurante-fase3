# restaurante-service

Microsserviço **opcional** (req. 5.1 da spec — "(Opcional) restaurante-service, que recebe um aviso de que o pedido foi confirmado e começa a preparar o pedido"). Modela o **bounded context da cozinha**.

A inclusão deste módulo expande o fluxo do pedido até a entrega: depois de pago, o pedido entra na fila de produção; o dono do restaurante avança o estado conforme prepara.

## O que faz

- **Consome** `pedido.pronto-para-cozinha` (publicado pelo `restaurante-pedido` após o status virar `PAGO`)
- Cria um agregado `PedidoCozinha` no estado `RECEBIDO`
- **Expõe mutations GraphQL** para o dono do restaurante avançar o estado:
  - `iniciarPreparo(pedidoCozinhaId)` → `EM_PREPARO`
  - `marcarComoPronto(pedidoCozinhaId)` → `PRONTO`
- **Publica** `pedido.em-preparo` e `pedido.pronto` para o `restaurante-pedido` refletir o status no pedido principal
- **Expõe queries** (`filaCozinha`, `pedidoCozinhaPorId`) para o dono acompanhar a operação

Todas as mutations são restritas ao perfil `DONO_RESTAURANTE` via `@PreAuthorize`.

## Portas

| Porta | Uso |
|---|---|
| `8084` | HTTP — API GraphQL + Actuator |

## Dependências externas

- **MySQL** (`cozinha_db`)
- **Kafka** (consome 1 tópico, publica em 2)

## Modelo de domínio

O agregado central é `PedidoCozinha`:

```
domain/entity/PedidoCozinha
  - id, pedidoId, restauranteId, itens, status
  - createdAt, updatedAt, iniciadoEm, finalizadoEm
  - transições: iniciarPreparo(), marcarComoPronto()

domain/entity/ItemCozinha (value object)
  - produtoId, nome, quantidade
  ← sem preço — irrelevante para a cozinha

domain/valueobject/StatusCozinha
  - RECEBIDO, EM_PREPARO, PRONTO
```

## Eventos Kafka

| Tópico | Direção | Quando |
|---|---|---|
| `pedido.pronto-para-cozinha` | consome | Pedido virou PAGO, entra na fila |
| `pedido.em-preparo` | publica | Dono iniciou preparo |
| `pedido.pronto` | publica | Dono marcou como pronto |

## Como rodar isoladamente

```bash
docker compose up -d --build restaurante-service mysql kafka
```

## Endpoints úteis

| URL | Para que |
|---|---|
| `http://localhost:8084/graphiql` | GraphiQL interativo (logue como DONO_RESTAURANTE) |
| `http://localhost:8084/graphql` | Endpoint GraphQL |
| `http://localhost:8084/actuator/health` | Healthcheck |

## Como testar manualmente

A coleção Postman cobre o fluxo até `PAGO`. Para continuar até `PRONTO`:

1. Faça login como `dono@fiap.com` / `dono123` no GraphiQL do `usuario-autenticacao` (`:8081`) e copie o token.
2. Abra `http://localhost:8084/graphiql`, cole o token em **Headers**:
   ```json
   {"Authorization": "Bearer <token>"}
   ```
3. Rode:
   ```graphql
   query { filaCozinha { id pedidoId status } }
   ```
4. Aciones as mutations:
   ```graphql
   mutation { iniciarPreparo(pedidoCozinhaId: "<id>") { status } }
   mutation { marcarComoPronto(pedidoCozinhaId: "<id>") { status } }
   ```
5. Volte ao Postman → `Pedido por ID`: o pedido original reflete `EM_PREPARO` → `PRONTO`.

## Testes

```bash
./mvnw -pl restaurante-service -am test
```

**39 testes** verdes — domain (`PedidoCozinha`, `ItemCozinha`), 4 use cases, adapter JPA + Kafka, GraphQL controller, smoke de contexto.

## Referências

- [Documentação geral](../README.md)
- [Documentação técnica ABNT](../docs/documentacao-arquitetura.pdf) (capítulo 3.2.4)
- [ADR 0010 — Bounded context separado para a cozinha](../docs/adr/0010-restaurante-service-bounded-context.md)
- [Diagrama de sequência (happy path completo)](../docs/diagramas/sequencia-happy-path.md)
