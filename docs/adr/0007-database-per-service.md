# ADR 0007: Database separado por serviço (no mesmo MySQL)

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

Cada microsserviço precisa persistir seu estado. Há dois eixos de
decisão:

1. **Schema isolado vs schema compartilhado** — todos no mesmo
   `database` MySQL com tabelas prefixadas, ou cada um com seu
   `database` separado?
2. **Instâncias MySQL separadas vs instância única compartilhada** —
   um container MySQL por serviço, ou um container único hospedando
   múltiplos databases?

A spec não exige nenhuma topologia específica de banco, só que cada
serviço tenha seu próprio modelo.

## Decisão

**Database separado por serviço, mesma instância MySQL.** O container
único `mysql:8.4` hospeda quatro databases criados por `init.sql`:

```sql
CREATE DATABASE auth_db;       -- usuario-autenticacao
CREATE DATABASE pedido_db;     -- restaurante-pedido
CREATE DATABASE pagamento_db;  -- pagamento
CREATE DATABASE cozinha_db;    -- restaurante-service
```

Cada serviço aponta sua `SPRING_DATASOURCE_URL` para o respectivo
database. **Nenhum serviço acessa o banco de outro.**

## Consequências

### Positivas

- **Evolução de schema independente:** mudar `pagamentos.status` não
  afeta `pedidos` — bancos diferentes, migrações desacopladas.
- **Joins entre bancos são impossíveis na prática:** força a
  comunicação via eventos, modelo correto em microsserviços.
- **Isolamento de credenciais (potencial):** cada app pode ter seu
  usuário MySQL com permissão apenas ao seu database (hoje todos
  usam root; melhoria futura).
- **Footprint econômico:** uma instância MySQL única é mais barata
  em RAM e mais simples de operar que 4 instâncias.

### Negativas

- **Single point of failure:** o container MySQL caindo derruba os
  4 serviços. Em produção, cada serviço deveria ter seu próprio
  cluster MySQL.
- **Recursos compartilhados:** uma query lenta em `pagamento_db`
  pode degradar o `pedido_db` (mesma JVM do MySQL, mesmo pool de
  threads).
- **Dados duplicados:** o `restaurante-service` mantém snapshot
  dos itens do pedido (produto, nome, quantidade) em vez de fazer
  JOIN com `pedido_db`. Aceitável: a duplicação reflete necessidade
  real do bounded context da cozinha.

## Alternativas consideradas

- **MySQL único com schema compartilhado e prefixos de tabela:**
  acopla os serviços ao nível de banco. Foreign keys entre serviços
  ficariam tentadoras e quebrariam o isolamento.
- **Uma instância MySQL por serviço (4 containers):** isolamento
  total, mas custo de 4× operação e memória. Não vale para dev/demo.
- **Bancos diferentes por serviço (Postgres em um, MongoDB em outro):**
  máximo de flexibilidade, mas dispersão tecnológica desnecessária
  para o escopo do projeto.
