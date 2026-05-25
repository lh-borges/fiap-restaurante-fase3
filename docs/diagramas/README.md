# Diagramas

Esta pasta contém os diagramas de arquitetura do projeto em duas formas
complementares:

- **`.md`** — fonte Mermaid (texto versionável que renderiza visualmente
  no GitHub e em IDEs modernas). É a **fonte da verdade** — sempre edite
  esses arquivos.
- **`.png`** — renderização gerada a partir do `.md` correspondente,
  usada na documentação ABNT (PDF) e no README principal.

Cada diagrama mora em seu próprio par de arquivos para permitir foco e
versionamento granular.

## Índice

| Fonte | Imagem | Tipo | O que mostra |
|---|---|---|---|
| [c4-contexto.md](c4-contexto.md) | [c4-contexto.png](c4-contexto.png) | **C4 nível 1 (Contexto)** | Sistema visto de fora: atores e sistemas externos |
| [c4-containers.md](c4-containers.md) | [c4-containers.png](c4-containers.png) | **C4 nível 2 (Containers)** | Topologia técnica: 4 apps Spring + MySQL + Kafka + procpag + protocolos |
| [componentes.md](componentes.md) | [componentes.png](componentes.png) | Componentes (visão lógica) | Detalhamento de eventos Kafka + ligações |
| [sequencia-happy-path.md](sequencia-happy-path.md) | [sequencia-happy-path.png](sequencia-happy-path.png) | Sequência | Fluxo feliz: cadastro → login → criar pedido → confirmar → PAGO → cozinha → PRONTO |
| [sequencia-resiliencia.md](sequencia-resiliencia.md) | [sequencia-resiliencia.png](sequencia-resiliencia.png) | Sequência | Fluxo de falha do gateway: pagamento pendente + reprocessamento automático |
| [maquina-estados-pedido.md](maquina-estados-pedido.md) | [maquina-estados-pedido.png](maquina-estados-pedido.png) | Máquina de estados | Estados do agregado `Pedido` e transições |

## Como visualizar

- **GitHub:** os blocos ```` ```mermaid ```` renderizam automaticamente.
- **VSCode:** extensão "Markdown Preview Mermaid Support" da Matt Bierner.
- **IntelliJ:** plugin "Mermaid" oficial.
- **PNG direto:** abra os `.png` em qualquer visualizador de imagens.

## Como regenerar os PNGs

Os PNGs devem ser regerados sempre que o `.md` correspondente mudar.
Use `mermaid-cli` via container Docker — sem necessidade de instalar
Node.js ou Chromium localmente:

```bash
cd docs/diagramas

# 1) extrai os blocos mermaid dos .md para .mmd temporários
for f in c4-contexto c4-containers componentes sequencia-happy-path sequencia-resiliencia maquina-estados-pedido; do
  awk '/^```mermaid$/,/^```$/' "$f.md" | sed '1d;$d' > "$f.mmd"
done

# 2) renderiza cada .mmd em PNG (largura 2000-2200 px, fundo branco)
for f in c4-contexto c4-containers componentes sequencia-happy-path sequencia-resiliencia maquina-estados-pedido; do
  docker run --rm -v "$PWD:/data" minlag/mermaid-cli:latest \
    -i "/data/$f.mmd" -o "/data/$f.png" -b white -t default -w 2400
done

# 3) limpa intermediários
rm *.mmd

# 4) (opcional) regere o PDF da documentação para incluir os PNGs novos
cd ..
docker run --rm -v "$PWD:/work" -w //work/build-pdf python:3.12-slim \
  sh -c "pip install -q reportlab && python gerar_documentacao_pdf.py"
```

## Por que Mermaid e não draw.io?

Mermaid é **texto** — versiona no Git, faz diff no PR, evita o
problema clássico de "o PNG no repo está desatualizado em relação
ao código". Para diagramas complexos demais (UML detalhado de
classes, por exemplo), draw.io faz sentido; para os 4 diagramas
deste projeto, Mermaid cobre.
