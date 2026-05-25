# ADR 0001: Adoção de microsserviços em vez de monolito

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O Tech Challenge da Fase 3 exige (requisito 5.1) uma arquitetura distribuída
em **mais de um serviço**, com no mínimo: serviços de autenticação, pedido,
pagamento e — opcionalmente — `restaurante-service` (cozinha) e
`api-gateway`. A spec abre a possibilidade de microsserviços ou de um
monolito modular bem segmentado; é decisão do time como dividir.

Os contextos identificados (usuário/auth, pedido, pagamento, cozinha)
têm modelos de dados muito distintos, ritmos de evolução diferentes
(pagamento muda mais que cadastro) e perfis de escala assimétricos
(pagamento sob estresse não deveria afundar o login).

## Decisão

Implementar **4 microsserviços independentes**, cada um com:

- Próprio módulo Maven (`usuario-autenticacao`, `restaurante-pedido`,
  `pagamento`, `restaurante-service`)
- Próprio container Docker
- Próprio banco MySQL (`auth_db`, `pedido_db`, `pagamento_db`, `cozinha_db`)
- Próprio entrypoint Spring Boot

A comunicação entre eles é feita por **eventos Kafka** (assíncrona) e,
quando síncrona, por **gRPC** (caso do `restaurante-pedido` consultando
`usuario-autenticacao`).

## Consequências

### Positivas

- **Evolução isolada:** mudanças em pagamento não tocam código de pedido.
- **Deploy seletivo:** cada serviço pode ir a produção independentemente.
- **Bounded contexts respeitados:** modelos de dados nascem coesos.
- **Escala granular:** sob carga, só o serviço sob pressão precisa ser
  escalado.
- **Isolamento de falhas:** uma JVM travada não derruba as outras três.

### Negativas

- **Complexidade operacional alta:** 4 deploys, 4 bancos, observabilidade
  distribuída, debugging cross-service.
- **Latência adicional:** chamadas que seriam in-process viram rede.
- **Consistência eventual:** estado distribuído exige convergência via
  eventos (vide ADR 0005).

## Alternativas consideradas

- **Monolito modular único:** menor complexidade operacional, mas dificulta
  isolamento real de falhas e impede escala assimétrica. Para a spec
  (que exige no mínimo 3 serviços), seria solução marginal.
- **Apenas 3 serviços (sem cozinha):** atende ao mínimo do requisito 5.1,
  mas perde a oportunidade pedagógica de modelar um 4º bounded context.
  Escolhemos implementar o opcional `restaurante-service` (vide ADR 0010).
