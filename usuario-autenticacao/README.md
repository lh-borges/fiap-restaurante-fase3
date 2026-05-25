# usuario-autenticacao

Microsserviço de **identidade**: cadastro de clientes, autenticação e emissão de tokens JWT. Atende ao requisito 4.1 da spec ("Gerenciamento de usuários") e provê a base para o requisito 5.2 ("Segurança com Spring Security + JWT").

## O que faz

- **Cadastrar usuário** (mutation GraphQL `cadastrarUsuario`) com BCrypt para a senha
- **Autenticar usuário** (mutation `login`) — devolve JWT RS256 com `subject = publicId` do usuário e claim `groups` com o perfil
- **Consultar usuário corrente** (query `me`) — usa o próprio JWT
- **Servidor gRPC interno** em `:9000` para consultas síncronas dos outros serviços

## Portas

| Porta | Protocolo | Uso |
|---|---|---|
| `8081` | HTTP | API GraphQL pública (`/graphql`, `/graphiql`) + Actuator (`/actuator/health`) |
| `9000` | gRPC | Consultas internas (não exposto pelo Docker — só acessível na rede `fase3net`) |

## Dependências

- **MySQL** (`auth_db`) — persistência
- **NÃO depende** de Kafka, gateway externo, ou de qualquer outro serviço Spring

## Stack específica deste módulo

- Spring Security 7 (OAuth2 Resource Server emite + valida JWT)
- `grpc-netty-shaded`, `grpc-protobuf`, `grpc-stub` — implementação gRPC server
- `jbcrypt` — hash de senha
- H2 (`scope=test`) — usado em testes

## Como rodar isoladamente

Para subir só este serviço (precisa de MySQL rodando):

```bash
./mvnw -pl usuario-autenticacao -am spring-boot:run
```

Ou via Docker (preferível):

```bash
docker compose up -d --build usuario-autenticacao mysql
```

## Endpoints úteis

| URL | Para que |
|---|---|
| `http://localhost:8081/graphiql` | Console GraphQL interativo |
| `http://localhost:8081/graphql` | Endpoint GraphQL (POST) |
| `http://localhost:8081/actuator/health` | Healthcheck |

## Contas seed

Criadas automaticamente na inicialização (via `DataSeeder`):

| E-mail | Senha | Perfil |
|---|---|---|
| `usuario@fiap.com` | `usuario123` | `USUARIO` |
| `dono@fiap.com` | `dono123` | `DONO_RESTAURANTE` |

## Estrutura interna

Arquitetura hexagonal padrão do projeto — ver `src/main/java/br/com/fiaprestaurante/usuarioautenticacao/`:

```
domain/                 # Usuario, PerfilUsuario, exceções
application/
  ├─ dto/              # commands e responses
  ├─ port/{input,output}
  └─ usecase/          # CadastrarUsuario, AutenticarUsuario, BuscarUsuarioAtual
adapter/
  ├─ inbound/graphql/  # UsuarioGraphQLController
  └─ outbound/security/ # JwtTokenProvider (RS256)
infrastructure/
  ├─ config/           # SecurityConfig, JwtConfig
  ├─ grpc/             # UsuarioGrpcService + GrpcServerRunner
  ├─ persistence/      # DataSeeder
  └─ exception/        # UsuarioGraphQLExceptionHandler
```

## Testes

```bash
./mvnw -pl usuario-autenticacao -am test
```

**63 testes** verdes. Não exige Docker, MySQL ou Kafka externos (usa H2 + mocks).

## Referências

- [Documentação geral](../README.md)
- [Documentação técnica ABNT](../docs/documentacao-arquitetura.pdf) (capítulo 3.2.1)
- [ADR 0008 — JWT RS256](../docs/adr/0008-jwt-rs256.md)
