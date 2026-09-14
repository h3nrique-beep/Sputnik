# Sputnik 2.0

> Gestão operacional para oficinas: estoque, atendimento, ordens de serviço e financeiro em uma única aplicação.

O Sputnik é uma aplicação web para gestão de oficinas automotivas, desenvolvida com **React + TypeScript** no frontend e **Java 17 + Spring Boot + H2** no backend. O projeto combina controle de estoque, cadastros, ordens de serviço, agenda e acompanhamento financeiro, com acesso orientado por permissões.

## Principais recursos

### Operação da oficina

- Cadastro centralizado de **clientes, veículos e peças**.
- Busca rápida por nome, CPF, telefone, placa, chassi, nome ou código da peça.
- Controle de estoque com entradas, saídas, saldo disponível, estoque mínimo e bloqueio de saldo negativo.
- Histórico de movimentações com operador, data, observação e saldo resultante.
- Reposição de itens críticos e exportação CSV.

### Ordens de serviço

- Abertura de OS e orçamentos com cliente, veículo, quilometragem e descrição.
- Inclusão de peças e serviços, totais automáticos, histórico e acompanhamento por status.
- Aprovação de orçamento com baixa de estoque somente após a aprovação.
- Agenda de atendimentos, retornos preventivos e período de garantia.
- Documento A4 de OS com campos complementares, impressão na própria tela e download em PDF.

### Financeiro e gestão

- Lançamentos de receitas e despesas, vencimentos, vínculo opcional com OS e baixa de pagamentos.
- Indicadores de contas a receber, a pagar, recebido e rentabilidade por OS, filtráveis por mês, próximos 30 dias ou histórico.
- Painel operacional com métricas, itens críticos, atendimentos e indicadores de estoque.
- Console administrativo com diagnóstico do ambiente e backups locais.

### Acesso e permissões

- Autenticação por usuário ou e-mail, com senha protegida por BCrypt e sessão segura.
- Primeiro acesso obrigatório com troca de senha e política de senha forte.
- Administração de usuários e permissões por módulo: **Peças**, **Ordens de serviço**, **Relatório mensal**, **Financeiro** e **Reposição**.
- Perfil financeiro isolado: acessa somente os lançamentos financeiros, sem acesso ao painel operacional.
- Menu lateral recolhível, com preferência preservada após atualizar a página.
- Dados operacionais separados pela empresa da sessão para clientes, veículos, peças, movimentações, OS e lançamentos financeiros.

## Stack

| Camada | Tecnologias |
| --- | --- |
| Frontend | React, TypeScript, Vite, Tailwind, shadcn/ui e Recharts |
| Backend | Java 17, Spring Boot, Spring MVC e Spring Data JPA |
| Banco de dados | H2 persistente em arquivo |
| Segurança | BCrypt, sessão HTTP, CSRF e permissões por módulo |
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

As páginas Thymeleaf legadas continuam disponíveis para compatibilidade, enquanto a experiência principal é servida pelo frontend React compilado.

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
npm install
npm run dev
```

Para gerar os arquivos estáticos servidos pelo Spring Boot:

```bash
cd frontend
npm run build:java
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
├── auth/                # autenticação e usuários
├── controller/api/      # API REST por módulo
├── domain/              # entidades e enums JPA
├── repository/          # acesso a dados
├── service/             # regras de negócio
└── web/                 # formulários de entrada

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
- [Relatório de auditoria de segurança](docs/security-audit/relatorio-auditoria-seguranca.pdf)

## Segurança

- Nunca versione senhas, tokens ou arquivos de banco com dados reais.
- Use variáveis de ambiente para configurações sensíveis.
- Em produção com HTTPS, configure `SPUTNIK_COOKIE_SECURE=true`.
- O console H2 é desativado por padrão; habilite-o somente em ambiente local quando necessário.

---

Projeto acadêmico de Engenharia de Software, evoluído como uma base prática de gestão para oficinas.
