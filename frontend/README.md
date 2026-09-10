# Front-end React do Sputnik

Este diretório contém a nova interface em React + TypeScript. O back-end Spring Boot continua responsável por autenticação, permissões, estoque, cadastros, ordens de serviço e banco H2.

## Primeiro uso

1. Instale o Node.js LTS (inclui o NPM).
2. Abra um terminal nesta pasta (`frontend`).
3. Execute `npm install`.
4. Inicie o back-end Java na porta 8080.
5. Execute `npm run dev`.
6. Abra `http://localhost:5173`.

Durante o desenvolvimento, o Vite encaminha as chamadas `/api` para o Spring Boot em `http://localhost:8080` e mantém o login por sessão.

## Estrutura

- `src/main.tsx`: telas e fluxos principais da aplicação.
- `src/styles.css`: novo visual responsivo.
- `vite.config.ts`: servidor do React e proxy para o Java.
- `package.json`: dependências e comandos do front-end.
