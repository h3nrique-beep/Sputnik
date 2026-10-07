# Auditoria de código — Sputnik

Data: 24/09/2026. Escopo: projeto local `sputnik-java`, backend Java/Spring, interface React, telas Thymeleaf, segurança, regras de estoque e distribuição dos arquivos estáticos.

## 1. Resultado executivo

A verificação inicial não encontrou erros de sintaxe ou tipos que impedissem a compilação. Os 18 testes Java existentes passavam, mas não cobriam problemas reais de isolamento entre empresas, sobreposição de cadastros e alguns fluxos de autenticação.

Foram corrigidos os 21 grupos de problemas descritos abaixo. A suíte foi ampliada com 13 testes Java e 5 testes do frontend. O objetivo foi preservar os recursos existentes e bloquear operações inválidas, sem substituir o framework, adicionar bibliotecas ou reformular a interface.

Resultado consolidado:

| Verificação | Resultado |
| --- | --- |
| Java: compilação e testes Maven | 31 testes, zero falhas, zero erros, zero ignorados |
| TypeScript | Verificação de tipos concluída, sem erros |
| Testes do cliente HTTP e datas | 5 aprovados, zero falhas |
| Build Vite | Aprovado; 1.896 módulos processados |
| JavaScript das telas legadas | `node --check` aprovado |
| Auditoria pnpm | Zero vulnerabilidades reportadas; 449 dependências na contagem da ferramenta |
| Consistência do lockfile pnpm | Verificação offline com lockfile congelado aprovada |
| Sincronização do frontend | HTML, CSS e JS idênticos em dist, recursos Spring e target/classes |
| Integridade do diff | `git diff --check` sem erros |

Esses resultados **não significam ausência garantida de qualquer defeito ou vulnerabilidade**. As limitações e os cuidados de implantação estão nas seções 6 e 7.

## 2. Método e abrangência

Inventário final: 106 arquivos Java de produção, 8 arquivos de testes Java, 63 arquivos em `frontend/src` e 21 templates HTML.

A compilação abrange o código-fonte Java e TypeScript. A revisão manual foi orientada pelos fluxos de autenticação, autorização, cadastros, estoque, orçamento, OS, financeiro, documentos, backups e consumo da API. Foram examinados controladores, serviços, entidades, repositórios, filtros, configuração, componentes e mecanismos de build associados a esses fluxos.

Os testes de integração novos usam H2 em memória, com `create-drop`, **sem uma transação externa envolvendo cada teste HTTP**. Isso reproduz melhor a abertura e o fechamento das transações de cada requisição. Na primeira execução, os cinco testes novos reproduziram quatro falhas de asserção e uma exceção não tratada na versão original.

As alterações anteriores de dashboard, tema escuro e botões já estavam presentes no diretório de trabalho. Foram preservadas; não são apresentadas como correções produzidas por esta auditoria.

Foi aplicada a orientação Ponytail Ultra: aproveitar Spring, Hibernate, APIs nativas do navegador e o executor de testes do Node, concentrando as correções em pontos compartilhados, sem novas dependências.

## 3. Achados e correções

As severidades abaixo representam o impacto potencial da versão anterior, não evidência de que alguém tenha explorado uma falha.

| ID | Severidade | Problema identificado | Correção e evidência |
| --- | --- | --- | --- |
| A01 | Crítica | O filtro de empresa era ativado em uma sessão Hibernate temporária, fora das transações dos repositórios. Uma empresa podia receber cadastros de outra. | `EmpresaContextFilter` vincula o EntityManager filtrado à requisição, reutilizado pelas transações, e libera os recursos no final. Reproduzido e testado sem `@Transactional` no teste. |
| A02 | Crítica | O JSON de criação de clientes/peças aceitava identificadores internos. Foi possível sobrescrever um cliente existente no teste. | IDs de clientes/peças e empresa são somente leitura no JSON; a empresa é atribuída pelo contexto autenticado. Binding MVC também bloqueia campos internos. Teste confirma preservação do cadastro original e criação na empresa correta. |
| A03 | Crítica | As telas administrativas MVC usavam a empresa padrão, não a empresa do administrador conectado. | Todas as operações dessas telas passam a empresa da sessão. Teste verifica listagem e impede reset de senha de outra empresa. |
| A04 | Crítica | Qualquer administrador de empresa podia listar/criar/baixar backups de todo o banco da instalação. | Endpoints técnicos exigem administrador da empresa configurada para administrar a instalação. Teste confirma bloqueio de leitura, listagem, diagnóstico e criação por outra empresa. Consulte a configuração na seção 7. |
| A05 | Alta | Alteração de permissões, reset de senha ou exclusão de usuário não removiam a autenticação das sessões abertas. | Registro de sessões com referências fracas e revogação da autenticação após essas operações. A definição de senha definitiva também revoga autenticações anteriores. Teste cobre permissões, reset e exclusão. |
| A06 | Alta | A proteção CSRF abrangia a API, mas não os formulários antigos. | O filtro valida também formulários MVC; o processador nativo do Spring/Thymeleaf inclui o campo oculto automaticamente. Testes verificam token renderizado, rejeição de POST sem token e envio legítimo. |
| A07 | Alta | O dashboard retornava dados de ordens a usuários que tinham apenas permissão de estoque. | O backend respeita as permissões por seção; mantém o formato da resposta com coleções vazias e contagens neutras para módulos não autorizados. Regressão com usuário restrito a peças. |
| A08 | Alta | Leituras seguidas de escrita de saldo/OS não tinham proteção contra alteração concorrente. | Bloqueio pessimista e atualização do estado antes das mutações. Teste simultâneo: apenas uma das duas saídas da última unidade é aceita; saldo final zero. Orçamento com peça repetida também é testado. |
| A09 | Alta | Uma movimentação igualava estoque disponível ao saldo total, apagando a diferença entre eles. Cadastros também aceitavam disponível maior que total. | Preservação da diferença, saída limitada ao disponível e total, validação da consistência e proteção contra estouro na soma de entradas. Disponível omitido herda o saldo. Teste de entrada com disponibilidade parcial e saída excessiva. |
| A10 | Alta | Uma OS aberta podia voltar ao status ORCAMENTO pela edição comum, contornando o ciclo normal de aprovação. | Transição rejeitada; abertura de orçamento e aprovação normal preservadas. Regressão verifica a rejeição e a baixa normal. |
| A11 | Média | Ranking de peças usadas consultava itens de todas as empresas e incluía peças ainda não consumidas de orçamentos. | Consulta explícita por empresa da OS e inclusão apenas de itens com estoque baixado. Teste cobre orçamento pendente e isolamento do ranking. |
| A12 | Média | Exportação CSV não escapava separadores, aspas ou quebras de linha; textos podiam ser interpretados como fórmulas. | Campos textuais são escapados e prefixos de fórmula neutralizados. Teste cobre ponto e vírgula, aspas, quebra de linha, `=` e `@`. |
| A13 | Alta | A troca obrigatória de senha redirecionava chamadas JSON para HTML, quebrando consulta da sessão e saída. Formulário sem senha podia gerar NullPointerException. | Rotas mínimas de sessão/CSRF/logout permanecem acessíveis; demais APIs recebem 403 em JSON. Comparação de senhas é segura para nulos. Regressão cobre sessão, operação bloqueada, formulário vazio e logout. |
| A14 | Média | Referências inválidas em cadastros podiam gerar 500; JSON incorreto e conflitos não tinham resposta uniforme para a interface. | Tratamento central de erros da API: entrada inválida 400; integridade e concorrência 409, sem retornar detalhes de SQL ou stack trace. Regressões cobrem referência inexistente e JSON inválido. |
| A15 | Média | Buscas podiam exibir uma resposta antiga após o usuário alterar o texto; rejeições de rede não eram tratadas em vários carregamentos. | Busca compartilhada cancela solicitações anteriores e apresenta estado de carregamento/erro. Tratamento de falhas em cadastros, ordens, agenda, assinatura e financeiro. TypeScript e build aprovados; não houve simulação visual exaustiva de rede. |
| A16 | Média | Headers personalizados substituíam os headers gerados pelo cliente HTTP, podendo retirar CSRF e Content-Type. | Uso de `Headers` e ordem correta de composição; FormData preserva o Content-Type gerado pelo navegador. Quatro testes HTTP cobrem composição, renovação do token, erro/cancelamento e FormData. |
| A17 | Média | Lista de itens usava objetos como chave React; trocar o usuário selecionado podia manter as permissões do usuário anterior. | Chaves estáveis para itens, detalhe da OS e editor de permissões. Erro antigo do detalhe é limpo após recarregamento bem-sucedido. Quilometragem zero não aparece mais como ausente. |
| A18 | Média | O financeiro calculava datas civis convertendo para UTC, podendo antecipar o dia perto da meia-noite local. | Função de data local usada em vencimento e filtros. Teste cobre fim do dia e virada do ano. |
| A19 | Média | Scripts inline e `onclick` das telas antigas eram bloqueados pela própria Content Security Policy. | Scripts de senha, confirmação e impressão movidos para arquivo local permitido, sem relaxar `script-src`. Sintaxe JS validada; os templates continuam sendo renderizados nos testes MVC. |
| A20 | Média | Backups criados no mesmo segundo podiam ter o mesmo nome; download carregava todo o ZIP na memória. | Nome com UUID além da data; envio por `FileSystemResource`. Revisão estática e compilação; não foi feito backup/restauração do banco real nesta auditoria. |
| A21 | Média | `package-lock.json` continha apenas parte das dependências atuais, divergindo de `package.json` e `pnpm-lock.yaml`. | Padronização em pnpm 11.25.0, remoção do lockfile npm obsoleto, comandos de documentação atualizados e script de build independente de chamada interna ao npm. Verificação offline do lockfile congelado aprovada. |

A sugestão e a ordenação de reposição também passaram a usar o estoque disponível, coerentemente com a identificação de peças críticas, em vez de usar o saldo total que poderia incluir unidades indisponíveis.

## 4. Funcionalidades preservadas e verificadas

Os testes existentes continuaram cobrindo:

- cadastro de cliente e vínculo de veículo;
- abertura de OS, quilometragem, agendamento, retorno e garantia;
- inclusão de peças e serviços, cálculo de totais e histórico;
- orçamento sem baixa imediata e aprovação com baixa;
- lançamento e liquidação financeira;
- cadastro de empresa e plano de teste;
- gestão de usuários, permissões e senha temporária;
- proteção de rotas, separação entre permissão financeira e relatório;
- inicialização, migração de empresa e documento/PDF da OS.

Os novos testes acrescentam os cenários de segurança e estoque da tabela. Um identificador de administrador fictício do teste antigo foi ajustado porque coincidia com o ID de um usuário real criado no próprio banco de teste; isso passou a ser detectado pela revogação correta de sessões. A correção da massa de teste não removeu nenhuma asserção funcional.

Durante o desenvolvimento, uma regressão de agendamento causada pelo recarregamento de entidades foi detectada pela suíte e corrigida antes da entrega. A atualização de entidades com bloqueio passou a preservar alterações pendentes da transação.

## 5. Evidências e reprodução

Ambiente observado: Java 17.0.12, alvo de compilação Java 17, Node 24.19.0, pnpm 11.25.0 e Vite 8.2.2. Não houve atualização de versão do Spring Boot nem das bibliotecas Java.

Com Maven e pnpm disponíveis no PATH, os equivalentes das verificações são:

```powershell
# Na raiz sputnik-java
rtk proxy mvn -B test
rtk proxy git diff --check

# No diretório frontend
rtk proxy pnpm run check
rtk proxy pnpm run test
rtk proxy pnpm run build:java
rtk proxy pnpm audit --json
rtk proxy pnpm install --frozen-lockfile --lockfile-only --offline --ignore-scripts
```

Nesta máquina, o Maven foi executado pelo caminho incluído no IntelliJ, com `-Dmaven.repo.local=C:\Users\henri\.m2\repository`. As primeiras tentativas restritas falharam por acesso ao cache ou à rede; foram repetidas com autorização. Esses bloqueios do ambiente não eram erros de sintaxe do Sputnik.

Arquivos de regressão:

- `src/test/java/br/edu/fag/sputnik/controller/api/AuditoriaApiIntegrationTest.java`;
- `src/test/java/br/edu/fag/sputnik/service/PecasAuditoriaTest.java`;
- `frontend/tests/api.test.mjs`.

Os resultados detalhados do Maven são gerados em `target/surefire-reports`; esse diretório é saída de build, não um registro permanente versionado.

Bundle final: `index-B97oUaO6.js` (306,01 kB; gzip 91,50 kB) e `index-0IbtHblS.css` (60,11 kB; gzip 13,05 kB). A igualdade dos bytes foi conferida entre `frontend/dist`, `src/main/resources/static` e `target/classes/static`.

A auditoria pnpm retornou: 376 dependências, 3 dependências de desenvolvimento, 70 opcionais, total 449; todas as categorias de vulnerabilidade em zero. Trata-se do resultado do serviço naquele momento, não de certificação de segurança. A consulta inicial ao lockfile npm antigo não foi usada como evidência final, pois não representava a interface atual.

## 6. Limitações, riscos remanescentes e decisões preservadas

1. **Dependências Java:** não foi executado um scanner completo de CVEs transitivas de Maven. A aprovação da compilação e dos testes não atesta que Spring Boot 3.3.5 e suas dependências estejam livres de vulnerabilidades. É necessária uma avaliação de atualização antes de exposição pública.
2. **Banco real e recuperação:** não foram feitas operações destrutivas, restauração de backup, correção de saldos históricos ou simulação de indisponibilidade no banco de produção. Não é possível concluir se os problemas antigos chegaram a afetar dados reais ou se houve exploração.
3. **Revisão visual:** não foi executada uma bateria E2E em todos os navegadores, resoluções e combinações de permissões. A corrida da busca foi corrigida por cancelamento e revisão de código, mas não recebeu um teste de componente React com rede controlada.
4. **Carga e concorrência:** há uma regressão de duas retiradas simultâneas da mesma unidade e uma de orçamento com peça repetida. Isso não substitui teste de carga, disputa de várias peças em ordens diferentes ou simulação de falha de infraestrutura.
5. **Múltiplas instâncias:** a revogação de sessões e os limites de requisição/tentativas são locais à JVM. Para vários servidores ou sessões persistidas externamente, será necessário um mecanismo compartilhado. A solução atual acompanha a implantação local existente.
6. **Contexto de empresa fora de HTTP:** serviços executados por futuros jobs, threads ou processamento assíncrono precisarão estabelecer explicitamente o contexto/transação. Os testes novos validam os fluxos HTTP síncronos existentes.
7. **Unicidade entre empresas:** código de peça e placa continuam sujeitos às restrições globais existentes no banco. Permitir o mesmo código/placa em empresas diferentes exige uma migração planejada para chaves compostas; isso não foi realizado silenciosamente.
8. **Regras comerciais não redefinidas:** não alterei políticas de reabertura/cancelamento de OS, devolução de estoque, histórico de quilometragem, cobrança ou limite de usuários sob cadastro concorrente. São decisões de negócio a especificar antes de ampliar o SaaS.
9. **Escalabilidade:** várias listagens e indicadores carregam conjuntos completos e agregam em memória. Não foram adicionadas paginação, índices novos ou reescritas amplas de consultas sem medição em uma base representativa.
10. **Versões e instalação:** permanecem alguns especificadores `latest` no manifesto, mas a instalação documentada usa `pnpm-lock.yaml` congelado. Atualizações deliberadas de dependências devem gerar nova rodada de testes. Não foi realizada uma instalação do zero em outra máquina.
11. **Operação de backups:** a autorização negativa foi testada, mas criação, streaming e restauração de ZIP real não foram exercitados contra o banco do usuário. O diagnóstico ainda considera o caminho local padrão do banco.

O relatório não inclui um percentual de cobertura, porque não foi executada uma ferramenta de cobertura de linhas/branches, nem uma alegação de inspeção manual de cada linha.

## 7. Aplicação e cuidados operacionais

- As correções estão no projeto local compartilhado com o IntelliJ. Não foi criado commit, enviado push ou feita implantação remota.
- O frontend foi compilado e sincronizado. Reinicie o Sputnik pelo IntelliJ para garantir que toda a versão Java nova seja carregada; uma aba do navegador já aberta precisa ser recarregada.
- Faça novo login após o reinício. As sessões da versão anterior não devem ser reutilizadas para validar as novas regras.
- Backups globais são restritos à empresa administradora da instalação. A propriedade é `sputnik.backup.admin-empresa-id`, configurável por `SPUTNIK_BACKUP_ADMIN_EMPRESA_ID`, com padrão **1**. Em uma base cujo administrador da instalação pertença a outra empresa, configure o ID correto antes de usar o console. Não conceda esse acesso a empresas clientes.
- Formulários abertos antes da atualização podem não conter o token CSRF novo: recarregue a página antes de enviar.
- O código e a configuração existentes continuam exigindo atenção ao HTTPS/cookie seguro ao sair de localhost. A auditoria não configurou proxy, certificado ou servidor público.
- Foi removido apenas o lockfile npm desatualizado, recuperável pelo histórico Git. O sincronizador substituiu assets gerados antigos; eles são reproduzíveis pelo build. Dados, backups e logs existentes do usuário não foram apagados.

## 8. Conclusão

A auditoria encontrou falhas de execução, segurança e consistência que não apareciam na compilação inicial. Elas foram tratadas nos pontos compartilhados e protegidas por regressões, mantendo os fluxos funcionais existentes testados.

O projeto local está com compilação e testes aprovados. A liberação pública ou multiempresa em produção deve considerar explicitamente as limitações da seção 6, principalmente dependências Java, validação operacional dos backups e implantação distribuída.
