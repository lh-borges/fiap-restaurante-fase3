# ADR 0002: Arquitetura hexagonal em cada microsserviço

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O requisito 5.5 da spec exige a aplicação de **Clean Architecture ou
Hexagonal Architecture** nos serviços. Além do cumprimento formal, é
desejável manter o domínio (regras de negócio) isolado dos detalhes de
framework (Spring, JPA, Kafka) — para facilitar testes, evoluções e
trocas de infraestrutura.

## Decisão

Adotar **Hexagonal Architecture (Ports & Adapters)** de Alistair
Cockburn em todos os 4 microsserviços, com a seguinte topologia de
pacotes:

```
<servico>/src/main/java/br/com/fiaprestaurante/<servico>/
├── domain/                     # entidades, value objects, exceções de negócio
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
    ├── exception/              # *GraphQLExceptionHandler
    └── scheduler/              # workers @Scheduled (só no pagamento)
```

**Regra de ouro:** o pacote `domain/` **não importa nada de Spring, JPA,
Kafka ou qualquer framework**. Toda dependência externa cruza a fronteira
via interfaces declaradas em `application/port/output/`.

## Consequências

### Positivas

- **Testabilidade direta do domínio:** unit tests sem `@SpringBootTest`,
  sem H2, sem mocks de framework — Mockito puro.
- **Troca de adapter é local:** substituir Kafka por RabbitMQ é mudar
  apenas o `*KafkaPublisher`/`*KafkaConsumer`; o use case não muda.
- **Regra de negócio legível:** invariantes de domínio (transições de
  estado, validações) ficam dentro dos métodos da entidade, não
  espalhados em services anêmicos.

### Negativas

- **Mais classes:** 1 use case típico envolve interface (port input) +
  implementação (service) + interfaces de saída (ports output) +
  adapters concretos. Trade-off explícito de verbosidade pelo
  isolamento.
- **Curva de aprendizado:** times acostumados a controller→service→repo
  precisam internalizar o modelo de ports.

## Alternativas consideradas

- **Layered Architecture clássica (controller → service → repository):**
  mais simples, mas o `service` acaba acoplado a JPA/Spring através do
  repository concreto, contaminando o domínio.
- **Clean Architecture com use case interactors mais granulares:**
  equivalente em essência ao hexagonal; escolhemos hexagonal pela
  nomenclatura mais clara de **ports** e **adapters**.
