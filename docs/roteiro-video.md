# Roteiro — Video de apresentacao (Tech Challenge Fase 3)

> **Duracao alvo:** 10 minutos. **Ritmo:** ~150 palavras/min. Use o
> roteiro como guia (nao precisa ler literalmente).

## Estrutura auditavel

Cada bloco termina com **CHECKPOINT** listando os requisitos da
spec cobertos no bloco — ID conforme a
[auditoria de conformidade](auditoria-conformidade.md). O
avaliador consegue marcar os itens em tempo real enquanto assiste.

| Bloco | Tempo | Tema |
|---|---|---|
| 1 | 0:00–1:00 | Apresentacao + problema + **arquitetura escolhida e POR QUE** |
| 2 | 1:00–2:30 | Visao tecnica da arquitetura |
| 3 | 2:30–5:30 | Demo happy path completo (cadastro -> entrega) |
| 4 | 5:30–7:00 | Demo de resiliencia (gateway off -> reprocesso) |
| 5 | 7:00–8:00 | Massa de testes + suite automatizada |
| 6 | 8:00–9:00 | Tour rapido no codigo |
| 7 | 9:00–10:00 | Encerramento + recap dos requisitos |

## Antes de gravar

1. `docker compose down -v && docker compose up -d --build` — espere todos os containers ficarem `(healthy)` (~3 min).
2. Abra **lado a lado**: terminal, Postman (com environment `fiap-fase-3-restaurante` ATIVO), navegador com 3 abas: Kafka UI (`http://localhost:8085`), GraphiQL pedido (`http://localhost:8082/graphiql`), GraphiQL cozinha (`http://localhost:8084/graphiql`).
3. **IDE aberta** no projeto (VSCode ou IntelliJ).
4. **Faca um login no Postman** antes de comecar a gravar — popula `{{token}}` e voce nao queima 30s do video logando ao vivo.
5. Segundo terminal pronto para `docker logs -f`.
6. Tenha **a auditoria de conformidade aberta** ([docs/auditoria-conformidade.md](auditoria-conformidade.md)) para consultar se esquecer algum ID.

---

## Bloco 1 · 0:00–1:00 · Apresentacao + arquitetura escolhida (e por que)

**Setup de tela:** slide simples com o titulo do projeto e diagrama de blocos (pode ser screenshot do diagrama em [`diagramas/componentes.md`](diagramas/componentes.md) renderizado no GitHub).

**Falar (~150 palavras):**

> "Ola! Sou o Danilo Fernando, falando em nome do grupo formado por
> Danilo Fernando de Paula e Silva, Gilmar da Costa Moraes Junior,
> Juliana Maria Dal Olio Braz, Luis Henrique Silveira Borges e
> Thiago de Jesus Cordeiro. Este eh o **Tech Challenge da Fase 3**
> da PosTech FIAP.
>
> O desafio: construir um sistema de pedidos online para
> restaurante. O cliente cadastra-se, autentica, monta o pedido,
> confirma — e o sistema processa o pagamento contra um
> **gateway externo eventualmente indisponivel**. Logo, precisa de
> **resiliencia**. Depois que o pagamento eh aprovado, o pedido vai
> para a cozinha, que prepara e finaliza.
>
> Escolhemos **arquitetura de microsservicos** em vez de monolito
> porque os quatro contextos identificados — usuario, pedido,
> pagamento e cozinha — tem modelos de dados distintos, ritmos de
> mudanca diferentes e perfis de escala assimetricos. **Cada
> microsservico evolui isolado e tem seu proprio banco.** Usamos
> **Apache Kafka** como cola assincrona — eh o que torna a
> resiliencia possivel: quando o gateway cai, o pedido nao trava."

**CHECKPOINT — bloco 1:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.i | Vídeo apresenta a **arquitetura escolhida e por que** |
| [ ] | 5.1.a–d | Arquitetura em multiplos servicos (visao geral) |

---

## Bloco 2 · 1:00–2:30 · Visao tecnica da arquitetura

**Setup:** abra [`docs/diagramas/componentes.md`](diagramas/componentes.md) no GitHub (renderiza Mermaid) ou um screenshot de tela cheia.

**Falar:**

> "A arquitetura segue tres principios.
>
> **Primeiro: hexagonal em cada modulo.** Domain isolado de
> framework, application com use cases e ports, adapters para o
> mundo externo. Voces verao no codigo: zero imports de Spring,
> JPA ou Kafka dentro do pacote `domain`. Isso permite testar
> regra de negocio sem `@SpringBootTest` e trocar adapter sem
> tocar regra.
>
> **Segundo: bounded contexts separados.** Cada microsservico tem
> seu proprio MySQL database — `auth_db`, `pedido_db`,
> `pagamento_db` e `cozinha_db`. Nada compartilhado a nivel de
> dados, so contratos.
>
> **Terceiro: Kafka como cola assincrona.** Seis topicos: tres do
> fluxo principal — `pedido.criado`, `pagamento.aprovado`,
> `pagamento.pendente` — e tres para o fluxo da cozinha:
> `pedido.pronto-para-cozinha`, `pedido.em-preparo` e
> `pedido.pronto`.
>
> O sistema inteiro sobe com **um unico comando**:
> `docker compose up -d --build`. Oito containers, todos com
> healthcheck. Kafka roda em **modo KRaft** — sem Zookeeper, mais
> leve, mais rapido."

**Comando opcional (mostrar na tela):**
```bash
docker compose ps
```
Mostre os 8 containers `(healthy)` ou `Up`.

**CHECKPOINT — bloco 2:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.c | `compose.yml` que sobe tudo em um comando |
| [ ] | 3.d | Diagrama da arquitetura (componentes) |
| [ ] | 5.1.f | Diagrama de componentes/sequencia/C4 |
| [ ] | 5.3.a–c | 3 topicos Kafka exigidos |
| [ ] | 5.5.a–b | Hexagonal aplicado em todos os modulos |

---

## Bloco 3 · 2:30–5:30 · Demo happy path (cadastro -> entrega)

**Setup:** Postman + Kafka UI + GraphiQL `:8084` lado a lado.

### 3.1 · Login (15s)

> "Login como cliente comum. Postman, pasta `1. Autenticacao`,
> *Login como Usuario*. O JWT eh salvo em `{{token}}`."

**Mostrar:** request → response com token; mencionar que o JWT eh
**RS256** (assinatura assimetrica) — chave privada so vive no
`usuario-autenticacao`.

**CHECK rapido:**
- 4.1.a Criar cliente *(ja existia no env, mostrar a request salva)*
- 4.1.b Autenticar cliente
- 5.2.a Login gerando JWT

### 3.2 · Criar pedido (30s)

> "Crio o pedido. Mando dois itens — um burger e uma batata frita.
> Repare que **nao envio o `clienteId`**: ele eh extraido do JWT
> no servidor, conforme o requisito 5.2 — *'o ID do cliente deve
> vir do token'*."

**Mostrar:** response com `id`, `valorTotal` calculado, `status: CRIADO`.

### 3.3 · Confirmar pedido (30s)

> "Confirmo o pedido. Isso publica o evento `pedido.criado` no
> Kafka."

**Alt-tab para Kafka UI:**

> "Aqui — topico `pedido.criado`, mensagem chegou."

**Alt-tab de volta para Postman.**

> "Em alguns segundos, o `pagamento-service` vai consumir, chamar
> o procpag, receber a aprovacao, e publicar `pagamento.aprovado`.
> O `restaurante-pedido` consome e atualiza o status."

### 3.4 · Verificar status PAGO (15s)

> "Rodando *Pedido por ID*... status `PAGO`. **Tudo automatico —
> eu so confirmei o pedido.**"

### 3.5 · Fluxo da cozinha (1 min)

> "Agora a parte nova — o `restaurante-service`. Quando o pedido
> virou PAGO, o `restaurante-pedido` publicou outro evento:
> `pedido.pronto-para-cozinha`, com os itens (sem preco — a
> cozinha nao se importa com isso)."

**GraphiQL `localhost:8084`, logado como `DONO_RESTAURANTE`:**

> "Consulto a `filaCozinha` — la esta o pedido, status `RECEBIDO`."

**Mutation:** `iniciarPreparo(pedidoCozinhaId: "...") { status }` → `EM_PREPARO`

> "Status `EM_PREPARO`. O `restaurante-service` publicou
> `pedido.em-preparo`, e o `restaurante-pedido` consumiu e
> atualizou tambem."

**Mutation:** `marcarComoPronto(pedidoCozinhaId: "...") { status }` → `PRONTO`

> "`PRONTO`. Volto no Postman e consulto o pedido original... ele
> tambem esta `PRONTO`. **Quatro microsservicos coordenados por
> Kafka, sem nenhuma chamada sincrona entre eles.**"

**CHECKPOINT — bloco 3:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 4.1.a | Criar cliente |
| [ ] | 4.1.b | Autenticar cliente |
| [ ] | 4.2.a–e | Criar pedido (clienteId do JWT, restaurante, itens, total, confirmacao) |
| [ ] | 4.3.a | Consultar pedido por ID |
| [ ] | 4.4.a–c | Processamento via gateway externo procpag |
| [ ] | 4.7.a | Atualizacao automatica para PAGO |
| [ ] | 4.7.b | (Opcional) Fluxo para servicos de producao (cozinha) |
| [ ] | 5.1.d | (Opcional) `restaurante-service` implementado |
| [ ] | 5.2.b | Perfis (USUARIO, DONO_RESTAURANTE) |
| [ ] | 5.2.c | Endpoints protegidos com token |
| [ ] | 5.2.d | ID do cliente vem do token |
| [ ] | 5.3.a, e, g | Tópico `pedido.criado` publicado e consumido |
| [ ] | 5.3.b | Tópico `pagamento.aprovado` |
| [ ] | 5.3.h | Tópicos extras da cozinha |

---

## Bloco 4 · 5:30–7:00 · Demo de resiliencia

**Setup:** terminal + Postman + segundo terminal com `docker logs -f pagamento`.

### 4.1 · Derrubar o procpag (15s)

```bash
docker stop procpag
```

> "Procpag fora do ar. Vamos simular um cenario real onde o
> gateway externo cai."

### 4.2 · Criar e confirmar novo pedido (30s)

> "Postman: novo pedido, confirmacao. O `pagamento-service` recebe,
> tenta chamar o procpag, falha. **Resilience4j entra em acao:**
>
> - **Retry** — 3 tentativas com backoff exponencial
> - **Timeout** — 5s por chamada (implementado no HTTP client)
> - **Circuit Breaker** — depois de 5 falhas em 50%, abre o circuito
> - **Fallback** — publica `pagamento.pendente`, marca pedido como `PENDENTE_PAGAMENTO`"

**Mostrar o segundo terminal** com as retries acontecendo.

### 4.3 · Ver status PENDENTE_PAGAMENTO (15s)

> "Pedido por ID... `PENDENTE_PAGAMENTO`. **O cliente nao recebeu
> erro**; o sistema absorveu a falha."

### 4.4 · Religar procpag e ver reprocesso automatico (30s)

```bash
docker start procpag
```

> "Procpag de volta. Tem um worker `@Scheduled` no `pagamento`
> que roda a cada 30 segundos, busca os pendentes e tenta de novo."

**Aguarde ~30-40s.** Mostre os logs:
```bash
docker logs -f pagamento
```

> "Olha aqui — *'Reprocessando pedido pendente... aprovado!'*. E
> agora o status..."

**Postman:** `Pedido por ID` → `PAGO`.

> "**Recuperou sozinho. Sem ninguem clicar em nada.**"

**Bonus:** mostre o estado do Circuit Breaker:
```bash
curl http://localhost:8083/actuator/circuitbreakers
```

**CHECKPOINT — bloco 4:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 4.5.a | Pedido nao falha quando gateway esta fora |
| [ ] | 4.5.b | Marcado como PENDENTE_PAGAMENTO |
| [ ] | 4.5.c | Vai para fila `pagamento.pendente` |
| [ ] | 4.6.a | Reprocessamento automatico quando gateway volta |
| [ ] | 4.6.b | Atualizacao para PAGO apos aprovacao |
| [ ] | 5.3.c | Tópico `pagamento.pendente` |
| [ ] | 5.3.f | Fluxo de falha + worker + atualizacao |
| [ ] | 5.4.a | Circuit Breaker |
| [ ] | 5.4.b | Retry |
| [ ] | 5.4.c | Timeout (via HTTP client) |
| [ ] | 5.4.d–e | Fallback marca PENDENTE + publica `pagamento.pendente` |
| [ ] | 3.f | **Pontos de resiliencia identificados claramente** |

---

## Bloco 5 · 7:00–8:00 · Testes (Postman Runner + suite unitaria)

### 5.1 · Massa de testes via Postman Collection Runner (30s)

> "Para mostrar que o sistema aguenta volume, tem uma pasta
> `4. Massa de Testes` na collection. Vou rodar 50 iteracoes no
> Collection Runner."

**Tela:** abrir Collection Runner → pasta `4. Massa de Testes` → Iterations: 50 → Run.

**Enquanto roda:**
> "Na Kafka UI da pra ver as mensagens chegando em tempo real."

### 5.2 · Suite unitaria automatizada (30s)

> "Em paralelo, temos **285 testes unitarios** cobrindo os 4
> modulos. Roda sem Docker, MySQL ou Kafka externos — usa H2 e
> EmbeddedKafka."

**Mostrar:**
```bash
./scripts/test-summary.sh
```

> "Output: distribuidos como — 63 no auth, 126 no pedido,
> 57 no pagamento, 39 na cozinha. **Todos verdes.**"

**CHECKPOINT — bloco 5:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.a | Aplicacao funcionando adequadamente (volume) |
| [ ] | 3.b | Arquivo de testes dos endpoints (Postman) |

---

## Bloco 6 · 8:00–9:00 · Tour rapido no codigo

**Setup:** IDE aberta no projeto.

### 6.1 · Hexagonal pura (20s)

**Abrir** `pagamento/src/main/java/.../domain/`.

> "Pasta `domain`: zero imports de Spring, JPA ou Kafka. So Java
> puro."

**Abrir** `pagamento/.../application/port/output/`.

> "Ports — interfaces. Implementadas em `adapter/outbound`."

### 6.2 · Resilience4j declarativo (20s)

**Abrir** `pagamento/.../adapter/outbound/http/ExternalPaymentClient.java`.

> "Olha as anotacoes: `@CircuitBreaker`, `@Retry`. **Toda a logica
> de resiliencia esta nas anotacoes** — o codigo de negocio fica
> limpo."

### 6.3 · Documentacao versionada (20s)

**Abrir** `docs/` no GitHub:

> "Tudo versionado: PDF de documentacao tecnica seguindo ABNT,
> 13 ADRs em `docs/adr/` documentando cada decisao arquitetural,
> 4 diagramas Mermaid em `docs/diagramas/` que renderizam aqui no
> GitHub, e a auditoria de conformidade em
> `docs/auditoria-conformidade.md` — onde voce pode marcar cada
> requisito a medida que assistir este video."

**CHECKPOINT — bloco 6:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.e | Descricao do fluxo principal (em `docs/`) |
| [ ] | 3.g | Repositorio com codigo-fonte |
| [ ] | 5.5.a–b | Boas praticas (Hexagonal evidente em IDE) |

---

## Bloco 7 · 9:00–10:00 · Encerramento + recap

> "Resumindo o que esta entregue:
>
> - **4 microsservicos** + procpag, todos rodando com **um unico
>   `docker compose up`**
> - **6 topicos Kafka** orquestrando o fluxo assincrono
> - **Resiliencia completa** na integracao com o gateway externo:
>   Circuit Breaker, Retry, Timeout, Fallback, mais o worker de
>   reprocessamento
> - **Arquitetura hexagonal** consistente nos 4 modulos
> - **JWT RS256** com perfis de acesso e `clienteId` extraido do token
> - **Modulo opcional `restaurante-service`** implementado, expandindo
>   o fluxo ate a cozinha
> - **285 testes unitarios** verdes em todos os modulos
> - **Performance otimizada**: Kafka KRaft, AppCDS no boot da JVM,
>   layered jars no Dockerfile, resource limits no compose
> - **Documentacao formal**: PDF ABNT de 17 paginas, 13 ADRs, 4
>   diagramas Mermaid, auditoria de conformidade requisito por
>   requisito
>
> **Decisoes-chave** estao documentadas como ADRs:
>
> - **GraphQL no lugar de REST** — schema fortemente tipado, cliente
>   pede exatamente os campos que precisa
> - **gRPC entre servicos** — contrato binario, mais rapido que JSON
> - **Hexagonal em todos os modulos** — paga o preco de mais classes
>   para ganhar testabilidade total
> - **Kafka como cola** — desacopla bounded contexts e habilita a
>   resiliencia exigida pelo requisito 4.5/4.6
> - **`restaurante-service` em bounded context proprio** — cozinha tem
>   modelo diferente (sem preco, com estados de producao)
>
> Codigo, documentacao e Postman collection no GitHub. **Obrigado!**"

**CHECKPOINT — bloco 7 (recap final):**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.h | **Video de no maximo 10 minutos** (✅ se voce esta vendo isto) |
| [ ] | 3.i | Arquitetura escolhida e POR QUE (recap das decisoes) |

---

## Apendice A — Comandos prontos para o clipboard

```bash
# Subir tudo
docker compose up -d --build

# Status
docker compose ps

# Logs do pagamento (mostrar retries e CB durante demo de resiliencia)
docker logs -f pagamento

# Demo de resiliencia
docker stop procpag
docker start procpag

# Inspecionar banco
docker exec -it mysql mysql -uroot -proot -e \
  "USE pedido_db; SELECT status, COUNT(*) FROM pedidos GROUP BY status;"

# Inspecionar fila da cozinha
docker exec -it mysql mysql -uroot -proot -e \
  "USE cozinha_db; SELECT status, COUNT(*) FROM pedidos_cozinha GROUP BY status;"

# Suite de testes
./scripts/test-summary.sh

# Estado do Circuit Breaker (mostrar OPEN/HALF_OPEN/CLOSED)
curl http://localhost:8083/actuator/circuitbreakers

# Worker @Scheduled ativo
curl http://localhost:8083/actuator/scheduledtasks

# Endpoints
# GraphiQL usuario-autenticacao:  http://localhost:8081/graphiql
# GraphiQL restaurante-pedido:    http://localhost:8082/graphiql
# GraphiQL pagamento:             http://localhost:8083/graphiql
# GraphiQL restaurante-service:   http://localhost:8084/graphiql
# Kafka UI:                       http://localhost:8085
# Actuator health (todos):        http://localhost:808X/actuator/health
```

---

## Apendice B — Recap de cobertura (auditoria por bloco)

Os IDs abaixo somam exatamente os 45 itens da
[auditoria de conformidade](auditoria-conformidade.md). Cada um
**aparece exatamente uma vez** no checkpoint de algum bloco, exceto
quando faz parte da introducao geral.

| Item | Bloco | Onde validar |
|---|---|---|
| 3.a | 5 | Volume + 285 testes verdes |
| 3.b | 5 | Postman collection rodando |
| 3.c | 2 | `docker compose ps` |
| 3.d | 2 | Diagrama Mermaid de componentes |
| 3.e | 6 | `docs/` no GitHub |
| 3.f | 4 | Demo de resiliencia |
| 3.g | 6 | Repositorio aberto na IDE |
| 3.h | 7 | O proprio video |
| 3.i | 1 + 7 | Arquitetura explicada (abre e fecha) |
| 4.1.a-b | 3 | Login/cadastro no Postman |
| 4.2.a-e | 3 | Criar pedido |
| 4.3.a | 3 | Consultar por ID |
| 4.3.b | 3 | (implicito em *Meus Pedidos*) |
| 4.4.a-c | 3 | Chamada ao procpag |
| 4.5.a-c | 4 | Cenario gateway off |
| 4.6.a-b | 4 | Worker reprocessando |
| 4.7.a | 3 | Transicao automatica para PAGO |
| 4.7.b | 3 | Fluxo da cozinha |
| 5.1.a-d | 1 | Apresentacao dos 4 servicos |
| 5.1.e | (skip) | Opcional nao implementado |
| 5.1.f | 2 | Diagrama |
| 5.2.a | 3 | Login retorna JWT |
| 5.2.b-d | 3 | Perfis + protecao + clienteId do token |
| 5.3.a | 3 | Topico `pedido.criado` |
| 5.3.b | 3 | Topico `pagamento.aprovado` |
| 5.3.c | 4 | Topico `pagamento.pendente` |
| 5.3.d-g | 3 + 4 | Fluxo completo via Kafka |
| 5.3.h | 3 | Topicos extras da cozinha |
| 5.4.a-e | 4 | Demo de resiliencia |
| 5.5.a-b | 2 + 6 | Hexagonal mencionado e mostrado no codigo |

---

## Checklist final antes de gravar

- [ ] `docker compose ps` mostra 5 `(healthy)` + 3 `Up`
- [ ] Postman environment `fiap-fase-3-restaurante` ATIVO
- [ ] Login feito (token em `{{token}}`)
- [ ] Kafka UI aberto em aba separada
- [ ] GraphiQL `:8084` aberto em outra aba (token DONO_RESTAURANTE colado em Headers)
- [ ] Segundo terminal aberto para `docker logs -f pagamento`
- [ ] [auditoria-conformidade.md](auditoria-conformidade.md) aberto em uma aba (consulta rapida se esquecer ID)
- [ ] Audio + camera testados (~5 min de gravacao de teste)
- [ ] Cronometro do celular pronto (manter o ritmo de 1 min/bloco)
