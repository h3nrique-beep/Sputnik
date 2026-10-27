# Sputnik 2.0

[![CI](https://github.com/h3nrique-beep/Sputnik/actions/workflows/ci.yml/badge.svg)](https://github.com/h3nrique-beep/Sputnik/actions/workflows/ci.yml)

> Gestão operacional para oficinas: estoque, atendimento, ordens de serviço e financeiro em uma única aplicação.

O Sputnik é uma aplicação web para gestão de oficinas automotivas, desenvolvida com **React + TypeScript** no frontend e **Java 21 + Spring Boot + PostgreSQL/H2** no backend. O projeto combina controle de estoque, cadastros, ordens de serviço, agenda e acompanhamento financeiro, com acesso orientado por permissões.

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
| Frontend | React, TypeScript, Vite, Tailwind, shadcn/ui, TanStack Query e Recharts |
| Backend | Java 21 (threads virtuais), Spring Boot, Spring MVC e Spring Data JPA |
| Banco de dados | H2 em arquivo (local) ou PostgreSQL com migrações Flyway (perfil `postgres`) |
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

- JDK 21+ (o bytecode é gerado para Java 21; JDKs mais novos, como 25 ou 26, também servem)
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

### Com PostgreSQL (perfil de produção)

O perfil `postgres` cria o esquema pelas migrações Flyway em `src/main/resources/db/migration` e o Hibernate apenas valida (`ddl-auto=validate`). Sem Docker, use um PostgreSQL instalado ou gerenciado e ajuste `SPUTNIK_DB_URL`.

**Tudo em contêineres (app + banco):**

```bash
cp .env.example .env   # defina SPUTNIK_DB_PASSWORD e, na primeira subida, SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD
docker compose up -d --build
```

O `Dockerfile` compila o frontend e o backend a partir do código (não usa o bundle versionado em `static/`) e roda o jar com JRE 21, usuário sem privilégios e healthcheck em `/login`. O app fica em [http://localhost:8080](http://localhost:8080); o banco só aceita conexões do próprio computador (`127.0.0.1:5432`) e os dados ficam no volume `sputnik-pg`.

**Só o banco em contêiner, app pelo IntelliJ ou Maven:**

```powershell
docker compose up -d postgres
$env:SPRING_PROFILES_ACTIVE = "postgres"
mvn spring-boot:run
```

| Variável | Padrão |
| --- | --- |
| `SPUTNIK_DB_URL` | `jdbc:postgresql://localhost:5432/sputnik` |
| `SPUTNIK_DB_USER` | `sputnik` |
| `SPUTNIK_DB_PASSWORD` | (obrigatória) |
| `SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD` | só na primeira subida, com o banco vazio |
| `SPUTNIK_COOKIE_SECURE` | `false`; use `true` atrás de HTTPS |

Mudanças de esquema entram como um novo arquivo `V2__descricao.sql`, `V3__...`; um arquivo já aplicado nunca é editado. Os dados do H2 local não são copiados para o PostgreSQL automaticamente, e o backup do console do desenvolvedor usa o comando do H2: no PostgreSQL, use `pg_dump` ou o backup do provedor.

### Deploy público (Render)

O [`render.yaml`](render.yaml) descreve o deploy inteiro: PostgreSQL gerenciado e o app pela imagem do `Dockerfile`, já ligados entre si, com HTTPS, cookie seguro e IP real do cliente (`X-Forwarded-For`) para o bloqueio de login.

1. Crie uma conta em [render.com](https://render.com) e conecte o GitHub.
2. **New → Blueprint** e escolha este repositório.
3. Quando pedido, informe `SPUTNIK_BOOTSTRAP_ADMIN_PASSWORD` (12+ caracteres com maiúscula, minúscula, número e símbolo) e confirme.
4. Ao terminar, o link fica em `https://sputnik-XXXX.onrender.com`. Entre com `admin` e essa senha; o sistema pede a troca no primeiro acesso.

A cada push na `main` o Render publica de novo (`autoDeploy`). No plano gratuito, o app hiberna após ~15 min sem acesso (a primeira visita depois disso leva cerca de 1 min) e o PostgreSQL gratuito expira após 30 dias; para uso real, troque os dois `plan: free` por um plano pago.

### Testes

```bash
mvn test
```

Os testes usam H2 em memória; `PostgresMigracaoIntegrationTest` sobe um PostgreSQL embutido (sem Docker), aplica o Flyway, valida o mapeamento e executa um fluxo de estoque.

### Integração contínua

A cada push na `main` e em pull requests, o GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) roda três verificações em paralelo:

| Job | O que garante |
| --- | --- |
| Backend | `mvn test`: os 38 testes, incluindo o PostgreSQL embutido e o contrato OpenAPI. |
| Frontend | Tipos da API iguais ao `openapi.json`, `check`, `format:check`, testes e build. |
| Imagem Docker + compose | `docker compose up --build --wait` até o healthcheck passar e login real do admin inicial. |

### Contrato da API (OpenAPI → TypeScript)

Os tipos que o frontend usa para conversar com a API são gerados, não escritos à mão:

1. `OpenApiContratoTest` gera a especificação OpenAPI da API e compara com `frontend/openapi.json`. Se a API mudou, o teste regrava o arquivo e falha.
2. Em `frontend`, `pnpm run api:types` gera `src/types/api.ts`; `src/types/domain.ts` dá nomes curtos a esses tipos.
3. `pnpm run check` aponta toda tela que ficou incompatível com a API.

Campos de resposta são obrigatórios por padrão; os que podem vir vazios levam `@Schema(nullable = true)` no record. A especificação não é exposta pela aplicação em execução (`springdoc.api-docs.enabled=false`). Os dados locais da aplicação são gravados em `data/sputnik` e não são enviados ao Git.

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
