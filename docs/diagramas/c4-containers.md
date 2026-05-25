# C4 nível 2 — Containers

Abrindo a "caixa" do sistema FIAP Restaurante: mostra os 4 microsserviços, a infraestrutura compartilhada (MySQL + Kafka) e o tipo de tecnologia + protocolo em cada relacionamento.

> **"Container" no C4 é diferente de Docker.** No C4, container é qualquer **unidade executável** que armazena dados ou processa código — pode ser uma aplicação Spring Boot, um banco MySQL, um broker Kafka, um SPA Angular, um job batch. Não é container Docker. A coincidência de nome confunde, mas é assim na literatura.

> **Nota técnica:** este diagrama usa `flowchart LR` com estilos
> que mimetizam o visual C4 (cores e formas), porque o
> `C4Container` nativo do Mermaid faz auto-layout vertical com
> setas sobrepostas quando há muitos relacionamentos. A
> semântica C4 é mantida (Person, Container, ContainerDb,
> ContainerQueue, System_Ext) — só o renderizador é diferente.

```mermaid
flowchart LR
    %% Estilos seguindo a paleta C4
    classDef person fill:#08427b,color:#fff,stroke:#073b6f,stroke-width:1px
    classDef container fill:#438dd5,color:#fff,stroke:#3c7fc0,stroke-width:1px
    classDef db fill:#438dd5,color:#fff,stroke:#3c7fc0,stroke-width:2px
    classDef queue fill:#438dd5,color:#fff,stroke:#3c7fc0,stroke-width:2px
    classDef external fill:#999999,color:#fff,stroke:#8a8a8a,stroke-width:1px

    Cliente(["👤 Cliente<br/>(perfil USUARIO)"]):::person
    Dono(["👤 Dono do Restaurante<br/>(perfil DONO_RESTAURANTE)"]):::person

    subgraph FIAP["&nbsp;&nbsp;FIAP Restaurante (System Boundary)&nbsp;&nbsp;"]
        direction TB

        subgraph Apps["&nbsp;Aplicações Spring Boot 4 / Java 25&nbsp;"]
            direction LR
            Auth["<b>usuario-autenticacao</b><br/>:8081<br/><i>GraphQL + gRPC server</i><br/>Cadastro · Login · JWT RS256"]:::container
            Pedido["<b>restaurante-pedido</b><br/>:8082<br/><i>GraphQL</i><br/>Ciclo do pedido<br/>CRIADO → PAGO → PRONTO"]:::container
            Pagamento["<b>pagamento-service</b><br/>:8083<br/><i>GraphQL · Worker @Scheduled</i><br/>Resilience4j: CB + Retry +<br/>Timeout + Fallback"]:::container
            Cozinha["<b>restaurante-service</b><br/>:8084<br/><i>GraphQL</i><br/>Fila da cozinha<br/>RECEBIDO → EM_PREPARO → PRONTO"]:::container
        end

        subgraph Infra["&nbsp;Infraestrutura compartilhada&nbsp;"]
            direction LR
            Kafka[("Kafka 7.7.1 KRaft<br/>6 tópicos<br/>(sem Zookeeper)")]:::queue
            MySQL[("MySQL 8.4<br/>4 databases isolados<br/>auth_db · pedido_db<br/>pagamento_db · cozinha_db")]:::db
        end
    end

    Procpag["<b>procpag</b> :8089<br/><i>Gateway externo</i><br/>(eventualmente disponível)"]:::external

    Cliente -- "Login / cadastro<br/>GraphQL" --> Auth
    Cliente -- "Cria e consulta pedidos<br/>GraphQL + JWT" --> Pedido
    Dono -- "Gerencia fila<br/>GraphQL + JWT" --> Cozinha

    Pedido -- "Valida usuário<br/>gRPC" --> Auth
    Pagamento -- "Processa cobrança<br/>HTTP + Resilience4j" --> Procpag

    Pedido <-- "publish + consume<br/>Kafka" --> Kafka
    Pagamento <-- "publish + consume<br/>Kafka" --> Kafka
    Cozinha <-- "publish + consume<br/>Kafka" --> Kafka

    Auth -. "JDBC<br/>(auth_db)" .-> MySQL
    Pedido -. "JDBC<br/>(pedido_db)" .-> MySQL
    Pagamento -. "JDBC<br/>(pagamento_db)" .-> MySQL
    Cozinha -. "JDBC<br/>(cozinha_db)" .-> MySQL
```

## Leitura rápida

- **4 microsserviços Spring** — todos com a mesma stack (Java 25 + Spring Boot 4) e a mesma arquitetura interna (Hexagonal). O que muda é o que cada um faz.
- **1 instância MySQL** com **4 databases isolados** — abordagem *database per service* (sem joins entre bancos). Ver [ADR 0007](../adr/0007-database-per-service.md).
- **1 broker Kafka em KRaft** — sem Zookeeper. Ver [ADR 0006](../adr/0006-kafka-kraft.md).
- **3 protocolos de rede** em uso:
  - **GraphQL/HTTPS** — entrada externa para todos os 4 serviços
  - **gRPC** — única chamada síncrona interna (`restaurante-pedido` → `usuario-autenticacao`)
  - **Kafka** — toda a comunicação assíncrona entre os serviços
- **1 sistema externo** — `procpag` (gateway de pagamento, eventualmente disponível)

## Por que dois níveis de C4 e não mais?

O modelo C4 oferece **4 níveis** (Contexto, Container, Componente, Code). Para este projeto:
- **Nível 1 (Contexto)** — útil para apresentar o sistema rapidamente; está em [`c4-contexto.md`](c4-contexto.md).
- **Nível 2 (Container)** — este aqui; mostra a topologia técnica. É o nível mais usado em arquitetura de microsserviços.
- **Nível 3 (Componente)** — equivale a abrir cada Container e mostrar seus pacotes/classes. **Já está documentado** pela estrutura hexagonal nos READMEs de cada módulo + nos diagramas de sequência. Adicionar um C4-C3 formal seria redundante.
- **Nível 4 (Code)** — UML de classes; raramente usado, e nosso domínio é suficientemente simples para que ler o código direto seja mais útil.
