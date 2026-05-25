# restaurante-pedido

Microsserviço de **ciclo de vida do pedido**. Atende aos requisitos funcionais 4.2 (criar pedido), 4.3 (consultar) e 4.7 (atualização automática de status), e participa do fluxo Kafka exigido pelo requisito 5.3.

Este módulo é o **núcleo de negócio** do sistema: tudo começa aqui (criação do pedido) e converge aqui (status final reflete o resultado de pagamento e de produção pela cozinha).

## O que faz

- **Criar pedido** (mutation `criarPedido`) — calcula `valorTotal`; `clienteId` extraído do JWT (req. 5.2.d)
- **Confirmar pedido** (mutation `confirmarPedido`) — publica `pedido.criado` no Kafka, disparando o pagamento
- **Consultar por ID** (query `pedidoPorId`) — com **ownership check** (só dono do pedido vê)
- **Listar do cliente autenticado** (query `meusPedidos`)
- **Atualiza status automaticamente** a partir de eventos Kafka — sem intervenção do cliente

## Portas

| Porta | Uso |
|---|---|
| `8082` | HTTP — API GraphQL + Swagger UI + Actuator |

## Dependências externas

- **MySQL** (`pedido_db`)
- **Kafka** (publica em 2 tópicos; consome de 4)
- **usuario-autenticacao** via gRPC (validação síncrona; chamada lazy quando necessário)

## Eventos Kafka

| Tópico | Direção | Quando |
|---|---|---|
| `pedido.criado` | publica | Após `confirmarPedido` |
| `pedido.pronto-para-cozinha` | publica | Após status virar `PAGO` |
| `pagamento.aprovado` | consome | Atualiza pedido para `PAGO` |
| `pagamento.pendente` | consome | Atualiza pedido para `PENDENTE_PAGAMENTO` |
| `pedido.em-preparo` | consome | Atualiza pedido para `EM_PREPARO` |
| `pedido.pronto` | consome | Atualiza pedido para `PRONTO` |

## Máquina de estados do `Pedido`

```
CRIADO → CONFIRMADO → PAGO → EM_PREPARO → PRONTO
                  ↘ PENDENTE_PAGAMENTO → PAGO ↗
                  ↘ CANCELADO
```

Diagrama detalhado em [`docs/diagramas/maquina-estados-pedido.md`](../docs/diagramas/maquina-estados-pedido.md).

## Stack específica

- Spring GraphQL 4 + GraphiQL embutido
- Spring Kafka 4
- Spring Data JPA + MySQL 8.4 (H2 em testes)
- Spring Security 7 (Resource Server validando JWT RS256)
- **SpringDoc OpenAPI** — documenta o endpoint `POST /graphql` no Swagger UI

## Como rodar isoladamente

```bash
docker compose up -d --build restaurante-pedido mysql kafka usuario-autenticacao
```

(O `usuario-autenticacao` é dependência por causa do gRPC; o restante é infra.)

## Endpoints úteis

| URL | Para que |
|---|---|
| `http://localhost:8082/graphiql` | GraphiQL interativo |
| `http://localhost:8082/graphql` | Endpoint GraphQL |
| `http://localhost:8082/swagger-ui.html` | Contrato OpenAPI |
| `http://localhost:8082/actuator/health` | Healthcheck |

## Testes

```bash
./mvnw -pl restaurante-pedido -am test
```

**126 testes** verdes — domain (`Pedido`, `ItemPedido`), 6 use cases, adapter JPA, 4 consumers Kafka, 2 publishers, GraphQL controller + slice tests + smoke de contexto.

## Referências

- [Documentação geral](../README.md)
- [Documentação técnica ABNT](../docs/documentacao-arquitetura.pdf) (capítulo 3.2.2)
- [ADR 0005 — Kafka assíncrono](../docs/adr/0005-kafka-assincrono.md)
- [Diagrama de sequência (happy path)](../docs/diagramas/sequencia-happy-path.md)
