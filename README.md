# FIAP Restaurante — Fase 3

Sistema de pedido online para restaurante, dividido em microsserviços, comunicando-se de forma síncrona (gRPC, GraphQL) e assíncrona (Apache Kafka), com resiliência aplicada na chamada ao gateway externo de pagamento.

Projeto desenvolvido como **Tech Challenge da Fase 3** da PosTech FIAP. A especificação completa está em [`requisito/tech-challenge-fase-03.pdf`](../requisito/tech-challenge-fase-03.pdf).

> **Para quem vai executar pela primeira vez:** comece pela seção [Como executar](#como-executar) e depois [Testar com o Postman](#testar-com-o-postman). O sistema inteiro sobe com **um único comando**.

---

## Índice

1. [Como executar](#como-executar)
2. [Testar com o Postman](#testar-com-o-postman)
3. [Swagger e contrato HTTP do GraphQL](#swagger-e-contrato-http-do-graphql)
4. [Arquitetura e fluxo principal](#arquitetura-e-fluxo-principal)
5. [Requisitos da Fase 3 — o que foi feito e como validar](#requisitos-da-fase-3--o-que-foi-feito-e-como-validar)
6. [O que ainda falta](#o-que-ainda-falta)
7. [Serviços e portas](#serviços-e-portas)
8. [Stack](#stack)
9. [Estrutura do repositório](#estrutura-do-repositório)
10. [Diagnóstico e inspeção](#diagnóstico-e-inspeção)

---

## Como executar

### Pré-requisitos

- **Docker Desktop** instalado e em execução (Docker 24+, Compose v2).
- ~3 GB de RAM livres.
- Portas livres no host: **8081, 8082, 8083, 8085, 8089, 9092**.
- JDK 25 (Temurin) — **opcional**, só necessário para compilar fora do Docker.

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

Esse único comando sobe os **8 containers** (3 aplicações + procpag + MySQL + Kafka + Zookeeper + Kafka UI).

> **Primeira execução é demorada** — o Maven baixa todas as dependências dentro dos containers. Se o build falhar por instabilidade de rede, **rode o mesmo comando de novo**: o cache de dependências (BuildKit) retoma de onde parou. Execuções seguintes aproveitam o cache e sobem em segundos.

### 2. Conferir se subiu

```bash
docker compose ps
```

Os 8 containers devem aparecer como `Up`; `mysql`, `kafka` e `zookeeper` com `(healthy)`.

Healthcheck das aplicações:

```bash
curl http://localhost:8082/actuator/health   # restaurante-pedido  -> {"status":"UP"}
curl http://localhost:8083/actuator/health   # pagamento           -> {"status":"UP"}
```

> O `usuario-autenticacao` não expõe Actuator — valide abrindo o GraphiQL em `http://localhost:8081/graphiql`.

### 3. Parar tudo

```bash
docker compose down       # mantém o volume do MySQL
docker compose down -v    # remove também os dados persistidos
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

---

## Arquitetura e fluxo principal

### Diagrama de componentes

```
   Cliente (Postman / GraphiQL)
        |
        | 1. cadastro + login (GraphQL)           +-----------------------+
        +---------------------------------------> | usuario-autenticacao  | :8081
        |  <-------------- JWT (RS256) ---------   |   emite o token JWT   |
        |                                         +-----------------------+
        | 2. criarPedido / confirmarPedido
        |    (GraphQL, com JWT)
        v
   +----------------------+   publica pedido.criado    +-----------+
   |  restaurante-pedido  | -------------------------> |   Kafka   |
   |        :8082         |                            +-----------+
   |                      |                              |      ^
   |  consome             | <-- pagamento.aprovado ------+      | publica
   |  pagamento.aprovado  |     pagamento.pendente              | pagamento.*
   |  pagamento.pendente  |                                     |
   +----------------------+                            +-----------------+
                                                       |    pagamento    | :8083
                                       consome         |  consome        |
                                       pedido.criado   |  pedido.criado  |
                                                       +-----------------+
                                                               |
                                          HTTP + Resilience4j   |
                                     (Circuit Breaker / Retry /  |
                                       Timeout / Fallback)        v
                                                          +-------------+
                                                          |   procpag   | :8089
                                                          | gateway de  |
                                                          | pagamento   |
                                                          +-------------+
```

### Fluxo principal

1. O cliente **cadastra-se** e faz **login** no `usuario-autenticacao`, que devolve um **JWT** assinado em RS256.
2. Com o token, o cliente chama `criarPedido` no `restaurante-pedido` — o serviço calcula o `valorTotal` e devolve o pedido no status `CRIADO`. O **ID do cliente é extraído do JWT**.
3. O cliente chama `confirmarPedido` — o pedido vai para `CONFIRMADO` e o evento **`pedido.criado`** é publicado no Kafka.
4. O `pagamento` consome `pedido.criado` e chama o gateway externo **`procpag`** via HTTP, protegido por **Resilience4j**.
5. **Sucesso:** o pagamento é aprovado, publica **`pagamento.aprovado`**; o `restaurante-pedido` consome e atualiza o pedido para **`PAGO`**.
6. **Falha do gateway:** o fallback marca o pagamento como pendente e publica **`pagamento.pendente`**; o `restaurante-pedido` consome e marca o pedido como **`PENDENTE_PAGAMENTO`**.
7. Um **worker** no `pagamento` (`@Scheduled`, a cada 30s) reprocessa os pagamentos pendentes; quando o gateway responde, o ciclo do passo 5 se completa e o pedido converge para `PAGO`.

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

## Requisitos da Fase 3 — o que foi feito e como validar

### Requisitos funcionais

| Requisito | Implementação | Como validar |
|---|---|---|
| ✅ **4.1** Criar e autenticar cliente | `usuario-autenticacao` — mutations GraphQL `cadastrarUsuario` e `login` (JWT RS256) | Postman › `1. Autenticacao` › *Cadastrar Usuario* e *Login como Usuario* |
| ✅ **4.2** Criar pedido (cliente do token, restaurante, itens, total, confirmação) | `restaurante-pedido` — mutations `criarPedido` (calcula `valorTotal`) e `confirmarPedido` | Postman › `2. Pedidos` › *Criar Pedido* (veja o `valorTotal`) e *Confirmar Pedido* |
| ✅ **4.3** Consultar pedido por ID e listar pedidos do cliente | `restaurante-pedido` — queries `pedidoPorId` e `meusPedidos` | Postman › `2. Pedidos` › *Pedido por ID* e *Meus Pedidos* |
| ✅ **4.4** Processar pagamento via gateway externo | `pagamento` — `ExternalPaymentClient` chama `procpag:8089/requisicao` ao consumir `pedido.criado` | Crie e confirme um pedido; depois Postman › `3. Pagamento` › *Pagamento por Pedido* → `APROVADO`. Logs: `docker logs pagamento` |
| ✅ **4.5** Pagamento pendente quando o gateway está indisponível | Fallback do Resilience4j marca `PENDENTE` e publica `pagamento.pendente`; o pedido não falha | `docker stop procpag`, crie+confirme um pedido; consulte *Pedido por ID* → `PENDENTE_PAGAMENTO` |
| ✅ **4.6** Reprocessamento automático | `pagamento` — `ReprocessamentoPagamentoWorker` (`@Scheduled` 30s) reprocessa pendentes | Após o teste 4.5, `docker start procpag`; aguarde ~30s e consulte *Pedido por ID* → `PAGO` |
| ✅ **4.7** Atualização automática de status | `restaurante-pedido` consome `pagamento.aprovado`/`pagamento.pendente` e atualiza o pedido | Consulte *Pedido por ID* após o pagamento — o status muda sem intervenção manual |

### Requisitos não funcionais

| Requisito | Implementação | Como validar |
|---|---|---|
| ✅ **5.1** Arquitetura em múltiplos serviços | `usuario-autenticacao`, `restaurante-pedido`, `pagamento` — 3 módulos Maven independentes | `docker compose ps` — 3 aplicações + `procpag` + infraestrutura |
| ✅ **5.2** Spring Security + JWT (login, perfis, endpoints protegidos, ID do token) | JWT RS256; perfis `USUARIO` (cliente) e `DONO_RESTAURANTE` (admin); `@PreAuthorize` nos resolvers; `clienteId` extraído do `subject` do token | Postman › *Me sem autenticacao* e *Criar Pedido sem autenticacao* → erro; com token → sucesso |
| ✅ **5.3** Comunicação assíncrona com Kafka | Tópicos `pedido.criado`, `pagamento.aprovado`, `pagamento.pendente` | Kafka UI em `http://localhost:8085` — inspecione os 3 tópicos e suas mensagens |
| ✅ **5.4** Resiliência (Resilience4j) | Circuit Breaker + Retry + Timeout + Fallback na chamada ao `procpag` | `curl http://localhost:8083/actuator/circuitbreakers`; logs mostram o CB abrindo sob falhas |
| ✅ **5.5** Boas práticas — Clean/Hexagonal | Camadas `domain` / `application` (use cases + ports) / `adapter` (inbound/outbound) / `infrastructure` em todos os módulos | Veja [Estrutura do repositório](#estrutura-do-repositório) |

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
| 🎬 **Vídeo de apresentação** (até 10 min) | **Pendente** — precisa demonstrar as funcionalidades e explicar a arquitetura. Não é código; deve ser gravado antes da entrega. |
| 🧪 **Testes automatizados** (unitários/integração) | Validado em 25/05/2026 com JaCoCo: `usuario-autenticacao` 46 testes / `98,99%` de linhas; `restaurante-pedido` 43 testes / `92,91%`; `pagamento` 49 testes / `96,89%`. |
| 📐 Diagrama C4 formal *(opcional)* | A spec aceita "diagrama de componentes, sequência **ou** C4". O diagrama de componentes ASCII deste README atende o requisito; um C4 formal seria um plus. |
| 🧩 `restaurante-service` e `api-gateway` *(opcionais)* | Marcados como **opcionais** na spec (itens 5.1) — não implementados. |

---

## Serviços e portas

| Serviço | Porta host | Imagem / Build | Papel |
|---|---|---|---|
| `usuario-autenticacao` | 8081 | Spring Boot (build local) | Cadastro/login, emissão de JWT, servidor gRPC de consulta de usuário |
| `restaurante-pedido` | 8082 | Spring Boot (build local) | Criação/confirmação/consulta de pedidos; produz `pedido.criado`, consome `pagamento.*` |
| `pagamento` | 8083 | Spring Boot (build local) | Consome `pedido.criado`, chama o `procpag` com resiliência, publica `pagamento.*`, worker de reprocessamento |
| `procpag` | 8089 | `docker.io/erickemprobr/procpag:latest` | **Gateway de pagamento externo (fornecido pelos professores)** — simula um serviço *eventualmente disponível*: ora autoriza, ora responde com erro/timeout |
| `mysql` | (interno) | `mysql:8.4` | Persistência — bancos `auth_db`, `pedido_db`, `pagamento_db` criados por `init.sql` |
| `zookeeper` | (interno) | `confluentinc/cp-zookeeper:7.4.0` | Coordenação do Kafka |
| `kafka` | 9092 | `confluentinc/cp-kafka:7.4.0` | Broker de mensageria assíncrona |
| `kafka-ui` | 8085 | `provectuslabs/kafka-ui:v0.7.2` | Interface web para inspecionar tópicos e mensagens |

Todos os containers vivem na rede `fase3net` e se enxergam pelo nome do serviço.

**Interfaces web úteis:**

| URL | O que é |
|---|---|
| `http://localhost:8081/graphiql` | GraphiQL do `usuario-autenticacao` |
| `http://localhost:8082/graphiql` | GraphiQL do `restaurante-pedido` |
| `http://localhost:8083/graphiql` | GraphiQL do `pagamento` |
| `http://localhost:8081/swagger-ui/index.html` | Swagger UI / OpenAPI do `usuario-autenticacao`, com exemplos de cadastro, login e `me` |
| `http://localhost:8082/swagger-ui/index.html` | Swagger UI / OpenAPI do `restaurante-pedido`, com exemplos de pedidos |
| `http://localhost:8083/swagger-ui/index.html` | Swagger UI / OpenAPI do `pagamento`, com exemplos de consulta de pagamento e respostas HTTP |
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
| Apache Kafka (Confluent) | 7.4.0 |
| Spring Kafka | 4.0.4 |
| MySQL | 8.4 |
| gRPC | 1.68.x |
| Resilience4j | 2.3.0 |
| Jackson (2, para serialização Kafka) | 2.21.2 |
| Maven | wrapper (3.9+) |
| Docker / Docker Compose | 24+ / v2 |

---

## Estrutura do repositório

```
fiap-restaurante-fase3/
├── docker-compose.yml          # sobe todos os serviços num comando
├── init.sql                    # cria os 3 databases no MySQL
├── pom.xml                     # parent POM multi-módulo
├── shared/                     # stubs gRPC + BusinessException compartilhada
├── usuario-autenticacao/       # microsserviço de cadastro/login/JWT
├── restaurante-pedido/         # microsserviço de pedidos
├── pagamento/                  # microsserviço de pagamento (Kafka + Resilience4j)
└── docs/
    ├── fiap-fase-3-restaurante.postman_collection.json    # coleção de testes
    └── fiap-fase-3-restaurante.postman_environment.json   # environment (URLs + credenciais)
```

Cada microsserviço segue o mesmo layout interno (arquitetura hexagonal):

```
<servico>/src/main/java/br/com/fiaprestaurante/<servico>/
├── <Servico>Application.java   # main Spring Boot
├── domain/                     # entidades puras, value objects, exceções de negócio
├── application/
│   ├── dto/                    # commands, responses, eventos
│   ├── port/input/             # interfaces dos use cases
│   ├── port/output/            # interfaces de repositório, mensageria, HTTP
│   └── usecase/                # implementações dos use cases
├── adapter/
│   ├── inbound/                # GraphQL controllers, Kafka consumers
│   └── outbound/               # JPA repositories, Kafka producers, HTTP clients
└── infrastructure/
    ├── config/                 # SecurityConfig, KafkaConfig, etc.
    └── scheduler/              # workers @Scheduled (quando houver)
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

Os tópicos `pedido.criado`, `pagamento.aprovado` e `pagamento.pendente` podem ser inspecionados visualmente no **Kafka UI** (`http://localhost:8085`).
