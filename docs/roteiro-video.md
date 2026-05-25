# Roteiro — Video de apresentacao (Tech Challenge Fase 3)

> Duracao alvo: **10 minutos**. Ritmo: ~150 palavras/min. Use o roteiro como guia (nao precisa ler literalmente).
>
> **Antes de gravar:**
> 1. `docker compose down -v && docker compose up -d --build` — espere todos os containers ficarem `(healthy)` (~3 min)
> 2. Abra: terminal, Postman (com environment `fiap-fase-3-restaurante` ativo), VSCode/IntelliJ no projeto, navegador com 2 abas (Kafka UI em `http://localhost:8085` e GraphiQL em `http://localhost:8081/graphiql`)
> 3. Tenha um segundo terminal aberto para `docker logs -f`
> 4. Faca um Login no Postman antes de comecar a gravar (popula `{{token}}`)

---

## 0:00 – 1:00 · Apresentacao + problema + stack (1 min)

**Falar (~150 palavras):**

> "Ola! Sou o Danilo Fernando, e este e o **Tech Challenge da Fase 3** da PosTech FIAP.
>
> O desafio: construir um sistema de pedidos online para um restaurante. O cliente entra, cadastra-se, autentica, monta o pedido, confirma, e o sistema processa o pagamento contra um **gateway externo eventualmente indisponivel** — entao precisa de **resiliencia**. Depois que o pagamento eh aprovado, o pedido entra na fila da cozinha, que prepara e finaliza.
>
> Para isso, dividi a aplicacao em **4 microsservicos** Spring Boot 4 com Java 25:
> - **`usuario-autenticacao`** — cadastro, login, JWT RS256, validacao gRPC entre servicos
> - **`restaurante-pedido`** — criacao e gestao de pedidos via GraphQL
> - **`pagamento`** — integracao com o gateway externo, resiliencia com Resilience4j (Circuit Breaker, Retry, Timeout, Fallback)
> - **`restaurante-service`** — fila da cozinha, modulo opcional do requisito 5.1
>
> Comunicacao: **sincrona** com gRPC + GraphQL, **assincrona** com Apache Kafka. Tudo orquestrado por **Docker Compose** — sobe inteiro com um comando."

**Tela:** README aberto no editor, ou um slide simples com o diagrama de blocos.

---

## 1:00 – 2:30 · Arquitetura (1:30 min)

**Falar:**

> "A arquitetura segue tres principios:
>
> **1. Hexagonal em cada modulo.** Domain isolado de framework, application com use cases e ports, adapters de entrada e saida. Voce ve no codigo: pasta `domain` sem nenhum import de Spring ou JPA, e os adapters convertendo no limite. Isso permite trocar Kafka por RabbitMQ ou MySQL por Postgres sem tocar regra de negocio.
>
> **2. Bounded contexts separados.** Cada microsservico tem seu proprio banco MySQL (`auth_db`, `pedido_db`, `pagamento_db`, `cozinha_db`) e seu proprio conjunto de eventos. Nada compartilhado a nivel de dados — so contratos.
>
> **3. Eventos Kafka como cola assincrona.** Sao **6 topicos** que conectam tudo:
> - `pedido.criado` — pedido confirmado, dispara processamento de pagamento
> - `pagamento.aprovado` / `pagamento.pendente` — resultado do gateway
> - `pedido.pronto-para-cozinha` — pagamento OK, cozinha pode comecar
> - `pedido.em-preparo` / `pedido.pronto` — cozinha avancando o pedido
>
> Cada evento permite que os servicos evoluam de forma independente — e e o que torna a resiliencia possivel, como vou mostrar mais a frente.
>
> No diagrama: 4 apps Spring + procpag (gateway externo, container fornecido pelos professores) + MySQL + Kafka em modo KRaft (sem Zookeeper) + Kafka UI."

**Tela:** Diagrama de componentes (pode ser o ASCII do README) ou seguindo a estrutura visual na IDE.

---

## 2:30 – 5:30 · Demo do happy path completo (3 min)

**Setup:** terminal, Postman, Kafka UI lado a lado.

**Falar + executar:**

> "Vamos rodar o fluxo completo de ponta a ponta."

### Passo 1 — Login (15s)

> "Primeiro, faco login como cliente comum. Postman → pasta `1. Autenticacao` → `Login como Usuario`. Recebo um JWT que e salvo automaticamente em `{{token}}`."

**Mostra:** request → response com token → tab Tests com `pm.environment.set('token', ...)`

### Passo 2 — Criar pedido (30s)

> "Agora vou criar um pedido. `2. Pedidos` → `Criar Pedido`. Envio dois itens — um burger e uma batata frita. Repare que **nao envio o `clienteId`**: ele eh extraido do JWT no servidor, conforme o requisito 5.2 — `O ID do cliente deve vir do token`.
>
> Resposta: pedido criado, status `CRIADO`, valor total calculado, `pedidoId` salvo em variavel."

### Passo 3 — Confirmar pedido (30s)

> "Confirmo o pedido. Isso publica o evento `pedido.criado` no Kafka."
>
> **(Alt-tab para Kafka UI)** "Olha aqui — topico `pedido.criado`, mensagem chegou."
>
> **(Alt-tab de volta)** "Em alguns segundos, o `pagamento-service` vai consumir, chamar o procpag, receber a aprovacao, e publicar `pagamento.aprovado`. O `restaurante-pedido` consome e atualiza o status."

### Passo 4 — Verificar status PAGO (15s)

> "Rodando `Pedido por ID`... status `PAGO`, `pagamentoId` preenchido. **Tudo automatico — eu so confirmei o pedido**."

### Passo 5 — Fluxo da cozinha (1 min)

> "Agora a parte nova — o `restaurante-service` (cozinha). Quando o pedido virou PAGO, o `restaurante-pedido` publicou outro evento: `pedido.pronto-para-cozinha`, com os itens (sem preco — a cozinha nao se importa com isso)."
>
> **(GraphiQL do restaurante-service em `localhost:8084/graphiql`, logado como DONO_RESTAURANTE)**
>
> > "Consulto a `filaCozinha` — la esta o pedido, status `RECEBIDO`."
>
> **Mutation:** `iniciarPreparo(pedidoCozinhaId)` → status `EM_PREPARO`
>
> > "O `restaurante-service` publicou `pedido.em-preparo`, e o `restaurante-pedido` consumiu e atualizou tambem. **Tudo via eventos.**"
>
> **Mutation:** `marcarComoPronto` → status `PRONTO`
>
> > "Pedido pronto. Final do fluxo. Quatro microsservicos coordenados, zero acoplamento direto entre eles."

---

## 5:30 – 7:00 · Demo de resiliencia (1:30 min)

**Falar:**

> "Agora a parte critica — o que acontece se o gateway de pagamento cair? Requisito 4.5 e 4.6."

### Passo 1 — Derrubar o procpag (15s)

```bash
docker stop procpag
```

> "Procpag fora do ar."

### Passo 2 — Criar e confirmar novo pedido (30s)

> "Postman: novo pedido, confirmacao. O `pagamento-service` recebe, tenta chamar o procpag, falha. **Resilience4j entra em acao:**
> - **Retry** — 3 tentativas com backoff exponencial
> - **Timeout** — 5s por chamada
> - **Circuit Breaker** — depois de 5 falhas, abre o circuito (fail-fast)
> - **Fallback** — publica `pagamento.pendente`"

### Passo 3 — Ver status PENDENTE (15s)

> "Pedido por ID... `PENDENTE_PAGAMENTO`. **O cliente nao recebeu erro**; o sistema absorveu a falha."

### Passo 4 — Religar o procpag e ver reprocesso (30s)

```bash
docker start procpag
```

> "Procpag de volta. Tem um worker `@Scheduled` no `pagamento-service` que roda a cada 30 segundos, consulta os pendentes e tenta de novo."
>
> **(Aguarde ~30-40s, mostre os logs)**
>
> ```bash
> docker logs -f pagamento
> ```
>
> > "Olha aqui — `Reprocessando pedido pendente... aprovado!`. E agora o status..."
>
> **Postman:** `Pedido por ID` → `PAGO`. "Recuperou sozinho."

---

## 7:00 – 8:00 · Massa de testes + suite automatizada (1 min)

### Massa de testes (Postman Collection Runner) — 30s

> "Para mostrar que o sistema aguenta volume, tem uma pasta `4. Massa de Testes` na collection. Vou rodar 50 iteracoes no Collection Runner."
>
> **Tela:** abrir Collection Runner → selecionar pasta `4. Massa de Testes` → Iterations: 50 → Run.
>
> **Enquanto roda:** "Em paralelo, na Kafka UI, da pra ver as mensagens chegando."

### Suite automatizada — 30s

> "E ainda tem **259 testes unitarios** cobrindo todos os 4 modulos. Roda direto com `mvn test` ou usando o script agregador:"
>
> ```bash
> ./scripts/test-summary.sh
> ```
>
> **Mostre o output:** todos verdes, distribuidos entre os modulos.

---

## 8:00 – 9:00 · Tour rapido no codigo (1 min)

**IDE aberta no projeto.**

> "Tres coisas que valem destaque no codigo:"

### 1 — Hexagonal pura (20s)

> **(Abre `pagamento/domain/`)** "Pasta `domain`: zero imports de Spring, JPA ou Kafka. So Java puro."
>
> **(Abre `pagamento/application/port/output/`)** "As ports definem o que o use case precisa — `PaymentEventPublisher`, `PagamentoRepository`. **Sao interfaces**, implementadas em `adapter/outbound`."

### 2 — Resilience4j declarativo (20s)

> **(Abre `pagamento/adapter/outbound/http/ExternalPaymentClient`)** "A chamada externa eh anotada com `@CircuitBreaker`, `@Retry`, `@TimeLimiter`. **Toda a logica de resiliencia esta nas anotacoes** — a regra de negocio fica limpa."

### 3 — Performance no Docker (20s)

> **(Abre `docker-compose.yml`)** "Kafka roda em **KRaft mode** — sem Zookeeper. MySQL com `innodb_buffer_pool=512M`, `skip-name-resolve`."
>
> **(Abre `pagamento/Dockerfile`)** "Build em 4 stages: compila, extrai em **layered jars** (cache de Docker fica melhor), faz training de **AppCDS** — Class Data Sharing. **Boot da JVM ~30% mais rapido**."

---

## 9:00 – 10:00 · Encerramento (1 min)

> "Resumindo o que esta entregue:
>
> - **4 microsservicos** + procpag + infra, todos rodando com **um unico `docker compose up`**
> - **6 topicos Kafka** orquestrando o fluxo assincrono
> - **Resiliencia completa** na integracao com o gateway externo
> - **Arquitetura hexagonal** consistente nos 4 modulos
> - **JWT RS256** com perfis de acesso (`USUARIO`, `DONO_RESTAURANTE`)
> - **Modulo opcional** `restaurante-service` (5.1) implementado
> - **259 testes unitarios** verdes em todos os modulos
> - **Performance otimizada**: Kafka KRaft, AppCDS, layered jars, MySQL tuning
>
> Decisoes-chave que tomei:
>
> - **GraphQL no lugar de REST** — schema fortemente tipado, exatamente os campos que o cliente pede
> - **gRPC entre servicos** — contrato binario, mais rapido que HTTP+JSON pra chamadas internas
> - **Hexagonal em todos os modulos** — paga o preco de mais classes para ganhar testabilidade
> - **Kafka como cola** — desacopla bounded contexts e habilita a resiliencia
>
> Codigo, README e Postman collection no GitHub (link na descricao do video). Obrigado!"

---

## Apendice — Comandos rapidos a ter no clipboard

```bash
# Subir tudo
docker compose up -d --build

# Status
docker compose ps

# Logs do pagamento (mostrar retries)
docker logs -f pagamento

# Demo de resiliencia
docker stop procpag
docker start procpag

# Inspecionar banco
docker exec -it mysql mysql -uroot -proot -e "USE pedido_db; SELECT status, COUNT(*) FROM pedidos GROUP BY status;"

# Suite de testes
./scripts/test-summary.sh

# Endpoints uteis
# GraphiQL usuario-autenticacao:  http://localhost:8081/graphiql
# GraphiQL restaurante-pedido:    http://localhost:8082/graphiql
# GraphiQL pagamento:             http://localhost:8083/graphiql
# GraphiQL restaurante-service:   http://localhost:8084/graphiql
# Kafka UI:                       http://localhost:8085
# Actuator health:                http://localhost:8081/actuator/health (e :8082, :8083, :8084)
```

---

## Checklist final antes de gravar

- [ ] `docker compose ps` mostra 8 containers `(healthy)` ou `Up`
- [ ] Postman environment `fiap-fase-3-restaurante` ATIVO
- [ ] Login feito (token salvo)
- [ ] Kafka UI aberta em aba separada
- [ ] GraphiQL do `restaurante-service` aberto em outra aba (autenticado como `DONO_RESTAURANTE`)
- [ ] Segundo terminal aberto para `docker logs`
- [ ] Audio e video testados (~5 min de teste de gravacao)
- [ ] Conexao com a internet estavel (se for streamar pra YouTube/Drive)
