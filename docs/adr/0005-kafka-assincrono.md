# ADR 0005: Apache Kafka para comunicação assíncrona

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O requisito 5.3 exige **comunicação assíncrona via Kafka** entre
serviços. O fluxo principal envolve eventos disparados pela criação
de pedidos, processamento de pagamentos e atualização de status.

A escolha pelo Kafka (em vez de chamadas síncronas) é a decisão
arquitetural mais importante do projeto, porque é o que **habilita
a resiliência** exigida pelos requisitos 4.5 e 4.6 (pagamento
pendente + reprocessamento automático quando o gateway externo
voltar).

## Decisão

Adotar **Apache Kafka** (Confluent Platform 7.7.1) como o **único**
mecanismo de comunicação assíncrona entre os 4 microsserviços, com
**6 tópicos** orquestrando o fluxo completo:

| Tópico | Publicador | Consumidor(es) |
|---|---|---|
| `pedido.criado` | restaurante-pedido | pagamento |
| `pagamento.aprovado` | pagamento | restaurante-pedido |
| `pagamento.pendente` | pagamento | restaurante-pedido |
| `pedido.pronto-para-cozinha` | restaurante-pedido | restaurante-service |
| `pedido.em-preparo` | restaurante-service | restaurante-pedido |
| `pedido.pronto` | restaurante-service | restaurante-pedido |

A chave de cada mensagem é o `pedidoId.toString()`, garantindo que
todos os eventos do mesmo pedido caem na mesma partição (preservando
ordem por pedido).

Serialização: JSON via Spring Kafka + Jackson 2 (`jackson-databind`
2.21.x), com `JavaTimeModule` para `Instant`.

## Consequências

### Positivas

- **Desacoplamento total entre bounded contexts:** o `restaurante-pedido`
  não importa nada do módulo `pagamento` e vice-versa — eles só
  conhecem o **contrato dos eventos**.
- **Habilita resiliência (req. 4.5/4.6):** quando o gateway de
  pagamento cai, o `pagamento` publica `pagamento.pendente` e a vida
  segue; o worker `@Scheduled` reprocessa quando o gateway volta.
- **Replay:** se um consumer tiver bug, basta corrigir e reprocessar
  do offset desejado.
- **Cliente percebe baixa latência:** o cliente recebe resposta de
  `confirmarPedido` em milissegundos, mesmo que o gateway leve
  segundos.
- **Auditoria implícita:** todos os eventos ficam armazenados no
  broker e podem ser inspecionados via Kafka UI (`:8085`).

### Negativas

- **Consistência eventual:** o pedido fica `CONFIRMADO` por alguns
  segundos antes de virar `PAGO`. A UX e a observabilidade precisam
  lidar com isso (mostrar "processando..." no cliente).
- **Operação adicional:** mais um componente para subir, monitorar
  e tunar (partições, replication factor, retention).
- **Debugging distribuído:** rastrear uma falha cruzando 4 serviços
  + Kafka exige correlation IDs (não implementados; trade-off
  consciente para projeto de demo).

## Alternativas consideradas

- **Chamadas REST síncronas:** o `restaurante-pedido` chamaria o
  `pagamento` diretamente. Quando o gateway externo do pagamento
  cair, o request do cliente falha. **Não atende o req. 4.5**.
- **RabbitMQ:** broker tradicional, AMQP, mais simples para low-volume.
  Kafka ganha quando se quer replay e particionamento. Padrão de
  mercado em microsserviços modernos.
- **Database queue + polling:** uma tabela `eventos_pendentes` com
  polling. Simples, mas reinventa Kafka mal.
- **AWS SQS/SNS:** acoplamento a vendor. Para um projeto on-prem,
  inadequado.
