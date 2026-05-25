# Sequência — Resiliência (gateway indisponível)

Fluxo de **falha + recuperação** quando o gateway externo (`procpag`)
está fora do ar. Cobre os requisitos 4.5 (pagamento pendente) e 4.6
(reprocessamento automático).

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Pedido as restaurante-pedido
    participant Kafka as Kafka
    participant Pagamento as pagamento
    participant Procpag as procpag (DOWN)
    participant Worker as pagamento.@Scheduled

    Note over Procpag: docker stop procpag

    Cliente->>Pedido: 1. mutation criarPedido + confirmarPedido
    Pedido->>Kafka: publish pedido.criado
    Pedido-->>Cliente: Pedido { status: CONFIRMADO }

    Kafka->>Pagamento: 2. consume pedido.criado
    Pagamento->>Procpag: HTTP POST /requisicao
    Note over Procpag: connection refused / timeout

    Note over Pagamento: Resilience4j (Retry: 3x com backoff exp)
    Pagamento->>Procpag: retry 1
    Pagamento->>Procpag: retry 2
    Pagamento->>Procpag: retry 3
    Note over Pagamento: Falhas atingem o threshold do<br/>Circuit Breaker (5 chamadas, 50% falha)<br/>=> CB abre

    Pagamento->>Pagamento: fallback: Pagamento { status: PENDENTE }
    Pagamento->>Kafka: publish pagamento.pendente

    Kafka->>Pedido: 3. consume pagamento.pendente
    Pedido->>Pedido: status -> PENDENTE_PAGAMENTO

    Cliente->>Pedido: 4. query pedidoPorId
    Pedido-->>Cliente: Pedido { status: PENDENTE_PAGAMENTO }

    Note over Procpag: docker start procpag<br/>(serviço volta)

    loop A cada 30s (@Scheduled)
        Worker->>Pagamento: buscar pendentes (batch 20)
        alt enquanto procpag fora
            Worker->>Procpag: tenta reprocessar
            Note over Procpag: ainda fora -> sem efeito
        else procpag voltou
            Worker->>Procpag: 5. HTTP POST /requisicao
            Procpag-->>Worker: 200 OK
            Worker->>Pagamento: Pagamento { status: APROVADO }
            Worker->>Kafka: publish pagamento.aprovado
        end
    end

    Kafka->>Pedido: 6. consume pagamento.aprovado
    Pedido->>Pedido: status -> PAGO

    Cliente->>Pedido: 7. query pedidoPorId
    Pedido-->>Cliente: Pedido { status: PAGO }
```

## Pontos-chave

- **Cliente nunca recebe erro:** o passo 1 retorna `CONFIRMADO`
  normalmente, mesmo com o gateway fora. A falha fica isolada no
  `pagamento`.
- **Circuit Breaker abre rápido:** após 5 chamadas com 50% de
  falha (configurado no `application.properties` do pagamento),
  o CB abre. Chamadas subsequentes são **rejeitadas em ms** via
  `CallNotPermittedException` — não pagam o overhead de tentar.
- **Retry ignora `CallNotPermittedException`:** evita pagar 3
  retries quando o CB já está aberto.
- **Worker reprocessa por tempo indeterminado:** enquanto o
  `procpag` estiver fora, o worker continua tentando a cada 30s
  sem desistir. **Não há retry limit no nível do worker** — a
  premissa é "vai voltar uma hora".
- **Pedido converge para `PAGO`:** quando o worker tem sucesso,
  publica `pagamento.aprovado`, e o `restaurante-pedido` atualiza
  o status. Mesmo fluxo do happy path a partir daí.

## Como demonstrar

```bash
docker stop procpag                          # derruba o gateway
# Postman: criarPedido + confirmarPedido     # pedido vira PENDENTE_PAGAMENTO em ~10s
docker start procpag                         # gateway volta
# Aguarde ~30s -- worker reprocessa sozinho
# Postman: pedidoPorId                       # status: PAGO
```

Logs úteis durante a demo:

```bash
docker logs -f pagamento                                   # retries e CB
curl http://localhost:8083/actuator/circuitbreakers        # estado do CB
curl http://localhost:8083/actuator/scheduledtasks         # confirma worker ativo
```
