# Plano de evolução do Sputnik

Este plano organiza a evolução do Sputnik de aplicação local para um produto SaaS de gestão de oficinas. As entregas concluídas estão registradas abaixo; os itens restantes são a sequência recomendada para produção.

## Etapa 1 — Fundação escalável e isolamento multiempresa

**Objetivo:** permitir que empresas coexistam sem compartilhar dados operacionais.

**Entregue nesta versão:** empresas, usuários por empresa, planos/assinaturas, `empresa_id` em entidades operacionais principais, atribuição automática no cadastro, migração de registros legados e teste de integração que bloqueia a listagem e a movimentação de uma peça de outra empresa.

- ~~Migrar a produção de H2 para PostgreSQL e manter H2 apenas no perfil local/teste.~~ Entregue: perfil `postgres` (out/2026).
- ~~Versionar o esquema com Flyway; eliminar `ddl-auto=update` em produção.~~ Entregue: `V1__esquema_inicial.sql` e `ddl-auto=validate`.
- Copiar os dados do H2 local para o PostgreSQL (script de exportação) quando houver dados reais a preservar.
- Adicionar `empresa_id` a clientes, peças, veículos, ordens, itens, movimentações, histórico e financeiro.
- Atualizar repositórios e serviços para sempre consultar e alterar dados da empresa da sessão.
- Migrar dados existentes para a empresa padrão, sem perder o banco local.
- Paginar consultas de listas e relatórios no servidor.

**Próximo aceite de produção:** uma conta não consegue listar, consultar, alterar, exportar ou agregar qualquer dado de outra empresa; testes de integração cobrem todas as rotas e relatórios. A versão atual cobre o fluxo operacional principal; a revisão de produção deve expandir a matriz de testes para exportações e agregações.

## Etapa 2 — Sistema visual e experiência de uso

**Objetivo:** consolidar uma interface com aparência de produto comercial.

**Entregue nesta versão:** componentes compartilhados, layout centralizado, menu lateral persistente, ícones Lucide no menu, estados de carregamento/erro/vazio e filtros visuais no módulo financeiro.

- Definir tokens de cor, tipografia, espaçamento, elevação e estados no design system existente.
- Substituir símbolos textuais por ícones Lucide consistentes.
- Redesenhar menu, cabeçalho, dashboard, tabelas, formulários, feedbacks e estados vazios.
- Padronizar ações de linha em menus, filtros, paginação e busca.
- Validar responsividade, contraste, teclado, foco e leitor de tela.

**Próximo aceite:** os módulos usam os mesmos componentes de navegação, formulário, tabela, badge, loading e erro; não há quebra em desktop ou celular.

## Etapa 3 — Fluxo comercial e operacional de OS

**Objetivo:** tornar o ciclo de atendimento o centro do produto.

**Entregue nesta versão:** abertura de OS/orçamento, aprovação com baixa controlada de estoque, serviços e peças, edição, agenda, retornos, garantia, documento A4, impressão na própria tela e download em PDF.

- Criar catálogo de serviços, tempo padrão e preço de mão de obra.
- Melhorar orçamento, aprovação, execução, entrega e retorno da OS.
- Adicionar fornecedor, pedido de compra e recebimento de estoque.
- Criar histórico completo do veículo e QR Code seguro para consulta da OS.
- Gerar documento de cliente com identidade da empresa, termos, assinatura e comprovante de retirada.

**Próximo aceite:** uma OS pode ser aberta, orçada, aprovada, executada, faturada, impressa e entregue sem depender de controles externos.

## Etapa 4 — Financeiro e inteligência de negócio

**Objetivo:** dar visibilidade à saúde financeira da oficina.

**Entregue nesta versão:** contas a pagar/receber, baixa, vínculo com OS, rentabilidade por OS, módulo financeiro com permissão própria e filtros por período.

- Fluxo de caixa por período, categoria e status de pagamento.
- Contas a pagar, contas a receber, vencidos e conciliação de baixas.
- Rentabilidade por OS, veículo, cliente, serviço e período.
- Indicadores de produtividade, ticket médio, taxa de retorno e tempo de atendimento.
- Exportações seguras para CSV/PDF e permissões específicas por módulo.

**Próximo aceite:** o administrador visualiza indicadores confiáveis por empresa; o usuário financeiro só acessa dados financeiros autorizados.

## Etapa 5 — Operação em produção e integrações

**Objetivo:** tornar o produto instalável, observável e vendável.

- Docker Compose para aplicação e PostgreSQL; configuração por variáveis de ambiente.
- CI no GitHub Actions para frontend, backend, testes e verificação de segurança.
- Health checks, logs estruturados, monitoramento de erros e rotina de backup externo.
- Armazenamento de documentos em serviço compatível com S3.
- Integração de cobrança recorrente e envio de WhatsApp/e-mail após escolha dos provedores e credenciais.

**Aceite:** uma instalação nova sobe sem ajustes manuais, passa no pipeline e consegue restaurar um backup validado.

## Decisões externas necessárias antes da ativação

As implementações internas podem ser concluídas no repositório. A ativação em produção exige escolhas do responsável pelo produto:

1. Provedor de hospedagem e PostgreSQL.
2. Serviço de armazenamento de documentos e backups.
3. Provedor de cobrança recorrente.
4. Provedor oficial de WhatsApp/e-mail e modelos de mensagem.

Até essas decisões, integrações externas permanecem desativadas; não serão incluídas chaves, contas ou credenciais no repositório.
