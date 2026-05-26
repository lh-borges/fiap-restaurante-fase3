# Roteiro — Video de apresentacao (Tech Challenge Fase 3)

> **Duracao alvo:** 10 minutos. **Ritmo:** ~150 palavras/min.
>
> Este roteiro foi escrito com um objetivo duplo: (1) servir de
> guia durante a gravacao, e (2) **funcionar como material de
> estudo** para quem assistir depois — colega de turma, dev novo
> na equipe, ou voce mesmo daqui a 6 meses. Por isso, cada bloco
> tem **uma intro conceitual curta** antes da demo: explica
> *por que* aquele padrao existe, nao so o que ele faz.

---

## Como ler este roteiro

- **As falas** sao o que voce diria na frente da camera. Pode ler,
  pode improvisar — o importante eh a ideia.
- **Os blocos de demo** tem o comando exato e a tela esperada.
- **Os "💡 Conceito"** sao explicacoes curtas para quem nunca viu
  aquilo. Mantenha durante a gravacao se houver tempo; corte para
  ganhar segundos.
- **Os "✓ Checkpoint"** ao final de cada bloco listam os
  requisitos cobertos (IDs da
  [auditoria de conformidade](auditoria-conformidade.md)). Sao para
  o avaliador acompanhar; nao precisa ler em voz alta.

> **🎬 Como executar a demo no Postman**
>
> A collection
> [`fiap-fase-3-restaurante.postman_collection.json`](fiap-fase-3-restaurante.postman_collection.json)
> tem uma pasta especial **`5. Roteiro do Video`** com **10 requisicoes
> numeradas em sequencia** (`01 → 10`), batendo exatamente com a ordem
> dos blocos `3.1`, `3.2`, `3.3`, `3.4`, `3.5`, e `4.1`–`4.4` deste
> documento. Durante a gravacao, basta executar os requests **na
> ordem** (clique → Send → proximo). Os tokens e IDs sao salvos
> automaticamente em variaveis de collection (`{{token}}`,
> `{{pedidoId}}`), entao nao precisa copiar nada.
>
> As demais pastas (`1. Autenticacao`, `2. Pedidos`, `3. Pagamento`,
> `4. Massa de Testes`) permanecem como referencia de todas as
> operacoes individuais — uteis fora do roteiro.

## Estrutura

| Bloco | Tempo | Tema | Conceitos centrais |
|---|---|---|---|
| 1 | 0:00–1:00 | Apresentacao + **POR QUE microsservicos** | Bounded context, motivacao |
| 2 | 1:00–2:30 | Visao tecnica via C4 | Modelo C4, arquitetura hexagonal, Kafka |
| 3 | 2:30–5:30 | Demo do fluxo completo | JWT, GraphQL, eventos assincronos |
| 4 | 5:30–7:00 | Demo de resiliencia | Circuit Breaker, Retry, Fallback, idempotencia |
| 5 | 7:00–8:00 | Testes (massa + suite) | Piramide de testes, H2/EmbeddedKafka |
| 6 | 8:00–9:00 | Tour no codigo | Hexagonal puro, anotacoes Resilience4j, AppCDS |
| 7 | 9:00–10:00 | Encerramento + recap | Decisoes-chave + ADRs |

---

## Antes de gravar (checklist de setup)

1. `docker compose down -v && docker compose up -d --build` — espere
   todos os containers ficarem `(healthy)` (~3 min).
2. **Janelas lado a lado:**
   - Terminal 1 (comandos)
   - Terminal 2 com `docker logs -f pagamento-service` rodando
   - Postman com environment `fiap-fase-3-restaurante` **ATIVO**
   - Navegador com 3 abas: Kafka UI `:8085`, GraphiQL pedido
     `:8082/graphiql`, GraphiQL cozinha `:8084/graphiql`
3. **Faca um login no Postman antes de comecar a gravar** — popula
   `{{token}}` e poupa 30 segundos.
4. IDE aberta no projeto (VSCode ou IntelliJ).
5. Audio + camera testados; cronometro do celular pronto.
6. [auditoria-conformidade.md](auditoria-conformidade.md) aberta
   numa aba — consulta rapida se esquecer ID de requisito.

---

## Bloco 1 · 0:00–1:00 · Apresentacao + por que microsservicos

**Tela:** slide simples com titulo + screenshot do
[`docs/diagramas/c4-contexto.png`](diagramas/c4-contexto.png).

### Falar

> "Ola! Sou o Danilo Fernando, falando em nome do grupo formado por
> mim, Gilmar Junior, Juliana Braz, Luis Henrique Borges e Thiago
> Cordeiro. Este eh o Tech Challenge da Fase 3 da PosTech FIAP.
>
> **O desafio em uma frase:** construir um sistema de pedidos
> online para restaurante, que precisa funcionar **mesmo quando
> o gateway de pagamento esta fora do ar**.
>
> O quebra-cabecas tem quatro pecas naturais: cadastro de cliente,
> ciclo do pedido, processamento de pagamento, e producao pela
> cozinha. Cada uma com regras diferentes, ritmos de mudanca
> diferentes, e perfis de carga diferentes. Em vez de empacotar
> tudo num monolito, escolhemos **quatro microsservicos
> independentes** — um para cada bounded context. Isso permite
> evoluir e escalar cada um isoladamente."

### 💡 Conceito — Bounded Context

> "Bounded context eh um termo do Domain-Driven Design. Significa
> 'fronteira de significado'. A palavra `Pedido`, por exemplo,
> tem um significado para quem cobra (precisa de `valor`,
> `pagamentoId`) e outro para quem cozinha (precisa de
> `produto`, `quantidade`, mas nao se importa com preco).
> Modelar isso como dois agregados separados em dois
> microsservicos diferentes evita um modelo 'inchado' que tenta
> agradar todo mundo."

**✓ Checkpoint — bloco 1:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.i | Vídeo apresenta a **arquitetura escolhida e por que** |
| [ ] | 5.1.a–d | Arquitetura em multiplos servicos (visao geral) |

---

## Bloco 2 · 1:00–2:30 · Arquitetura via C4

**Tela:** [`docs/diagramas/c4-containers.png`](diagramas/c4-containers.png) em tela cheia.

### Falar

> "Este eh o **diagrama C4 nivel 2**, que mostra a topologia
> tecnica. Quatro microsservicos Spring Boot 4 com Java 25, mais
> a infra compartilhada — MySQL e Kafka — e o gateway externo
> procpag.
>
> Vamos do mais simples para o mais sutil.
>
> **Tres protocolos de rede em uso, cada um com proposito claro:**
>
> Primeiro, **GraphQL com JWT**, em verde por cima. Eh por aqui que
> o cliente fala com o sistema. Escolhemos GraphQL em vez de REST
> porque o cliente decide quais campos quer em cada query — uma
> mesma query pode trazer so o status do pedido ou tudo: itens,
> preco, pagamento. Sem versionamento de endpoint.
>
> Segundo, **gRPC** entre o `restaurante-pedido` e o
> `usuario-autenticacao`. Eh uma chamada interna, sincrona,
> com contrato fechado. Protobuf eh binario — tres a dez vezes
> menor que JSON — e o stub gerado da type-safety em tempo de
> compilacao. Errei o contrato? Quebra no `mvn package`, nao em
> producao.
>
> Terceiro, e **a decisao mais importante do projeto: Kafka**,
> para tudo que eh assincrono. Seis topicos."

### 💡 Conceito — por que Kafka muda tudo

> "Se o `restaurante-pedido` ligasse direto pro `pagamento-service`
> via REST, esperando a resposta sincrona, o que aconteceria
> quando o gateway externo caisse? O request do cliente ficaria
> pendurado, daria timeout, e o pedido falharia.
>
> Com Kafka no meio, o `restaurante-pedido` **publica o evento e
> segue a vida**. O cliente recebe `CONFIRMADO` em milissegundos.
> O pagamento processa quando puder. Se o gateway esta fora?
> O `pagamento-service` reprocessa depois, sozinho. Isso eh o
> que possibilita a resiliencia que vamos demonstrar no bloco 4."

### 💡 Conceito — arquitetura hexagonal

> "Cada um dos quatro microsservicos por dentro segue **arquitetura
> hexagonal** (tambem chamada Ports and Adapters). O dominio
> — as regras de negocio puras — fica isolado de framework. Zero
> imports de Spring, JPA ou Kafka dentro do pacote `domain`.
> Isso significa que **testar regra de negocio dispensa
> `@SpringBootTest`** — eh JUnit puro com Mockito. E trocar Kafka
> por RabbitMQ amanha eh mudar um adapter, nao a regra."

### Demonstracao rapida

```bash
docker compose ps
```

> "Oito containers, todos `(healthy)`. Subiu tudo com um unico
> comando: `docker compose up -d --build`."

**✓ Checkpoint — bloco 2:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.c | `compose.yml` que sobe tudo em um comando |
| [ ] | 3.d | Diagrama da arquitetura (C4 + componentes) |
| [ ] | 5.1.f | Diagrama de componentes/sequencia/C4 |
| [ ] | 5.3.a–c | 3 topicos Kafka exigidos |
| [ ] | 5.5.a–b | Hexagonal aplicado em todos os modulos |

---

## Bloco 3 · 2:30–5:30 · Demo do fluxo completo

**Tela:** Postman + Kafka UI + GraphiQL `:8084` lado a lado.

### 3.1 · Login e JWT (20s)

```
Postman -> 5. Roteiro do Video -> 01 - Login como Usuario (bloco 3.1)
```

> "Faco login como cliente. Recebo um **JWT assinado em RS256**."

### 💡 Conceito — por que RS256 e nao HS256

> "RS256 eh assinatura **assimetrica**. Quem assina (o
> `usuario-autenticacao`) tem a chave privada. Quem valida (os
> outros tres servicos) tem so a chave publica. Resultado:
> mesmo que o `restaurante-pedido` seja comprometido, o atacante
> nao consegue **emitir** tokens novos — so verificar. Com HS256
> (chave compartilhada), todos poderiam emitir. RS256 limita o
> blast radius."

### 3.2 · Criar pedido (30s)

```
Postman -> 5. Roteiro do Video -> 02 - Criar Pedido (bloco 3.2)
```

> "Crio o pedido com dois itens. **Repare que nao envio o
> `clienteId`** — ele eh extraido do JWT no servidor. Eh o
> requisito 5.2: 'o ID do cliente deve vir do token'. Isso eh
> seguranca elementar: se o cliente pudesse mandar qualquer
> clienteId, eu pediria pedidos no nome dos outros."

Mostre a resposta: `id`, `valorTotal` calculado, `status: CRIADO`.

### 3.3 · Confirmar pedido (40s)

```
Postman -> 5. Roteiro do Video -> 03 - Confirmar Pedido (bloco 3.3)
```

> "Confirmo. Isso publica o evento `pedido.criado` no Kafka."

**Alt-tab para Kafka UI:**

> "Aqui — topico `pedido.criado`, a mensagem chegou. O
> `pagamento-service` ja esta consumindo. Ele vai chamar o
> procpag, receber a aprovacao, e publicar `pagamento.aprovado`.
> O `restaurante-pedido` consome esse evento e atualiza o status
> do pedido para `PAGO`."

### 3.4 · Ver status PAGO (15s)

```
Postman -> 5. Roteiro do Video -> 04 - Pedido por ID (espera PAGO - bloco 3.4)
```

> "Status `PAGO`. **Sem que eu tenha clicado em mais nada.** O
> evento Kafka fez toda a coordenacao automatica."

### 3.5 · Fluxo da cozinha (1 min 15s)

> "Agora a parte que vai alem do minimo da spec — o
> `restaurante-service`, modulo opcional do item 5.1. Quando o
> pedido virou PAGO, o `restaurante-pedido` publicou outro evento:
> `pedido.pronto-para-cozinha`, com os itens (sem preco — a
> cozinha nao se importa com isso, eh outro bounded context)."

### 💡 Conceito — o que e GraphiQL

> "Antes de mostrar o codigo, deixa eu explicar a ferramenta que
> vou usar aqui. **GraphiQL** (com 'i' minusculo no meio, le-se
> 'graphical') eh um console interativo para GraphQL que vem
> embutido em cada um dos nossos servicos. Pense nele como um
> Postman embutido — mas com autocomplete do schema e documentacao
> sempre atualizada, porque le direto do servidor.
>
> Para usar:
>
> 1. Abro no navegador: `http://localhost:8084/graphiql`.
> 2. Como esta endpoint exige JWT do perfil DONO_RESTAURANTE,
>    preciso colar o token. Em GraphiQL, isso vai no painel
>    inferior esquerdo, 'Request Headers':
>
>    ```json
>    { \"Authorization\": \"Bearer eyJraWQi...\" }
>    ```
>
> 3. No painel superior esquerdo, escrevo a query ou mutation.
>    Autocomplete (Ctrl+Space) ajuda — ele conhece todos os tipos.
> 4. Clico no botao 'play' (▶) ou aperto Ctrl+Enter.
> 5. A resposta sai no painel da direita, em JSON.
>
> Pra ver a documentacao completa do schema (todas as queries,
> mutations e tipos), eh so clicar em 'Docs' no canto superior
> direito."

**No Postman, faca o login do DONO para pegar o token:**

```
Postman -> 5. Roteiro do Video -> 05 - Login como Dono (token p/ GraphiQL :8084 - bloco 3.5)
```

> "Copio o token desta response (campo `data.login.token`) e
> colo no Request Headers do GraphiQL `:8084`."

**Agora na pratica, no GraphiQL `:8084`, com token de `dono@fiap.com` ja colado em Request Headers:**

```graphql
query { filaCozinha { id pedidoId status } }
```

> "Aqui esta o pedido, status `RECEBIDO`. Sou o dono do
> restaurante; vou iniciar o preparo."

```graphql
mutation { iniciarPreparo(pedidoCozinhaId: "<id>") { status } }
```

> "Status `EM_PREPARO`. Isso publicou `pedido.em-preparo` no
> Kafka, e o `restaurante-pedido` consumiu e refletiu o status
> no agregado principal."

```graphql
mutation { marcarComoPronto(pedidoCozinhaId: "<id>") { status } }
```

> "`PRONTO`. Volto no Postman e consulto o pedido original... ele
> tambem esta `PRONTO`. **Quatro microsservicos coordenados, com
> zero chamada sincrona entre eles.** Pura mensageria assincrona."

**✓ Checkpoint — bloco 3:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 4.1.a–b | Criar e autenticar cliente |
| [ ] | 4.2.a–e | Criar pedido (clienteId do JWT, restaurante, itens, total, confirmacao) |
| [ ] | 4.3.a | Consultar pedido por ID |
| [ ] | 4.4.a–c | Processamento via gateway externo |
| [ ] | 4.7.a | Atualizacao automatica para PAGO |
| [ ] | 4.7.b | (Opcional) Fluxo para servicos de producao (cozinha) |
| [ ] | 5.1.d | (Opcional) `restaurante-service` implementado |
| [ ] | 5.2.a–d | JWT, perfis, endpoints protegidos, clienteId do token |
| [ ] | 5.3.a, b, e, g, h | Tópicos Kafka principais e do fluxo cozinha |

---

## Bloco 4 · 5:30–7:00 · Demo de resiliencia

**Tela:** terminal + Postman + segundo terminal com
`docker logs -f pagamento-service`.

### 💡 Conceito — por que resiliencia importa

> "Toda integracao com sistema externo eh um ponto de risco. O
> procpag aqui simula um gateway de pagamento real: ora aprova,
> ora cai. Se a gente nao tratar isso, qualquer instabilidade
> deles vira instabilidade nossa. **Cliente ve erro, vendas
> param, suporte explode.**
>
> A boa noticia: existem **padroes consagrados** para isolar essa
> falha — Circuit Breaker, Retry, Timeout, Fallback. Vamos ver
> os quatro funcionando."

### 4.1 · Derrubar o procpag (15s)

```bash
docker stop procpag
```

> "Procpag fora. Vamos simular o gateway externo caindo."

### 4.2 · Criar e confirmar novo pedido (30s)

**Importante:** antes destes dois, execute `06 - Re-login como Usuario`
para garantir que o `{{token}}` esta com perfil USUARIO (o passo 05
trocou o token para DONO).

```
Postman -> 5. Roteiro do Video -> 06 - Re-login como Usuario (preparar bloco 4)
Postman -> 5. Roteiro do Video -> 07 - Criar Pedido (gateway off - bloco 4.2)
Postman -> 5. Roteiro do Video -> 08 - Confirmar Pedido (gateway off - bloco 4.2)
```

> "Postman: novo pedido, confirmacao. O `pagamento-service`
> recebe `pedido.criado`, tenta chamar o procpag, e ai entra em
> acao o Resilience4j."

**Aponte para o segundo terminal mostrando logs:**

> "Olhem aqui — **Retry: 3 tentativas com backoff exponencial**
> (2s, depois 4s, depois 8s). Depois de varias falhas
> consecutivas, o **Circuit Breaker abre**. Ele eh basicamente
> um disjuntor eletrico: detectou problema, corta a corrente,
> nao tenta mais. Novas chamadas falham instantaneamente sem
> nem tocar no procpag. Isso protege tudo — nao adianta seguir
> martelando um servico que esta fora.
>
> Quando todas as estrategias falham, entra o **Fallback**:
> marca o pagamento como `PENDENTE` e publica `pagamento.pendente`
> no Kafka. O `restaurante-pedido` consome e marca o pedido como
> `PENDENTE_PAGAMENTO`."

### 4.3 · Ver status PENDENTE_PAGAMENTO (10s)

```
Postman -> 5. Roteiro do Video -> 09 - Pedido por ID (espera PENDENTE_PAGAMENTO - bloco 4.3)
```

> "`PENDENTE_PAGAMENTO`. **O cliente nao recebeu erro.** O sistema
> absorveu a falha — eh o requisito 4.5: 'o pedido nao deve
> falhar'."

### 4.4 · Religar procpag e ver reprocesso (35s)

```bash
docker start procpag
```

> "Procpag de volta."

### 💡 Conceito — o worker de reprocessamento

> "Como o sistema sabe que o gateway voltou? Tem um worker
> `@Scheduled` no `pagamento-service`, rodando a cada 30
> segundos, varrendo a tabela de pagamentos pendentes e
> retentando. Eh dele que vem o requisito 4.6: 'reprocessamento
> automatico'. Sem ninguem clicar em nada."

**Aguarde ~30s. Mostre os logs:**

> "*Reprocessando pedido pendente... aprovado!*"

```
Postman -> 5. Roteiro do Video -> 10 - Pedido por ID (espera PAGO apos reprocesso - bloco 4.4)
```

> "`PAGO`. **Recuperou sozinho.**"

### Bonus: estado do Circuit Breaker

```bash
curl http://localhost:8083/actuator/circuitbreakers
```

> "O Resilience4j expoe o estado pelo Actuator. Da pra monitorar
> em producao — alarme dispara quando o CB abre."

**✓ Checkpoint — bloco 4:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 4.5.a–c | Pedido nao falha + PENDENTE_PAGAMENTO + fila |
| [ ] | 4.6.a–b | Reprocessamento + atualizacao para PAGO |
| [ ] | 5.3.c, f | Tópico `pagamento.pendente` + fluxo de falha |
| [ ] | 5.4.a–e | Circuit Breaker + Retry + Timeout + Fallback |
| [ ] | 3.f | **Pontos de resiliencia identificados claramente** |

---

## Bloco 5 · 7:00–8:00 · Testes

### 💡 Conceito — piramide de testes

> "A piramide de testes diz: muitos unitarios na base (rapidos,
> isolados), alguns de integracao no meio, poucos end-to-end no
> topo (lentos, fragils). Nesse projeto, **285 testes
> unitarios** mais alguns smoke tests de contexto Spring, e a
> 'massa de testes' no Postman como pseudo-E2E."

### 5.1 · Massa de testes via Postman Runner (30s)

> "Vou rodar 50 iteracoes na pasta `4. Massa de Testes` do
> Collection Runner."

**Tela:** Collection Runner → pasta `4. Massa de Testes` → 50 iteracoes → Run.

**Em paralelo, na Kafka UI:**

> "Da pra ver as mensagens chegando em tempo real."

### 5.2 · Suite unitaria automatizada (30s)

```bash
./scripts/test-summary.sh
```

> "285 testes verdes:
> - 63 no `usuario-autenticacao`
> - 126 no `restaurante-pedido`
> - 57 no `pagamento-service`
> - 39 no `restaurante-service`
>
> **Zero dependencia externa.** O ambiente de teste usa H2
> em memoria no lugar do MySQL e `@EmbeddedKafka` no lugar do
> broker real. Roda em qualquer notebook, em 3 minutos."

**✓ Checkpoint — bloco 5:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.a | Aplicacao funcionando adequadamente (volume) |
| [ ] | 3.b | Arquivo de testes dos endpoints (Postman) |

---

## Bloco 6 · 8:00–9:00 · Tour no codigo

**Tela:** IDE aberta no projeto.

### 6.1 · Hexagonal pura (20s)

**Abrir** `pagamento-service/src/main/java/.../domain/`.

> "Pasta `domain`. **Zero imports de Spring, JPA, Kafka.** So
> Java puro, regras de negocio puras."

**Abrir** `pagamento-service/.../application/port/output/`.

> "Aqui estao as **ports** — interfaces que descrevem **o que** o
> use case precisa, sem dizer **como**. `PaymentGateway`,
> `PagamentoRepository`. As implementacoes vivem em
> `adapter/outbound`, dependem dessas interfaces. Isso eh o
> coracao do **Dependency Inversion Principle** — o D do SOLID."

### 6.2 · Resilience4j declarativo (20s)

**Abrir** `pagamento-service/.../adapter/outbound/http/ExternalPaymentClient.java`.

> "Olhem as anotacoes:
>
> ```java
> @Retry(name = CB_NAME)
> @CircuitBreaker(name = CB_NAME)
> public boolean processar(UUID pedidoId, BigDecimal valor) {
>     // ... codigo de negocio limpo
> }
> ```
>
> Toda a logica de resiliencia esta nas anotacoes. O metodo
> recebe a chamada como se fosse normal. Por baixo, AOP do
> Resilience4j intercepta e aplica as estrategias. **Codigo
> de negocio fica limpo, regra de resiliencia fica explicita.**"

### 6.3 · Documentacao versionada (20s)

**Abrir** a pasta `docs/` no GitHub:

> "Tudo aqui versionado junto com o codigo:
>
> - PDF de documentacao tecnica seguindo ABNT (Times 12,
>   margens 3-2-3-2, etc.)
> - **13 ADRs** em `docs/adr/` documentando cada decisao
>   arquitetural — quem ler o sistema daqui a um ano sabe **por
>   que** as coisas estao do jeito que estao
> - **6 diagramas Mermaid + PNG** em `docs/diagramas/` —
>   incluindo dois diagramas C4 oficiais
> - **Auditoria de conformidade** em
>   `docs/auditoria-conformidade.md` — onde voces, avaliadores,
>   podem ir marcando cada requisito enquanto assistem"

**✓ Checkpoint — bloco 6:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.e | Descricao do fluxo principal (em `docs/`) |
| [ ] | 3.g | Repositorio com codigo-fonte |
| [ ] | 5.5.a–b | Boas praticas (Hexagonal evidente em IDE) |

---

## Bloco 7 · 9:00–10:00 · Encerramento

> "Pra fechar: o que esta entregue.
>
> - **4 microsservicos** + procpag, todos rodando com **um unico
>   `docker compose up`**
> - **6 topicos Kafka** orquestrando o fluxo assincrono
> - **Resiliencia completa**: Circuit Breaker, Retry, Timeout,
>   Fallback, mais o worker de reprocessamento
> - **Arquitetura hexagonal** consistente nos 4 modulos
> - **JWT RS256** com perfis de acesso e `clienteId` extraido
>   do token
> - **Modulo opcional `restaurante-service`** implementado,
>   expandindo o fluxo ate a entrega
> - **285 testes unitarios** verdes
> - **Documentacao formal**: PDF ABNT, **2 diagramas C4** (de
>   Contexto e de Containers), 13 ADRs com Contexto/Decisao/
>   Consequencias, auditoria de conformidade requisito por
>   requisito
>
> **Cinco decisoes-chave** estao documentadas como ADRs e merecem
> destaque:
>
> 1. **Microsservicos em vez de monolito** — porque os 4
>    contextos sao realmente diferentes
> 2. **Hexagonal em todos os modulos** — paga preco em verbosidade,
>    ganha em testabilidade e isolamento de framework
> 3. **Kafka como cola assincrona** — o que torna possivel a
>    resiliencia exigida pelos requisitos 4.5 e 4.6
> 4. **Database per service** — cada microsservico com seu MySQL
>    isolado, sem joins entre bancos
> 5. **Resilience4j declarativo** — anotacoes em vez de
>    try/catch/sleep espalhados no codigo
>
> Codigo, documentacao e Postman collection estao no GitHub.
> Obrigado!"

**✓ Checkpoint — bloco 7:**

| ✓ | ID | Requisito |
|---|---|---|
| [ ] | 3.h | **Video de no maximo 10 minutos** |
| [ ] | 3.i | Arquitetura escolhida e POR QUE (recap das decisoes) |

---

## Apendice A — Comandos prontos para o clipboard

```bash
# Subir tudo
docker compose up -d --build

# Status
docker compose ps

# Logs do pagamento-service (mostra retries + CB durante demo de resiliencia)
docker logs -f pagamento-service

# Demo de resiliencia
docker stop procpag
docker start procpag

# Inspecionar bancos
docker exec -it mysql mysql -uroot -proot -e \
  "USE pedido_db; SELECT status, COUNT(*) FROM pedidos GROUP BY status;"

docker exec -it mysql mysql -uroot -proot -e \
  "USE cozinha_db; SELECT status, COUNT(*) FROM pedidos_cozinha GROUP BY status;"

# Suite de testes
./scripts/test-summary.sh

# Resilience4j em runtime
curl http://localhost:8083/actuator/circuitbreakers
curl http://localhost:8083/actuator/scheduledtasks

# Endpoints publicos
# GraphiQL usuario-autenticacao:  http://localhost:8081/graphiql
# GraphiQL restaurante-pedido:    http://localhost:8082/graphiql
# GraphiQL pagamento-service:     http://localhost:8083/graphiql
# GraphiQL restaurante-service:   http://localhost:8084/graphiql
# Swagger restaurante-pedido:     http://localhost:8082/swagger-ui.html
# Kafka UI:                       http://localhost:8085
```

---

## Apendice B — Glossario express

Termos que aparecem no video, explicados em uma linha:

| Termo | Em uma frase |
|---|---|
| **Bounded context** | Fronteira de significado de um modelo de dominio (DDD). |
| **Hexagonal architecture** | Separar regra de negocio (domain) do mundo externo (adapters) via interfaces (ports). |
| **Modelo C4** | Documentar arquitetura em ate 4 niveis: Contexto, Containers, Componentes, Code (Simon Brown). |
| **JWT RS256** | Token assinado com chave RSA assimetrica — chave privada para emitir, publica para verificar. |
| **Kafka topic** | Caixa postal nomeada onde mensagens sao publicadas e consumidas em ordem por particao. |
| **Circuit Breaker** | "Disjuntor eletrico" do software — abre o circuito quando detecta falhas repetidas, fail-fast. |
| **Retry com backoff exponencial** | Tentar de novo, esperando cada vez mais (2s, 4s, 8s...) para nao sobrecarregar o servico em recuperacao. |
| **Fallback** | Resposta alternativa quando a chamada principal falha (no nosso caso: marcar PENDENTE + publicar evento). |
| **Idempotencia** | Operacao que pode ser repetida sem efeito colateral (chave para tolerar at-least-once delivery do Kafka). |
| **At-least-once delivery** | Garantia de que cada mensagem chega ao menos uma vez (pode chegar mais de uma — daí a idempotencia). |
| **AppCDS** | Application Class Data Sharing — pre-carrega classes da JVM em um archive binario para acelerar boot. |
| **Worker `@Scheduled`** | Componente Spring que executa um metodo automaticamente em intervalo fixo. |
| **Spring Actuator** | Endpoints HTTP de gerenciamento (`/actuator/health`, `/actuator/circuitbreakers` etc.). |

---

## Apendice C — Cobertura completa dos 45 itens da auditoria

| Item | Bloco | Onde validar |
|---|---|---|
| 3.a | 5 | Volume + 285 testes verdes |
| 3.b | 5 | Postman collection rodando |
| 3.c | 2 | `docker compose ps` |
| 3.d | 2 | C4 + diagrama de componentes |
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
| 5.1.f | 2 | Diagramas C4 |
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
- [ ] **GraphiQL `:8084` aberto em outra aba**, com:
  - Login feito como `dono@fiap.com` / `dono123` no `:8081/graphiql` antes, para copiar o token
  - O token colado no painel inferior esquerdo (**Request Headers**) no formato:
    ```json
    { "Authorization": "Bearer <token>" }
    ```
  - Painel "Docs" abre se você esquecer alguma operação (canto superior direito)
- [ ] Segundo terminal aberto para `docker logs -f pagamento-service`
- [ ] [auditoria-conformidade.md](auditoria-conformidade.md) aberta em uma aba (consulta rapida de IDs)
- [ ] Audio + camera testados (~5 min de gravacao de teste)
- [ ] Cronometro do celular pronto (manter o ritmo de 1 min/bloco)
