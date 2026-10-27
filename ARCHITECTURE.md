# Organização do Sputnik

## Back-end — Spring Boot

- `domain/`: entidades e enums do domínio da oficina.
- `repository/`: acesso persistente ao H2 por Spring Data JPA.
- `service/PecasService`: peças, movimentação, relatório mensal e reposição.
- `service/CadastrosService`: cadastro e consulta de clientes e veículos.
- `service/OrdensService`: abertura, edição, itens, histórico e indicadores de OS.
- `service/FinanceiroService`: lançamentos, vencimentos e baixa.
- `service/OrdemServicoPdfService` e `DocumentoOsLayout`: documento A4 da OS em HTML e PDF.
- `auth/`: sessão, credenciais e ciclo de senha.
- `web/`: formulários validados utilizados pelas rotas do Spring.
- `controller/`: `SpaController` entrega o React em `/login` e `/sistemas/**`; `PrimeiroAcessoController` é a única tela Thymeleaf (troca obrigatória de senha); `ApiExceptionHandler` padroniza erros em JSON.
- `controller/api/`: API REST em `/api`, separada em autenticação, dashboard, estoque, cadastros, usuários, desenvolvedor e ordens.
- `controller/api/dto/`: contratos de entrada e saída da API, um tipo por arquivo.
- `controller/api/mapper/ApiMapper`: conversões entre entidades e respostas da API.
- Contrato OpenAPI: cada endpoint declara o tipo de resposta (`ResponseEntity<PecaDto>`, não `<?>`); erros de regra (`IllegalArgumentException`) viram 400 no `ApiExceptionHandler`, sem `try/catch` nos controladores. `config/OpenApiConfig` torna obrigatórios os campos dos `*Dto`, exceto os `@Schema(nullable = true)`. `OpenApiContratoTest` mantém `frontend/openapi.json` atualizado e `pnpm run api:types` gera `frontend/src/types/api.ts`.
- `config/`: inicialização de dados, migrações locais, codificador de senha e interceptador do primeiro acesso.
- `security/`: token anti-CSRF, cabeçalhos de segurança, limite de requisições e bloqueio de tentativas de login.
- `tenant/`: identifica a empresa da sessão e ativa o filtro Hibernate que isola os dados operacionais.

## Front-end — React + TypeScript

- `frontend/src/main.tsx`: inicialização do React.
- `frontend/src/app/`: composição do aplicativo e títulos da navegação.
- `frontend/src/features/`: telas agrupadas por auth, dashboard, catalog, inventory, clients, vehicles, orders, schedule, finance, reports, users, subscription e developer.
- `frontend/src/features/orders/`: lista e quadro (`Orders`), detalhe, modo oficina (`Workshop`), regras do quadro (`status.ts`) e troca de status (`mudarStatus.ts`).
- `frontend/src/features/orders/components/`: modal do documento da OS e definição dos campos por etapa.
- `frontend/src/components/ui/`: componentes visuais reutilizáveis, um por arquivo.
- `frontend/src/lib/api.ts`: cliente HTTP único para a API Spring e sessão, e o `queryClient` do TanStack Query. A chave de cada leitura é a própria URL (`useQuery<Order[]>({ queryKey: ["/ordens"] })`), com cache de 30 s. Toda gravação bem-sucedida (POST/PUT/DELETE) invalida o cache, e as telas abertas recarregam sozinhas, sem `load()` manual. O logout limpa o cache.
- `frontend/src/lib/format.ts`: formatações de moeda e data, variação percentual e geração de CSV.
- `frontend/src/types/`: contratos reutilizáveis; os tipos de documento não dependem da camada de interface.
- `frontend/src/styles/`: base, formulários, acesso, tema, console, impressão e modal. `index.css` mantém a ordem de carregamento para preservar a cascata.

## Banco de dados

- `src/main/resources/application.properties`: H2 em arquivo, esquema automático (uso local).
- `src/main/resources/application-postgres.properties`: perfil `postgres`, Flyway e `ddl-auto=validate`.
- `src/main/resources/db/migration/`: migrações versionadas (`V1__esquema_inicial.sql`).
- `docker-compose.yml`: serviços `postgres` (16, volume `sputnik-pg`) e `app` (perfil `postgres`, sobe só depois do banco saudável). Variáveis em `.env` (modelo em `.env.example`).
- `render.yaml`: blueprint do Render (PostgreSQL + app Docker). Passa host/porta/nome do banco separados (`SPUTNIK_DB_HOST`, `SPUTNIK_DB_PORT`, `SPUTNIK_DB_NAME`), `SPUTNIK_COOKIE_SECURE=true` e `SERVER_FORWARD_HEADERS_STRATEGY=native`, sem o qual o bloqueio de login e o limite de requisições veriam todos os usuários com o IP do proxy.
- `Dockerfile`: três estágios: Node gera o bundle, Maven empacota o jar com esse bundle, e a imagem final tem só o JRE 21 e o jar, com usuário sem privilégios e healthcheck.

## Execução

Java 21 com `spring.threads.virtual.enabled=true`: cada requisição roda numa thread virtual, então a espera por banco ou arquivo não ocupa uma thread do Tomcat. Trechos que bloqueiam I/O usam `ReentrantLock` em vez de `synchronized` (ver `BackupService`), porque no Java 21 um monitor preso fixa a thread virtual na portadora.


O Spring Boot entrega a aplicação React compilada em `http://localhost:8080`.
Durante desenvolvimento do front, execute `npm run dev` na pasta `frontend`.
Use `npm run format` para padronizar TypeScript, TSX e CSS antes de alterações maiores.

Use `npm run build:java` em `frontend` para compilar e copiar o resultado para os recursos do Spring e para `target/classes` quando já existir. `npm run check` verifica tipos e imports sem uso; `npm run format:check` verifica formatação.

## Preservação dos fluxos

Em outubro de 2026 a interface Thymeleaf antiga foi removida: 7 controladores, 20 templates, `static/js/legacy.js`, `ModeloPaginaService` e a fachada `EstoqueService`. Os controladores da API passaram a chamar diretamente `PecasService`, `CadastrosService` e `OrdensService`, que já carregam as anotações `@Transactional`. A única função que existia só na tela antiga, a exportação CSV da reposição, foi levada para `GET /api/reposicao.csv` e ganhou botão na tela de Reposição. As rotas `/api` e seus formatos de resposta não mudaram; os testes de segurança que usavam as telas antigas passaram a cobrir as mesmas regras pela API e pelo primeiro acesso.

O Java usa formatação de quatro espaços. O IntelliJ está configurado para importar `pom.xml` e excluir dependências, saídas de build e dados locais da indexação do módulo. Consulte `docs/INTELLIJ.md`.
