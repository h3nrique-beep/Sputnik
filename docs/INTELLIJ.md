# Guia do IntelliJ IDEA

## Abrir o projeto

1. No IntelliJ IDEA, use **File → Open**.
2. Selecione a pasta `sputnik-java`, a mesma que contém o arquivo `pom.xml`.
3. Confirme a abertura como projeto Maven quando o IntelliJ solicitar.
4. Em **File → Project Structure**, confirme o SDK **Java 17**.
5. Aguarde a importação do Maven terminar. As dependências aparecerão na janela Maven e em External Libraries.

## Executar

Há duas formas equivalentes:

- Abra `SputnikApplication.java` e clique no ícone verde ao lado do método `main`.
- Na janela Maven, execute `Plugins → spring-boot → spring-boot:run`.

O sistema abre em `http://localhost:8080`.

## Desenvolvimento com atualização automática

O projeto contém `spring-boot-devtools`. Ao salvar classes Java compiladas ou recursos como HTML e CSS, o Spring Boot atualiza a aplicação. Caso uma alteração de Java não apareça automaticamente, use **Build → Build Project** e atualize o navegador.

Para alterações React, execute `pnpm run build:java` no terminal em `frontend`. Esse comando verifica os tipos, gera o bundle e sincroniza os recursos do Spring. Use `pnpm run dev` para trabalhar com o Vite durante o desenvolvimento.

## Estrutura que deve aparecer no painel Project

```text
sputnik-java
├── src/main/java/br.edu.fag.sputnik
│   ├── auth, config, controller, domain, repository, service e web
│   └── SputnikApplication.java
├── src/main/resources
│   ├── static
│   ├── templates
│   └── application.properties
├── src/test/java
├── frontend/src
│   ├── app
│   ├── components/ui
│   ├── features
│   │   ├── auth, dashboard, inventory, clients, vehicles
│   │   └── orders, reports, users, developer
│   ├── lib, types e styles
│   └── main.tsx
├── docs
├── pom.xml
└── README.md
```

No pacote `controller`, utilize a visualização **Project** do IntelliJ: cada arquivo corresponde a um módulo funcional (`PecaController`, `CadastroController`, `OrdemServicoController` e assim por diante). Isso evita concentrar alterações diferentes na mesma classe.

A API utilizada pelo React está em `controller/api`. Os tipos de resposta ficam em `controller/api/dto` e as conversões em `controller/api/mapper`. Os serviços principais são `PecasService`, `CadastrosService` e `OrdensService`; `EstoqueService` mantém compatibilidade com as chamadas anteriores.

A configuração local do módulo aponta para `sputnik.iml` e para o `pom.xml` deste projeto. Se o IntelliJ estiver aberto com uma árvore antiga, use **Reload All Maven Projects** na janela Maven. O Maven recompõe suas dependências ao importar o projeto. Não execute uma segunda instância se a porta 8080 já estiver ocupada.

`target`, `frontend/node_modules`, `frontend/.node`, `frontend/dist`, `data`, `backups` e `logs` estão excluídos da indexação do módulo. Permanecem no disco; não são fontes para edição. Java usa quatro espaços e TypeScript/CSS, dois. Execute `pnpm run format` para padronizar o frontend.

## Testes

Para executar todos os testes, clique com o botão direito em `src/test/java` e selecione **Run Tests**, ou execute no terminal integrado:

```bash
mvn test
```

Os testes Spring usam H2 em memória. O teste de regressão da API cobre cadastro e abertura de OS, consumo de peças, totais, bloqueio de saldo, permissões e reset de senha.

## Arquivos que não devem ser editados manualmente

- `target/`: saída de compilação do Maven.
- `.idea/workspace.xml`: preferências locais da sua instalação do IntelliJ.
- `data/sputnik.*`: dados persistidos do H2; só remova quando quiser reiniciar a base de demonstração.
