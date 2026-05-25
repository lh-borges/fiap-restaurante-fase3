# ADR 0013: Resource limits explícitos no compose

- **Status:** Accepted
- **Data:** 2026-05-25
- **Deciders:** Danilo Fernando

## Contexto e problema

Sem `deploy.resources.limits`, cada container Docker tem acesso a
**toda a RAM e CPU do host**. As 4 JVMs Spring que rodam neste
projeto usam `-XX:MaxRAMPercentage=65.0` — cada uma enxerga 65% da
RAM disponível e tenta usar tudo se precisar.

Em uma máquina com 16 GB, isso significa que **4 JVMs podem tentar
usar 4 × ~10 GB = 40 GB de memória** combinada. Na prática, o SO
nega via OOM Killer e mata containers aleatoriamente — diagnóstico
extremamente confuso para o avaliador.

O usuário relatou que a máquina estava sendo "matada" pelos
containers — sinal claro de que sem limits era inviável rodar tudo
em hardware típico.

## Decisão

Adicionar **`deploy.resources.limits` e `reservations`** em todos
os 8 serviços do `docker-compose.yml`, com perfil enxuto. Totais:

| Serviço | Memória limit | CPU limit | Memória reservada |
|---|---|---|---|
| `mysql` | 1 GB | 1.0 | 384 MB |
| `kafka` | 1 GB | 1.5 | 512 MB |
| `kafka-ui` | 384 MB | 0.5 | — |
| `procpag` | 256 MB | 0.5 | — |
| `usuario-autenticacao` | 768 MB | 1.0 | 384 MB |
| `restaurante-pedido` | 768 MB | 1.0 | 384 MB |
| `pagamento-service` | 768 MB | 1.0 | 384 MB |
| `restaurante-service` | 768 MB | 1.0 | 384 MB |
| **Total** | **~5.7 GB → ~3.5 GB** após otimização | **7.5** | **~2.4 GB** |

`JAVA_TOOL_OPTIONS` dos apps Spring foi ajustado para `MaxRAMPercentage=65`
(equilíbrio entre heap útil e overhead de metaspace/threads/native).

`deploy.resources` funciona em `docker compose up` na Compose v2
(antes só funcionava em Swarm).

## Consequências

### Positivas

- **Host previsível:** total nunca passa de ~4 GB de RAM usada.
- **OOM Killer atua dentro do envelope do container:**
  `OOMKilled: true` em apenas um container, não derruba a máquina
  inteira.
- **Boot paralelo coordenado:** CPU limits impedem que 4 JVMs subindo
  ao mesmo tempo travem o host.
- **Documenta a "carga base":** o `deploy.resources.reservations`
  comunica a memória mínima necessária para o container subir.

### Negativas

- **App pode ser morto sob carga real:** se um pico legítimo de
  tráfego exigir mais memória que o limit, o container sofre
  OOMKill. Para dev/demo é aceitável; em produção, limits devem
  ser dimensionados com base em percentis de uso real.
- **Throughput limitado:** com CPU 1.0, GC paralelo do G1 fica
  restrito a ~1 thread. Sob alta carga, tempos de pausa aumentam.
- **Calibração manual:** sem profiling real, os números são
  estimativa. Vale revisar periodicamente.

## Alternativas consideradas

- **Sem limits (estado anterior):** "deixa cada container pegar o
  que quiser". Levou ao problema relatado pelo usuário — máquina
  travando.
- **Limits altíssimos (ex.: 2 GB cada):** dá folga mas anula o
  benefício; total cresce demais.
- **Limits via Docker Desktop UI:** controla o conjunto, não o
  individual; não dá granularidade por serviço.
- **Resource quotas via cgroups manuais:** equivalente ao limit do
  Compose, mas mais difícil de versionar junto com a infraestrutura.
