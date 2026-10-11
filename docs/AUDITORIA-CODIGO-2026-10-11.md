# Auditoria de código — Sputnik (2ª rodada)

Data: 11/10/2026. Escopo: estrutura do projeto, backend Java/Spring, frontend React, telas Thymeleaf, scripts legados, dependências Java e JavaScript e segurança. Esta rodada parte do estado deixado pela auditoria de 24/09/2026 (`AUDITORIA-CODIGO-2026-09-24.md`) e não repete os 21 itens já corrigidos ali.

## 1. Resultado executivo

| Verificação | Antes | Depois |
| --- | --- | --- |
| Testes Java (Maven) | 32 aprovados | 35 aprovados (3 regressões novas) |
| Testes do frontend (`node --test`) | 9 aprovados | 9 aprovados |
| TypeScript (`tsc --noEmit`, estrito) | sem erros | sem erros |
| `javac -Xlint:all` | sem avisos | sem avisos |
| Spring Boot | 3.3.5 (fora de suporte) | 3.5.16 |
| `pnpm audit` | 10 vulnerabilidades (1 crítica, 5 altas, 4 moderadas) | 0 |
| Prettier (frontend) | conforme | conforme |
| `git diff --check` | — | sem erros |

Foram encontradas e corrigidas 3 falhas de segurança, 3 erros de execução e problemas de dependências. A padronização de sintaxe foi verificada por comparação de bytecode: o código compilado ficou idêntico, exceto pela remoção de um método privado nunca chamado.

## 2. Estrutura do projeto

A organização está coerente e não exigiu reestruturação:

- `auth`, `security`, `tenant`: sessão, CSRF, limites de requisição/tentativas, cabeçalhos e isolamento por empresa;
- `controller/api` (SPA React) e `controller` (telas Thymeleaf legadas), com regras concentradas em `service`;
- `domain` + `repository` (JPA/H2), com `EntidadeDaEmpresa` aplicando o filtro Hibernate por empresa;
- `frontend/` com build próprio, sincronizado em `src/main/resources/static`.

Pontos confirmados como corretos nesta rodada: todas as rotas da API e das telas verificam permissão; toda busca por ID em entidade de empresa passa por `EmpresaContext.exigirPertencimento` (o filtro Hibernate não cobre `findById`); não há SQL nativo nem concatenação de consultas; o HTML e o PDF da OS escapam o conteúdo; os templates não usam `th:utext`; o download de backups valida nome e caminho; não há segredos versionados no código nem no histórico do Git.

## 3. Achados e correções

| ID | Severidade | Problema | Correção e evidência |
| --- | --- | --- | --- |
| B01 | Média | O login de uma conta inexistente respondia sem executar o BCrypt, bem mais rápido que o de uma conta existente. O tempo de resposta revelava quais e-mails/usuários estão cadastrados. | `UsuarioService.autenticar` verifica a senha contra um hash de referência quando a conta não existe. Teste `UsuarioServiceAutenticacaoTest`. |
| B02 | Média | O cadastro público de empresa autenticava a sessão sem trocar o identificador nem renovar o token anti-CSRF (fixação de sessão). Recurso desabilitado por padrão (`SPUTNIK_SAAS_SELF_SIGNUP_ENABLED`). | `changeSessionId()` e renovação do token, como no login; o frontend descarta o token antigo após o cadastro. Teste `cadastroPublicoRenovaSessaoETokenAntiCsrf`. |
| B03 | Baixa | O mapa de tentativas de login por IP nunca era limpo; origens que não voltavam acumulavam memória indefinidamente. | Limpeza periódica das janelas vencidas, no mesmo padrão do limitador de requisições da API. |
| B04 | Alta | Abrir OS ou orçamento com um veículo cadastrado sem proprietário gerava `NullPointerException` e erro 500. | Validação explícita com a mensagem já existente ("O carro selecionado não pertence à pessoa cadastrada."). Teste `ordemComVeiculoSemProprietarioEhRecusadaSemErroInterno` reproduz o erro na versão anterior. |
| B05 | Baixa | Na tela legada de edição de OS, um erro de validação combinado com um ID inexistente ou de outra empresa gerava erro 500. | Redireciona para `/ordens?erro=ordem`, como as demais rotas da tela. |
| B06 | Baixa | `downloadApi` revogava a URL do arquivo no mesmo ciclo do clique, o que pode cancelar o download em alguns navegadores. | Revogação adiada com `setTimeout`. |
| D01 | Alta | Spring Boot 3.3.5 está fora do suporte de código aberto e não recebe correções de segurança (Spring Framework, Tomcat, Hibernate). | Atualizado para 3.5.16, última versão da linha 3.x, ainda com Java 17. Suíte completa aprovada; uma cópia do banco real foi aberta pelo H2 novo com contagens idênticas, e a aplicação subiu sobre essa cópia com login, CSRF e cabeçalhos verificados. |
| D02 | Crítica (build) | `pnpm audit`: 10 vulnerabilidades. Nove vinham da CLI `shadcn`, declarada como dependência da aplicação sem nenhum uso em tempo de execução; uma do `source-map-js` (build). Nenhuma chegava ao bundle do navegador. | CLI removida (os componentes já estão em `src/components/shadcn`; a CLI pode ser usada sob demanda com `pnpm dlx`), override `source-map-js >= 1.2.2`. Resultado: 0 vulnerabilidades. |
| D03 | Média | Especificadores `latest` no `package.json` (React, Vite, TypeScript etc.) atualizavam versões sem aviso a cada nova resolução do lockfile. Item pendente da auditoria anterior. | Fixados nas versões já travadas no lockfile. Nenhuma versão direta mudou (React 19.2.8, Vite 8.2.2, TypeScript 7.0.2). |

## 4. Padronização de sintaxe (sem alteração funcional)

- Todo o Java formatado no padrão já declarado no `.editorconfig` (4 espaços, 100 colunas) com google-java-format `--aosp`. Antes havia arquivos com linhas de até 370 caracteres e classes inteiras em uma linha.
- 78 nomes de classe totalmente qualificados no meio do código substituídos por `import`.
- `@Override` adicionado aos métodos implementados em `FormSecurityConfig`.
- Removidos o import sem uso em `MigracaoMultiempresa` e o método privado `CadastrosService.buscarVeiculo`, que nunca era chamado.
- `legacy.js` e `os-documento.js`, antes escritos em linhas únicas, formatados com Prettier; `node --check` aprovado.
- README do frontend atualizado: citava `src/styles.css`, que não existe mais.

**Evidência de equivalência:** o `src/main` foi compilado antes e depois da padronização e as classes foram comparadas com `javap -c -p`. A única diferença é a ausência do método morto removido.

## 5. Bundle do frontend

O bundle foi recompilado com a correção B06. O CSS passou de 73,3 kB para 60,7 kB. A diferença corresponde somente a 109 utilitários Tailwind que o build anterior gerava a partir dos arquivos do pacote `shadcn` removido. O CSS novo é um subconjunto estrito do anterior e nenhuma dessas classes é usada no código ou nas folhas de estilo do projeto.

## 6. Recomendações não aplicadas (decisões de negócio ou de implantação)

1. **Política de senha:** o primeiro acesso exige 8 caracteres com maiúscula, minúscula, número e símbolo; o administrador inicial e o cadastro público exigem 12. Recomenda-se unificar em 12, o que muda a regra exibida aos usuários.
2. **Senha temporária criada pelo administrador:** `UsuarioCadastroForm` aceita qualquer senha não vazia. A troca no primeiro acesso é obrigatória, mas um mínimo de tamanho reduziria o risco no intervalo entre o cadastro e o primeiro login.
3. **H2 com `AUTO_SERVER=TRUE` e usuário sem senha:** o modo misto abre uma porta TCP local, protegida por chave aleatória, para que o IntelliJ acesse o banco com a aplicação rodando. Antes de expor o servidor em rede, desative o modo ou defina credenciais via `SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD`.
4. **Cookie seguro:** em produção com HTTPS, defina `SPUTNIK_COOKIE_SECURE=true` (já documentado em `application.properties`).
5. **Limites em memória:** revogação de sessões e limites de tentativas/requisições continuam locais à JVM (item 5 da auditoria anterior). Para várias instâncias, é necessário armazenamento compartilhado.
6. **Spring Boot 4:** a linha 3.5 é a última da série 3.x. Uma migração futura para a 4.x exige rodada própria de testes.

## 7. Como reproduzir

```powershell
# Na raiz sputnik-java
mvn -B test
git diff --check

# Em frontend/
pnpm install --frozen-lockfile
pnpm run check
pnpm run test
pnpm audit
pnpm run build:java
```

Regressões novas:

- `src/test/java/br/edu/fag/sputnik/controller/api/RevisaoSegurancaIntegrationTest.java`;
- `src/test/java/br/edu/fag/sputnik/auth/UsuarioServiceAutenticacaoTest.java`.

Os três testes novos falham na versão anterior às correções e passam na atual.

## 8. Limitações

Esta auditoria não substitui um teste de intrusão nem uma revisão visual completa em todos os navegadores. Não foi executado um scanner de CVEs de Maven com base NVD. A atualização do Spring Boot foi validada pela suíte de testes e por um teste de fumaça sobre uma cópia do banco, não por um uso prolongado em produção. O banco original em `data/` não foi alterado.
