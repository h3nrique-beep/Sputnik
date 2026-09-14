from pathlib import Path
from datetime import date

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    PageBreak, Paragraph, Spacer, Table, TableStyle
)
from reportlab.graphics.shapes import Drawing, String
from reportlab.graphics.charts.piecharts import Pie
from reportlab.graphics.charts.barcharts import VerticalBarChart

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).with_name("relatorio-auditoria-seguranca.pdf")

RED = "#B91C1C"
ORANGE = "#EA580C"
AMBER = "#D97706"
BLUE = "#2563EB"
GREEN = "#059669"
INK = colors.HexColor("#102A43")
MUTED = colors.HexColor("#486581")
PALE = colors.HexColor("#F5F7FA")


def chart_drawings():
    donut = Drawing(250, 170)
    pie = Pie()
    pie.x, pie.y, pie.width, pie.height = 38, 12, 130, 130
    pie.data = [1, 1]
    pie.labels = []
    pie.startAngle = 90
    pie.slices[0].fillColor = colors.HexColor(ORANGE)
    pie.slices[1].fillColor = colors.HexColor(AMBER)
    pie.slices.strokeColor = colors.white
    pie.slices.strokeWidth = 1
    pie.innerRadiusFraction = .55
    donut.add(pie)
    donut.add(String(103, 82, "2", fontName="Helvetica-Bold", fontSize=17, fillColor=INK, textAnchor="middle"))
    donut.add(String(103, 67, "achados", fontName="Helvetica", fontSize=8, fillColor=MUTED, textAnchor="middle"))
    donut.add(String(192, 103, "Alta: 1", fontName="Helvetica", fontSize=9, fillColor=INK))
    donut.add(String(192, 78, "Media: 1", fontName="Helvetica", fontSize=9, fillColor=INK))

    bars = Drawing(315, 170)
    chart = VerticalBarChart()
    chart.x, chart.y, chart.width, chart.height = 34, 35, 250, 105
    chart.data = [[2, 0, 0, 0]]
    chart.valueAxis.valueMin = 0
    chart.valueAxis.valueMax = 2.5
    chart.valueAxis.valueStep = 1
    chart.valueAxis.labels.fontSize = 7
    chart.categoryAxis.categoryNames = ["Chaves", "Banco/IDOR", "Permissao", "XSS"]
    chart.categoryAxis.labels.fontSize = 7
    chart.categoryAxis.labels.angle = 18
    chart.barWidth = 23
    chart.groupSpacing = 18
    chart.bars[0].fillColor = colors.HexColor(ORANGE)
    chart.bars[0].strokeColor = colors.HexColor(ORANGE)
    bars.add(chart)
    bars.add(String(158, 154, "Achados por categoria", fontName="Helvetica-Bold", fontSize=10, fillColor=INK, textAnchor="middle"))
    return donut, bars


def para(text, style):
    return Paragraph(text, style)


def severity(label, color, styles):
    return para(f'<font color="{color}"><b>{label.upper()}</b></font>', styles["small"])


def header_footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#D9E2EC"))
    canvas.line(doc.leftMargin, A4[1] - 1.35 * cm, A4[0] - doc.rightMargin, A4[1] - 1.35 * cm)
    canvas.setFont("Helvetica-Bold", 8)
    canvas.setFillColor(MUTED)
    canvas.drawString(doc.leftMargin, A4[1] - 1.0 * cm, "Relatorio de Auditoria de Seguranca - Sputnik")
    canvas.setFont("Helvetica", 8)
    canvas.drawRightString(A4[0] - doc.rightMargin, 0.9 * cm, f"Pagina {doc.page}")
    canvas.restoreState()


def issue(number, title, severity_name, labels, problem, evidence, impact, fix, acceptance, styles):
    body = [
        f"--- ISSUE {number} ---",
        f"# [Seguranca] {title}",
        f"**Labels sugeridas:** {labels}",
        "## Problema", problem,
        "## Evidencia", evidence,
        "## Impacto", impact,
        "## Sugestao de correcao", fix,
        "## Criterios de aceite", acceptance,
        f"--- FIM ISSUE {number} ---",
    ]
    return [para(line.replace("\n", "<br/>"), styles["code"] if line.startswith("---") or line.startswith("#") or line.startswith("**") or line.startswith("##") else styles["body"]) for line in body]


def build():
    donut, bars = chart_drawings()
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle(name="cover", parent=styles["Title"], fontName="Helvetica-Bold", fontSize=27,
                              leading=33, textColor=INK, alignment=TA_CENTER, spaceAfter=16))
    styles.add(ParagraphStyle(name="subtitle", parent=styles["Normal"], fontSize=11, leading=16,
                              textColor=MUTED, alignment=TA_CENTER))
    styles.add(ParagraphStyle(name="h1x", parent=styles["Heading1"], fontName="Helvetica-Bold", fontSize=18,
                              leading=23, textColor=INK, spaceBefore=10, spaceAfter=10))
    styles.add(ParagraphStyle(name="h2x", parent=styles["Heading2"], fontName="Helvetica-Bold", fontSize=12,
                              leading=16, textColor=INK, spaceBefore=8, spaceAfter=6))
    styles.add(ParagraphStyle(name="body", parent=styles["BodyText"], fontSize=9.1, leading=13.5,
                              textColor=INK, spaceAfter=6))
    styles.add(ParagraphStyle(name="small", parent=styles["BodyText"], fontSize=7.6, leading=10,
                              textColor=INK))
    styles.add(ParagraphStyle(name="code", parent=styles["BodyText"], fontName="Courier", fontSize=7.4,
                              leading=10, textColor=colors.HexColor("#243B53"), backColor=colors.HexColor("#F0F4F8"),
                              borderPadding=5, spaceAfter=4))

    doc = __import__("reportlab.platypus", fromlist=["SimpleDocTemplate"]).SimpleDocTemplate(
        str(OUT), pagesize=A4, leftMargin=2*cm, rightMargin=2*cm, topMargin=2*cm, bottomMargin=1.7*cm)
    story = []
    story += [Spacer(1, 2.5*cm), para("Relatorio de Auditoria de Seguranca", styles["cover"]),
              para("Sputnik - Gestao de Oficina", ParagraphStyle("p", parent=styles["subtitle"], fontSize=16, leading=22)),
              Spacer(1, .45*cm), para(f"Data da auditoria: {date.today().strftime('%d/%m/%Y')}", styles["subtitle"]),
              Spacer(1, 1.4*cm)]
    cover_table = Table([
        [para("<b>Escopo auditado</b><br/>Backend Spring Boot, entidades JPA/H2, API REST, controladores Thymeleaf, frontend React/Vite, configuracoes, documentacao, bundle estatico e historico Git.", styles["body"])],
        [para("<b>Nota metodologica</b><br/>RLS foi mapeado para isolamento por organizacao/dono nas queries JPA. Gates React foram cruzados com os endpoints REST. IDOR foi revisado em todos os handlers com IDs. Segredos foram pesquisados no codigo, configuracoes, documentacao, bundle e historico. XSS foi mapeado para JSX, Thymeleaf e HTML gerado no backend.", styles["body"])],
    ], colWidths=[16.8*cm])
    cover_table.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,-1), PALE), ("BOX", (0,0), (-1,-1), .4, colors.HexColor("#D9E2EC")),
                                     ("INNERGRID", (0,0), (-1,-1), .4, colors.HexColor("#D9E2EC")), ("LEFTPADDING", (0,0), (-1,-1), 12), ("RIGHTPADDING", (0,0), (-1,-1), 12), ("TOPPADDING", (0,0), (-1,-1), 10), ("BOTTOMPADDING", (0,0), (-1,-1), 10)]))
    story += [cover_table, Spacer(1, 1.4*cm), para("Classificacao: auditoria de codigo estatico. Somente achados reproduziveis pelo fluxo implementado foram reportados.", styles["subtitle"]), PageBreak()]

    story += [para("Resumo executivo", styles["h1x"]),
              para("Foram confirmados <b>2 achados</b>: 1 de severidade alta e 1 de severidade media. Ambos se concentram no ciclo de senhas. Nao foram encontrados achados confirmados de isolamento multi-tenant, autorizacao apenas no navegador, IDOR ou XSS nesta revisao.", styles["body"])]
    metrics = Table([[para("<b>0</b><br/><font color='#486581'>Critica</font>", styles["body"]), para("<b>1</b><br/><font color='#486581'>Alta</font>", styles["body"]), para("<b>1</b><br/><font color='#486581'>Media</font>", styles["body"]), para("<b>0</b><br/><font color='#486581'>Baixa</font>", styles["body"])]], colWidths=[4.2*cm]*4)
    metrics.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,-1), PALE), ("BOX", (0,0), (-1,-1), .4, colors.HexColor("#D9E2EC")), ("INNERGRID", (0,0), (-1,-1), .4, colors.HexColor("#D9E2EC")), ("ALIGN", (0,0), (-1,-1), "CENTER"), ("VALIGN", (0,0), (-1,-1), "MIDDLE"), ("TOPPADDING", (0,0), (-1,-1), 10), ("BOTTOMPADDING", (0,0), (-1,-1), 10)]))
    story += [Spacer(1, .25*cm), metrics, Spacer(1, .35*cm), Table([[donut, bars]], colWidths=[8.2*cm, 8.6*cm]), PageBreak()]

    story += [para("Achados confirmados", styles["h1x"])]
    findings = [
        ("A-01", "ALTA", ORANGE, "Credenciais de bootstrap fixas e publicadas", "src/main/java/br/edu/fag/sputnik/auth/UsuarioService.java:102-118; README.md:54-61", "Ao detectar banco sem usuarios, o servico cria um administrador 'admin' e um balconista conhecidos, ambos com troca obrigatoria de senha desativada. O README publica as mesmas credenciais. Qualquer pessoa que alcance uma instalacao nova ou tenha seu banco reinicializado pode autenticar como administrador.", "Trecho: criarUsuarioPadrao(... 'admin', 'Admin#2026!', Perfil.ADMIN, ...); e usuario.setTrocaSenhaObrigatoria(false)."),
        ("A-02", "MEDIA", AMBER, "Senha temporaria de reset fixa e conhecida", "src/main/java/br/edu/fag/sputnik/auth/UsuarioService.java:22, 82-86; frontend/src/features/users/Users.tsx:19-22", "O reset redefine qualquer Balconista para '12345678', senha exibida pela interface. Depois de um reset administrativo, quem souber o e-mail do usuario pode entrar com essa senha ate que o titular a altere. O endpoint e restrito a admin, mas a senha resultante e universal e previsivel.", "Trecho: private static final String SENHA_TEMPORARIA_RESET = '12345678'; e usuario.setSenhaHash(encoder.encode(SENHA_TEMPORARIA_RESET))."),
    ]
    table_rows = [[para("Severidade", styles["small"]), para("Arquivo:linha", styles["small"]), para("Descricao", styles["small"])]]
    for _, sev, color, title, loc, desc, _ in findings:
        table_rows.append([severity(sev, color, styles), para(loc, styles["small"]), para(f"<b>{title}</b><br/>{desc}", styles["small"])])
    detail_table = Table(table_rows, colWidths=[2.1*cm, 5.0*cm, 9.7*cm], repeatRows=1)
    detail_table.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,0), colors.HexColor("#EAF2F8")), ("GRID", (0,0), (-1,-1), .35, colors.HexColor("#D9E2EC")), ("VALIGN", (0,0), (-1,-1), "TOP"), ("LEFTPADDING", (0,0), (-1,-1), 6), ("RIGHTPADDING", (0,0), (-1,-1), 6), ("TOPPADDING", (0,0), (-1,-1), 7), ("BOTTOMPADDING", (0,0), (-1,-1), 7)]))
    story += [detail_table, Spacer(1, .25*cm)]
    for ident, sev, color, title, loc, desc, evidence in findings:
        story += [para(f"{ident} - {title}", styles["h2x"]), para(f"<b>Severidade:</b> <font color='{color}'>{sev}</font><br/><b>Local:</b> {loc}", styles["body"]), para(desc, styles["body"]), para(evidence, styles["code"])]
    story += [PageBreak()]

    story += [para("Pontos fortes e cobertura", styles["h1x"])]
    strengths = [
        ("Isolamento / IDOR", "O modelo JPA nao possui entidade organizacao, tenant ou dono de registros: e uma oficina unica compartilhada por funcionarios. Portanto RLS/filtro de tenant nao se aplica ao modelo atual. Todos os handlers com ID foram revisados. Rotas de OS exigem ORDENS antes de buscar ou alterar (OrdensApiController.java:67-155); rotas administrativas exigem perfil ADMIN (UsuariosApiController.java:36-88); backup valida nome por regex e normaliza o caminho (BackupService.java:62-68)."),
        ("Permissao no navegador", "Os gates React em App.tsx:47-113 foram cruzados com as APIs. PECAS, ORDENS, RELATORIO_MENSAL e REPOSICAO sao validados no servidor pelos controladores de API; Usuarios e Desenvolvedor usam ehAdministrador. Nao foi encontrado endpoint sensivel acessivel apenas por ocultacao de UI."),
        ("XSS", "Nao ha dangerouslySetInnerHTML, innerHTML, eval ou new Function no fonte React. JSX usa escape automatico. Templates Thymeleaf usam th:text, nao th:utext. O HTML de impressao escapa &, <, >, aspas e apostrofos antes de inserir valores (DocumentoOsLayout.java:132-163)."),
        ("Defesas adicionais", "API mutavel exige token CSRF (ApiCsrfFilter.java:27-52); cookies sao HttpOnly e SameSite=Strict (application.properties:11-14); cabecalhos CSP, frame deny e nosniff sao definidos no filtro de seguranca."),
        ("Historico e deploy", "O historico Git local possui um unico commit e foi pesquisado para credenciais. Nao ha Dockerfile, docker-compose, CI, Helm, charts ou Terraform no escopo versionado."),
    ]
    for heading, body in strengths:
        story += [para(heading, styles["h2x"]), para(body, styles["body"])]
    story += [para("Observacao de escopo", styles["h2x"]), para("Se o Sputnik evoluir para SaaS ou para varias oficinas, sera necessario adicionar organizacao_id as entidades e filtrar todas as listagens, buscas, agregacoes, exportacoes e buscas por ID no repositorio/servico. Isto e uma recomendacao arquitetural futura, nao um achado de vazamento confirmado no modelo de oficina unica atual.", styles["body"]), PageBreak()]

    story += [para("Recomendacoes priorizadas", styles["h1x"]),
              para("<b>P1 - Remover bootstrap com senhas fixas.</b> Exigir segredo de bootstrap por variavel de ambiente ou fluxo de criacao inicial de uso unico; falhar no startup se estiver ausente em perfil de producao; marcar a conta inicial para troca obrigatoria.", styles["body"]),
              para("<b>P1 - Revogar as credenciais expostas.</b> Em ambientes ja inicializados, altere imediatamente as senhas de admin e balconista e invalide sessoes existentes. Remova a tabela de credenciais do README e, como elas ja entraram no Git, considere-as comprometidas.", styles["body"]),
              para("<b>P2 - Tornar o reset aleatorio.</b> Gere senha temporaria criptograficamente aleatoria ou token de definicao de senha com expiracao e entrega fora da interface. Nunca mostre nem persista uma senha fixa de reset.", styles["body"]),
              para("<b>P3 - Preparar multitenancy se virar requisito.</b> Criar contexto de organizacao e filtros obrigatorios por organizacao em JPA antes de oferecer dados de mais de uma oficina.", styles["body"]), PageBreak()]

    story += [para("ISSUES PARA O GITHUB", styles["h1x"]), para("Textos completos prontos para copiar e colar.", styles["body"])]
    for item in issue(1, "Remover credenciais previsiveis criadas no bootstrap", "alta", "security, high", "Em banco vazio, a aplicacao cria automaticamente contas de Balconista e Administrador com credenciais conhecidas e sem troca obrigatoria de senha.", "src/main/java/br/edu/fag/sputnik/auth/UsuarioService.java:102-118\ncriarUsuarioPadrao(... 'admin', 'Admin#2026!', Perfil.ADMIN, ...)\nsrc/main/java/br/edu/fag/sputnik/auth/UsuarioService.java:147-148\nusuario.setTrocaSenhaObrigatoria(false);\nREADME.md:54-61 publica as mesmas credenciais.", "Tomada imediata de controle administrativo em uma instalacao nova ou banco reinicializado.", "Remover os literais, usar segredo de bootstrap externo de uso unico ou assistente de primeiro acesso, exigir troca obrigatoria e falhar no startup de producao se a configuracao segura nao existir.", "- [ ] Nao existe senha de bootstrap no codigo, README ou bundle.\n- [ ] Startup de producao nao cria admin com senha previsivel.\n- [ ] Primeiro admin precisa definir senha segura.\n- [ ] Credenciais atuais sao revogadas e documentacao atualizada.", styles):
        story.append(item)
    story.append(Spacer(1, .35*cm))
    for item in issue(2, "Substituir senha temporaria fixa no reset de usuario", "media", "security, medium", "O reset de Balconista atribui a mesma senha conhecida a todas as contas e a interface a exibe antes da requisicao.", "src/main/java/br/edu/fag/sputnik/auth/UsuarioService.java:22, 82-86\nprivate static final String SENHA_TEMPORARIA_RESET = '12345678';\nusuario.setSenhaHash(encoder.encode(SENHA_TEMPORARIA_RESET));\nfrontend/src/features/users/Users.tsx:19-22 mostra a senha fixa.", "Uma pessoa que conheca o e-mail de uma conta resetada pode autenticar durante a janela anterior a troca obrigatoria.", "Gerar segredo aleatorio por reset ou token de definicao com expiracao; entregar por canal autenticado e nunca mostrar senha global na UI.", "- [ ] Cada reset gera valor/token unico com CSPRNG.\n- [ ] O valor nao esta no codigo, docs ou bundle.\n- [ ] Token expira e e inutilizado apos uso.\n- [ ] Teste prova que dois resets nao produzem o mesmo segredo.", styles):
        story.append(item)

    doc.build(story, onFirstPage=header_footer, onLaterPages=header_footer)
    print(OUT)


if __name__ == "__main__":
    build()
