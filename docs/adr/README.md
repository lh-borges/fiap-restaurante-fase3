# Architecture Decision Records (ADRs)

Registros de decisões arquiteturais do projeto **FIAP Restaurante — Fase 3**.

Cada ADR documenta uma decisão técnica significativa no formato proposto por
Michael Nygard: **Contexto + Decisão + Consequências**. Quando alguma decisão
for revisitada no futuro, o ADR existente recebe status `Superseded by ADR-NNNN`
e um novo ADR é criado — preservando o histórico.

## Índice

| ADR | Título | Status |
|---|---|---|
| [0001](0001-microsservicos.md) | Adoção de microsserviços em vez de monolito | Accepted |
| [0002](0002-arquitetura-hexagonal.md) | Arquitetura hexagonal em cada microsserviço | Accepted |
| [0003](0003-graphql-no-entrypoint.md) | GraphQL no ponto de entrada das aplicações | Accepted |
| [0004](0004-grpc-entre-servicos.md) | gRPC para chamadas síncronas entre serviços | Accepted |
| [0005](0005-kafka-assincrono.md) | Apache Kafka para comunicação assíncrona | Accepted |
| [0006](0006-kafka-kraft.md) | Kafka em modo KRaft (sem Zookeeper) | Accepted |
| [0007](0007-database-per-service.md) | Database separado por serviço (mesmo MySQL) | Accepted |
| [0008](0008-jwt-rs256.md) | JWT com assinatura assimétrica RS256 | Accepted |
| [0009](0009-resilience4j.md) | Resilience4j para resiliência na integração externa | Accepted |
| [0010](0010-restaurante-service-bounded-context.md) | Bounded context separado para a cozinha | Accepted |
| [0011](0011-docker-compose.md) | Docker Compose como orquestrador | Accepted |
| [0012](0012-appcds-layered-jars.md) | AppCDS + Layered JARs no Dockerfile | Accepted |
| [0013](0013-resource-limits-compose.md) | Resource limits explícitos no compose | Accepted |

## Formato de cada ADR

```
# ADR NNNN: Título da decisão

- **Status:** Accepted | Proposed | Deprecated | Superseded by ADR-XXXX
- **Data:** YYYY-MM-DD
- **Deciders:** Danilo Fernando

## Contexto e problema

(O que motivou a decisão? Quais forças estão em jogo?)

## Decisão

(O que foi decidido, em uma ou duas frases.)

## Consequências

### Positivas
- (Ganhos)

### Negativas
- (Custos, riscos)

## Alternativas consideradas

- **Alternativa A** — por que foi descartada
- **Alternativa B** — por que foi descartada
```

## Como adicionar um novo ADR

1. Copie o template acima.
2. Use o próximo número sequencial (`00NN-titulo-curto-com-hifen.md`).
3. Status inicial: `Proposed`. Quando aceito, vire `Accepted` e adicione data.
4. Atualize o índice deste README.
5. Commit em branch própria + PR.
