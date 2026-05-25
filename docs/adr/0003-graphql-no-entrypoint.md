# ADR 0003: GraphQL no ponto de entrada das aplicações

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

Cada microsserviço precisa expor uma API para o cliente externo
(Postman, eventualmente um frontend). REST é o padrão de mercado e
o caminho de menor resistência, mas vem com algumas dificuldades
conhecidas: versionamento por URL (`/v1`, `/v2`), over-fetching de
campos desnecessários, e múltiplos endpoints para cada entidade.

A spec **não exige** REST nem GraphQL — só "endpoints protegidos por
token JWT" (req. 5.2). Há liberdade de escolha.

## Decisão

Expor todos os 4 microsserviços via **GraphQL** (Spring GraphQL +
GraphiQL embutido), com schema próprio por serviço:

- `usuario-autenticacao` em `:8081/graphql`
- `restaurante-pedido` em `:8082/graphql`
- `pagamento` em `:8083/graphql`
- `restaurante-service` em `:8084/graphql`

Os schemas `.graphqls` ficam em `src/main/resources/graphql/`. Cada
controller resolver eh anotado com `@PreAuthorize` para validar perfis
do JWT (`USUARIO`, `DONO_RESTAURANTE`).

Para clientes que precisam de contrato HTTP formal (Swagger), o
endpoint `POST /graphql` também eh documentado via SpringDoc OpenAPI
em `/swagger-ui.html` de cada serviço.

## Consequências

### Positivas

- **Cliente decide os campos:** uma única query pode retornar `id` +
  `status` ou `id` + `cliente` + `itens` + `pagamento` sem mexer no
  servidor.
- **Schema autodocumentado:** GraphiQL embutido lista todas as
  queries e mutations disponíveis com tipagem.
- **Evolução sem versionamento:** novos campos podem ser adicionados
  sem quebrar consumidores antigos (que ignoram o que não pedem).
- **Tipagem forte:** o `schema.graphqls` é o contrato; clientes que
  pedem campos inexistentes recebem erro estruturado.

### Negativas

- **Cache HTTP padrão não funciona:** todas as queries vão em `POST`,
  então CDN/proxies não cacheiam por URL. Cache GraphQL exige Apollo
  Cache ou similar no cliente.
- **N+1 oculto:** resolvers ingênuos podem disparar 1 query SQL por
  campo de cada item em uma lista — exige atenção e DataLoaders.
- **Curva de aprendizado:** time precisa entender queries, mutations,
  resolvers, scalars custom (ex.: `BigDecimal` em pagamentos).

## Alternativas consideradas

- **REST puro:** mais simples e cacheável, mas com over-fetching e
  versionamento explícito que viraria dívida ao longo da evolução.
- **gRPC para tudo (inclusive externo):** rápido e tipado, mas exige
  cliente que entenda Protobuf — pesado para uso via Postman.
- **REST + GraphQL coexistindo:** dobra esforço de manter dois
  endpoints semanticamente equivalentes. Descartado.
