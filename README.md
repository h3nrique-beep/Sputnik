# Sputnik 2.0 — Gestão de Estoque Automotivo

Aplicação web de gestão de estoque e ordens de serviço para oficina, construída com **React, TypeScript, Java 17, Spring Boot e H2**. As páginas Thymeleaf anteriores permanecem disponíveis.

## O que está implementado

- Login por **e-mail (Balconista) ou usuário (Admin)** e senha protegida por hash BCrypt, com perfis e permissões persistidos no H2.
- Área exclusiva do administrador para cadastrar usuários e definir os módulos liberados para cada Balconista.
- Senha temporária definida pelo Admin e troca obrigatória no primeiro acesso do Balconista, com política de senha forte.
- Cadastro de peças com validação e bloqueio de código duplicado (RN02).
- Consulta por nome, código ou fabricante e filtros de itens críticos/zerados.
- Entrada e saída de estoque, com bloqueio de saldo negativo (RN03/RN05).
- Histórico auditável: operador, data, quantidade, saldo após a operação e observação.
- Relatório de reposição e exportação CSV.
- Banco H2 persistente em `data/sputnik`.
- Clientes e veículos vinculados, abertura e edição de OS, status, itens, totais e histórico.
- Documento A4 de OS com dados complementares em modal, impressão e download em PDF.
- Console administrativo com diagnóstico do ambiente e backups.

## Documentação

- [Arquitetura e lógica do sistema](docs/ARQUITETURA.md)
- [Guia de abertura e execução no IntelliJ IDEA](docs/INTELLIJ.md)
- [Mapa atual dos arquivos e responsabilidades](ARCHITECTURE.md)
- [Modelo de documento da OS](docs/MODELO-OS.md)

## Como executar

1. Instale o JDK 17 e Maven 3.9+.
2. Abra o terminal na pasta do projeto.
3. Execute:

```bash
mvn spring-boot:run
```

4. Abra `http://localhost:8080`.

Para atualizar a interface servida na porta 8080, execute `npm run build:java` dentro de `frontend`. Para desenvolvimento com Vite, use `npm run dev`. Os testes Java usam H2 em memória; os dados locais permanecem em `data/`.

## Estrutura

- `domain`: entidades JPA e enums.
- `repository`: acesso ao banco de dados.
- `service`: regras separadas entre peças/estoque, cadastros e ordens; fachada compatível com as chamadas existentes.
- `controller/api`: API REST separada por área, com DTOs e conversores próprios.
- `controller`: controladores das páginas legadas.
- `frontend/src/features`: telas React por funcionalidade.
- `frontend/src/components/ui`: componentes compartilhados.
- `frontend/src/styles`: estilos organizados por área, com cascata preservada.
- `templates`: telas Thymeleaf.
- `static/app.css`: interface responsiva inspirada no protótipo Sputnik 2.0.

## Acesso temporário

| Login | Senha | Perfil | Acesso |
| --- | --- | --- | --- |
| `balconista@sputnik.com.br` | `Sputnik@123` | Balconista | Peças e ordens de serviço |
| `admin` | `Admin#2026!` | Administrador | Todos os módulos e relatórios |

Os usuários ficam nas tabelas `usuarios_sistema` e `usuario_permissoes` do H2. O administrador pode criar novos usuários pela opção **Usuários** do menu. Os dois acessos acima são criados somente quando o banco ainda não tem usuários.
