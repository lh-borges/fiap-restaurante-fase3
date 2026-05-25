# FIAP Restaurante — Fase 3

Sistema de pedido online para restaurante, dividido em **4 microsserviços** Spring Boot 4 / Java 25, comunicando-se de forma síncrona (gRPC, GraphQL) e assíncrona (Apache Kafka), com resiliência aplicada na chamada ao gateway externo de pagamento. Inclui o fluxo completo do pedido até a cozinha (recebido → em preparo → pronto).

Projeto desenvolvido como **Tech Challenge da Fase 3** da PosTech FIAP. A especificação completa está em [`requisito/tech-challenge-fase-03.pdf`](../requisito/tech-challenge-fase-03.pdf).

> **Para quem vai executar pela primeira vez:** comece pela seção [Como executar](#como-executar) e depois [Testar com o Postman](#testar-com-o-postman). O sistema inteiro sobe com **um único comando**.

---

## Índice

1. [Como executar](#como-executar)
2. [Documentação técnica](#documentação-técnica)
3. [Testar com o Postman](#testar-com-o-postman)
4. [Testes automatizados](#testes-automatizados)
5. [Arquitetura e fluxo principal](#arquitetura-e-fluxo-principal)
6. [Decisões arquiteturais — e por que escolhemos cada uma](#decisões-arquiteturais--e-por-que-escolhemos-cada-uma)
7. [Requisitos da Fase 3 — o que foi feito e como validar](#requisitos-da-fase-3--o-que-foi-feito-e-como-validar)
8. [O que ainda falta](#o-que-ainda-falta)
9. [Serviços e portas](#serviços-e-portas)
10. [Stack](#stack)
11. [Estrutura do repositório](#estrutura-do-repositório)
12. [Diagnóstico e inspeção](#diagnóstico-e-inspeção)

---

## Como executar

### Pré-requisitos

- **Docker Desktop** instalado e em execução (Docker 24+, Compose v2).
- ~**4 GB** de RAM livres (limits totais do compose somam ~3.5 GB; sobra para overhead do Docker).
- Portas livres no host: **8081, 8082, 8083, 8084, 8085, 8089, 9092**.
- JDK 25 (Temurin) — **opcional**, só necessário para rodar `mvn test` fora do Docker. Instalável via SDKMAN: `sdk install java 25-tem`.

> **Se você já tem MySQL instalado nativamente** (porta 3306 ocupada): crie um `docker-compose.override.yml` na raiz (já ignorado pelo Git) com:
> ```yaml
> services:
>   mysql:
>     ports: !reset []
> ```
> O container continua acessível pelos demais serviços via rede interna.

### 1. Subir todos os serviços

A partir da raiz do projeto (`fiap-restaurante-fase3/`):

```bash
docker compose up -d --build
```

Esse único comando sobe os **8 containers** (4 aplicações + procpag + MySQL + Kafka KRaft + Kafka UI).

> **Primeira execução é demorada** — o Maven baixa todas as dependências e cada Dockerfile faz um training de AppCDS durante o build (~1 min por app, mas resulta em boot ~30% mais rápido no runtime). Se o build falhar por instabilidade de rede, **rode o mesmo comando de novo**: o cache de dependências (BuildKit) retoma de onde parou. Execuções seguintes aproveitam o cache e sobem em segundos.

### 2. Conferir se subiu

```bash
docker compose ps
```

Os 8 containers devem aparecer como `Up`; `mysql`, `kafka`, e as 4 aplicações Spring com `(healthy)`.

Healthcheck das aplicações (todas expõem `/actuator/health`):

```bash
curl http://localhost:8081/actuator/health   # usuario-autenticacao -> {"status":"UP"}
curl http://localhost:8082/actuator/health   # restaurante-pedido   -> {"status":"UP"}
curl http://localhost:8083/actuator/health   # pagamento            -> {"status":"UP"}
curl http://localhost:8084/actuator/health   # restaurante-service  -> {"status":"UP"}
```

### 3. Parar tudo

```bash
docker compose down       # mantém o volume do MySQL
docker compose down -v    # remove também os dados persistidos
```

---

## Documentação técnica

A documentação formal do projeto vive em [`docs/`](docs/):

| Artefato | O que é | Quando consultar |
|---|---|---|
| [`docs/documentacao-arquitetura.pdf`](docs/documentacao-arquitetura.pdf) | **Documento técnico ABNT** (17 páginas, Times 12, espaço 1.5, margens 3-2-3-2 cm) com capa, resumo, sumário, 10 capítulos e referências | Visão completa do sistema para leitura linear (avaliação, onboarding) |
| [`docs/adr/`](docs/adr/) | **13 ADRs** (Architecture Decision Records, formato Nygard) cobrindo todas as decisões arquiteturais relevantes | Quando precisar entender *por que* algo foi feito de uma forma e não de outra |
| [`docs/diagramas/`](docs/diagramas/) | **4 diagramas Mermaid** (componentes, sequência happy path, sequência resiliência, máquina de estados do pedido) | Visualização rápida de fluxo e topologia |
| [`docs/roteiro-video.md`](docs/roteiro-video.md) | Roteiro detalhado do vídeo de apresentação (10 min) com falas, tempos e comandos exatos | Antes de gravar o vídeo de entrega |

O PDF é versionado: novas versões substituem o mesmo arquivo (`docs/documentacao-arquitetura.pdf`). Para regenerar a partir do código-fonte (`docs/build-pdf/gerar_documentacao_pdf.py`):

```bash
docker run --rm -v "$(pwd)/docs:/work" -w //work/build-pdf \
  python:3.12-slim sh -c "pip install -q reportlab && python gerar_documentacao_pdf.py"
```

---

## Testar com o Postman

### Onde estão os arquivos

Na pasta [`docs/`](docs/):

| Arquivo | Importar no Postman como |
|---|---|
| [`docs/fiap-fase-3-restaurante.postman_collection.json`](docs/fiap-fase-3-restaurante.postman_collection.json) | **Collection** |
| [`docs/fiap-fase-3-restaurante.postman_environment.json`](docs/fiap-fase-3-restaurante.postman_environment.json) | **Environment** |

### Como importar e configurar

1. No Postman: **Import** → selecione os **dois** arquivos acima.
2. No seletor de environment (canto superior direito), **ative o environment `fiap-fase-3-restaurante`**. Sem isso, variáveis como `{{authUrl}}` ficam vazias e as requisições falham.
3. Pronto — a collection `fiap-fase-3-restaurante` aparece organizada em **4 pastas**.

### Estrutura da collection

| Pasta | Conteúdo |
|---|---|
| `1. Autenticacao` | Cadastrar usuário/dono, login (salva o JWT automaticamente), consultar `me` |
| `2. Pedidos` | Status do módulo, criar/confirmar pedido, consultar por ID, listar meus pedidos |
| `3. Pagamento` | Consultar pagamento por pedido, listar pagamentos pendentes |
| `4. Massa de Testes` | Fluxo otimizado para o Collection Runner — gera pedidos com itens aleatórios |

### Fluxo de teste manual (passo a passo)

1. `1. Autenticacao` → **Login como Usuario** — o JWT é salvo automaticamente em `{{token}}`.
2. `2. Pedidos` → **Criar Pedido** — devolve o `id` e o `valorTotal` calculado; o `pedidoId` é salvo automaticamente.
3. `2. Pedidos` → **Confirmar Pedido** — publica o evento `pedido.criado` no Kafka.
4. Aguarde alguns segundos e rode `2. Pedidos` → **Pedido por ID** — o status evolui para `PAGO` (ou `PENDENTE_PAGAMENTO` se o gateway falhar).
5. `3. Pagamento` → **Pagamento por Pedido** — confirma o pagamento `APROVADO`. Como o processamento é assíncrono, uma consulta feita antes de o evento ser consumido retorna `404 NOT_FOUND`; aguarde alguns segundos e consulte novamente.

### Fluxo da cozinha (via GraphiQL)

A collection Postman atual cobre o caminho até `PAGO`. Para continuar até `EM_PREPARO` e `PRONTO`, use o GraphiQL do `restaurante-service` (até a collection ser atualizada):

1. Faça login como `dono@fiap.com` / `dono123` (perfil `DONO_RESTAURANTE`) — copie o `token` da resposta.
2. Abra `http://localhost:8084/graphiql` e cole o token em **Headers**: `{"Authorization": "Bearer <token>"}`.
3. Rode `query { filaCozinha { id pedidoId status } }` — o pedido aprovado no passo anterior aparece com `status: "RECEBIDO"`.
4. Rode `mutation { iniciarPreparo(pedidoCozinhaId: "<id-da-fila>") { status } }` → `EM_PREPARO`.
5. Rode `mutation { marcarComoPronto(pedidoCozinhaId: "<id-da-fila>") { status } }` → `PRONTO`.
6. Volte ao Postman, rode `2. Pedidos` → **Pedido por ID** — o `Pedido` original reflete o estado da cozinha (`PAGO` → `EM_PREPARO` → `PRONTO`), provando que os 4 microsserviços conversam via Kafka.

### Massa de testes (Collection Runner)

A pasta `4. Massa de Testes` foi feita para gerar **vários pedidos com dados aleatórios**:

1. Rode `1. Autenticacao` → **Login como Usuario** uma vez (popula o `{{token}}`).
2. Abra o **Collection Runner** (botão **Run**) e selecione **somente** a pasta `4. Massa de Testes`.
3. Em **Iterations**, informe quantos pedidos quer gerar (ex.: `50`). Opcionalmente defina um **Delay** em ms entre iterações.
4. **Run** — cada iteração cria e confirma um pedido com itens sorteados de um catálogo; o Runner mostra o pass/fail das asserções.

**Para exercitar o cenário de resiliência** (gerar pedidos `PENDENTE_PAGAMENTO`): pare o gateway antes de rodar a massa e religue depois —

```bash
docker stop procpag      # antes de rodar a massa
docker start procpag     # depois de rodar a massa
```

Os pedidos criados com o gateway fora ficam `PENDENTE_PAGAMENTO` e são reprocessados automaticamente pelo worker quando ele volta. Confira o resultado em `2. Pedidos` → **Meus Pedidos**.

---

## Swagger e contrato HTTP do GraphQL

Cada microsserviço publica a operação HTTP `POST /graphql` no Swagger UI, com exemplos de requisição e das respostas esperadas:

| Serviço | Swagger UI | Documento OpenAPI |
|---|---|---|
| `usuario-autenticacao` | `http://localhost:8081/swagger-ui/index.html` | `http://localhost:8081/v3/api-docs` |
| `restaurante-pedido` | `http://localhost:8082/swagger-ui/index.html` | `http://localhost:8082/v3/api-docs` |
| `pagamento` | `http://localhost:8083/swagger-ui/index.html` | `http://localhost:8083/v3/api-docs` |

Embora APIs GraphQL frequentemente respondam erros de execução com HTTP `200`, esta aplicação configura o endpoint para refletir a classificação do erro também no **status HTTP**:

| Status HTTP | Classificação GraphQL | Exemplo de cenário |
|---|---|---|
| `200 OK` | — | Operação executada com sucesso. |
| `400 BAD_REQUEST` | `BAD_REQUEST` | UUID inválido ou entrada incompatível com o contrato. |
| `401 UNAUTHORIZED` | — | Token Bearer inválido ou expirado, rejeitado pelo Spring Security antes da execução GraphQL. |
| `403 FORBIDDEN` | `FORBIDDEN` | Requisição sem autenticação ou usuário sem perfil para a operação. |
| `404 NOT_FOUND` | `NOT_FOUND` | Consulta a usuário, pedido ou pagamento inexistente. |
| `500 INTERNAL_SERVER_ERROR` | `INTERNAL_ERROR` | Falha inesperada do serviço. |

Na query `pagamentoPorPedido`, um `pedidoId` em formato UUID válido sem pagamento registrado agora responde `404 NOT_FOUND` e inclui o erro GraphQL no corpo. Isso diferencia recurso ainda inexistente de um pagamento retornado com sucesso.

## Testes automatizados

A suíte unitária + smoke cobre as **4 aplicações** com **285 testes** e roda em ~3 min. **Não exige Docker, MySQL nem Kafka externos** — usa H2 em memória, `@EmbeddedKafka` e mocks nas dependências.

### Cobertura atual por módulo

| Módulo | Testes | O que cobre |
|---|---|---|
| `usuario-autenticacao` | 63 | Domain (`Usuario`, `PerfilUsuario`, exceções) + use cases (cadastrar, autenticar, buscar) + BCrypt + `JwtTokenProvider` + controller GraphQL + `UsuarioGraphQLExceptionHandler` + `UsuarioMapper` + payloads + adapter gRPC + `DataSeeder` + smoke de contexto + smoke GraphQL |
| `restaurante-pedido` | 126 | Domain (`Pedido`/`ItemPedido` com invariantes, transições e ownership) + use cases (criar, confirmar, consultar, atualizar status de pagamento e de cozinha) + adapters JPA, Kafka e GraphQL + `RestaurantePedidoGraphQLExceptionHandler` + 4 consumers (`pagamento.aprovado`, `pagamento.pendente`, `pedido.em-preparo`, `pedido.pronto`) + `AuthenticatedUser` + smoke de contexto + smoke GraphQL (8 cenários) |
| `pagamento` | 57 | Domain (`Pagamento`, `StatusPagamento`, exceções) + use cases (processar, reprocessar, consultar) + `ExternalPaymentClient` + adapters Kafka/JPA/GraphQL + `PagamentoGraphQLExceptionHandler` + worker `@Scheduled` + configs (Bean/Kafka/Security) + smoke de contexto + smoke GraphQL |
| `restaurante-service` | 39 | Domain (`PedidoCozinha`/`ItemCozinha`/`StatusCozinha`, invariantes e transições) + 4 use cases (receber pedido, iniciar preparo, marcar pronto, consultar fila) + adapters JPA + Kafka publisher/consumer + GraphQL controller + smoke de contexto |
| **Total** | **285** | |

### Rodar tudo

A partir da raiz do projeto:

```bash
./mvnw test            # roda os 4 módulos
```

> ⚠️ **O Maven Surefire mostra `Tests run: N` por módulo, mas não agrega no Reactor Summary.** O número que aparece no fim é só do último módulo executado. Para ver o total agregado, use o script abaixo.

### Resumo agregado (script)

Para ter um relatório consolidado com totais por módulo + total agregado, use os scripts em [`scripts/`](scripts/):

| SO | Comando |
|---|---|
| Linux / macOS | `./scripts/test-summary.sh` |
| Windows (PowerShell) | `.\scripts\test-summary.ps1` |

Saída:

```
============================================================
 RESUMO POR MODULO
============================================================
  pagamento                 tests:   57   falhas: 0   erros: 0   skip: 0   tempo:  14,92s
  restaurante-pedido        tests:  126   falhas: 0   erros: 0   skip: 0   tempo:  61,00s
  restaurante-service       tests:   39   falhas: 0   erros: 0   skip: 0   tempo:  51,14s
  usuario-autenticacao      tests:   63   falhas: 0   erros: 0   skip: 0   tempo:  79,00s

============================================================
 TOTAL AGREGADO
============================================================
  Tests run:  285
  Failures:   0
  Errors:     0
  Skipped:    0
  Tempo:      ~3 min
```

Linhas em **verde** se o módulo passou, **vermelhas** se houve falha/erro. O exit code é `1` se algum teste falhou — pronto para CI.

**Opções dos scripts:**

| Linux/macOS | Windows | Efeito |
|---|---|---|
| `--quiet` ou `-q` | `-Quiet` | Roda o Maven com `-q` (saída reduzida) |
| `--skip-build` | `-SkipBuild` | Pula o `mvn test` e só lê os XMLs existentes (útil quando o build já rodou) |
| `--help` ou `-h` | `Get-Help .\scripts\test-summary.ps1` | Mostra ajuda |

> Se a Execution Policy do PowerShell bloquear, rode via:
> ```powershell
> powershell -ExecutionPolicy Bypass -File .\scripts\test-summary.ps1
> ```

### Rodar testes de um único módulo

```bash
./mvnw -pl restaurante-pedido -am test    # só restaurante-pedido (compila shared antes)
./mvnw -pl pagamento -am test             # só pagamento
./mvnw -pl usuario-autenticacao -am test  # só auth
./mvnw -pl restaurante-service -am test   # só cozinha
```

### Stack de testes

- **JUnit 5** (Jupiter) — runner
- **AssertJ** — fluent assertions
- **Mockito** — mocks dos colaboradores
- **Spring Boot Test** + `@SpringBootTest` — testes de contexto (smoke)
- **H2** in-memory + `MODE=MySQL` — substitui o MySQL nos testes
- **`@EmbeddedKafka`** — broker Kafka iniciado pelo próprio teste
- **Spring Security Test** + `@WithMockUser` — autenticação simulada

### Como o script de resumo funciona

Cada execução do Maven Surefire grava um XML por classe de teste em `<modulo>/target/surefire-reports/TEST-*.xml` no formato JUnit padrão. O script percorre todos esses XMLs, soma os atributos do `<testsuite>` (`tests`, `failures`, `errors`, `skipped`, `time`) e imprime o agregado. Não há dependência externa — o `.sh` usa só `find`/`grep`/`sed`/`awk` e o `.ps1` usa `[xml]` do PowerShell.

---

## Arquitetura e fluxo principal

### Diagrama de componentes

```
   Cliente (Postman / GraphiQL)
        |
        | 1. cadastro + login (GraphQL)           +-----------------------+
        +---------------------------------------> | usuario-autenticacao  | :8081
        |  <-------------- JWT (RS256) ----------- |  emite o token JWT    |
        |                                         +-----------------------+
        | 2. criarPedido / confirmarPedido                     ^
        |    (GraphQL, com JWT)                                | gRPC (validacao)
        v                                                      |
   +----------------------+   publica pedido.criado    +-----------+
   |  restaurante-pedido  | -------------------------> |           |
   |        :8082         |                            |   Kafka   |
   |                      | <-- pagamento.aprovado --- |  (KRaft)  |
   |  consome 4 topicos:  |     pagamento.pendente     |           |
   |  - pagamento.*       |     pedido.em-preparo      +-----------+
   |  - pedido.em-preparo |     pedido.pronto             |     ^
   |  - pedido.pronto     |                               |     | publica
   |                      | --- pedido.pronto-para-cozinha (apos PAGO)
   |                      |                               |     |
   +----------------------+                       consome |     |
        publica pedido.                       pedido.criado     |
        pronto-para-cozinha                          |          |
                                                     v          |
                                              +----------------+
                                              |   pagamento    | :8083
                                              | publica        |
                                              | pagamento.*    |
                                              +----------------+
                                                     |
                                  HTTP + Resilience4j (CB + Retry +
                                  Timeout + Fallback)
                                                     v
                                              +-------------+
                                              |   procpag   | :8089
                                              | gateway de  |
                                              | pagamento   |
                                              +-------------+

                                              +----------------------+
                                              |  restaurante-service | :8084
                                              |       (cozinha)      |
                                              |                      |
                                              | consome              |
                                              | pedido.pronto-para-  |
                                              | cozinha              |
                                              |                      |
                                              | publica              |
                                              | pedido.em-preparo    |
                                              | pedido.pronto        |
                                              +----------------------+
```

**6 tópicos Kafka** orquestram o fluxo:

| Tópico | Publicador | Consumidor(es) |
|---|---|---|
| `pedido.criado` | restaurante-pedido | pagamento |
| `pagamento.aprovado` | pagamento | restaurante-pedido |
| `pagamento.pendente` | pagamento | restaurante-pedido |
| `pedido.pronto-para-cozinha` | restaurante-pedido | restaurante-service |
| `pedido.em-preparo` | restaurante-service | restaurante-pedido |
| `pedido.pronto` | restaurante-service | restaurante-pedido |

### Fluxo principal

1. O cliente **cadastra-se** e faz **login** no `usuario-autenticacao`, que devolve um **JWT** assinado em RS256.
2. Com o token, o cliente chama `criarPedido` no `restaurante-pedido` — o serviço calcula o `valorTotal` e devolve o pedido no status `CRIADO`. O **ID do cliente é extraído do JWT**.
3. O cliente chama `confirmarPedido` — o pedido vai para `CONFIRMADO` e o evento **`pedido.criado`** é publicado no Kafka.
4. O `pagamento` consome `pedido.criado` e chama o gateway externo **`procpag`** via HTTP, protegido por **Resilience4j**.
5. **Sucesso:** o pagamento é aprovado, publica **`pagamento.aprovado`**; o `restaurante-pedido` consome e atualiza o pedido para **`PAGO`**.
6. **Falha do gateway:** o fallback marca o pagamento como pendente e publica **`pagamento.pendente`**; o `restaurante-pedido` consome e marca o pedido como **`PENDENTE_PAGAMENTO`**.
7. Um **worker** no `pagamento` (`@Scheduled`, a cada 30s) reprocessa os pagamentos pendentes; quando o gateway responde, o ciclo do passo 5 se completa e o pedido converge para `PAGO`.
8. **Quando o pedido vira `PAGO`**, o `restaurante-pedido` publica **`pedido.pronto-para-cozinha`** com os itens (sem preço — irrelevante para a cozinha).
9. O `restaurante-service` consome o evento e cria um `PedidoCozinha` no status `RECEBIDO`. O dono do restaurante (`DONO_RESTAURANTE`) consulta a fila via GraphQL em `:8084/graphql`.
10. Mutations `iniciarPreparo` e `marcarComoPronto` avançam o estado (RECEBIDO → EM_PREPARO → PRONTO) e publicam **`pedido.em-preparo`** e **`pedido.pronto`**. O `restaurante-pedido` consome e reflete o status no `Pedido` principal.

### Pontos de resiliência

Todos na chamada do `pagamento` ao `procpag` (`ExternalPaymentClient` + `ProcessarPagamentoService`):

- **Circuit Breaker** — abre após sequência de falhas, evitando martelar um gateway indisponível.
- **Retry** — 3 tentativas com backoff exponencial.
- **Timeout** — corta chamadas que não respondem em 5s.
- **Fallback** — em qualquer falha, marca o pagamento como `PENDENTE` e publica `pagamento.pendente` (o pedido nunca falha para o cliente).
- **Worker de reprocessamento** — drena os pendentes automaticamente quando o gateway volta.

### O worker de reprocessamento (requisito 4.6)

O reprocessamento automático de pagamentos pendentes é feito pelo **`ReprocessamentoPagamentoWorker`** — um componente `@Scheduled` que vive **inteiramente no microsserviço `pagamento`**.

**Onde está configurado** (3 arquivos, todos no módulo `pagamento`):

| Arquivo | Papel |
|---|---|
| `pagamento/.../PagamentoApplication.java` | `@EnableScheduling` — habilita o agendador do Spring |
| `pagamento/.../infrastructure/scheduler/ReprocessamentoPagamentoWorker.java` | A classe do worker, com o método anotado `@Scheduled` |
| `pagamento/src/main/resources/application.properties` | Os parâmetros do worker |

```properties
# pagamento/src/main/resources/application.properties
pagamento.reprocess.fixed-delay-ms=30000     # intervalo entre execucoes (30s)
pagamento.reprocess.initial-delay-ms=15000   # espera apos o startup (15s)
pagamento.reprocess.batch-size=20            # maximo de pendentes por ciclo
```

> Essas propriedades só têm efeito no módulo `pagamento` — é lá que existe o `@Scheduled` que as lê. Declará-las em outro módulo não tem efeito algum.

**Por que o worker está no `pagamento` (e não no `restaurante-pedido`)?**

- **O `pagamento` é o dono do estado do pagamento.** Os registros de pagamento (`PENDENTE`/`APROVADO`) vivem no banco `pagamento_db`, que pertence ao `pagamento`. Reprocessar é varrer esses registros pendentes — uma operação sobre os dados do próprio módulo.
- **Só o `pagamento` conhece o gateway externo (`procpag`).** Reprocessar significa "tentar de novo a chamada ao gateway"; essa integração (`ExternalPaymentClient` + Resilience4j) vive no `pagamento`. O `restaurante-pedido` não conhece — nem deveria conhecer — o `procpag`.
- **Separação de responsabilidades (bounded context).** O `restaurante-pedido` cuida do ciclo de vida do *pedido*; o `pagamento`, do ciclo de vida do *pagamento*. Colocar o worker de pagamento no `restaurante-pedido` misturaria os dois contextos e acoplaria o serviço de pedido a detalhes de pagamento.
- O `restaurante-pedido` apenas **reage** ao resultado: consome os eventos `pagamento.aprovado` / `pagamento.pendente` e atualiza o status do pedido.

**Como isso atende o requisito 4.6 (Reprocessamento Automático):**

O requisito 4.6 pede que, quando o serviço de pagamento voltar a funcionar, os pedidos pendentes sejam reprocessados **automaticamente** e, se aprovados, passem a `PAGO`. O worker cumpre exatamente isso:

1. Roda **automaticamente a cada 30s**, sem nenhuma ação humana (`@Scheduled`).
2. A cada ciclo, busca os pagamentos em `PENDENTE` (lote de até 20) e retenta a cobrança contra o `procpag`.
3. Enquanto o gateway estiver fora, o worker falha em silêncio e tenta de novo no ciclo seguinte — **indefinidamente, sem desistir**.
4. Quando o `procpag` volta, o pagamento é aprovado, marcado `APROVADO`, e o evento `pagamento.aprovado` é publicado no Kafka.
5. O `restaurante-pedido` consome esse evento e atualiza o pedido para **`PAGO`** — fechando o ciclo "reprocessado → confirmado → PAGO" exigido pelo requisito.

---

## Decisões arquiteturais — e por que escolhemos cada uma

Cada decisão técnica deste projeto carrega um trade-off. Esta seção documenta as principais — não para justificar formalmente em prova, mas para que qualquer leitor (ou nosso eu do futuro) entenda **por que** as coisas estão como estão e o que mudaria se o contexto fosse outro.

### Microsserviços (em vez de monolito)

**Decisão:** 4 microsserviços (usuário/autenticação, pedido, pagamento, cozinha) em vez de uma aplicação Spring Boot única.

**Por que:** o spec exige no mínimo 3 serviços separados (req. 5.1), mas a decisão faz sentido além da formalidade. Os 4 contextos têm **modelos de dados distintos** (usuário não tem nada a ver com cozinha, pagamento tem ciclo de vida próprio), **ritmos de mudança diferentes** (regras de pagamento mudam mais que regras de cadastro) e **necessidades de escala diferentes** (um serviço de pagamento sob estresse não deveria afundar o login). Microsserviços permitem evoluir e escalar cada um isoladamente.

**Trade-off:** complexidade operacional aumenta (4 deploys, 4 bancos, observabilidade distribuída). Para uma demo isso vale; para um produto inicial pequeno, monolito modular provavelmente seria melhor.

### Arquitetura hexagonal em cada microsserviço

**Decisão:** todos os 4 módulos seguem o padrão Ports & Adapters (Cockburn) — `domain` puro, `application` com use cases e portas, `adapter` para o mundo externo.

**Por que:** o **domínio fica isolado de framework** (zero imports de Spring/JPA/Kafka em `domain/`). Resultado direto: testes de regra de negócio são triviais (sem `@SpringBootTest`, sem H2), e trocar Kafka por RabbitMQ ou MySQL por Postgres é uma mudança de adapter — não de regra de negócio. Em um projeto que cresce, esse isolamento paga juros compostos.

**Trade-off:** mais classes (interface + implementação + mapper). Aceitável para ganhar testabilidade e independência de framework.

### GraphQL no ponto de entrada (em vez de REST)

**Decisão:** todos os 4 serviços expõem GraphQL como API pública.

**Por que:** o cliente decide quais campos quer em cada query — sem versionamento de endpoints (`/v1/pedidos`, `/v2/pedidos`). O schema é fortemente tipado e autodocumentado (GraphiQL embutido em cada serviço). Para evoluir o modelo, adiciona-se campos novos sem quebrar consumidores antigos.

**Trade-off:** caching é mais difícil (não dá pra usar HTTP cache padrão); curva de aprendizado maior do que REST. Para sistemas com poucos clientes e modelo que evolui, vale.

### gRPC entre serviços (em vez de HTTP/JSON)

**Decisão:** o `restaurante-pedido` consulta o `usuario-autenticacao` via gRPC, não REST/JSON.

**Por que:** chamada interna, contrato fechado, latência crítica. **Protobuf é binário** (3-10× menor que JSON) e o stub gerado dá type-safety em tempo de compilação — erros de contrato pegam no `mvn package`, não em produção.

**Trade-off:** menor visibilidade (não dá pra debugar com `curl`). Aceitável para tráfego interno.

### Apache Kafka para comunicação assíncrona (em vez de chamadas síncronas)

**Decisão:** o `pagamento` não recebe chamadas síncronas do `restaurante-pedido` — ele escuta o evento `pedido.criado`. Idem para o `restaurante-service`.

**Por que isso é a decisão mais importante do projeto:**

1. **Desacopla bounded contexts.** O `restaurante-pedido` não conhece o `pagamento` (e vice-versa) — eles só falam pela linguagem ubíqua dos eventos. Trocar a implementação do `pagamento` é invisível para os outros.
2. **Habilita a resiliência exigida pelo req. 4.5/4.6.** Quando o gateway externo cai, o `restaurante-pedido` não fica com requisição pendurada — ele já recebeu o evento e seguiu a vida. O `pagamento` reprocessa quando puder.
3. **Permite replay.** Se um consumer tiver bug, é só consertar e re-processar o tópico desde o offset desejado.
4. **Desliga o cliente do trabalho pesado.** O cliente recebe resposta de `confirmarPedido` em ms, mesmo se o gateway de pagamento demorar segundos.

**Trade-off:** consistência eventual (o pedido fica `CONFIRMADO` antes de virar `PAGO` — observabilidade precisa lidar com isso). Sistemas que precisam de consistência forte deveriam usar transações distribuídas (saga + outbox).

### Kafka em modo KRaft (em vez de Zookeeper)

**Decisão:** broker Kafka single-node em modo KRaft (sem Zookeeper).

**Por que:** KRaft é GA desde Kafka 3.3 e é o futuro do projeto (Zookeeper será removido na 4.0). Para nós: **−1 container, −150 MB de RAM, ~10 s a menos no startup** e menos um ponto de configuração. Para single-node em dev/demo, é o caminho óbvio.

**Trade-off:** para produção multi-broker, vale revisar — Zookeeper ainda é mais battle-tested em alta disponibilidade. Mas mesmo lá KRaft está consolidando.

### Cada microsserviço com seu próprio database MySQL

**Decisão:** 4 bancos (`auth_db`, `pedido_db`, `pagamento_db`, `cozinha_db`) no mesmo MySQL — não tabelas compartilhadas.

**Por que:** evolução de schema **independente** (mudar `pagamentos.status` não afeta `pedidos`). Joins entre bancos são impossíveis na prática — força a conversa via eventos, que é o modelo correto em microsserviços. **"Database per service"** é literal aqui.

**Trade-off:** dados duplicados em alguns lugares (ex.: o `restaurante-service` armazena snapshot dos itens do pedido em vez de fazer JOIN com `pedido_db`). Aceitável — a duplicação reflete a necessidade real de cada contexto.

### JWT RS256 (assinatura assimétrica, em vez de HS256)

**Decisão:** o JWT é assinado pelo `usuario-autenticacao` com **chave privada RSA**, e validado pelos outros 3 serviços usando apenas a **chave pública** (distribuída via classpath, em `keys/publicKey.pem`).

**Por que:** com HS256 (chave simétrica), **todos os serviços precisariam conhecer a chave secreta** — qualquer um deles poderia *emitir* tokens, não só validá-los. Com RS256, só o `usuario-autenticacao` emite; os outros apenas verificam. Limita explosão de blast radius se um serviço de pedido for comprometido.

**Trade-off:** chave pública precisa ser distribuída (hoje via classpath; em produção seria via JWKS endpoint dinâmico).

### Resilience4j (em vez de retry manual ou Hystrix)

**Decisão:** Circuit Breaker, Retry, Timeout e Fallback declarativos via anotações Resilience4j.

**Por que:** Hystrix está em **maintenance mode** desde 2018. Resilience4j é a alternativa moderna e ativamente mantida, com integração nativa Spring Boot. **Declarativo** > imperativo — em vez de escrever try/catch/retry à mão, anota-se o método e a regra é separada da lógica de negócio. Ler o código fica mais limpo.

**Trade-off:** mais uma dependência. Padrão de mercado, vale.

### Bounded context separado para a cozinha (em vez de mais um agregado no `restaurante-pedido`)

**Decisão:** `restaurante-service` é um microsserviço novo, com banco próprio (`cozinha_db`) — não uma tabela extra no `pedido_db`.

**Por que:** **a cozinha enxerga um modelo diferente.** O `restaurante-pedido` se importa com preço, cliente, valor total. A cozinha se importa com produto, quantidade e estado de produção (`RECEBIDO`/`EM_PREPARO`/`PRONTO`). Misturar os dois mata a clareza de cada um. Ao separar, cada agregado fica simples e responsável por sua regra própria.

**Trade-off:** mais um deploy. Para um restaurante real onde a cozinha realmente vive em outro processo (display físico no balcão), vale 100%. Se a "cozinha" for só uma tela administrativa no mesmo app, talvez não.

### Docker Compose (em vez de Kubernetes)

**Decisão:** orquestração via `docker-compose.yml`.

**Por que:** o spec pede explicitamente um `compose.yml` que sobe tudo num comando. Compose é trivial de operar em uma máquina única, perfeito para dev e demo. **"Um comando sobe tudo"** é parte do entregável.

**Trade-off:** zero replicação real, zero failover, sem service discovery dinâmico. Em produção, k8s/ECS/Nomad são o caminho.

### AppCDS + Layered JARs no Dockerfile (em vez de fat jar simples)

**Decisão:** Dockerfiles multi-stage que (1) extraem o jar em **layers Spring Boot** e (2) fazem um **training run de AppCDS** durante o build, gerando um `application.jsa` carregado no startup.

**Por que:** layered jars deixam o cache do Docker mais inteligente — mudança de código invalida ~5 MB (layer `application`), não 50 MB (jar inteiro). AppCDS pré-resolve as classes da aplicação, reduzindo o boot da JVM **~30%** (de ~3 s para ~2 s na fase JVM, validado via `java -version` exibindo `mixed mode, sharing`).

**Trade-off:** build mais lento (~1 min a mais por app para o training). Para apps que sobem várias vezes, paga rapidamente.

### Resource limits enxutos no compose (memory + cpu)

**Decisão:** cada container tem `deploy.resources.limits` explícito (memória + CPU). Total: ~3.5 GB e ~6 CPUs.

**Por que:** sem limit, uma JVM com `MaxRAMPercentage=65` enxerga toda a RAM do host — 4 JVMs simultâneas tentam pegar 65% × 4 = 260% da memória. Com limit, cada uma respeita seu envelope. Idem para CPU durante o boot paralelo (4 apps subindo ao mesmo tempo saturam o host).

**Trade-off:** se algum app legitimamente precisar de mais memória sob carga, vai sofrer OOMKill. Para dev/demo, aceitável; em produção, ajuste fino por percentil de uso real.

---

## Requisitos da Fase 3 — o que foi feito e como validar

### Requisitos funcionais

| Requisito | Implementação | Como validar |
|---|---|---|
| ✅ **4.1** Criar e autenticar cliente | `usuario-autenticacao` — mutations GraphQL `cadastrarUsuario` e `login` (JWT RS256) | Postman › `1. Autenticacao` › *Cadastrar Usuario* e *Login como Usuario* |
| ✅ **4.2** Criar pedido (cliente do token, restaurante, itens, total, confirmação) | `restaurante-pedido` — mutations `criarPedido` (calcula `valorTotal`) e `confirmarPedido` | Postman › `2. Pedidos` › *Criar Pedido* (veja o `valorTotal`) e *Confirmar Pedido* |
| ✅ **4.3** Consultar pedido por ID e listar pedidos do cliente | `restaurante-pedido` — queries `pedidoPorId` (com **verificação de ownership** — só retorna se o pedido pertencer ao cliente do JWT) e `meusPedidos` | Postman › `2. Pedidos` › *Pedido por ID* e *Meus Pedidos*. Tentar consultar pedido alheio retorna `404 NOT_FOUND` |
| ✅ **4.4** Processar pagamento via gateway externo | `pagamento` — `ExternalPaymentClient` chama `procpag:8089/requisicao` ao consumir `pedido.criado` | Crie e confirme um pedido; depois Postman › `3. Pagamento` › *Pagamento por Pedido* → `APROVADO`. Logs: `docker logs pagamento` |
| ✅ **4.5** Pagamento pendente quando o gateway está indisponível | Fallback do Resilience4j marca `PENDENTE` e publica `pagamento.pendente`; o pedido não falha | `docker stop procpag`, crie+confirme um pedido; consulte *Pedido por ID* → `PENDENTE_PAGAMENTO` |
| ✅ **4.6** Reprocessamento automático | `pagamento` — `ReprocessamentoPagamentoWorker` (`@Scheduled` 30s) reprocessa pendentes | Após o teste 4.5, `docker start procpag`; aguarde ~30s e consulte *Pedido por ID* → `PAGO` |
| ✅ **4.7** Atualização automática de status | `restaurante-pedido` consome `pagamento.aprovado`/`pagamento.pendente` e atualiza o pedido | Consulte *Pedido por ID* após o pagamento — o status muda sem intervenção manual |

### Requisitos não funcionais

| Requisito | Implementação | Como validar |
|---|---|---|
| ✅ **5.1** Arquitetura em múltiplos serviços | `usuario-autenticacao`, `restaurante-pedido`, `pagamento`, `restaurante-service` (opcional, implementado) — 4 módulos Maven independentes | `docker compose ps` — 4 aplicações + `procpag` + infraestrutura |
| ✅ **5.2** Spring Security + JWT (login, perfis, endpoints protegidos, ID do token) | JWT RS256; perfis `USUARIO` (cliente) e `DONO_RESTAURANTE` (admin); `@PreAuthorize` nos resolvers; `clienteId` extraído do `subject` do token | Postman › *Me sem autenticacao* e *Criar Pedido sem autenticacao* → erro; com token → sucesso |
| ✅ **5.3** Comunicação assíncrona com Kafka | **6 tópicos**: `pedido.criado`, `pagamento.aprovado`, `pagamento.pendente`, `pedido.pronto-para-cozinha`, `pedido.em-preparo`, `pedido.pronto` | Kafka UI em `http://localhost:8085` — inspecione os 6 tópicos e suas mensagens |
| ✅ **5.4** Resiliência (Resilience4j) | Circuit Breaker + Retry + Timeout + Fallback na chamada ao `procpag` | `curl http://localhost:8083/actuator/circuitbreakers`; logs mostram o CB abrindo sob falhas |
| ✅ **5.5** Boas práticas — Clean/Hexagonal | Camadas `domain` / `application` (use cases + ports) / `adapter` (inbound/outbound) / `infrastructure` em todos os 4 módulos | Veja [Estrutura do repositório](#estrutura-do-repositório) |

### Entregáveis (item 3 da spec)

| Entregável | Status | Onde |
|---|---|---|
| ✅ Aplicação funcionando com todos os serviços | Feito | `docker compose up -d --build` |
| ✅ Arquivo `compose.yml` que sobe tudo num comando | Feito | [`docker-compose.yml`](docker-compose.yml) |
| ✅ Arquivo para teste dos endpoints | Feito | Collection + environment Postman em [`docs/`](docs/) |
| ✅ Documentação (diagrama, fluxo, pontos de resiliência) | Feito | Este README — seção [Arquitetura e fluxo principal](#arquitetura-e-fluxo-principal) |
| ✅ Repositório com o código-fonte | Feito | Este repositório |
| ❌ Vídeo de apresentação (até 10 min) | **Pendente** | Ainda precisa ser gravado |

---

## O que ainda falta

| Item | Situação |
|---|---|
| 🎬 **Vídeo de apresentação** (até 10 min) | **Pendente — única entrega bloqueante** restante. Roteiro detalhado já escrito em [`docs/roteiro-video.md`](docs/roteiro-video.md). Falta gravar. |
| 🧹 Limpeza de testes duplicados *(opcional)* | Após o merge de duas branches de testes em paralelo, alguns use cases têm versão "consolidada" (ex.: `PedidoUseCaseServiceTest`) coexistindo com versão "granular" (ex.: `CriarPedidoServiceTest` + `ConfirmarPedidoServiceTest` + ...). Funciona; só duplica execução. Pode ser limpado num PR à parte. |
| 📐 Diagrama C4 formal *(opcional)* | A spec aceita "diagrama de componentes, sequência **ou** C4". O diagrama de componentes ASCII deste README atende o requisito; um C4 formal seria um plus visual. |
| 🧩 `api-gateway` *(opcional)* | Marcado como opcional na spec (item 5.1). Análise feita: ganho funcional pequeno para o esforço, complica a demo de resiliência. Decisão consciente de não fazer. O `restaurante-service` (outro opcional) foi implementado. |

---

## Serviços e portas

| Serviço | Porta host | Imagem / Build | Papel |
|---|---|---|---|
| `usuario-autenticacao` | 8081 | Spring Boot (build local) | Cadastro/login, emissão de JWT, servidor gRPC de consulta de usuário |
| `restaurante-pedido` | 8082 | Spring Boot (build local) | Criação/confirmação/consulta de pedidos; produz `pedido.criado` e `pedido.pronto-para-cozinha`, consome `pagamento.*`, `pedido.em-preparo` e `pedido.pronto` |
| `pagamento` | 8083 | Spring Boot (build local) | Consome `pedido.criado`, chama o `procpag` com resiliência, publica `pagamento.*`, worker de reprocessamento |
| `restaurante-service` | 8084 | Spring Boot (build local) | Fila da cozinha (RECEBIDO/EM_PREPARO/PRONTO); consome `pedido.pronto-para-cozinha`, publica `pedido.em-preparo` e `pedido.pronto` |
| `procpag` | 8089 | `docker.io/erickemprobr/procpag:latest` | **Gateway de pagamento externo (fornecido pelos professores)** — simula um serviço *eventualmente disponível*: ora autoriza, ora responde com erro/timeout |
| `mysql` | 3307 (host) → 3306 | `mysql:8.4` | Persistência — bancos `auth_db`, `pedido_db`, `pagamento_db`, `cozinha_db` criados por `init.sql` |
| `kafka` | 9092 | `confluentinc/cp-kafka:7.7.1` | Broker de mensageria assíncrona em modo **KRaft** (single-node, sem Zookeeper) |
| `kafka-ui` | 8085 | `provectuslabs/kafka-ui:v0.7.2` | Interface web para inspecionar tópicos e mensagens |

Todos os containers vivem na rede `fase3net` e se enxergam pelo nome do serviço.

**Interfaces web úteis:**

| URL | O que é |
|---|---|
| `http://localhost:8081/graphiql` | GraphiQL do `usuario-autenticacao` |
| `http://localhost:8082/graphiql` | GraphiQL do `restaurante-pedido` |
| `http://localhost:8083/graphiql` | GraphiQL do `pagamento` |
| `http://localhost:8084/graphiql` | GraphiQL do `restaurante-service` (cozinha) |
| `http://localhost:8085` | Kafka UI |

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 25 (Temurin LTS) |
| Spring Boot | 4.0.5 |
| Spring GraphQL | 4.0.x |
| Springdoc OpenAPI / Swagger UI | 3.0.3 |
| Spring Security (OAuth2 Resource Server) | 7.x |
| Apache Kafka (Confluent, **KRaft mode**) | 7.7.1 |
| Spring Kafka | 4.0.4 |
| MySQL | 8.4 |
| gRPC | 1.68.x |
| Resilience4j | 2.3.0 |
| H2 (runtime, p/ training de AppCDS no docker build) | embutida pelo Spring Boot |
| Jackson (2, para serialização Kafka) | 2.21.2 |
| Maven | wrapper (3.9+) |
| Docker / Docker Compose | 24+ / v2 |
| Otimizações JVM no Dockerfile | AppCDS (`-XX:SharedArchiveFile`) + Layered JARs (`-Djarmode=layertools`) |

---

## Estrutura do repositório

```
fiap-restaurante-fase3/
├── docker-compose.yml          # sobe os 8 containers num comando
├── init.sql                    # cria os 4 databases no MySQL
├── pom.xml                     # parent POM multi-módulo (5 módulos)
├── shared/                     # stubs gRPC + BusinessException compartilhada
├── usuario-autenticacao/       # microsserviço de cadastro/login/JWT
├── restaurante-pedido/         # microsserviço de pedidos
├── pagamento/                  # microsserviço de pagamento (Kafka + Resilience4j)
├── restaurante-service/        # microsserviço de cozinha (fila de produção)
├── docs/
│   ├── documentacao-arquitetura.pdf                       # documentação técnica ABNT (17 pp)
│   ├── adr/                                               # 13 Architecture Decision Records
│   ├── diagramas/                                         # diagramas Mermaid (componentes + sequência + estados)
│   ├── build-pdf/                                         # script Python que regenera o PDF
│   ├── fiap-fase-3-restaurante.postman_collection.json    # coleção de testes
│   ├── fiap-fase-3-restaurante.postman_environment.json   # environment (URLs + credenciais)
│   └── roteiro-video.md                                   # roteiro detalhado do vídeo de entrega
└── scripts/
    ├── test-summary.sh         # roda mvn test e imprime resumo agregado (Linux/macOS)
    └── test-summary.ps1        # idem para Windows (PowerShell)
```

Cada microsserviço segue o mesmo layout interno (arquitetura hexagonal):

```
<servico>/src/main/java/br/com/fiaprestaurante/<servico>/
├── <Servico>Application.java   # main Spring Boot
├── domain/                     # entidades puras, value objects, exceções de negócio (ZERO framework)
├── application/
│   ├── dto/                    # commands, responses, eventos
│   ├── port/input/             # interfaces dos use cases
│   ├── port/output/            # interfaces de repositório, mensageria, HTTP
│   └── usecase/                # implementações dos use cases
├── adapter/
│   ├── inbound/                # GraphQL controllers, Kafka consumers
│   └── outbound/               # JPA repositories, Kafka producers, HTTP clients
└── infrastructure/
    ├── config/                 # SecurityConfig, KafkaConfig, GraphQlConfig
    ├── exception/              # *GraphQLExceptionHandler (tradução de exceções de negócio em erros GraphQL)
    └── scheduler/              # workers @Scheduled (só no `pagamento`)
```

### Contas seed

Criadas automaticamente na inicialização:

| E-mail | Senha | Perfil |
|---|---|---|
| `usuario@fiap.com` | `usuario123` | `USUARIO` (cliente) |
| `dono@fiap.com` | `dono123` | `DONO_RESTAURANTE` (admin) |

---

## Diagnóstico e inspeção

**Logs em tempo real:**

```bash
docker logs -f restaurante-pedido
docker logs -f pagamento
docker logs -f usuario-autenticacao
docker logs -f restaurante-service
```

**Inspecionar o estado dos pedidos no MySQL:**

```bash
docker exec -it mysql mysql -uroot -proot -e "
USE pedido_db;
SELECT BIN_TO_UUID(id) AS pedido, status, valor_total
FROM pedidos ORDER BY created_at DESC LIMIT 10;"
```

**Inspecionar os pagamentos:**

```bash
docker exec -it mysql mysql -uroot -proot -e "
USE pagamento_db;
SELECT BIN_TO_UUID(pedido_id) AS pedido, valor, status, tentativas FROM pagamentos;"
```

**Inspecionar a fila da cozinha:**

```bash
docker exec -it mysql mysql -uroot -proot -e "
USE cozinha_db;
SELECT BIN_TO_UUID(id) AS pedido_cozinha, BIN_TO_UUID(pedido_id) AS pedido_origem,
       status, iniciado_em, finalizado_em
FROM pedidos_cozinha ORDER BY created_at DESC LIMIT 10;"
```

**Publicar um evento `pedido.criado` manualmente** (simula um pedido sem passar pelo GraphQL):

```bash
echo '{"pedidoId":"00000000-0000-0000-0000-000000000001","valorTotal":42.50}' \
  | docker exec -i kafka kafka-console-producer \
      --bootstrap-server kafka:9092 --topic pedido.criado
```

**Estado do Circuit Breaker:**

```bash
curl http://localhost:8083/actuator/circuitbreakers
```

**Confirmar que o AppCDS está ativo** em um dos apps:

```bash
docker exec restaurante-pedido java -XX:SharedArchiveFile=application.jsa -Xshare:on -version
# saída deve incluir "mixed mode, sharing"
```

Os 6 tópicos Kafka podem ser inspecionados visualmente no **Kafka UI** (`http://localhost:8085`).
