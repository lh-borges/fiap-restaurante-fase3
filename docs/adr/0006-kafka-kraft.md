# ADR 0006: Kafka em modo KRaft (sem Zookeeper)

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando
- **Supersedes:** versão inicial do `docker-compose.yml` (commit anterior a `4e27b90`) que rodava Kafka 7.4 com Zookeeper separado.

## Contexto e problema

A versão inicial do compose subia **dois containers** para o subsistema
de mensageria: `kafka` (broker) e `zookeeper` (coordenação de metadata).
Isso somava:

- 2 containers extras (Kafka + Zookeeper)
- ~150 MB de RAM adicional (overhead do Zookeeper)
- ~10 s a mais no startup (Kafka espera o Zookeeper ficar healthy)
- Mais um ponto de configuração (healthcheck do `ruok`, `KAFKA_OPTS`
  com whitelist de comandos 4lw)

A partir do Kafka 3.3, o modo **KRaft** (Kafka Raft) chegou GA — o
broker passa a coordenar metadata sozinho, eliminando o Zookeeper.
Confluent Platform 7.4+ suporta KRaft de forma estável.

## Decisão

Rodar Kafka em **modo KRaft single-node** com `confluentinc/cp-kafka:7.7.1`.
Configuração essencial:

```yaml
KAFKA_NODE_ID: 1
KAFKA_PROCESS_ROLES: "broker,controller"
KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
KAFKA_LISTENERS: "PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093"
KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:9092"
KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
CLUSTER_ID: "MkU3OEVBNTcwNTJENDM2Qk"
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

O Zookeeper foi removido do `docker-compose.yml` e do
`docker-compose.override.yml`.

## Consequências

### Positivas

- **-1 container** no compose (de 9 para 8).
- **-150 MB de RAM** consumida pelo Zookeeper.
- **~10 s a menos** no startup do stack inteiro.
- **Menos configuração para manter** (sem `KAFKA_OPTS` com whitelist
  4lw, sem healthcheck Zookeeper).
- **Path de evolução natural:** Zookeeper será removido na Kafka 4.0.

### Negativas

- **Single-node não é production-grade:** KRaft em produção
  recomenda mínimo 3 controllers para tolerância a falhas. Para
  dev/demo, single-node é apropriado.
- **`CLUSTER_ID` precisa ser fixo entre reinicializações:** removê-lo
  faria o broker formatar o storage novamente e perder estado. Hoje
  é hard-coded no compose.

## Alternativas consideradas

- **Manter Zookeeper:** funciona, mas é dívida — Kafka 4.0 não
  suporta mais. Migrar agora evita refatoração futura.
- **Kafka via image alternativa (bitnami, apache/kafka):** considerada.
  Bitnami é boa, mas o ecossistema Spring espera o cliente Confluent.
  A imagem oficial `confluentinc/cp-kafka` mantém compatibilidade
  com o Schema Registry e outras peças do stack caso evoluamos.
- **Redpanda (Kafka-compatible):** mais leve, mais rápido, sem JVM.
  Excelente alternativa, mas exige aprender outro produto e
  validar compatibilidade com Spring Kafka 4.0.4.
