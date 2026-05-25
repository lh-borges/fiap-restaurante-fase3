# Diagramas

Esta pasta contém os diagramas de arquitetura do projeto em formato
**Mermaid** (texto versionável que renderiza visualmente no GitHub e
em IDEs modernas).

Cada diagrama mora em seu próprio arquivo `.md` para permitir foco e
versionamento granular.

## Índice

| Arquivo | Tipo | O que mostra |
|---|---|---|
| [componentes.md](componentes.md) | Componentes (C4 nível 2) | Visão estática: 4 microsserviços + infra + procpag + cliente |
| [sequencia-happy-path.md](sequencia-happy-path.md) | Sequência | Fluxo feliz: cadastro → login → criar pedido → confirmar → PAGO → cozinha → PRONTO |
| [sequencia-resiliencia.md](sequencia-resiliencia.md) | Sequência | Fluxo de falha do gateway: pagamento pendente + reprocessamento automático |
| [maquina-estados-pedido.md](maquina-estados-pedido.md) | Máquina de estados | Estados do agregado `Pedido` e transições |

## Como visualizar

- **GitHub:** os blocos ```` ```mermaid ```` renderizam automaticamente.
- **VSCode:** extensão "Markdown Preview Mermaid Support" da Matt Bierner.
- **IntelliJ:** plugin "Mermaid" do oficial.
- **CLI:** `mmdc -i diagrama.md -o diagrama.svg` (via mermaid-cli).

## Por que Mermaid e não desenhos no draw.io?

Mermaid é **texto** — versiona no Git, faz diff no PR, evita o
problema clássico de "o PNG no repo está desatualizado em relação
ao código". Para diagramas complexos demais (UML detalhado de
classes, por exemplo), draw.io faz sentido; para os 4 diagramas
deste projeto, Mermaid cobre.
