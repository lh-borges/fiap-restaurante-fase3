# FIAP Restaurante — Fase 3

Sistema de pedido online para restaurante, dividido em microsserviços, comunicando-se de forma síncrona (gRPC, GraphQL) e assíncrona (Apache Kafka), com resiliência aplicada na chamada ao gateway externo de pagamento.

Projeto desenvolvido como **Tech Challenge da Fase 3** da PosTech FIAP. A especificação completa está em [`requisito/tech-challenge-fase-03.pdf`](../requisito/tech-challenge-fase-03.pdf).

---

## Visão geral

```
                    +------------------+
   Cliente HTTP --->|  usuario-auth    |  (cadastro + login + JWT RS256)
                    +------------------+
                            | gRPC
                            v
                    +-------------------+
                    | restaurante-pedido|  produz pedido.criado
                    +-------------------+
                            | Kafka
                            v
                    +------------------+         HTTP (+ Resilience4j)
                    |    pagamento     |  -----------------------------> procpag
                    +------------------+   (Circuit Breaker + Retry + Timeout)
                            | Kafka
                            v
              pagamento.aprovado / pagamento.pendente
```

- **Padrão arquitetural por serviço:** Hexagonal (Ports & Adapters) — `domain` puro, `application` com use cases e ports, `adapter` com inbound/outbound, `infrastructure` com configs.
- **Autenticação:** JWT assinado em RS256. O `usuario-autenticacao` é o único emissor; os demais serviços validam usando a chave pública distribuída.
- **Comunicação síncrona entre serviços:** gRPC (consulta de dados de usuário).
- **Comunicação assíncrona:** Kafka, com 3 tópicos (`pedido.criado`, `pagamento.aprovado`, `pagamento.pendente`).
- **Resiliência:** Resilience4j na chamada do `pagamento` ao gateway externo `procpag` — Circuit Breaker, Retry, Timeout e Fallback que marca o pagamento como PENDENTE e publica em `pagamento.pendente`.

---

## Mapeamento dos requisitos da Fase 3

| Item da spec | O que pede | Onde fica implementado |
|---|---|---|
| 4.1 Gerenciamento de usuários | Criar e autenticar cliente | `usuario-autenticacao` (GraphQL: `cadastrarUsuario`, `login`) |
| 4.2 Criar pedido | Pedido com cliente, restaurante e itens | `restaurante-pedido` — mutations `criarPedido` e `confirmarPedido` |
| 4.3 Consultas | Pedido por ID e por cliente autenticado | `restaurante-pedido` — queries `pedidoPorId` e `meusPedidos` |
| 4.4 Processamento de pagamento | Chamar `procpag` ao receber pedido | `pagamento` — `ExternalPaymentClient` → `procpag:8089/requisicao` |
| 4.5 Pagamento pendente | Quando gateway indisponível, marcar PENDENTE e enfileirar | `pagamento` — fallback em `ProcessarPagamentoService.tentarGateway` + tópico `pagamento.pendente` |
| 4.6 Reprocessamento automático | Worker reprocessa pendentes quando gateway volta | `pagamento` — `ReprocessamentoPagamentoWorker` (`@Scheduled` 30s) |
| 4.7 Atualização automática de status | Após confirmação, pedido vira PAGO | Evento `pagamento.aprovado` publicado pelo `pagamento`, consumido por `restaurante-pedido` |
| 5.1 Múltiplos serviços | Auth + pedido + pagamento separados | 3 módulos Maven independentes |
| 5.2 Spring Security + JWT | Login emite JWT, demais serviços validam | RS256 com chave pública/privada em PEM |
| 5.3 Kafka | 3 tópicos obrigatórios | `pedido.criado` (consumer), `pagamento.aprovado` e `pagamento.pendente` (publishers) |
| 5.4 Resilience4j | CB + Retry + Timeout + Fallback no gateway | `pagamento` — anotações no `ExternalPaymentClient` + fallback no use case |
| 5.5 Hexagonal | Camadas controller / use case / domain / infra | Aplicado em todos os módulos |

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 25 (Temurin LTS) |
| Spring Boot | 4.0.5 |
| Spring GraphQL | 4.0.x |
| Spring Security (OAuth2 Resource Server) | 7.x |
| Apache Kafka (Confluent) | 7.4.0 |
| Spring Kafka | 4.0.4 |
| MySQL | 8.4 |
| gRPC | 1.68.x |
| Resilience4j | 2.3.0 |
| Jackson (2 para Kafka serialization) | 2.21.2 |
| Maven | wrapper (3.9+) |
| Docker / Docker Compose | 24+ / v2 |

---

## Serviços

| Serviço | Porta host | Tecnologia / Imagem | Papel |
|---|---|---|---|
| `usuario-autenticacao` | 8081 | App Spring Boot (build local) | Cadastro/login de usuários, emissão de JWT, servidor gRPC para consulta de usuário |
| `restaurante-pedido` | 8082 | App Spring Boot (build local) | CRUD de restaurantes/produtos e pedidos, produz `pedido.criado` e consome `pagamento.*` |
| `pagamento` | 8083 | App Spring Boot (build local) | Consome `pedido.criado`, chama procpag com resiliência, publica `pagamento.aprovado`/`pagamento.pendente`, worker de reprocessamento |
| `procpag` | 8089 | `docker.io/erickemprobr/procpag:latest` | **Gateway de pagamento externo fornecido pelos professores** — sempre autoriza quando disponível |
| `mysql` | (interno) | `mysql:8.4` | Persistência. Três databases criados via `init.sql`: `auth_db`, `pedido_db`, `pagamento_db` |
| `zookeeper` | (interno) | `confluentinc/cp-zookeeper:7.4.0` | Coordenação do Kafka |
| `kafka` | 9092 | `confluentinc/cp-kafka:7.4.0` | Broker de mensageria assíncrona |
| `kafka-ui` | 8085 | `provectuslabs/kafka-ui:v0.7.2` | Interface web para inspecionar tópicos e mensagens |

Todos os containers vivem na rede `fase3net` e se enxergam pelo nome do serviço.

---

## Como executar

### Pré-requisitos

- Docker Desktop instalado e rodando
- ~3 GB de RAM livres
- Portas livres no host: **8081, 8082, 8083, 8085, 8089, 9092**
- (Opcional, só para build/teste local fora do Docker): JDK 25 da Adoptium Temurin

> **Atenção se você tem MySQL instalado nativamente:** a porta 3306 fica ocupada e o container `mysql` não conseguirá expor essa porta. Crie um arquivo `docker-compose.override.yml` (já ignorado pelo Git) com o conteúdo abaixo:
> ```yaml
> services:
>   mysql:
>     ports: !reset []
> ```
> Isso mantém o container acessível pelos demais serviços via rede interna sem tentar bindar no host.

### Subir tudo

A partir da raiz do projeto:

```bash
docker compose up -d --build
```

Na primeira execução o build é demorado (Maven baixa todas as dependências dentro dos containers). Próximas execuções aproveitam o cache.

### Verificar saúde

```bash
curl http://localhost:8081/actuator/health   # usuario-autenticacao
curl http://localhost:8082/actuator/health   # restaurante-pedido
curl http://localhost:8083/actuator/health   # pagamento
```

Cada um deve retornar `{"status":"UP", ...}`.

### Interfaces úteis

| URL | O que é |
|---|---|
| `http://localhost:8081/graphiql` | GraphiQL do `usuario-autenticacao` |
| `http://localhost:8082/graphiql` | GraphiQL do `restaurante-pedido` |
| `http://localhost:8083/graphiql` | GraphiQL do `pagamento` |
| `http://localhost:8085` | Kafka UI (inspecionar tópicos e mensagens) |

### Parar tudo

```bash
docker compose down              # mantém o volume do MySQL
docker compose down -v           # remove também os dados persistidos
```

---

## Testando o fluxo de pagamento (via Kafka UI ou linha de comando)

Publique manualmente um evento no tópico `pedido.criado` para simular a criação de um pedido confirmado:

```bash
# via docker exec
echo '{"pedidoId":"00000000-0000-0000-0000-000000000001","valorTotal":42.50}' \
  | docker exec -i kafka kafka-console-producer \
      --bootstrap-server kafka:9092 --topic pedido.criado
```

Acompanhe os logs:

```bash
docker logs -f pagamento
```

Inspecione os tópicos `pagamento.aprovado` e `pagamento.pendente` no Kafka UI em `http://localhost:8085`.

Inspecione a tabela `pagamentos` no MySQL:

```bash
docker exec -it mysql mysql -uroot -proot \
  -e "USE pagamento_db; SELECT BIN_TO_UUID(pedido_id) AS pedido_id, valor, status, tentativas FROM pagamentos;"
```

### Testar o fluxo de resiliência

Pare o gateway externo e publique um novo evento — o pagamento ficará PENDENTE e o evento `pagamento.pendente` será publicado:

```bash
docker stop procpag
# publique outro evento com pedidoId diferente, observe logs
docker start procpag
# em até 30s o worker de reprocessamento vira PENDENTE -> APROVADO
```

---

## Estrutura do repositório

```
fiap-restaurante-fase3/
├── docker-compose.yml          # sobe todos os serviços
├── init.sql                    # cria os 3 databases no MySQL
├── pom.xml                     # parent POM multi-módulo
├── shared/                     # stubs gRPC + BusinessException
├── usuario-autenticacao/       # microsserviço de cadastro/login/JWT
├── restaurante-pedido/         # microsserviço de pedidos
├── pagamento/                  # microsserviço de pagamento (+ Kafka + Resilience4j)
└── docs/                       # documentação adicional (ADRs etc.)
```

Cada microsserviço segue o mesmo layout interno (arquitetura hexagonal):

```
<servico>/src/main/java/br/com/fiaprestaurante/<servico>/
├── <Servico>Application.java                 # main Spring Boot
├── domain/
│   ├── entity/         # entidades puras
│   ├── valueobject/    # value objects e enums
│   └── exception/      # exceções de regra de negócio
├── application/
│   ├── dto/            # commands, responses, eventos
│   ├── port/input/     # interfaces dos use cases
│   ├── port/output/    # interfaces de repositório, mensageria, HTTP
│   └── usecase/        # implementações dos use cases
├── adapter/
│   ├── inbound/        # GraphQL, Kafka consumers
│   └── outbound/       # JPA repos, Kafka producers, HTTP clients
└── infrastructure/
    ├── config/         # SecurityConfig, KafkaConfig, BeanConfig
    └── scheduler/      # workers @Scheduled (se houver)
```

---

## Status de implementação por serviço

| Serviço | Estado |
|---|---|
| `usuario-autenticacao` | Completo (cadastro, login, JWT, servidor gRPC, seeder de dados) |
| `pagamento` | Completo (use cases, Kafka consumer/publisher, Resilience4j, worker de reprocessamento, GraphQL de consulta, persistência MySQL) |
| `restaurante-pedido` | Completo (criar/confirmar/consultar pedido, publica `pedido.criado`, consome `pagamento.aprovado` e `pagamento.pendente`, persistência MySQL, JWT) |

---

## Documentação adicional

- ADRs em `docs/` (em construção): decisões arquiteturais (Hexagonal, Kafka, Resilience4j, JWT RS256, UUID v7).
- JavaDoc em todas as classes e métodos do módulo `pagamento`.
- Coleção Postman: [`docs/fiap-restaurante.postman_collection.json`](docs/fiap-restaurante.postman_collection.json).
