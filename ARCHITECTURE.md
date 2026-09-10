# Organização do Sputnik

## Back-end — Spring Boot

- `domain/`: entidades e enums do domínio da oficina.
- `repository/`: acesso persistente ao H2 por Spring Data JPA.
- `service/PecasService`: peças, movimentação, relatório mensal e reposição.
- `service/CadastrosService`: cadastro e consulta de clientes e veículos.
- `service/OrdensService`: abertura, edição, itens, histórico e indicadores de OS.
- `service/EstoqueService`: fachada de compatibilidade que delega aos três serviços; os controladores legados conservam as mesmas chamadas.
- `auth/`: sessão, credenciais e ciclo de senha.
- `web/`: formulários validados utilizados pelas rotas do Spring.
- `controller/`: páginas legadas Thymeleaf.
- `controller/api/`: API REST em `/api`, separada em autenticação, dashboard, estoque, cadastros, usuários, desenvolvedor e ordens.
- `controller/api/dto/`: contratos de entrada e saída da API, um tipo por arquivo.
- `controller/api/mapper/ApiMapper`: conversões entre entidades e respostas da API.
- `config/`: inicialização de dados, segurança de senha e interceptadores.

## Front-end — React + TypeScript

- `frontend/src/main.tsx`: inicialização do React.
- `frontend/src/app/`: composição do aplicativo e títulos da navegação.
- `frontend/src/features/`: telas agrupadas por auth, dashboard, inventory, clients, vehicles, orders, reports, users e developer.
- `frontend/src/features/orders/components/`: modal do documento da OS e definição dos campos por etapa.
- `frontend/src/components/ui/`: componentes visuais reutilizáveis, um por arquivo.
- `frontend/src/lib/api.ts`: cliente HTTP único para a API Spring e sessão.
- `frontend/src/lib/format.ts`: formatações de moeda e data.
- `frontend/src/types/`: contratos reutilizáveis; os tipos de documento não dependem da camada de interface.
- `frontend/src/styles/`: base, formulários, acesso, tema, console, impressão e modal. `index.css` mantém a ordem de carregamento para preservar a cascata.

## Execução

O Spring Boot entrega a aplicação React compilada em `http://localhost:8080`.
Durante desenvolvimento do front, execute `npm run dev` na pasta `frontend`.
Use `npm run format` para padronizar TypeScript, TSX e CSS antes de alterações maiores.

Use `npm run build:java` em `frontend` para compilar e copiar o resultado para os recursos do Spring e para `target/classes` quando já existir. `npm run check` verifica tipos e imports sem uso; `npm run format:check` verifica formatação.

## Preservação dos fluxos

A extração conserva os métodos, as 31 rotas REST e seus formatos de resposta. As operações transacionais continuam anotadas; a persistência, as entidades e o modelo de impressão/PDF mantêm o comportamento anterior. Uma versão duplicada e sem uso do console foi removida; a tela ativa continua em `features/developer`.

O Java usa formatação de quatro espaços. O IntelliJ está configurado para importar `pom.xml` e excluir dependências, saídas de build e dados locais da indexação do módulo. Consulte `docs/INTELLIJ.md`.
