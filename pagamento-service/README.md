# pagamento-service

Microsserviço de **processamento de pagamentos** — o nome bate literalmente com o requisito 5.1 da spec. Atende aos requisitos 4.4 (processamento via gateway externo), 4.5 (pendente em falha) e 4.6 (reprocessamento automático). É o componente responsável pelos pontos de **resiliência** exigidos pelo requisito 5.4.

## O que faz

- **Consome** `pedido.criado` (publicado pelo `restaurante-pedido`)
- **Chama o gateway externo `procpag`** via HTTP, protegido por Resilience4j (Circuit Breaker + Retry + Timeout + Fallback)
- **Publica** `pagamento.aprovado` ou `pagamento.pendente` conforme resultado
- **Reprocessa pendentes** via worker `@Scheduled` que roda a cada 30s — quando o gateway voltar, drena a fila

## Portas

| Porta | Uso |
|---|---|
| `8083` | HTTP — API GraphQL + Actuator (`/actuator/health`, `/circuitbreakers`, `/retries`, `/scheduledtasks`) |

## Dependências externas

- **MySQL** (`pagamento_db`)
- **Kafka** (consome 1 tópico, publica em 2)
- **procpag** (gateway externo) — chamada HTTP via `ExternalPaymentClient` na porta `:8089`

## Eventos Kafka

| Tópico | Direção | Quando |
|---|---|---|
| `pedido.criado` | consome | Dispara processamento da cobrança |
| `pagamento.aprovado` | publica | Gateway aprovou |
| `pagamento.pendente` | publica | Gateway indisponível (fallback) |

## Padrões de resiliência (Resilience4j)

Aplicados em `ExternalPaymentClient.processar()` como anotações declarativas:

| Padrão | Configuração |
|---|---|
| **`@Retry`** | 3 tentativas com backoff exponencial (2s, 4s, 8s); ignora `CallNotPermittedException` |
| **`@CircuitBreaker`** | Janela deslizante de 5 chamadas; abre em 50% de falhas; espera 30s antes de HALF_OPEN |
| **Timeout** | 5s por chamada (via HTTP client síncrono; ver Javadoc do client para o motivo de não usar `@TimeLimiter`) |
| **Fallback** | Marca pagamento como `PENDENTE` + publica `pagamento.pendente` |

Estado em runtime: `curl http://localhost:8083/actuator/circuitbreakers`.

## Worker de reprocessamento (`ReprocessamentoPagamentoWorker`)

`@Scheduled` que vive **somente neste módulo** — porque só o `pagamento-service` é dono do estado de pagamento e da integração com o `procpag`. Configurado por properties:

```properties
pagamento.reprocess.fixed-delay-ms=30000     # intervalo entre execucoes (30s)
pagamento.reprocess.initial-delay-ms=15000   # espera apos o startup (15s)
pagamento.reprocess.batch-size=20            # maximo de pendentes por ciclo
```

## Como rodar isoladamente

```bash
docker compose up -d --build pagamento-service mysql kafka procpag
```

## Endpoints úteis

| URL | Para que |
|---|---|
| `http://localhost:8083/graphiql` | GraphiQL interativo |
| `http://localhost:8083/actuator/circuitbreakers` | Estado do CB em runtime |
| `http://localhost:8083/actuator/scheduledtasks` | Confirma worker ativo |

## Testes

```bash
./mvnw -pl pagamento-service -am test
```

**~60 testes** verdes — domain, use cases, ExternalPaymentClient (mock do procpag), worker, adapters Kafka/JPA/GraphQL.

## Demo de resiliência (req. 4.5/4.6)

```bash
docker stop procpag      # derruba o gateway
# crie+confirme um pedido via Postman -> status: PENDENTE_PAGAMENTO
docker start procpag     # gateway volta
# aguarde ~30s -> worker reprocessa -> status: PAGO
```

Logs ao vivo: `docker logs -f pagamento-service`.

## Referências

- [Documentação geral](../README.md)
- [Documentação técnica ABNT](../docs/documentacao-arquitetura.pdf) (capítulos 3.2.3 e 5)
- [ADR 0009 — Resilience4j](../docs/adr/0009-resilience4j.md)
- [Diagrama de sequência (resiliência)](../docs/diagramas/sequencia-resiliencia.md)
