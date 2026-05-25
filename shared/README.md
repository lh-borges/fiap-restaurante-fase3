# shared

Módulo Maven sem `main` — apenas uma **biblioteca compartilhada** entre os 4 microsserviços. Não roda como aplicação.

## Conteúdo

- **`BusinessException`** — exceção de negócio comum, usada por todos os módulos para sinalizar violações de regra (ex.: token inválido, transição de estado proibida). Os `*GraphQLExceptionHandler` de cada serviço traduzem para erro GraphQL com `BAD_REQUEST`.
- **Stubs gRPC** — contratos compartilhados de comunicação síncrona interna. O `.proto` é compilado por este módulo e os stubs Java ficam disponíveis para quem importar `br.com.fiaprestaurante:shared`. Hoje, usados pelo `restaurante-pedido` para consultar o `usuario-autenticacao` na porta interna `:9000`.

## Como é usado

Cada microsserviço Spring inclui no seu `pom.xml`:

```xml
<dependency>
    <groupId>br.com.fiaprestaurante</groupId>
    <artifactId>shared</artifactId>
</dependency>
```

A versão é gerenciada pelo `dependencyManagement` do parent POM (`fiap-restaurante`).

## Como compilar

Não é necessário compilar isoladamente — o `mvn package` do parent já compila o `shared` antes dos demais módulos. Para compilar só este:

```bash
./mvnw -pl shared compile
```

## Decisões relacionadas

- **`BusinessException` compartilhada** evita duplicação da mesma classe em 4 módulos.
- **gRPC para o tráfego síncrono interno** — ver [ADR 0004](../docs/adr/0004-grpc-entre-servicos.md).
