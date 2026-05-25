# ADR 0011: Docker Compose como orquestrador

- **Status:** Accepted
- **Data:** 2026-05-24
- **Deciders:** Danilo Fernando

## Contexto e problema

O requisito 3 da spec exige um arquivo `compose.yml` que **sobe todos
os serviços com um único comando**. Há liberdade entre Docker Compose,
Podman Compose, Kubernetes (Kind/Minikube/k3d) ou scripts ad hoc.

O público-alvo da entrega é o avaliador do Tech Challenge, que vai
executar localmente em uma máquina Windows/macOS/Linux com Docker
Desktop. **Simplicidade de execução** é mais importante que recursos
avançados de orquestração.

## Decisão

Usar **Docker Compose v2** com um único `docker-compose.yml`
declarando 8 serviços:

```
mysql + kafka + kafka-ui + procpag (4 infra)
usuario-autenticacao + restaurante-pedido + pagamento + restaurante-service (4 apps)
```

Comando único de execução:

```bash
docker compose up -d --build
```

Recursos adicionais usados:

- **Anchors YAML** (`x-app-logging`, `x-app-jvm-opts`) para reduzir
  duplicação.
- **`deploy.resources.limits`** (memória + CPUs) — vide ADR 0013.
- **Healthchecks** com `start_period` em `mysql`, `kafka` e nas 4
  apps Spring.
- **`depends_on: service_healthy`** para cascatear startup
  determinístico.
- **Logging driver** `json-file` com `max-size: 10m` e `max-file: 3`
  para evitar saturar disco.
- **`init: true`** nos containers Spring para SIGTERM ser propagado
  ao processo Java (shutdown gracioso ~2s em vez de 10s).

## Consequências

### Positivas

- **"Um comando sobe tudo"** atende literalmente o requisito 3 da spec.
- **Curva zero para o avaliador:** qualquer máquina com Docker
  Desktop instalado consegue rodar.
- **Reproduzível:** o `docker-compose.yml` é o contrato — não
  depende de cluster externo configurado.
- **Sem complexidade desnecessária:** sem service discovery
  dinâmico, sem ingress controllers, sem RBAC.

### Negativas

- **Single host:** sem replicação real, sem failover, sem balanceamento.
- **Limit de escala vertical:** estourou a RAM do host, acabou.
- **Não reflete cenário de produção real:** em produção, microsserviços
  rodariam em k8s/ECS com replicação, autoscaling, service mesh.

## Alternativas consideradas

- **Kubernetes (Minikube/Kind/k3d):** mais próximo de produção, mas
  exige instalar e configurar o cluster local — fricção alta para
  o avaliador.
- **Podman Compose:** alternativa rootless ao Docker Compose, mas
  ainda menos onipresente que Docker Desktop.
- **Scripts shell (`run.sh`):** subir manualmente cada container.
  Antiquado, propenso a erro, sem dependência declarativa.
