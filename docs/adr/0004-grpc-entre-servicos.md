# ADR 0004: gRPC para chamadas síncronas entre serviços

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

Há um caso de comunicação síncrona entre serviços internos: o
`restaurante-pedido` precisa validar/consultar dados de usuário no
`usuario-autenticacao` durante a criação de pedidos (cenários de
validação de perfil). Essa chamada acontece **dentro da rede privada
dos containers** — tráfego confiável, controlado por nós.

A spec menciona explicitamente comunicação síncrona entre serviços
(req. 5.1). Há liberdade de escolher REST/JSON ou gRPC.

## Decisão

Usar **gRPC** (com `grpc-netty-shaded` 1.68.x) para a chamada
`restaurante-pedido → usuario-autenticacao`. O `.proto` fica no módulo
compartilhado `shared/`, gerando stubs em `target/generated-sources/`
para ambos os módulos. O servidor gRPC do `usuario-autenticacao`
escuta na porta `9000` (interna ao container).

## Consequências

### Positivas

- **Performance:** Protobuf é binário e tipicamente **3-10× menor**
  que JSON equivalente; HTTP/2 com multiplexação reduz latência.
- **Contrato compilado:** mudança incompatível no `.proto` quebra o
  `mvn package` — não em produção. Erros pegam cedo.
- **Type safety:** stubs gerados em Java garantem que cliente e
  servidor falam a mesma linguagem em tempo de compilação.

### Negativas

- **Debugging mais complicado:** não dá pra abrir Postman e mandar um
  request; precisa de `grpcurl` ou cliente gerado.
- **Menos visibilidade em logs:** mensagens binárias requerem
  ferramentas especializadas para inspeção.
- **Mais um protocolo no projeto:** soma-se a GraphQL externo + HTTP
  para `procpag` + Kafka.

## Alternativas consideradas

- **REST/JSON entre serviços:** padrão, debugável, mas mais lento e
  sem contrato compilado.
- **OpenFeign:** abstrai REST com tipagem em Java, mas não resolve o
  overhead de JSON nem o contrato compilado.
- **Mensageria assíncrona via Kafka:** descartado porque o caso de
  uso aqui é **consulta síncrona** — não notificação.
