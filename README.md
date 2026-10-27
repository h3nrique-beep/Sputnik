# Sputnik 2.0

> Gestão operacional para oficinas: estoque, atendimento, ordens de serviço e financeiro em uma única aplicação.

O Sputnik é uma aplicação web para gestão de oficinas automotivas, desenvolvida com **React + TypeScript** no frontend e **Java 17 + Spring Boot + H2** no backend. O projeto combina controle de estoque, cadastros, ordens de serviço, agenda e acompanhamento financeiro, com acesso orientado por permissões.

## Principais recursos

### Operação da oficina

- Cadastro centralizado de **clientes, veículos e peças**.
- Busca rápida por nome, CPF, telefone, placa, chassi, nome ou código da peça.
- **Busca global** no topo: número da OS abre a ordem, placa abre veículos e o restante busca peças.
- Seletores com busca (peça, cliente, veículo) que escolhem sozinhos quando sobra um resultado.
- Controle de estoque com entradas, saídas, saldo disponível, estoque mínimo e bloqueio de saldo negativo.
- Histórico de movimentações com operador, data, observação e saldo resultante.
- Reposição de itens críticos com exportação CSV (`/api/reposicao.csv`).

### Ordens de serviço

- Abertura de OS e orçamentos com cliente, veículo, quilometragem e descrição.
- Inclusão de peças e serviços, totais automáticos, histórico e acompanhamento por status.
- Aprovação de orçamento com baixa de estoque somente após a aprovação, listando antes as peças que sairão do estoque.
- **Quadro das OS** por status: arrastar o cartão muda o status (orçamento só muda pela aprovação); alterna com a visão em lista.
- Detalhe da OS com itens e lançamento em primeiro plano e histórico em linha do tempo, com as trocas de status destacadas.
- **Modo oficina** para tablet: OS em execução em cartões grandes, com o próximo passo a um toque e filtro "só as minhas" pelo consultor técnico.
- Agenda de atendimentos, retornos preventivos e período de garantia.
- Documento A4 de OS com campos complementares, impressão na própria tela e download em PDF.

### Financeiro e gestão

- Lançamentos de receitas e despesas, vencimentos, vínculo opcional com OS e baixa de pagamentos.
- Indicadores de contas a receber, a pagar, recebido e rentabilidade por OS, filtráveis por mês, próximos 30 dias ou histórico, com comparação ao mês anterior.
- Ações em lote nos lançamentos: baixar vários pendentes de uma vez e exportar os selecionados ou o período em CSV.
- **Painel de atenção**: o que exige ação hoje (clicável e filtrado por permissão), ações rápidas e indicadores de estoque.
- Movimentação de estoque mostra o saldo antes e depois e bloqueia saída acima do disponível.
- Console administrativo com diagnóstico do ambiente e backups locais.

### Acesso e permissões

- Autenticação por usuário ou e-mail, com senha protegida por BCrypt e sessão segura.
- Primeiro acesso obrigatório com troca de senha e política de senha forte.
- Administração de usuários e permissões por módulo: **Peças**, **Ordens de serviço**, **Relatório mensal**, **Financeiro** e **Reposição**.
- Perfil financeiro isolado: acessa somente os lançamentos financeiros, sem acesso ao painel operacional.
- Menu lateral agrupado e recolhível, com preferência preservada após atualizar a página; o F5 não exibe mais um lampejo da tela de login.
- Atalhos de teclado: `Ctrl+K` foca a busca global e `N` abre uma nova OS.
- Carregamento com skeletons no lugar de spinners, respeitando a preferência de movimento reduzido.
- Tema claro/escuro que segue o sistema quando não há escolha salva; status com rótulo e ícone (a cor nunca é o único sinal), alvos de toque de 44px e erros anunciados a leitores de tela.
- Dados operacionais separados pela empresa da sessão para clientes, veículos, peças, movimentações, OS e lançamentos financeiros.

## Stack

| Camada | Tecnologias |
| --- | --- |
| Frontend | React, TypeScript, Vite, Tailwind, shadcn/ui e Recharts |
| Backend | Java 17, Spring Boot, Spring MVC e Spring Data JPA |
| Banco de dados | H2 persistente em arquivo |
| Segurança | BCrypt, sessão HTTP, CSRF, limite de tentativas de login e permissões por módulo |
| Testes | JUnit 5, Spring Boot Test e MockMvc |

## Arquitetura

```text
React / TypeScript
        │  /api
        ▼
Spring Boot Controllers
        ▼
Services (regras de negócio)
        ▼
Spring Data JPA
        ▼
H2 (data/sputnik)
```

Toda a interface é o frontend React compilado, servido pelo Spring Boot. A única página renderizada no servidor é a troca obrigatória de senha do primeiro acesso (`/primeiro-acesso`, Thymeleaf).

## Como executar

### Pré-requisitos

- JDK 17+
- Maven 3.9+
- Node.js 20+ (somente para alterar ou recompilar o frontend)

### 1. Configure o primeiro administrador

Em um banco vazio, defina uma senha forte com ao menos 12 caracteres, incluindo maiúscula, minúscula, número e símbolo.

```powershell
$env:SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD = Read-Host "Digite a senha do administrador"
# Opcional: padrão é "admin"
$env:SPUTNIK_BOOTSTRAP_ADMIN_USUARIO = "admin"
```

### 2. Inicie o backend

```bash
mvn spring-boot:run
```

Abra [http://localhost:8080](http://localhost:8080).

> A conta inicial deve trocar a senha no primeiro acesso. Não existem credenciais padrão no código.

### Desenvolvimento do frontend

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run dev
```

Para gerar os arquivos estáticos servidos pelo Spring Boot:

```bash
cd frontend
pnpm run build:java
```

### Testes

```bash
mvn test
```

Os testes usam H2 em memória. Os dados locais da aplicação são gravados em `data/sputnik` e não são enviados ao Git.

## Multiempresa e assinaturas

O Sputnik possui empresas, planos e assinaturas como base para a oferta SaaS. A empresa da sessão é aplicada aos registros operacionais e novos registros recebem automaticamente o identificador da empresa atual. Na atualização, registros legados são associados à primeira empresa existente para preservar a continuidade local.

O cadastro público continua **desativado por padrão** (`SPUTNIK_SAAS_SELF_SIGNUP_ENABLED=false`). Antes de ativá-lo em produção, use PostgreSQL e migrações versionadas, configure observabilidade e revise a capacidade dos planos — passos registrados no plano de evolução.

## Estrutura do projeto

```text
src/main/java/.../
├── auth/                # autenticação, sessão e usuários
├── config/              # dados iniciais, migrações locais e interceptadores
├── controller/          # entrega do React (SPA) e primeiro acesso
├── controller/api/      # API REST por módulo, DTOs e mapeadores
├── domain/              # entidades e enums JPA
├── repository/          # acesso a dados (Spring Data JPA)
├── security/            # CSRF, cabeçalhos, limite de requisições e de login
├── service/             # regras de negócio
├── tenant/              # empresa da sessão e isolamento dos dados
└── web/                 # formulários de entrada validados

frontend/src/
├── app/                 # composição e navegação
├── components/          # UI compartilhada e shadcn
├── features/            # módulos funcionais
├── hooks/               # hooks reutilizáveis
├── lib/                 # cliente HTTP e utilitários
└── styles/              # estilos organizados por área
```

## Documentação

- [Arquitetura e lógica do sistema](docs/ARQUITETURA.md)
- [Guia de abertura no IntelliJ IDEA](docs/INTELLIJ.md)
- [Mapa de arquivos e responsabilidades](ARCHITECTURE.md)
- [Modelo do documento de OS](docs/MODELO-OS.md)
- [Plano de evolução do produto](docs/PLANO-EVOLUCAO-PRODUTO.md)
- [Auditoria de código (out/2026)](docs/AUDITORIA-CODIGO-2026-10-11.md)
- [Landing page de divulgação](docs/landing/sputnik-landing.html)
- [Relatório de auditoria de segurança](docs/security-audit/relatorio-auditoria-seguranca.pdf)

## Segurança

- Nunca versione senhas, tokens ou arquivos de banco com dados reais.
- Use variáveis de ambiente para configurações sensíveis.
- Em produção com HTTPS, configure `SPUTNIK_COOKIE_SECURE=true`.
- O console H2 é desativado por padrão; habilite-o somente em ambiente local quando necessário.

---

Projeto acadêmico de Engenharia de Software, evoluído como uma base prática de gestão para oficinas.
