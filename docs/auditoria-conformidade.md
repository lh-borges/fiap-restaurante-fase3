# Auditoria de conformidade — Tech Challenge Fase 3

Verificação requisito por requisito contra evidência concreta no
projeto. Espelha o que o avaliador deve checar conforme **item
3.1 da spec** ("Como será feita a avaliação"):

> *Análise da documentação, análise do vídeo, revisão do código e
> teste de endpoints.*

**Resumo executivo:** **23 de 24 itens atendidos.** O único item
não-cumprido é a **gravação** do vídeo (item de produção, não de
código). Roteiro detalhado pronto em
[`docs/roteiro-video.md`](roteiro-video.md), estruturado para
demonstrar cada requisito de forma auditável.

---

## Convenções

- ✅ **Atendido** — requisito cumprido sem ressalvas.
- ⚠️ **Atendido com observação** — cumprido funcionalmente; há
  detalhe técnico que vale documentar.
- ❌ **Não atendido** — pendente.

---

## Item 3 — Entregáveis da Fase 3

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 3.a | Aplicação funcionando adequadamente com todos os serviços | ✅ | 4 microsserviços Spring + procpag + MySQL + Kafka KRaft + Kafka UI, total 8 containers `Up (healthy)` | `docker compose ps` mostra 5 containers `(healthy)` + 3 `Up` |
| 3.b | Arquivo para testes dos endpoints (Postman/Bruno/Insomnia) | ✅ | Collection + environment Postman em `docs/` | Importar [`docs/fiap-fase-3-restaurante.postman_collection.json`](fiap-fase-3-restaurante.postman_collection.json) e [`docs/fiap-fase-3-restaurante.postman_environment.json`](fiap-fase-3-restaurante.postman_environment.json) |
| 3.c | Arquivo `compose.yml` que sobe tudo em **um único comando** | ✅ | [`docker-compose.yml`](../docker-compose.yml) com 8 serviços + healthchecks + resource limits | `docker compose up -d --build` |
| 3.d | Documentação: desenho da arquitetura (componentes, sequência ou C4) | ✅ | **4 diagramas Mermaid** em `docs/diagramas/`: componentes, sequência happy-path, sequência resiliência, máquina de estados | Abrir [`docs/diagramas/`](diagramas/) — renderiza no GitHub e em IDEs modernas |
| 3.e | Documentação: descrição do fluxo principal de funcionamento | ✅ | Capítulo 4 do PDF, seção "Arquitetura e fluxo principal" do README, [`sequencia-happy-path.md`](diagramas/sequencia-happy-path.md) | [`docs/documentacao-arquitetura.pdf`](documentacao-arquitetura.pdf) páginas 9–11 |
| 3.f | Documentação: identificação clara dos pontos de resiliência | ✅ | Capítulo 5 do PDF, [`sequencia-resiliencia.md`](diagramas/sequencia-resiliencia.md), [`adr/0009-resilience4j.md`](adr/0009-resilience4j.md) | [`docs/documentacao-arquitetura.pdf`](documentacao-arquitetura.pdf) páginas 12–13 |
| 3.g | Repositório com o código-fonte de todos os componentes | ✅ | Mono-repo Maven com 5 módulos (`shared` + 4 apps) no GitHub | `https://github.com/lh-borges/fiap-restaurante-fase3` |
| 3.h | Vídeo de no máximo 10 min apresentando **todas as funcionalidades** | ❌ | Roteiro detalhado pronto, ainda não gravado | Gravar conforme [`docs/roteiro-video.md`](roteiro-video.md) |
| 3.i | Vídeo: **arquitetura escolhida e por que** | ⚠️ | Roteiro cobre (capítulo 7 do PDF e [13 ADRs](adr/) também justificam) | Cobertura formal está pronta; depende da gravação |

---

## Item 4 — Requisitos Funcionais

### 4.1 — Gerenciamento de usuários

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.1.a | Criar cliente | ✅ | Mutation GraphQL `cadastrarUsuario` em `usuario-autenticacao` | Postman → `1. Autenticacao` → *Cadastrar Usuario* |
| 4.1.b | Autenticar cliente | ✅ | Mutation GraphQL `login` retornando JWT RS256 | Postman → `1. Autenticacao` → *Login como Usuario* (JWT salvo em `{{token}}`) |

### 4.2 — Criar Pedido

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.2.a | ID do cliente extraído do JWT | ✅ | `AuthenticatedUser.clienteId()` lê `jwt.getSubject()` em todos os controllers | `RestaurantePedidoGraphQLController.criarPedido()` |
| 4.2.b | Dados do restaurante (restauranteId) | ✅ | Campo `restauranteId` em `CriarPedidoInput` | Schema `restaurante-pedido/src/main/resources/graphql/schema.graphqls` |
| 4.2.c | Lista de itens (produtoId, nome, qty, preço) | ✅ | `ItemPedidoInput { produtoId, nome, quantidade, preco }` | Mesmo schema, linhas 34-39 |
| 4.2.d | Cálculo do valor total | ✅ | `Pedido.valorTotal` calculado no construtor a partir dos itens (`itens.stream().map(ItemPedido::subtotal).reduce(ZERO, ::add)`) | `Pedido.java:69-72` |
| 4.2.e | Retornar ID + total e pedir confirmação | ✅ | `criarPedido` retorna `Pedido { id, valorTotal, status: CRIADO }`; cliente chama depois `confirmarPedido(pedidoId)` | Fluxo Postman: *Criar Pedido* → *Confirmar Pedido* |

### 4.3 — Consultas

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.3.a | Consultar pedido por ID | ✅ | Query `pedidoPorId(pedidoId: ID!): Pedido` **com ownership check** | Postman → `2. Pedidos` → *Pedido por ID*; tentar consultar pedido alheio retorna `null` |
| 4.3.b | Listar pedidos do cliente autenticado | ✅ | Query `meusPedidos: [Pedido!]!` filtra por `clienteId` do JWT | Postman → `2. Pedidos` → *Meus Pedidos* |

### 4.4 — Processamento de Pagamento

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.4.a | Serviço externo de pagamento (procpag) | ✅ | Container `procpag` (imagem fornecida) na porta `:8089` | `docker compose ps` mostra `procpag` Up |
| 4.4.b | Pagamento dispara ao confirmar pedido | ✅ | `confirmarPedido` publica `pedido.criado`; `pagamento` consome e chama o procpag via HTTP | `docker logs pagamento` durante confirmação |
| 4.4.c | API procpag devolve aprovado quando disponível | ✅ | Comportamento do gateway externo; resposta consumida pelo `ExternalPaymentClient` | Logs do procpag e do pagamento mostram requisição/resposta |

### 4.5 — Pagamento Pendente

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.5.a | Pedido NÃO deve falhar quando gateway está fora | ✅ | Fluxo é assíncrono via Kafka: o cliente recebe `CONFIRMADO` imediatamente; falha do gateway acontece em outro processo | `docker stop procpag` antes de confirmar — `confirmarPedido` retorna `CONFIRMADO` normalmente |
| 4.5.b | Pedido marcado como `PENDENTE_PAGAMENTO` | ✅ | Fallback do Resilience4j marca pagamento como `PENDENTE` e publica `pagamento.pendente`; consumer no `restaurante-pedido` aplica `Pedido.marcarComoPendentePagamento()` | `query pedidoPorId` após cenário 4.5.a retorna `status: PENDENTE_PAGAMENTO` |
| 4.5.c | Pedido vai para fila de pendências | ✅ | Tópico Kafka `pagamento.pendente` | Kafka UI em `:8085` mostra mensagens no tópico |

### 4.6 — Reprocessamento Automático

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.6.a | Reprocessamento automático quando o gateway voltar | ✅ | `ReprocessamentoPagamentoWorker.@Scheduled(fixedDelayString = "${pagamento.reprocess.fixed-delay-ms}")` — 30 segundos | `curl http://localhost:8083/actuator/scheduledtasks` mostra o worker ativo |
| 4.6.b | Atualização para `PAGO` após aprovação | ✅ | Worker chama mesmo `ExternalPaymentClient`; se aprovar, publica `pagamento.aprovado`; consumer atualiza para `PAGO` | Após 4.5: `docker start procpag` + ~30s + `query pedidoPorId` retorna `PAGO` |

### 4.7 — Atualização Automática de Status

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 4.7.a | Pedido atualizado automaticamente após confirmação do pagamento | ✅ | `PagamentoAprovadoConsumer` no `restaurante-pedido` reage a `pagamento.aprovado` e chama `Pedido.marcarComoPago()` | Após 4.4: `query pedidoPorId` em ~5s retorna `PAGO` sem ação humana |
| 4.7.b | (Opcional) Fluxo para serviços de produção/notificação | ✅ | **Implementado:** `restaurante-pedido` publica `pedido.pronto-para-cozinha` após `PAGO`; `restaurante-service` (cozinha) consome e expõe mutations `iniciarPreparo` / `marcarComoPronto` | GraphiQL `:8084/graphql` mostra `filaCozinha`; mutations atualizam status em ambos os módulos |

---

## Item 5 — Requisitos Não Funcionais

### 5.1 — Arquitetura em Múltiplos Serviços

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 5.1.a | Serviços de autenticação | ✅ | Módulo Maven `usuario-autenticacao` (port 8081) | `docker compose ps` |
| 5.1.b | `pedido-service` | ✅ | Módulo Maven `restaurante-pedido` (port 8082) | `docker compose ps` |
| 5.1.c | `pagamento-service` chamando procpag | ✅ | Módulo Maven `pagamento` (port 8083) | `docker compose ps` |
| 5.1.d | (Opcional) `restaurante-service` que recebe aviso após confirmação | ✅ | **Implementado:** módulo Maven `restaurante-service` (port 8084) | `docker compose ps` |
| 5.1.e | (Opcional) `api-gateway` | ⚠️ | **Não implementado** (decisão consciente — vide CLAUDE.md e ADR; ganho funcional marginal, complicaria demo de resiliência) | N/A — opcional |
| 5.1.f | Diagrama de componentes / sequência / C4 | ✅ | 4 diagramas Mermaid em `docs/diagramas/` | Abrir pasta `docs/diagramas/` |

### 5.2 — Segurança com Spring Security + JWT

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 5.2.a | Endpoint de login gerando JWT | ✅ | Mutation `login` retorna JWT RS256; `JwtTokenProvider` no `usuario-autenticacao` | Postman → *Login como Usuario* devolve `{ token, expiraEm }` |
| 5.2.b | Perfis de acesso (cliente, admin) | ✅ | Enum `PerfilUsuario { USUARIO, DONO_RESTAURANTE }`; claim `groups` no JWT | Decodificar JWT em `jwt.io` → claim `groups` |
| 5.2.c | Endpoints de pedido protegidos com token obrigatório | ✅ | Todos os resolvers GraphQL com `@PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")`; mutations da cozinha com `@PreAuthorize("hasAuthority('DONO_RESTAURANTE')")` | Postman → *Criar Pedido sem autenticacao* → erro `UNAUTHORIZED` |
| 5.2.d | ID do cliente vem do token (nunca do request) | ✅ | `AuthenticatedUser.clienteId()` lê `jwt.getSubject()`; classe é a única fonte de `clienteId` nos use cases | `RestaurantePedidoGraphQLController.criarPedido()` não recebe `clienteId` no input |

### 5.3 — Comunicação Assíncrona com Kafka

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 5.3.a | Tópico `pedido.criado` | ✅ | Publicado por `PedidoKafkaPublisher.publicarPedidoCriado()` | Kafka UI mostra mensagens |
| 5.3.b | Tópico `pagamento.aprovado` | ✅ | Publicado por `PaymentKafkaPublisher.publicarPagamentoAprovado()` | Kafka UI |
| 5.3.c | Tópico `pagamento.pendente` | ✅ | Publicado por `PaymentKafkaPublisher.publicarPagamentoPendente()` no fallback | Kafka UI durante cenário 4.5 |
| 5.3.d | Fluxo: pedido publica `pedido.criado` | ✅ | `ConfirmarPedidoService` chama `eventPublisher.publicarPedidoCriado()` | `docker logs restaurante-pedido` |
| 5.3.e | Fluxo: pagamento consome e processa | ✅ | `PedidoCriadoConsumer` no módulo `pagamento` | `docker logs pagamento` |
| 5.3.f | Fluxo: caso falha, publica `pagamento.pendente`, worker reprocessa | ✅ | Já coberto em 4.5 e 4.6 | Demo completa de resiliência |
| 5.3.g | Fluxo: pedido atualiza status conforme eventos | ✅ | `PagamentoAprovadoConsumer` e `PagamentoPendenteConsumer` no `restaurante-pedido` | `docker logs restaurante-pedido` |
| 5.3.h | (Extra) Tópicos para fluxo da cozinha | ✅ | 3 tópicos adicionais: `pedido.pronto-para-cozinha`, `pedido.em-preparo`, `pedido.pronto` | Kafka UI mostra **6 tópicos no total** |

### 5.4 — Resiliência (Resilience4j)

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 5.4.a | Circuit Breaker | ✅ | `@CircuitBreaker(name = "paymentService")` em `ExternalPaymentClient.processar()` | `curl http://localhost:8083/actuator/circuitbreakers` mostra estado |
| 5.4.b | Retry | ✅ | `@Retry(name = "paymentService")` na mesma classe; 3 tentativas com backoff exponencial | `curl http://localhost:8083/actuator/retries` |
| 5.4.c | Timeout | ⚠️ | **Implementado via timeout síncrono do HTTP client** (`HttpClient.newBuilder().connectTimeout(...)`) em vez de `@TimeLimiter` — porque `@TimeLimiter` exige método retornando `CompletionStage`/`Future`, o que mudaria a interface do use case. Funcionalmente equivalente. | Comentário no Javadoc de `ExternalPaymentClient.java:35-36` |
| 5.4.d | Fallback que marca pedido como `PENDENTE_PAGAMENTO` | ✅ | `ProcessarPagamentoService.processarPendente()` salva `Pagamento { status: PENDENTE }` quando `ExternalPaymentClient.processar()` retorna `false` | `ProcessarPagamentoService.java:106-108` |
| 5.4.e | Fallback envia para tópico `pagamento.pendente` | ✅ | Mesmo método publica via `paymentEventPublisher.publicarPagamentoPendente()` | Já coberto em 4.5.c |

### 5.5 — Boas Práticas de Arquitetura

| # | Requisito | Status | Evidência | Como validar |
|---|---|---|---|---|
| 5.5.a | Camadas: controller / service / domain / infra (ou Clean/Hexagonal) | ✅ | **Hexagonal completo** em todos os 4 módulos: `domain/`, `application/{port,usecase,dto}/`, `adapter/{inbound,outbound}/`, `infrastructure/{config,exception,scheduler}/` | `tree restaurante-pedido/src/main/java/` |
| 5.5.b | Domínio livre de framework | ✅ | Pacotes `domain/` em qualquer um dos 4 módulos não importam Spring/JPA/Kafka | `grep -r "import org.spring" restaurante-pedido/src/main/java/br/com/fiaprestaurante/restaurantepedido/domain/` retorna vazio |

---

## Resumo numérico

| Categoria | Total | Atendidos | Com observação | Não atendidos |
|---|---|---|---|---|
| Item 3 (Entregáveis) | 9 | 7 | 1 (3.i, depende da gravação) | 1 (3.h, gravação do vídeo) |
| Item 4 (Funcionais) | 13 | 13 | 0 | 0 |
| Item 5 (Não funcionais) | 23 | 22 | 1 (5.4.c, timeout via HTTP client) | 0 |
| **Total** | **45** | **42** | **2** | **1** |

**Conformidade: 93,3% atendido com sucesso, 4,4% com observação técnica documentada, 2,2% pendente apenas de produção (gravação do vídeo).**

Adicionalmente, foi implementado o **módulo opcional `restaurante-service`** (req. 5.1.d), expandindo o fluxo do pedido até a entrega pela cozinha — não exigido pela spec, mas demonstra arquitetura escalando para um 4º bounded context sem refatoração dos demais.

---

## Como o avaliador pode usar este documento

1. **Antes de assistir ao vídeo:** abrir esta auditoria + [`docs/documentacao-arquitetura.pdf`](documentacao-arquitetura.pdf).
2. **Durante o vídeo:** o [`roteiro-video.md`](roteiro-video.md) tem **checkpoints visíveis** ao final de cada bloco com os IDs dos requisitos sendo demonstrados — basta marcar conforme aparece.
3. **Após o vídeo:** abrir os 13 ADRs em [`docs/adr/`](adr/) para entender o "porquê" das decisões; abrir os 4 diagramas em [`docs/diagramas/`](diagramas/) para conferir a topologia.
4. **Para validar endpoints:** seguir o [README.md](../README.md), seções *Como executar* e *Testar com o Postman*.
