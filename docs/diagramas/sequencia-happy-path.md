# Sequência — Happy Path (do cadastro à entrega)

Fluxo completo de um pedido bem-sucedido: cliente cria, paga e a
cozinha entrega. **9 passos**, cruzando os 4 microsserviços e o
gateway externo.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Auth as usuario-autenticacao
    participant Pedido as restaurante-pedido
    participant Kafka as Kafka
    participant Pagamento as pagamento
    participant Procpag as procpag
    participant Cozinha as restaurante-service

    Cliente->>Auth: 1. mutation login(email, senha)
    Auth-->>Cliente: JWT RS256 (subject = clienteId)

    Cliente->>Pedido: 2. mutation criarPedido(itens) + JWT
    Pedido->>Pedido: calcula valorTotal, status=CRIADO
    Pedido-->>Cliente: Pedido { id, valorTotal, status: CRIADO }

    Cliente->>Pedido: 3. mutation confirmarPedido(pedidoId)
    Pedido->>Pedido: status -> CONFIRMADO
    Pedido->>Kafka: publish pedido.criado
    Pedido-->>Cliente: Pedido { status: CONFIRMADO }

    Kafka->>Pagamento: 4. consume pedido.criado
    Pagamento->>Procpag: HTTP POST /requisicao (Resilience4j)
    Procpag-->>Pagamento: 200 OK (aprovado)
    Pagamento->>Kafka: publish pagamento.aprovado
    Pagamento->>Pagamento: salva Pagamento { status: APROVADO }

    Kafka->>Pedido: 5. consume pagamento.aprovado
    Pedido->>Pedido: status -> PAGO
    Pedido->>Kafka: publish pedido.pronto-para-cozinha

    Kafka->>Cozinha: 6. consume pedido.pronto-para-cozinha
    Cozinha->>Cozinha: cria PedidoCozinha { status: RECEBIDO }

    Note over Cliente,Cozinha: Dono do restaurante (perfil DONO_RESTAURANTE)<br/>passa a operar a fila via GraphQL em :8084

    Cliente->>Cozinha: 7. mutation iniciarPreparo(pedidoCozinhaId) + JWT(DONO)
    Cozinha->>Cozinha: status -> EM_PREPARO
    Cozinha->>Kafka: publish pedido.em-preparo

    Kafka->>Pedido: consume pedido.em-preparo
    Pedido->>Pedido: status -> EM_PREPARO

    Cliente->>Cozinha: 8. mutation marcarComoPronto(pedidoCozinhaId)
    Cozinha->>Cozinha: status -> PRONTO
    Cozinha->>Kafka: publish pedido.pronto

    Kafka->>Pedido: consume pedido.pronto
    Pedido->>Pedido: status -> PRONTO

    Cliente->>Pedido: 9. query pedidoPorId(pedidoId)
    Pedido-->>Cliente: Pedido { status: PRONTO }
```

## Pontos a observar

- **Passos 1-3 são síncronos** do ponto de vista do cliente; o
  servidor responde imediatamente. O cliente nunca espera o
  pagamento.
- **Passos 4-9 são assíncronos** via Kafka — o ciclo completo do
  pedido (`CONFIRMADO → PAGO → EM_PREPARO → PRONTO`) acontece sem
  intervenção do cliente original; ele consulta o status quando
  quiser.
- **Passos 7 e 8** são acionados pelo **dono do restaurante**
  (perfil `DONO_RESTAURANTE` no JWT) via GraphiQL em `:8084` — a
  collection Postman atual cobre até o passo 6; cozinha é via
  GraphiQL.
- **Conversa via gRPC** entre `restaurante-pedido` e `usuario-autenticacao`
  pode ocorrer no passo 2 (validação de perfil); omitida do diagrama
  para legibilidade.
