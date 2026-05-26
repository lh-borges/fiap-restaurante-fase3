#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gera o PDF docs/documentacao-arquitetura.pdf seguindo normas ABNT
para trabalhos academicos:
- Fonte Times New Roman 12pt
- Espacamento 1.5 entre linhas
- Margens: esquerda 3 cm, superior 3 cm, direita 2 cm, inferior 2 cm
- Recuo de primeira linha: 1.25 cm (1.25 cm = ~35 pt)
- Paginas numeradas no canto superior direito (a partir da introducao)
- Capa, folha de rosto, resumo, sumario automatico, capitulos, referencias

Como rodar:
    docker run --rm -v <repo>:/work -w /work/docs/build \
        python:3.12-slim sh -c "pip install -q reportlab && python gerar_documentacao_pdf.py"
"""

from datetime import datetime
from pathlib import Path

from reportlab.lib.colors import black, HexColor
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    Image,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)
from reportlab.platypus.tableofcontents import TableOfContents

# --------------------------------------------------------------------- #
# Constantes ABNT
# --------------------------------------------------------------------- #
PAGE_SIZE = A4  # 21 cm x 29.7 cm
MARGIN_LEFT = 3 * cm
MARGIN_RIGHT = 2 * cm
MARGIN_TOP = 3 * cm
MARGIN_BOTTOM = 2 * cm
FONT_BODY = "Times-Roman"
FONT_BODY_BOLD = "Times-Bold"
FONT_BODY_ITALIC = "Times-Italic"
FONT_SIZE_BODY = 12
LEADING_15 = FONT_SIZE_BODY * 1.5  # 18 pt para corresponder ao 1.5 entre linhas
INDENT_FIRST_LINE = 1.25 * cm  # 35.4 pt

# --------------------------------------------------------------------- #
# Estilos
# --------------------------------------------------------------------- #
def build_styles():
    styles = getSampleStyleSheet()

    body = ParagraphStyle(
        "ABNTBody",
        parent=styles["Normal"],
        fontName=FONT_BODY,
        fontSize=FONT_SIZE_BODY,
        leading=LEADING_15,
        firstLineIndent=INDENT_FIRST_LINE,
        alignment=TA_JUSTIFY,
        spaceAfter=0,
    )

    body_noindent = ParagraphStyle(
        "ABNTBodyNoIndent",
        parent=body,
        firstLineIndent=0,
    )

    bullet = ParagraphStyle(
        "ABNTBullet",
        parent=body,
        firstLineIndent=0,
        leftIndent=1.25 * cm,
        bulletIndent=0.4 * cm,
        spaceAfter=2,
    )

    h1 = ParagraphStyle(
        "ABNTH1",
        parent=styles["Heading1"],
        fontName=FONT_BODY_BOLD,
        fontSize=14,
        leading=18,
        textTransform="uppercase",
        spaceBefore=24,
        spaceAfter=12,
        alignment=TA_LEFT,
        keepWithNext=True,
    )

    h2 = ParagraphStyle(
        "ABNTH2",
        parent=styles["Heading2"],
        fontName=FONT_BODY_BOLD,
        fontSize=13,
        leading=16,
        spaceBefore=18,
        spaceAfter=10,
        alignment=TA_LEFT,
        keepWithNext=True,
    )

    h3 = ParagraphStyle(
        "ABNTH3",
        parent=styles["Heading3"],
        fontName=FONT_BODY_BOLD,
        fontSize=12,
        leading=15,
        spaceBefore=14,
        spaceAfter=8,
        alignment=TA_LEFT,
        keepWithNext=True,
    )

    capa_titulo = ParagraphStyle(
        "ABNTCapaTitulo",
        parent=styles["Title"],
        fontName=FONT_BODY_BOLD,
        fontSize=16,
        leading=22,
        alignment=TA_CENTER,
        spaceBefore=24,
    )

    capa_subtitulo = ParagraphStyle(
        "ABNTCapaSubtitulo",
        parent=styles["Title"],
        fontName=FONT_BODY,
        fontSize=14,
        leading=20,
        alignment=TA_CENTER,
        spaceBefore=12,
    )

    capa_inst = ParagraphStyle(
        "ABNTCapaInst",
        parent=styles["Normal"],
        fontName=FONT_BODY,
        fontSize=12,
        leading=18,
        alignment=TA_CENTER,
    )

    code = ParagraphStyle(
        "ABNTCode",
        parent=styles["Code"],
        fontName="Courier",
        fontSize=9,
        leading=12,
        leftIndent=1.0 * cm,
        rightIndent=1.0 * cm,
        spaceBefore=6,
        spaceAfter=6,
        textColor=HexColor("#222222"),
        backColor=HexColor("#F4F4F4"),
        borderPadding=4,
    )

    toc_h1 = ParagraphStyle(
        "ABNTToCH1",
        fontName=FONT_BODY_BOLD,
        fontSize=12,
        leading=18,
        leftIndent=0,
    )
    toc_h2 = ParagraphStyle(
        "ABNTToCH2",
        fontName=FONT_BODY,
        fontSize=11,
        leading=16,
        leftIndent=1 * cm,
    )
    toc_h3 = ParagraphStyle(
        "ABNTToCH3",
        fontName=FONT_BODY_ITALIC,
        fontSize=11,
        leading=15,
        leftIndent=2 * cm,
    )

    return {
        "body": body,
        "body_noindent": body_noindent,
        "bullet": bullet,
        "h1": h1,
        "h2": h2,
        "h3": h3,
        "capa_titulo": capa_titulo,
        "capa_subtitulo": capa_subtitulo,
        "capa_inst": capa_inst,
        "code": code,
        "toc": [toc_h1, toc_h2, toc_h3],
    }


# --------------------------------------------------------------------- #
# Document template com pre-textuais (capa, folha rosto, etc.)
# --------------------------------------------------------------------- #
class ABNTDocTemplate(BaseDocTemplate):
    """Doc template com numeracao a partir da introducao."""

    def __init__(self, filename, **kw):
        BaseDocTemplate.__init__(self, filename, **kw)
        frame = Frame(
            MARGIN_LEFT,
            MARGIN_BOTTOM,
            PAGE_SIZE[0] - MARGIN_LEFT - MARGIN_RIGHT,
            PAGE_SIZE[1] - MARGIN_TOP - MARGIN_BOTTOM,
            id="normal",
            leftPadding=0,
            rightPadding=0,
            topPadding=0,
            bottomPadding=0,
        )
        # paginas iniciais (capa, folha de rosto, resumo, sumario) sem numero
        self.addPageTemplates([
            PageTemplate(id="pre", frames=[frame], onPage=self._draw_no_page_number),
            PageTemplate(id="numbered", frames=[frame], onPage=self._draw_page_number),
        ])
        self.allowSplitting = 1
        self._page_offset = 0  # contagem da paginacao "real" inicia apos pretextuais

    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph):
            style = flowable.style.name
            text = flowable.getPlainText()
            if style == "ABNTH1":
                self.notify("TOCEntry", (0, text, self.page))
            elif style == "ABNTH2":
                self.notify("TOCEntry", (1, text, self.page))
            elif style == "ABNTH3":
                self.notify("TOCEntry", (2, text, self.page))

    @staticmethod
    def _draw_no_page_number(canvas, doc):
        pass

    @staticmethod
    def _draw_page_number(canvas, doc):
        canvas.saveState()
        canvas.setFont(FONT_BODY, 10)
        page_num = canvas.getPageNumber()
        canvas.drawRightString(
            PAGE_SIZE[0] - MARGIN_RIGHT, PAGE_SIZE[1] - 1.5 * cm, str(page_num)
        )
        canvas.restoreState()


# --------------------------------------------------------------------- #
# Helpers para construir o conteudo
# --------------------------------------------------------------------- #
def p(text, style):
    return Paragraph(text, style)


def bullets(items, style):
    return [Paragraph(f"• {item}", style) for item in items]


def figure(filename: str, caption: str, max_width_cm: float = 15.5):
    """Insere uma imagem com largura limitada e legenda no estilo ABNT.

    O caminho eh relativo a pasta docs/ — assumimos que o script roda em
    docs/build-pdf/, entao subimos um nivel para acessar diagramas/.
    """
    img_path = Path(__file__).resolve().parents[1] / filename
    img = Image(str(img_path))
    # Escala proporcional ao max_width
    aspect = img.imageHeight / img.imageWidth
    img.drawWidth = max_width_cm * cm
    img.drawHeight = img.drawWidth * aspect
    caption_style = ParagraphStyle(
        "FigureCaption",
        fontName=FONT_BODY_ITALIC,
        fontSize=10,
        leading=12,
        alignment=TA_CENTER,
        spaceBefore=4,
        spaceAfter=12,
    )
    return KeepTogether([
        img,
        Paragraph(caption, caption_style),
    ])


def table_simple(data, col_widths=None):
    """Tabela basica em estilo academico (cabecalho cinza, bordas finas)."""
    t = Table(data, colWidths=col_widths, hAlign="LEFT", repeatRows=1)
    t.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), HexColor("#E0E0E0")),
                ("FONTNAME", (0, 0), (-1, 0), FONT_BODY_BOLD),
                ("FONTNAME", (0, 1), (-1, -1), FONT_BODY),
                ("FONTSIZE", (0, 0), (-1, -1), 10),
                ("LEADING", (0, 0), (-1, -1), 13),
                ("GRID", (0, 0), (-1, -1), 0.4, black),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 3),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
            ]
        )
    )
    return t


# --------------------------------------------------------------------- #
# Construcao do PDF
# --------------------------------------------------------------------- #
def build(filename: str):
    styles = build_styles()
    body = styles["body"]
    noindent = styles["body_noindent"]
    bullet = styles["bullet"]
    h1 = styles["h1"]
    h2 = styles["h2"]
    h3 = styles["h3"]
    code = styles["code"]

    story = []

    # ------------------------------------------------------------- #
    # CAPA
    # ------------------------------------------------------------- #
    story.append(Spacer(1, 3 * cm))
    story.append(p("PÓS-GRADUAÇÃO FIAP — POSTECH", styles["capa_inst"]))
    story.append(Spacer(1, 0.5 * cm))
    story.append(p("ARQUITETURA E DESENVOLVIMENTO DE SISTEMAS DE SOFTWARE", styles["capa_inst"]))
    story.append(Spacer(1, 5 * cm))
    story.append(p("FIAP RESTAURANTE — FASE 3", styles["capa_titulo"]))
    story.append(p(
        "Documentação de arquitetura do sistema de pedidos on-line para restaurante",
        styles["capa_subtitulo"],
    ))
    story.append(Spacer(1, 5 * cm))
    autores = [
        "Danilo Fernando de Paula e Silva",
        "Gilmar da Costa Moraes Junior",
        "Juliana Maria Dal Olio Braz",
        "Luis Henrique Silveira Borges",
        "Thiago de Jesus Cordeiro",
    ]
    for autor in autores:
        story.append(p(autor, styles["capa_inst"]))
    story.append(Spacer(1, 0.5 * cm))
    _meses_pt = {
        1: "Janeiro", 2: "Fevereiro", 3: "Março", 4: "Abril",
        5: "Maio", 6: "Junho", 7: "Julho", 8: "Agosto",
        9: "Setembro", 10: "Outubro", 11: "Novembro", 12: "Dezembro",
    }
    _now = datetime.now()
    today = f"{_meses_pt[_now.month]} de {_now.year}"
    story.append(p(f"São Paulo — {today}", styles["capa_inst"]))
    story.append(PageBreak())

    # ------------------------------------------------------------- #
    # FOLHA DE ROSTO
    # ------------------------------------------------------------- #
    story.append(Spacer(1, 2 * cm))
    for autor in autores:
        story.append(p(autor, styles["capa_inst"]))
    story.append(Spacer(1, 2 * cm))
    story.append(p("FIAP RESTAURANTE — FASE 3", styles["capa_titulo"]))
    story.append(p(
        "Documentação de arquitetura do sistema de pedidos on-line para restaurante",
        styles["capa_subtitulo"],
    ))
    story.append(Spacer(1, 3 * cm))
    nota_apresentacao = (
        "Documentação técnica apresentada ao programa de Pós-Graduação "
        "FIAP — PosTech como parte das entregas do Tech Challenge da "
        "Fase 3. O sistema descrito implementa um conjunto de "
        "microsserviços para gerenciamento de pedidos, integração com "
        "gateway externo de pagamento e fluxo de produção da cozinha."
    )
    nota_style = ParagraphStyle(
        "Nota",
        parent=body,
        firstLineIndent=0,
        leftIndent=7 * cm,
        alignment=TA_JUSTIFY,
        fontSize=11,
        leading=16,
    )
    story.append(p(nota_apresentacao, nota_style))
    story.append(Spacer(1, 4 * cm))
    story.append(p(f"São Paulo — {today}", styles["capa_inst"]))
    story.append(PageBreak())

    # ------------------------------------------------------------- #
    # ACESSO AO CODIGO E AO VIDEO (elemento pre-textual)
    # ------------------------------------------------------------- #
    # URLs centralizadas aqui para facilitar manutencao: trocar
    # YOUTUBE_URL para a string da URL apos a gravacao e regerar o PDF.
    GITHUB_URL = "https://github.com/lh-borges/fiap-restaurante-fase3"
    YOUTUBE_URL = None  # ex.: "https://youtu.be/XXXXXXXXXXX" apos a publicacao

    story.append(Spacer(1, 4 * cm))
    story.append(p("ACESSO AO CÓDIGO E AO VÍDEO", h1))
    story.append(Spacer(1, 0.6 * cm))

    link_label_style = ParagraphStyle(
        "LinkLabel",
        parent=body,
        firstLineIndent=0,
        fontName=FONT_BODY_BOLD,
        fontSize=13,
        leading=18,
        spaceBefore=16,
        spaceAfter=4,
    )
    link_url_style = ParagraphStyle(
        "LinkUrl",
        parent=body,
        firstLineIndent=0,
        fontName="Courier",
        fontSize=12,
        leading=16,
        textColor=HexColor("#0645AD"),
        spaceAfter=8,
    )
    link_desc_style = ParagraphStyle(
        "LinkDesc",
        parent=body,
        firstLineIndent=0,
        fontSize=11,
        leading=15,
        textColor=HexColor("#555555"),
        spaceAfter=14,
    )

    # Repositorio
    story.append(p("Repositório no GitHub", link_label_style))
    story.append(p(
        f'<link href="{GITHUB_URL}"><u>{GITHUB_URL}</u></link>',
        link_url_style,
    ))
    story.append(p(
        "Código-fonte completo, histórico de PRs, coleção Postman, "
        "ADRs e diagramas. Clone o repositório e execute "
        "<font face='Courier'>docker compose up -d --build</font> "
        "na raiz para subir o sistema inteiro com um único comando.",
        link_desc_style,
    ))

    # Video
    story.append(p("Vídeo de apresentação no YouTube", link_label_style))
    if YOUTUBE_URL:
        story.append(p(
            f'<link href="{YOUTUBE_URL}"><u>{YOUTUBE_URL}</u></link>',
            link_url_style,
        ))
        story.append(p(
            "Apresentação técnica de até 10 minutos cobrindo o fluxo "
            "completo do sistema, demonstração de resiliência e as "
            "decisões arquiteturais. Roteiro detalhado em "
            "<font face='Courier'>docs/roteiro-video.md</font>.",
            link_desc_style,
        ))
    else:
        story.append(p(
            "<i>Aguardando publicação após a gravação.</i>",
            ParagraphStyle("Aguardando", parent=link_url_style,
                           textColor=HexColor("#888888"),
                           fontName=FONT_BODY_ITALIC),
        ))
        story.append(p(
            "O vídeo de apresentação técnica (até 10 minutos) está "
            "em fase de gravação. O roteiro detalhado, com falas, "
            "tempos e comandos exatos, está disponível em "
            "<font face='Courier'>docs/roteiro-video.md</font> e "
            "cobre os 7 blocos da apresentação. A URL desta seção "
            "será atualizada após a publicação no YouTube.",
            link_desc_style,
        ))

    story.append(PageBreak())

    # ------------------------------------------------------------- #
    # RESUMO
    # ------------------------------------------------------------- #
    story.append(p("RESUMO", h1))
    story.append(p(
        "Este documento descreve a arquitetura de um sistema "
        "distribuído desenvolvido como entrega do Tech Challenge da "
        "Fase 3 do programa de Pós-Graduação FIAP — PosTech. O sistema "
        "implementa o fluxo on-line de pedidos para um restaurante, "
        "incluindo cadastro e autenticação de clientes, criação e "
        "confirmação de pedidos, comunicação com um gateway externo "
        "eventualmente disponível para processamento de pagamentos, "
        "tratamento de falhas com resiliência e propagação do pedido "
        "para a cozinha após a aprovação do pagamento. A solução é "
        "composta de quatro microsserviços Spring Boot 4 / Java 25, "
        "comunicando-se de forma síncrona via gRPC e GraphQL e "
        "assíncrona via Apache Kafka, com persistência em MySQL 8.4 "
        "isolada por bounded context. A resiliência da chamada ao "
        "gateway é provida pela biblioteca Resilience4j (Circuit "
        "Breaker, Retry, Timeout e Fallback), e a recuperação "
        "automática é garantida por um worker agendado. Toda a "
        "orquestração local é provida por Docker Compose, permitindo "
        "execução do sistema completo com um único comando.",
        body,
    ))
    story.append(Spacer(1, 0.3 * cm))
    story.append(p(
        "<b>Palavras-chave:</b> microsserviços; arquitetura hexagonal; "
        "Apache Kafka; Resilience4j; Spring Boot; GraphQL; Docker; JWT.",
        noindent,
    ))
    story.append(PageBreak())

    # ------------------------------------------------------------- #
    # SUMARIO
    # ------------------------------------------------------------- #
    story.append(p("SUMÁRIO", h1))
    toc = TableOfContents()
    toc.levelStyles = styles["toc"]
    story.append(toc)
    story.append(PageBreak())

    # ------------------------------------------------------------- #
    # 1. INTRODUCAO (a partir daqui, paginas numeradas)
    # ------------------------------------------------------------- #
    from reportlab.platypus.doctemplate import NextPageTemplate

    story.append(NextPageTemplate("numbered"))
    story.append(PageBreak())  # trigger template switch

    story.append(p("1 INTRODUÇÃO", h1))
    story.append(p(
        "Os sistemas de pedidos on-line para restaurantes evoluíram "
        "rapidamente nos últimos anos, pressionados pelo crescimento "
        "do delivery e pela necessidade de integração com gateways "
        "externos de pagamento, plataformas de logística e displays "
        "operacionais nas cozinhas. Esses sistemas precisam ser "
        "<b>resilientes</b> (toleram a indisponibilidade temporária "
        "de serviços de terceiros), <b>escaláveis</b> (suportam picos "
        "como horário de almoço sem degradar a experiência) e "
        "<b>seguros</b> (proteção dos dados do cliente e dos "
        "pagamentos).",
        body,
    ))
    story.append(p(
        "Este documento apresenta a arquitetura do <b>FIAP Restaurante</b>, "
        "um sistema distribuído desenvolvido como Tech Challenge da "
        "Fase 3 do programa de Pós-Graduação FIAP — PosTech. O sistema "
        "cobre o ciclo completo de um pedido: cadastro do cliente, "
        "autenticação, criação e confirmação do pedido, comunicação "
        "com um gateway externo eventualmente disponível, tratamento "
        "de falhas, e propagação do pedido para a cozinha até sua "
        "finalização.",
        body,
    ))
    story.append(p(
        "A solução é composta de quatro microsserviços Spring Boot 4 / "
        "Java 25, conectados por um broker Apache Kafka em modo KRaft "
        "e persistindo em databases isolados sobre uma instância MySQL "
        "8.4. Toda a infraestrutura é descrita em um único arquivo "
        "<b>docker-compose.yml</b>, que sobe o sistema completo "
        "(oito containers) com um único comando.",
        body,
    ))
    story.append(p(
        "Este documento detalha a arquitetura, as decisões técnicas "
        "que a guiaram, os mecanismos de resiliência implementados, "
        "a estratégia de testes adotada e os procedimentos de "
        "operação. Decisões pontuais estão também registradas em "
        "ADRs (Architecture Decision Records) na pasta "
        "<font face='Courier'>docs/adr/</font> do repositório.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 2. VISAO GERAL DO SISTEMA
    # ------------------------------------------------------------- #
    story.append(p("2 VISÃO GERAL DO SISTEMA", h1))

    story.append(p("2.1 Domínio do problema", h2))
    story.append(p(
        "O sistema modela quatro <b>bounded contexts</b> da operação "
        "de um restaurante on-line:",
        body,
    ))
    story.extend(bullets([
        "<b>Identidade do cliente:</b> cadastro, autenticação e emissão de tokens JWT.",
        "<b>Pedido:</b> criação, cálculo de valor total, confirmação, consulta e atualização de status conforme eventos externos.",
        "<b>Pagamento:</b> processamento da cobrança contra um gateway externo eventualmente disponível, com tratamento de falhas e reprocessamento automático.",
        "<b>Cozinha (produção):</b> fila de pedidos aprovados, com transições de estado disparadas pelo dono do restaurante (recebido, em preparo, pronto).",
    ], bullet))
    story.append(Spacer(1, 0.2 * cm))
    story.append(p(
        "Cada um desses contextos foi materializado em um "
        "microsserviço independente, com modelo de dados próprio e "
        "fronteiras explícitas. A comunicação entre eles privilegia "
        "eventos assíncronos via Apache Kafka, recorrendo a "
        "chamadas síncronas (gRPC) apenas quando a operação exige.",
        body,
    ))

    story.append(p("2.2 Requisitos atendidos", h2))
    story.append(p(
        "Os requisitos formais da Fase 3 estão sumarizados a seguir, "
        "com indicação do componente responsável por cada um:",
        body,
    ))
    req_data = [
        ["Req.", "Descrição", "Componente"],
        ["4.1", "Cadastro e autenticação de clientes", "usuario-autenticacao"],
        ["4.2", "Criação e confirmação de pedido", "restaurante-pedido"],
        ["4.3", "Consulta de pedido por ID e listagem por cliente", "restaurante-pedido"],
        ["4.4", "Processamento de pagamento via gateway externo", "pagamento-service + procpag"],
        ["4.5", "Marcação de pagamento pendente em falha", "pagamento-service (Resilience4j)"],
        ["4.6", "Reprocessamento automático ao restabelecimento", "pagamento-service (@Scheduled)"],
        ["4.7", "Atualização automática do status do pedido", "restaurante-pedido (consumer)"],
        ["5.1", "Arquitetura em múltiplos serviços", "4 microsserviços"],
        ["5.2", "Spring Security + JWT (RS256)", "todos os serviços de aplicação"],
        ["5.3", "Comunicação assíncrona via Kafka", "6 tópicos"],
        ["5.4", "Resiliência (CB + Retry + Timeout + Fallback)", "pagamento-service.ExternalPaymentClient"],
        ["5.5", "Clean / Hexagonal Architecture", "todos os 4 módulos"],
    ]
    story.append(table_simple(req_data, col_widths=[1.0 * cm, 8.5 * cm, 6.5 * cm]))
    story.append(Spacer(1, 0.4 * cm))
    story.append(p(
        "Além do mínimo exigido, foi implementado o módulo opcional "
        "<b>restaurante-service</b> (item 5.1 da spec), responsável "
        "pelo bounded context da cozinha.",
        body,
    ))

    story.append(p("2.3 Componentes externos", h2))
    story.append(p(
        "O sistema integra-se a um gateway externo de pagamento "
        "denominado <b>procpag</b>, fornecido pelos professores como "
        "uma imagem Docker (<font face='Courier'>docker.io/erickemprobr/procpag</font>). "
        "Esse serviço é deliberadamente instável: ora aprova, ora "
        "responde com erro ou timeout, simulando um gateway real "
        "eventualmente disponível. Toda a estratégia de resiliência "
        "da solução é desenhada em torno dessa imprevisibilidade.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 3. ARQUITETURA
    # ------------------------------------------------------------- #
    story.append(p("3 ARQUITETURA", h1))

    story.append(p("3.1 Visão de componentes", h2))
    story.append(p(
        "A arquitetura é composta de oito containers em rede privada "
        "Docker. Quatro deles são as aplicações Spring Boot (uma por "
        "bounded context), três são peças de infraestrutura (MySQL, "
        "Kafka e Kafka UI) e o oitavo é o gateway externo procpag.",
        body,
    ))
    story.append(p(
        "A arquitetura está documentada em três níveis complementares, "
        "seguindo o <b>modelo C4</b> de Simon Brown:",
        body,
    ))
    story.extend(bullets([
        "<b>C4 nível 1 — Contexto</b> (Figura 1): o sistema visto de fora; atores e sistemas externos.",
        "<b>C4 nível 2 — Containers</b> (Figura 2): a topologia técnica interna; aplicações Spring, MySQL, Kafka, com protocolos e tecnologias.",
        "<b>Diagrama de componentes</b> (Figura 3): visão de componentes lógicos com o detalhamento dos eventos Kafka.",
    ], bullet))
    story.append(Spacer(1, 0.3 * cm))

    story.append(figure(
        "diagramas/c4-contexto.png",
        "Figura 1 — Diagrama de Contexto (C4 nível 1).",
        max_width_cm=14,
    ))
    story.append(figure(
        "diagramas/c4-containers.png",
        "Figura 2 — Diagrama de Containers (C4 nível 2).",
    ))
    story.append(figure(
        "diagramas/componentes.png",
        "Figura 3 — Diagrama de componentes (visão lógica complementar).",
    ))

    story.append(p("3.2 Microsserviços", h2))
    story.append(p(
        "Cada microsserviço expõe sua API pública via GraphQL "
        "(Spring GraphQL), com GraphiQL embutido na URL "
        "<font face='Courier'>/graphiql</font>. Schemas residem "
        "em <font face='Courier'>src/main/resources/graphql/schema.graphqls</font> "
        "de cada módulo.",
        body,
    ))

    story.append(p("3.2.1 usuario-autenticacao", h3))
    story.append(p(
        "Responsável por cadastro de usuários, autenticação, "
        "emissão de JWT (RS256) e consulta interna via gRPC. "
        "Persiste no database <font face='Courier'>auth_db</font>. "
        "Não consome nem publica eventos Kafka. Expõe servidor "
        "gRPC na porta interna 9000 para os demais serviços "
        "consultarem dados de usuário de forma síncrona.",
        body,
    ))

    story.append(p("3.2.2 restaurante-pedido", h3))
    story.append(p(
        "Responsável pelo ciclo de vida do pedido: criação, "
        "cálculo de valor total, confirmação, consulta por ID, "
        "listagem por cliente autenticado e atualização "
        "automática de status conforme eventos vindos do "
        "pagamento e da cozinha. Persiste no database "
        "<font face='Courier'>pedido_db</font>. É o componente que "
        "publica os eventos <font face='Courier'>pedido.criado</font> "
        "(disparando o pagamento) e "
        "<font face='Courier'>pedido.pronto-para-cozinha</font> "
        "(após confirmação do pagamento).",
        body,
    ))

    story.append(p("3.2.3 pagamento-service", h3))
    story.append(p(
        "Consome o evento <font face='Courier'>pedido.criado</font> "
        "e tenta processar a cobrança contra o gateway externo "
        "procpag. Aplica Resilience4j (Circuit Breaker, Retry, "
        "Timeout, Fallback). Em caso de falha, marca o pagamento "
        "como pendente e publica "
        "<font face='Courier'>pagamento.pendente</font>; em caso de "
        "sucesso, publica <font face='Courier'>pagamento.aprovado</font>. "
        "Hospeda também o worker <font face='Courier'>@Scheduled</font> "
        "que, a cada 30 segundos, drena os pagamentos pendentes "
        "tentando novamente a chamada ao gateway. Persiste no "
        "database <font face='Courier'>pagamento_db</font>.",
        body,
    ))

    story.append(p("3.2.4 restaurante-service", h3))
    story.append(p(
        "Modela o bounded context da cozinha. Consome "
        "<font face='Courier'>pedido.pronto-para-cozinha</font> e "
        "cria um agregado <font face='Courier'>PedidoCozinha</font> "
        "no estado RECEBIDO. O dono do restaurante (perfil "
        "DONO_RESTAURANTE no JWT) avança o estado via mutations "
        "GraphQL: <font face='Courier'>iniciarPreparo</font> "
        "(RECEBIDO → EM_PREPARO) e "
        "<font face='Courier'>marcarComoPronto</font> "
        "(EM_PREPARO → PRONTO). Cada transição publica um evento "
        "(<font face='Courier'>pedido.em-preparo</font> ou "
        "<font face='Courier'>pedido.pronto</font>) consumido pelo "
        "<font face='Courier'>restaurante-pedido</font> para "
        "refletir o status no agregado principal. Persiste no "
        "database <font face='Courier'>cozinha_db</font>.",
        body,
    ))

    story.append(p("3.3 Infraestrutura", h2))

    story.append(p("3.3.1 Apache Kafka em modo KRaft", h3))
    story.append(p(
        "O broker roda em <b>modo KRaft</b> (Kafka Raft, GA desde "
        "Kafka 3.3), eliminando a dependência de Zookeeper. Em "
        "configuração <b>single-node</b> apropriada para "
        "desenvolvimento e demonstração, com replication factor "
        "1 nos tópicos internos. O ADR 0006 detalha a decisão.",
        body,
    ))
    story.append(p(
        "São <b>seis tópicos</b> orquestrando o fluxo:",
        body,
    ))
    topicos_data = [
        ["Tópico", "Publicador", "Consumidor(es)"],
        ["pedido.criado", "restaurante-pedido", "pagamento-service"],
        ["pagamento.aprovado", "pagamento-service", "restaurante-pedido"],
        ["pagamento.pendente", "pagamento-service", "restaurante-pedido"],
        ["pedido.pronto-para-cozinha", "restaurante-pedido", "restaurante-service"],
        ["pedido.em-preparo", "restaurante-service", "restaurante-pedido"],
        ["pedido.pronto", "restaurante-service", "restaurante-pedido"],
    ]
    story.append(table_simple(topicos_data, col_widths=[6 * cm, 5 * cm, 5 * cm]))
    story.append(Spacer(1, 0.4 * cm))
    story.append(p(
        "A chave de cada mensagem é o "
        "<font face='Courier'>pedidoId.toString()</font>, garantindo "
        "que todos os eventos do mesmo pedido caem na mesma "
        "partição e preservam ordem por agregado.",
        body,
    ))

    story.append(p("3.3.2 MySQL", h3))
    story.append(p(
        "Uma única instância MySQL 8.4 hospeda <b>quatro databases</b> "
        "isolados, um por microsserviço — abordagem "
        "<i>database per service</i>. O script "
        "<font face='Courier'>init.sql</font> cria os quatro databases "
        "na primeira inicialização do container. O ADR 0007 detalha "
        "a decisão de manter os bancos isolados, mas na mesma "
        "instância MySQL.",
        body,
    ))
    story.append(p(
        "O MySQL recebe tuning explícito no docker-compose:",
        body,
    ))
    story.extend(bullets([
        "<font face='Courier'>innodb-buffer-pool-size=512M</font> (default é 128 MB)",
        "<font face='Courier'>innodb-redo-log-capacity=128M</font>",
        "<font face='Courier'>innodb-flush-log-at-trx-commit=2</font> (latência menor; tolerância de até 1 segundo perdido em crash do host)",
        "<font face='Courier'>innodb-flush-method=O_DIRECT</font> (evita double-buffering)",
        "<font face='Courier'>skip-name-resolve</font> (sem reverse DNS por conexão)",
    ], bullet))

    story.append(p("3.3.3 procpag", h3))
    story.append(p(
        "Gateway de pagamento externo fornecido pelos professores "
        "(imagem <font face='Courier'>docker.io/erickemprobr/procpag</font>). "
        "Simula um serviço eventualmente disponível, alternando "
        "respostas de aprovação com erros e timeouts. Toda a "
        "estratégia de resiliência (capítulo 5) gira em torno "
        "dessa imprevisibilidade controlada.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 4. FLUXO PRINCIPAL
    # ------------------------------------------------------------- #
    story.append(p("4 FLUXO PRINCIPAL", h1))
    story.append(p(
        "O fluxo feliz (sem falhas) do sistema percorre, em "
        "ordem, os passos descritos a seguir. A Figura 4 apresenta "
        "a mesma narrativa em forma gráfica como diagrama de "
        "sequência. A fonte Mermaid está em "
        "<font face='Courier'>docs/diagramas/sequencia-happy-path.md</font>.",
        body,
    ))
    story.append(figure(
        "diagramas/sequencia-happy-path.png",
        "Figura 4 — Sequência do fluxo feliz (cadastro → entrega pela cozinha).",
    ))
    story.append(p(
        "<b>1.</b> O cliente envia <font face='Courier'>mutation login</font> "
        "ao <i>usuario-autenticacao</i>, que valida credenciais e "
        "devolve um JWT RS256 contendo nas claims o "
        "<font face='Courier'>subject</font> (UUID do cliente) e a "
        "claim <font face='Courier'>groups</font> com seu perfil.",
        body,
    ))
    story.append(p(
        "<b>2.</b> Com o token no header <font face='Courier'>Authorization: Bearer "
        "&lt;jwt&gt;</font>, o cliente envia "
        "<font face='Courier'>mutation criarPedido</font> ao "
        "<i>restaurante-pedido</i>. O serviço calcula o valor "
        "total a partir dos itens, persiste o pedido no status "
        "<b>CRIADO</b> e devolve o agregado completo. O "
        "<font face='Courier'>clienteId</font> é sempre extraído "
        "do JWT, nunca recebido do cliente (req. 5.2).",
        body,
    ))
    story.append(p(
        "<b>3.</b> O cliente envia "
        "<font face='Courier'>mutation confirmarPedido</font>. O "
        "status passa para <b>CONFIRMADO</b> e o evento "
        "<font face='Courier'>pedido.criado</font> é publicado no "
        "Kafka. O cliente recebe a resposta em milissegundos.",
        body,
    ))
    story.append(p(
        "<b>4.</b> O <i>pagamento-service</i> consome "
        "<font face='Courier'>pedido.criado</font>, persiste um "
        "registro de pagamento e chama o procpag via HTTP. A "
        "chamada é envolvida por Resilience4j (CB + Retry + "
        "Timeout). No caminho feliz, o procpag responde 200 OK.",
        body,
    ))
    story.append(p(
        "<b>5.</b> O <i>pagamento-service</i> persiste o pagamento como "
        "APROVADO e publica <font face='Courier'>pagamento.aprovado</font>.",
        body,
    ))
    story.append(p(
        "<b>6.</b> O <i>restaurante-pedido</i> consome o evento, "
        "transita o pedido para <b>PAGO</b> e publica "
        "<font face='Courier'>pedido.pronto-para-cozinha</font> com "
        "os itens (sem preço — irrelevante para a cozinha).",
        body,
    ))
    story.append(p(
        "<b>7.</b> O <i>restaurante-service</i> consome "
        "<font face='Courier'>pedido.pronto-para-cozinha</font> e "
        "cria o agregado <font face='Courier'>PedidoCozinha</font> "
        "no estado <b>RECEBIDO</b>.",
        body,
    ))
    story.append(p(
        "<b>8.</b> O dono do restaurante consulta a fila via "
        "<font face='Courier'>query filaCozinha</font> em "
        "<font face='Courier'>:8084/graphql</font> e aciona "
        "<font face='Courier'>mutation iniciarPreparo</font>. O "
        "agregado passa a <b>EM_PREPARO</b>; o evento "
        "<font face='Courier'>pedido.em-preparo</font> é publicado.",
        body,
    ))
    story.append(p(
        "<b>9.</b> O <i>restaurante-pedido</i> consome o evento e "
        "atualiza o pedido principal para <b>EM_PREPARO</b>. "
        "Quando o preparo termina, o dono aciona "
        "<font face='Courier'>marcarComoPronto</font>, o evento "
        "<font face='Courier'>pedido.pronto</font> propaga e o "
        "ciclo se fecha com o pedido em <b>PRONTO</b>.",
        body,
    ))

    story.append(p("4.1 Máquina de estados do agregado Pedido", h2))
    story.append(p(
        "Todas as transições descritas acima são reguladas pela "
        "máquina de estados implementada no agregado "
        "<font face='Courier'>Pedido</font> do módulo "
        "<i>restaurante-pedido</i>. A Figura 5 mostra os estados "
        "válidos e as transições permitidas; estados terminais "
        "(<b>PRONTO</b> e <b>CANCELADO</b>) não admitem saída. "
        "Todas as transições disparadas por eventos Kafka são "
        "<b>idempotentes</b> — receber o mesmo evento duas vezes "
        "não causa efeito colateral.",
        body,
    ))
    story.append(figure(
        "diagramas/maquina-estados-pedido.png",
        "Figura 5 — Máquina de estados do agregado Pedido.",
        max_width_cm=13,
    ))

    # ------------------------------------------------------------- #
    # 5. RESILIENCIA
    # ------------------------------------------------------------- #
    story.append(p("5 RESILIÊNCIA E TOLERÂNCIA A FALHAS", h1))
    story.append(p(
        "A resiliência do sistema concentra-se na integração com o "
        "gateway externo procpag, ponto único de instabilidade "
        "previsível. Toda a estratégia foi implementada com "
        "<b>Resilience4j 2.3.0</b> via anotações no método de "
        "integração HTTP, e complementada por um worker agendado "
        "para reprocessamento automático. A Figura 6 ilustra o "
        "fluxo completo de falha e recuperação.",
        body,
    ))
    story.append(figure(
        "diagramas/sequencia-resiliencia.png",
        "Figura 6 — Sequência de falha do gateway + reprocessamento automático.",
    ))

    story.append(p("5.1 Padrões aplicados", h2))
    story.append(p(
        "Os quatro padrões clássicos de resiliência foram "
        "aplicados em camadas:",
        body,
    ))
    story.extend(bullets([
        "<b>Retry</b> — três tentativas com backoff exponencial (2s, 4s, 8s).",
        "<b>Timeout</b> — cinco segundos por chamada; chamadas mais lentas são interrompidas.",
        "<b>Circuit Breaker</b> — abre após 50% de falhas em uma janela deslizante de 5 chamadas. Quando aberto, novas chamadas falham imediatamente sem tocar o gateway. Após 30 segundos, transita para HALF_OPEN e testa 2 chamadas; se passarem, fecha.",
        "<b>Fallback</b> — quando todas as estratégias acima falham, o pagamento é marcado como <b>PENDENTE</b>, e o evento <font face='Courier'>pagamento.pendente</font> é publicado no Kafka. O pedido nunca falha do ponto de vista do cliente.",
    ], bullet))

    story.append(p("5.2 Worker de reprocessamento", h2))
    story.append(p(
        "Vive exclusivamente no módulo <i>pagamento-service</i>. É um "
        "<font face='Courier'>@Scheduled</font> que executa a "
        "cada 30 segundos (configurável em "
        "<font face='Courier'>pagamento.reprocess.fixed-delay-ms</font>), "
        "busca um lote de até 20 pagamentos no estado PENDENTE e "
        "retenta cada um deles contra o procpag. Quando o gateway "
        "voltar e a chamada for bem-sucedida, o pagamento é "
        "marcado APROVADO e <font face='Courier'>pagamento.aprovado</font> "
        "é publicado — fechando o ciclo de recuperação.",
        body,
    ))
    story.append(p(
        "<b>Por que o worker está no pagamento-service, e não no "
        "restaurante-pedido?</b> O worker opera sobre dados do "
        "próprio bounded context (registros de pagamento). "
        "Reprocessar significa tentar de novo a chamada ao "
        "gateway, integração que vive no pagamento-service e em mais "
        "lugar nenhum. Manter o worker fora do "
        "<i>restaurante-pedido</i> preserva a separação de "
        "responsabilidades: o <i>restaurante-pedido</i> apenas "
        "reage a eventos.",
        body,
    ))

    story.append(p("5.3 Garantias e limitações", h2))
    story.append(p(
        "O sistema garante <b>at-least-once</b> na entrega de "
        "eventos Kafka (o consumer pode receber o mesmo evento "
        "mais de uma vez em cenários de reprocessamento). As "
        "transições de estado nas entidades de domínio são "
        "<b>idempotentes</b>: marcar um pedido já PAGO como PAGO "
        "novamente não causa efeito. Isso é exercitado nos "
        "testes unitários do agregado <font face='Courier'>Pedido</font>.",
        body,
    ))
    story.append(p(
        "Não há, no escopo atual, mecanismo de <b>outbox</b> ou "
        "transações distribuídas entre o commit do banco local e "
        "a publicação no Kafka. Em cenário de crash entre as duas "
        "operações, há risco teórico de inconsistência (o pedido "
        "vira PAGO no banco mas o evento não é publicado). Para "
        "o escopo do projeto, esse risco é considerado aceitável.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 6. SEGURANCA
    # ------------------------------------------------------------- #
    story.append(p("6 SEGURANÇA", h1))

    story.append(p("6.1 Autenticação e autorização", h2))
    story.append(p(
        "A autenticação é baseada em <b>JWT RS256</b> emitido "
        "pelo <i>usuario-autenticacao</i>. A chave privada RSA "
        "vive apenas neste serviço; a chave pública é distribuída "
        "no classpath dos demais (<font face='Courier'>"
        "src/main/resources/keys/publicKey.pem</font>), permitindo "
        "validação local sem necessidade de chamar o "
        "<i>usuario-autenticacao</i> a cada requisição. O ADR "
        "0008 detalha a escolha pela assinatura assimétrica.",
        body,
    ))
    story.append(p(
        "A autorização usa <b>perfis</b> codificados na claim "
        "<font face='Courier'>groups</font> do JWT: USUARIO "
        "(cliente final) e DONO_RESTAURANTE (administrador da "
        "fila da cozinha). Cada resolver GraphQL é protegido por "
        "<font face='Courier'>@PreAuthorize</font>, com regras "
        "específicas para cada operação.",
        body,
    ))

    story.append(p("6.2 Ownership check em pedidos", h2))
    story.append(p(
        "Na query <font face='Courier'>pedidoPorId</font>, o "
        "serviço extrai o <font face='Courier'>clienteId</font> do "
        "JWT e compara com o do pedido buscado. Pedidos de outros "
        "clientes retornam <font face='Courier'>null</font> — a "
        "mesma resposta de 'não encontrado', para não vazar a "
        "existência do recurso. Esta verificação foi adicionada "
        "como melhoria de segurança identificada em auditoria de "
        "conformidade.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 7. DECISOES ARQUITETURAIS
    # ------------------------------------------------------------- #
    story.append(p("7 DECISÕES ARQUITETURAIS", h1))
    story.append(p(
        "Cada decisão técnica significativa do projeto está "
        "registrada como ADR (Architecture Decision Record) na "
        "pasta <font face='Courier'>docs/adr/</font> do "
        "repositório, no formato proposto por Michael Nygard "
        "(Contexto + Decisão + Consequências). O índice completo "
        "está em <font face='Courier'>docs/adr/README.md</font>.",
        body,
    ))
    story.append(p(
        "São 13 ADRs vigentes:",
        body,
    ))
    adr_data = [
        ["ADR", "Decisão", "Status"],
        ["0001", "Adoção de microsserviços em vez de monolito", "Accepted"],
        ["0002", "Arquitetura hexagonal em cada microsserviço", "Accepted"],
        ["0003", "GraphQL no ponto de entrada das aplicações", "Accepted"],
        ["0004", "gRPC para chamadas síncronas entre serviços", "Accepted"],
        ["0005", "Apache Kafka para comunicação assíncrona", "Accepted"],
        ["0006", "Kafka em modo KRaft (sem Zookeeper)", "Accepted"],
        ["0007", "Database separado por serviço (mesmo MySQL)", "Accepted"],
        ["0008", "JWT com assinatura assimétrica RS256", "Accepted"],
        ["0009", "Resilience4j para resiliência na integração", "Accepted"],
        ["0010", "Bounded context separado para a cozinha", "Accepted"],
        ["0011", "Docker Compose como orquestrador", "Accepted"],
        ["0012", "AppCDS + Layered JARs no Dockerfile", "Accepted"],
        ["0013", "Resource limits explícitos no compose", "Accepted"],
    ]
    story.append(table_simple(adr_data, col_widths=[1.5 * cm, 12 * cm, 2.5 * cm]))
    story.append(Spacer(1, 0.4 * cm))
    story.append(p(
        "Os ADRs descrevem não apenas a decisão tomada mas também "
        "as <b>alternativas consideradas e descartadas</b>, "
        "preservando o raciocínio que conduziu à escolha. Quando "
        "uma decisão for revisitada no futuro, o ADR existente "
        "receberá status <i>Superseded by ADR-XXXX</i> e um novo "
        "ADR será criado.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 8. TESTES
    # ------------------------------------------------------------- #
    story.append(p("8 ESTRATÉGIA DE TESTES", h1))
    story.append(p(
        "A suíte de testes do projeto totaliza <b>285 testes "
        "automatizados</b> distribuídos pelos quatro módulos, "
        "executando em aproximadamente três minutos. Toda a "
        "suíte roda sem dependências externas — utiliza H2 "
        "em memória no lugar do MySQL, "
        "<font face='Courier'>@EmbeddedKafka</font> no lugar do "
        "broker real e mocks para integrações HTTP.",
        body,
    ))
    testes_data = [
        ["Módulo", "Testes", "Cobertura"],
        ["usuario-autenticacao", "63", "Domain, use cases, JWT, gRPC, GraphQL, smoke"],
        ["restaurante-pedido", "126", "Domain, 4 consumers, 2 publishers, GraphQL, smoke"],
        ["pagamento-service", "57", "Domain, Resilience4j, worker, GraphQL, smoke"],
        ["restaurante-service", "39", "Domain, 4 use cases, JPA, Kafka, GraphQL, smoke"],
        ["TOTAL", "285", ""],
    ]
    story.append(table_simple(testes_data, col_widths=[5 * cm, 2 * cm, 9 * cm]))
    story.append(Spacer(1, 0.4 * cm))
    story.append(p(
        "A pirâmide de testes adotada privilegia <b>testes "
        "unitários</b> rápidos (JUnit 5 + Mockito + AssertJ) "
        "para a maior parte da cobertura, complementados por "
        "<b>smoke tests</b> de contexto Spring "
        "(<font face='Courier'>@SpringBootTest</font>) e por "
        "<b>testes de integração</b> da camada GraphQL via "
        "MockMvc. O domínio puro permite que regras de negócio "
        "sejam testadas sem qualquer dependência de framework.",
        body,
    ))

    # ------------------------------------------------------------- #
    # 9. OPERACAO E DEPLOY
    # ------------------------------------------------------------- #
    story.append(p("9 OPERAÇÃO E DEPLOY", h1))
    story.append(p(
        "Todo o sistema é orquestrado por Docker Compose. O "
        "comando único de subida é:",
        body,
    ))
    story.append(p("docker compose up -d --build", code))
    story.append(p(
        "Esse comando constrói as imagens das quatro aplicações "
        "(quando necessário) e sobe os oito containers. O "
        "primeiro build é demorado porque cada Dockerfile "
        "executa um <i>training run</i> de AppCDS para gerar o "
        "archive de classes compartilhadas (ver ADR 0012). "
        "Execuções subsequentes aproveitam o cache de camadas e "
        "ficam em poucos segundos.",
        body,
    ))

    story.append(p("9.1 Limites de recursos", h2))
    story.append(p(
        "Cada serviço tem limites explícitos de memória e CPU "
        "configurados em <font face='Courier'>deploy.resources.limits</font>. "
        "O total combinado fica em aproximadamente 3,5 GB de RAM "
        "e 7,5 vCPUs, permitindo execução em máquinas "
        "convencionais sem comprometer o sistema operacional do "
        "host. O ADR 0013 detalha a calibração.",
        body,
    ))

    story.append(p("9.2 Observabilidade", h2))
    story.append(p(
        "Cada aplicação expõe Spring Boot Actuator nos "
        "endpoints <font face='Courier'>/actuator/health</font>, "
        "<font face='Courier'>/actuator/info</font> e "
        "<font face='Courier'>/actuator/metrics</font>. O serviço "
        "<i>pagamento-service</i> expõe adicionalmente "
        "<font face='Courier'>/actuator/circuitbreakers</font> "
        "(estado dos breakers Resilience4j), "
        "<font face='Courier'>/actuator/retries</font> e "
        "<font face='Courier'>/actuator/scheduledtasks</font> "
        "(confirma o worker ativo). Os tópicos Kafka podem ser "
        "inspecionados via Kafka UI em "
        "<font face='Courier'>http://localhost:8085</font>.",
        body,
    ))

    story.append(p("9.3 Performance", h2))
    story.append(p(
        "Três otimizações foram aplicadas para reduzir o "
        "consumo de recursos e acelerar o startup das "
        "aplicações Spring:",
        body,
    ))
    story.extend(bullets([
        "<b>Kafka em modo KRaft</b> — eliminou o container Zookeeper, reduzindo em ~150 MB o consumo de RAM (ADR 0006).",
        "<b>AppCDS</b> — pré-resolve classes em um archive binário gerado durante o docker build, reduzindo o boot da JVM em aproximadamente 30 por cento (ADR 0012).",
        "<b>Layered JARs</b> — o jar é particionado em camadas semânticas, permitindo cache mais inteligente do Docker em rebuilds: mudança de código invalida apenas a camada de aplicação (~5 MB), não o jar completo (~50 MB) (ADR 0012).",
    ], bullet))

    # ------------------------------------------------------------- #
    # 10. CONSIDERACOES FINAIS
    # ------------------------------------------------------------- #
    story.append(p("10 CONSIDERAÇÕES FINAIS", h1))
    story.append(p(
        "A arquitetura desenhada cumpre integralmente os "
        "requisitos da Fase 3 e ainda implementa o módulo "
        "opcional <i>restaurante-service</i>, expandindo o "
        "fluxo do pedido até a entrega pela cozinha. Os "
        "principais ganhos da abordagem distribuída — "
        "isolamento de falhas, escalabilidade granular, "
        "evolução independente — são contrabalançados pelo "
        "aumento da complexidade operacional, que se manifesta "
        "em oito containers, quatro databases e um broker de "
        "mensageria coordenando tudo.",
        body,
    ))
    story.append(p(
        "Decisões pontuais foram documentadas em ADRs para "
        "preservar o raciocínio que conduziu a cada escolha, "
        "permitindo que evoluções futuras tenham contexto "
        "completo. Diagramas em formato Mermaid acompanham "
        "esta documentação na pasta "
        "<font face='Courier'>docs/diagramas/</font>.",
        body,
    ))
    story.append(p(
        "Como pendência identificada e não resolvida no escopo "
        "da Fase 3, registra-se a ausência de mecanismo de "
        "<b>outbox</b> para garantir atomicidade entre commit "
        "do banco local e publicação de eventos Kafka, e a "
        "não implementação de <b>correlation IDs</b> para "
        "rastreabilidade distribuída de requisições — itens "
        "naturais para uma próxima iteração.",
        body,
    ))
    story.append(p(
        "O sistema está pronto para demonstração, com suíte "
        "verde de 285 testes e roteiro de apresentação "
        "detalhado em <font face='Courier'>docs/roteiro-video.md</font>.",
        body,
    ))

    # ------------------------------------------------------------- #
    # REFERENCIAS
    # ------------------------------------------------------------- #
    story.append(p("REFERÊNCIAS", h1))
    refs = [
        "COCKBURN, Alistair. <b>Hexagonal Architecture</b>. 2005. Disponível em: "
        "https://alistair.cockburn.us/hexagonal-architecture/. Acesso em: "
        + datetime.now().strftime("%d %b. %Y") + ".",

        "EVANS, Eric. <b>Domain-Driven Design: Tackling Complexity in the Heart "
        "of Software</b>. Boston: Addison-Wesley, 2003.",

        "NYGARD, Michael. <b>Documenting Architecture Decisions</b>. 2011. "
        "Disponível em: https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions. "
        "Acesso em: " + datetime.now().strftime("%d %b. %Y") + ".",

        "PIVOTAL SOFTWARE. <b>Spring Boot Reference Documentation</b>. 2025. "
        "Disponível em: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/. "
        "Acesso em: " + datetime.now().strftime("%d %b. %Y") + ".",

        "APACHE SOFTWARE FOUNDATION. <b>Apache Kafka Documentation</b>. 2025. "
        "Disponível em: https://kafka.apache.org/documentation/. Acesso em: "
        + datetime.now().strftime("%d %b. %Y") + ".",

        "RESILIENCE4J. <b>Resilience4j User Guide</b>. 2025. Disponível em: "
        "https://resilience4j.readme.io/. Acesso em: "
        + datetime.now().strftime("%d %b. %Y") + ".",

        "FOWLER, Martin. <b>Microservices</b>. 2014. Disponível em: "
        "https://martinfowler.com/articles/microservices.html. Acesso em: "
        + datetime.now().strftime("%d %b. %Y") + ".",

        "OPENJDK. <b>JEP 310: Application Class-Data Sharing</b>. 2018. "
        "Disponível em: https://openjdk.org/jeps/310. Acesso em: "
        + datetime.now().strftime("%d %b. %Y") + ".",

        "ASSOCIAÇÃO BRASILEIRA DE NORMAS TÉCNICAS. <b>NBR 14724</b>: "
        "informação e documentação — trabalhos acadêmicos — apresentação. "
        "Rio de Janeiro, 2011.",

        "FIAP. <b>Tech Challenge — Fase 3</b>. Material institucional do "
        "programa de Pós-Graduação PosTech. São Paulo, 2025.",
    ]
    ref_style = ParagraphStyle(
        "ABNTRef",
        parent=body,
        firstLineIndent=0,
        leftIndent=0,
        alignment=TA_LEFT,
        leading=15,
        spaceAfter=8,
    )
    for ref in refs:
        story.append(p(ref, ref_style))

    # ------------------------------------------------------------- #
    # Build
    # ------------------------------------------------------------- #
    doc = ABNTDocTemplate(
        filename,
        pagesize=PAGE_SIZE,
        leftMargin=MARGIN_LEFT,
        rightMargin=MARGIN_RIGHT,
        topMargin=MARGIN_TOP,
        bottomMargin=MARGIN_BOTTOM,
        title="Documentação de Arquitetura — FIAP Restaurante Fase 3",
        author="Danilo Fernando de Paula e Silva; Gilmar da Costa Moraes Junior; "
               "Juliana Maria Dal Olio Braz; Luis Henrique Silveira Borges; "
               "Thiago de Jesus Cordeiro",
        subject="Tech Challenge Fase 3 — PosTech FIAP",
        keywords="microsservicos; hexagonal; kafka; resilience4j; spring-boot; "
                 "graphql; docker; jwt; appcds; kraft",
    )

    # Para o ToC popular corretamente, e necessario rodar multi-build
    doc.multiBuild(story)


if __name__ == "__main__":
    out = Path(__file__).resolve().parents[1] / "documentacao-arquitetura.pdf"
    build(str(out))
    print(f"Generated: {out}")
