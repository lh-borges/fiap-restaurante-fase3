# ADR 0010: Bounded context separado para a cozinha (`restaurante-service`)

- **Status:** Accepted
- **Data:** 2026-05-25
- **Deciders:** Danilo Fernando

## Contexto e problema

O requisito 5.1 da spec marca o `restaurante-service` (que recebe
"aviso de que o pedido foi confirmado e começa a preparar o pedido")
como **opcional**. A discussão era se valia a pena implementá-lo, e
**onde modelar a fila da cozinha**:

1. **Não fazer:** ficar nos 3 serviços obrigatórios.
2. **Adicionar uma tabela `cozinha` no `pedido_db`** dentro do
   `restaurante-pedido`, com queries `iniciarPreparo()`,
   `marcarComoPronto()` etc.
3. **Criar um 4º microsserviço** com banco próprio e modelo próprio.

Os modelos de domínio do "pedido" e da "cozinha" são diferentes:

- **Pedido:** se importa com cliente, preço, valor total, pagamento.
- **Cozinha:** se importa com produto, quantidade, tempo de preparo.
  Preço é irrelevante — o cozinheiro não precifica nada.

## Decisão

Implementar `restaurante-service` como **4º microsserviço completo**,
com:

- Próprio módulo Maven (`restaurante-service/`)
- Próprio container Docker (`:8084`)
- Próprio database (`cozinha_db`) no mesmo MySQL
- Agregado próprio (`PedidoCozinha` com estados
  `RECEBIDO → EM_PREPARO → PRONTO`)
- Schema GraphQL próprio em `:8084/graphql`
- Consumo de `pedido.pronto-para-cozinha` (publicado pelo
  `restaurante-pedido` apenas após o pedido virar `PAGO`)
- Publicação de `pedido.em-preparo` e `pedido.pronto`, consumidos de
  volta pelo `restaurante-pedido` para refletir status no agregado
  principal

Mutations restritas ao perfil `DONO_RESTAURANTE` (apenas o
restaurante manipula sua própria fila).

## Consequências

### Positivas

- **Modelo de cozinha fica simples:** `PedidoCozinha` não tem
  cliente, preço, valor total — apenas o que importa para a
  produção. Itens guardam só `produtoId`, `nome`, `quantidade`.
- **Demonstra event-driven em ação:** ciclo completo do pedido
  passa por 4 microsserviços conversando exclusivamente por
  eventos Kafka.
- **Modelo escalável:** se a cozinha ganhar interface dedicada
  (display físico no balcão), está pronto para servir como
  backend dela.

### Negativas

- **+1 container, +1 deploy, +1 banco para manter.**
- **+~25-30 testes** novos para escrever (cobertura completa do
  módulo).
- **Atraso na entrega da Fase 3:** o tempo investido no
  `restaurante-service` é tempo não investido no roteiro do vídeo
  (item bloqueante). Trade-off consciente.

## Alternativas consideradas

- **Não implementar (opcional na spec):** caminho mais rápido,
  cumpre o requisito mínimo. Descartado pelo valor pedagógico do
  bounded context adicional.
- **Tabela `cozinha_pedido` dentro do `restaurante-pedido`:** evita
  +1 container mas mistura modelos (`Pedido` cresce com campos
  irrelevantes para venda) e contaminaria o domínio de pedidos
  com regras de produção. Descartado.
- **Worker `@Scheduled` no `restaurante-pedido` que avança o
  pedido automaticamente:** mais simples, mas sem nenhuma
  interação do "dono do restaurante" — perde a demonstração do
  perfil `DONO_RESTAURANTE` em mutations GraphQL.
