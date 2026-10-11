# Front-end React do Sputnik

Este diretório contém a nova interface em React + TypeScript. O back-end Spring Boot continua responsável por autenticação, permissões, estoque, cadastros, ordens de serviço e banco H2.

## Primeiro uso

1. Instale o Node.js LTS (inclui o NPM).
2. Abra um terminal nesta pasta (`frontend`).
3. Execute `pnpm install --frozen-lockfile`.
4. Inicie o back-end Java na porta 8080.
5. Execute `pnpm run dev`.
6. Abra `http://localhost:5173`.

Durante o desenvolvimento, o Vite encaminha as chamadas `/api` para o Spring Boot em `http://localhost:8080` e mantém o login por sessão.

## Estrutura

- `src/main.tsx`: ponto de entrada da aplicação.
- `src/app/`: shell, navegação e roteamento entre telas.
- `src/features/`: telas por módulo (estoque, ordens, financeiro, usuários etc.).
- `src/components/`: UI compartilhada; `components/shadcn/` guarda os componentes shadcn/ui já copiados para o projeto.
- `src/lib/api.ts`: cliente HTTP único, com sessão e token anti-CSRF.
- `src/styles/`: tema, layout e estilos de impressão.
- `vite.config.ts`: servidor do React e proxy para o Java.
- `pnpm-workspace.yaml`: overrides de segurança de dependências transitivas.

## Comandos

- `pnpm run check`: verificação de tipos.
- `pnpm run test`: testes do cliente HTTP, datas, layout, modal e impressão.
- `pnpm run build:java`: gera o bundle e o sincroniza em `src/main/resources/static`.

Para adicionar um novo componente shadcn/ui, use a CLI sob demanda (`pnpm dlx shadcn@latest add <componente>`); ela não faz parte das dependências da aplicação.
