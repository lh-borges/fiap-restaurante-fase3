# C4 nível 1 — Contexto

Visão "do alto" — o sistema visto **de fora**, sem detalhes técnicos. Mostra os atores que interagem com o sistema e os sistemas externos com os quais ele se integra.

> **Para que serve esse nível?** É a primeira coisa que um stakeholder não-técnico (gerente, professor, cliente) precisa entender. Responde: "quem usa, com quem conversa, e qual o propósito?".

```mermaid
C4Context
    title Diagrama de Contexto — FIAP Restaurante (Fase 3)

    Person(cliente, "Cliente", "Pessoa que faz pedidos online no restaurante")
    Person(dono, "Dono do Restaurante", "Operador que gerencia a fila da cozinha")

    System(fiap, "FIAP Restaurante", "Sistema de pedidos on-line com fluxo completo: autenticação, pedido, pagamento e produção pela cozinha")

    System_Ext(procpag, "procpag", "Gateway externo de pagamento — fornecido pelos professores como imagem Docker. Eventualmente indisponível.")

    Rel(cliente, fiap, "Cadastra-se, faz e consulta pedidos", "GraphQL/HTTPS")
    Rel(dono, fiap, "Avança o estado dos pedidos na cozinha", "GraphQL/HTTPS")
    Rel(fiap, procpag, "Processa pagamentos", "HTTP")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Leitura rápida

- **2 atores humanos**: `Cliente` (perfil JWT `USUARIO`) e `Dono do Restaurante` (perfil `DONO_RESTAURANTE`).
- **1 sistema próprio** (o que construímos): `FIAP Restaurante`.
- **1 sistema externo**: `procpag` — gateway de pagamento que pode estar fora do ar; é justamente por causa dele que existe o capítulo de resiliência.

A complexidade interna (4 microsserviços, Kafka, hexagonal) **não aparece neste nível** — está no [Diagrama de Containers (C4 nível 2)](c4-containers.md).
