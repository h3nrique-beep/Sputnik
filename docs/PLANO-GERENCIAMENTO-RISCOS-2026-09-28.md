# Plano de gerenciamento de riscos do projeto Sputnik

**Projeto:** Sputnik — sistema de gestão de oficinas automotivas  
**Data-base:** 28 de setembro de 2026  
**Elaboração:** grupo responsável pelo projeto; os responsáveis abaixo são funções e devem receber os nomes dos integrantes antes da entrega.

## 1. Objetivo, contexto e método

Este plano identifica, avalia, prioriza, trata e acompanha eventos incertos que podem comprometer a entrega do Sputnik. O sistema já reúne cadastro de clientes, veículos e peças, estoque, ordens de serviço (OS), orçamentos, agenda, financeiro, usuários e permissões, além de uma base para múltiplas empresas e assinaturas. A interface principal usa React/TypeScript; a API e as regras de negócio usam Java 17 e Spring Boot; o banco local é H2 em arquivo. O cadastro público de empresas está desativado por padrão. O plano de evolução prevê PostgreSQL, migrações versionadas, integrações externas e operação SaaS, mas não os declara como concluídos.

Risco, neste trabalho, é um **evento futuro incerto** com causa e consequência para os objetivos do projeto. Uma falha já observada entra no registro como risco de recorrência ou de impacto ao ampliar o produto. A auditoria de código de 24/09/2026 corrigiu falhas importantes e aprovou 31 testes Java e 5 do frontend, mas registrou lacunas de testes de ponta a ponta, restauração de backup e implantação distribuída. Esses controles existentes foram considerados ao avaliar a exposição **residual**, especialmente antes de uma possível implantação SaaS.

O PMBOK® Guide, 8ª edição, inclui risco entre seus domínios de desempenho e orienta adaptar a gestão ao contexto do projeto. O PMI define o registro de riscos como repositório dos resultados do gerenciamento e o responsável pelo risco como quem o monitora e seleciona/implementa a resposta. A escala e os limiares a seguir são **decisões deste grupo**, não números prescritos pelo PMBOK. Fontes: [PMBOK® Guide — PMI](https://www.pmi.org/standards/pmbok) e [PMI Lexicon of Project Management Terms](https://www.pmi.org/-/media/pmi/documents/registered/pdf/pmbok-standards/pmi-lexicon-pm-terms.pdf?rev=447328d841c249af985d14177ddd5f95).

### Escala adotada

| Probabilidade | Valor | Critério de julgamento neste projeto |
| --- | ---: | --- |
| Baixa | 1 | Evento excepcional ou dependente de uma mudança ainda não planejada para a entrega atual. |
| Média | 2 | Evento plausível no próximo ciclo ou antes da implantação; já existe uma exposição técnica ou dependência conhecida. |
| Alta | 3 | Evento provável se o trabalho prosseguir sem uma decisão ou controle adicional; há precedente no projeto ou pressão concreta do escopo. |

| Impacto | Valor | Critério de julgamento neste projeto |
| --- | ---: | --- |
| Baixo | 1 | Incômodo localizado, sem comprometer dados, fluxo principal ou marco de entrega. |
| Médio | 2 | Retrabalho, atraso limitado ou falha de um módulo/critério de aceite, com alternativa operacional. |
| Alto | 3 | Exposição ou perda de dados, resultado operacional/financeiro incorreto, ou inviabilização de um marco importante. |

**Pontuação = probabilidade × impacto.** Crítico: 6 ou 9; moderado: 3 ou 4; baixo: 1 ou 2. Empates são ordenados por potencial de dano a dados, proximidade do marco e facilidade de detecção. São avaliações qualitativas do grupo, a revisar com dados de uso, prazo e volume reais. O PMI recomenda combinar probabilidade e impacto e registrar causas, respostas, gatilhos e responsáveis; a matriz abaixo adapta essa prática ao Sputnik. [Referência PMI sobre registro e análise de riscos](https://www.pmi.org/learning/library/practical-risk-management-approach-8248).

## 2. Identificação dos riscos

| ID | Nome e descrição do evento | Causa concreta no Sputnik | Consequência | Categoria | Responsável pelo risco |
| --- | --- | --- | --- | --- | --- |
| R01 | **Dados de outra oficina:** uma nova rota, relatório, exportação ou processo assíncrono deixa de aplicar o contexto da empresa e mostra/altera dados de outra oficina. | O isolamento depende da empresa da sessão, do filtro Hibernate e de verificações nos serviços; a cobertura de todas as rotas ainda é um aceite futuro. A auditoria encontrou e corrigiu uma falha desse tipo. | Exposição de dados de clientes, veículos, estoque ou finanças; perda de confiança e necessidade de investigar registros afetados. | Tecnologia / Requisitos | Líder de backend e segurança |
| R02 | **Perda ou indisponibilidade do banco:** o arquivo H2 fica corrompido, é apagado ou a máquina falha antes de existir uma cópia restaurável. | Persistência local em `data/sputnik`; há criação de ZIP local, mas restauração periódica e cópia externa não foram comprovadas na auditoria. | Paralisação da oficina e perda de cadastros, OS, movimentações e lançamentos. | Tecnologia | Responsável por dados e infraestrutura |
| R03 | **Divergência no estoque:** movimentações e baixas de OS concorrentes ou uma regra nova deixam saldo/estoque disponível incompatíveis com o histórico. | Estoque tem entradas, saídas e baixa por aprovação de orçamento; as correções atuais testam uma disputa simples, não carga extensa nem todos os caminhos futuros. | Venda/uso de peça indisponível, orçamento incorreto, retrabalho e perda financeira. | Tecnologia / Integração | Responsável pelo módulo de estoque |
| R04 | **Expansão de escopo no mesmo prazo:** recursos do plano SaaS, cobrança, comunicação, melhorias visuais e operação de produção entram na entrega acadêmica sem revisão do cronograma. | O [plano de evolução](PLANO-EVOLUCAO-PRODUTO.md) contém cinco etapas e várias decisões externas; o projeto já reúne muitos módulos. | Fluxos centrais incompletos, redução do tempo de teste e atraso da entrega. | Escopo / Prazo | Líder do grupo / gerente do projeto |
| R05 | **Migração de dados mal executada:** a passagem do H2 com esquema atualizado automaticamente para PostgreSQL com migrações versionadas altera ou perde registros legados. | Configuração atual usa H2 em arquivo e `spring.jpa.hibernate.ddl-auto=update`; a migração para produção está planejada, não implementada. | Base inconsistente, indisponibilidade, perda de histórico e atraso da entrada em produção. | Tecnologia / Integração | Responsável por banco e backend |
| R06 | **Critérios de aceite ambíguos:** grupo e representante da oficina discordam sobre quando uma OS pode ser concluída/cancelada, como devolver peça ou como reconhecer receita. | O fluxo cobre orçamento, aprovação, execução e financeiro, mas decisões de reabertura, devolução e cobrança não foram fechadas no plano técnico. | Reimplementação de regras e demonstração que não atende à expectativa do usuário. | Requisitos / Cliente | Analista de requisitos e representante do cliente |
| R07 | **Frontend diferente do backend entregue:** alterações React ficam só no servidor Vite ou o bundle estático não acompanha a versão Java. | Há build Vite, sincronização para recursos Spring e também páginas Thymeleaf legadas; são caminhos de entrega distintos. | O avaliador vê tela antiga, chamada de API incompatível ou função aparentemente ausente. | Integração | Responsável pela integração frontend/backend |
| R08 | **Ambiente não reproduzível no dia da apresentação:** JDK, Maven, Node/pnpm ou porta 8080 diferem entre máquinas. | O projeto requer Java 17, Maven e build do frontend; já houve aviso de JDK ausente no IntelliJ. | Tempo perdido em configuração e possível impossibilidade de demonstrar o sistema. | Tecnologia / Prazo | Responsável pelo ambiente e demonstração |
| R09 | **Dependência de fornecedores externos:** hospedagem, pagamentos ou envio de mensagens não são contratados/definidos a tempo de uma entrega que os prometa. | Provedores e credenciais dependem de decisão do responsável pelo produto; as integrações estão desativadas atualmente. | Módulo prometido não pode ser validado ou precisa de solução improvisada. | Integração / Cliente | Líder de produto e responsável por integrações |
| R10 | **Conhecimento concentrado:** somente um integrante entende filtros multiempresa, regras de estoque ou o build. | Backend, frontend, banco e telas legadas exigem conhecimentos diferentes; mudanças recentes atravessaram essas camadas. | Correções ficam paradas na ausência do integrante; revisão e manutenção perdem qualidade. | Equipe | Líder técnico do grupo |
| R11 | **Usabilidade insuficiente na oficina:** telas, botões ou modo escuro dificultam tarefas em celular, teclado ou balcão. | A interface está evoluindo; houve retrabalho visual recente e ainda não ocorreu bateria completa de testes de acessibilidade/responsividade. | Mais erros de operação, menor aceitação e retrabalho de interface. | Cliente / Requisitos | Responsável por UX/frontend |
| R12 | **Impressão diferente em impressora/navegador específico:** o documento da OS sai com quebra ou margem diferente da visualização. | Há impressão HTML e PDF A4; drivers e navegadores variam e não foram testados todos. | Reimpressão pontual; o download de PDF oferece alternativa. | Tecnologia / Cliente | Responsável pelo documento da OS |
| R13 | **Sessões inconsistentes em múltiplos servidores:** uma alteração de senha/permissão revoga sessão em uma JVM, mas não em outra. | Revogação e limites de tentativas/requisições usam memória local da JVM, adequada à implantação atual de instância única. | Acesso antigo permanece até expirar em outra instância. | Tecnologia / Segurança | Responsável por infraestrutura e segurança |
| R14 | **Dependência Java vulnerável ou incompatível:** uma biblioteca transitiva apresenta alerta relevante após a data da auditoria ou falha em atualização. | O frontend teve auditoria pnpm, mas não houve varredura completa de CVEs Maven; Spring Boot e outras versões permanecem fixadas. | Retrabalho de atualização, restrição à publicação ou exposição de segurança. | Tecnologia | Responsável por dependências/backend |
| R15 | **Código/placa duplicado entre oficinas:** uma segunda empresa tenta cadastrar código de peça ou placa já usados por outra e recebe conflito. | Restrições de unicidade globais herdadas do modelo de empresa única ainda exigem migração para regras por empresa. | Cadastro bloqueado e retrabalho operacional na expansão multiempresa. | Requisitos / Tecnologia | Responsável pelo modelo de dados |

## 3. Análise qualitativa e priorização

Cada avaliação abaixo mostra a probabilidade e o impacto **se o risco se materializar**. A justificativa é específica do estado atual e da evolução prevista.

| ID | Probabilidade e justificativa | Impacto e justificativa | P × I | Prioridade |
| --- | --- | --- | ---: | --- |
| R01 | **Alta:** houve falhas reais de isolamento corrigidas, e a matriz completa de rotas/exportações ainda não é um aceite concluído para SaaS. | **Alto:** dados de oficinas diferentes podem ser divulgados ou alterados; o dano pode persistir mesmo após corrigir o código. | 9 | **Crítico — 1º** |
| R04 | **Alta:** o roteiro contém muitas entregas futuras e decisões externas; aceitar pedidos sem corte de escopo é plausível. | **Alto:** prazo e teste de módulos centrais seriam sacrificados. | 9 | **Crítico — 2º** |
| R02 | **Média:** H2 local e ZIP local funcionam, mas máquina/arquivo podem falhar e não há restauração validada. | **Alto:** não se recuperam OS e lançamentos apenas recompilando o programa. | 6 | **Crítico — 3º** |
| R03 | **Média:** há bloqueio e testes atuais, porém diversos fluxos de estoque e carga futura deixam possibilidade de regressão. | **Alto:** saldo incorreto afeta atendimento, compra e valor financeiro. | 6 | **Crítico — 4º** |
| R05 | **Média:** a migração está prevista como etapa de produção, sem script versionado pronto. | **Alto:** inconsistência ou perda na conversão inviabiliza a entrada em produção. | 6 | **Crítico — 5º** |
| R06 | **Média:** regras de reabertura, devolução e faturamento precisam de validação com o usuário. | **Médio:** um fluxo será refeito, mas a base atual pode continuar operando. | 4 | Moderado |
| R07 | **Média:** dois frontends e uma etapa explícita de sincronização criam chance de pacote desatualizado. | **Médio:** a demonstração falha até novo build/sincronização; os dados permanecem. | 4 | Moderado |
| R08 | **Média:** já houve configuração de JDK incorreta, mas o guia e testes locais reduzem recorrência. | **Médio:** atrasa ou impede uma apresentação específica, sem perda permanente de dados. | 4 | Moderado |
| R09 | **Média:** escolhas de fornecedores estão pendentes caso integrações entrem no próximo incremento. | **Médio:** afeta a funcionalidade prometida; a versão atual opera com integração desativada. | 4 | Moderado |
| R10 | **Média:** o mesmo conjunto pequeno de pessoas tende a assumir módulos especializados. | **Médio:** ausência atrasa revisão e correção, mas documentação e pareamento permitem substituição. | 4 | Moderado |
| R11 | **Média:** houve pedidos concretos de correção de visual; ainda falta validação em dispositivos variados. | **Médio:** aumenta erro e rejeição, sem afetar diretamente a persistência quando o backend valida. | 4 | Moderado |
| R13 | **Baixa:** múltiplas instâncias não fazem parte da instalação local atual. | **Alto:** se adotadas sem sessão compartilhada, a revogação de acesso falha entre nós. | 3 | Moderado |
| R14 | **Média:** a varredura Maven não foi feita e novas notificações podem surgir. | **Médio:** exigiria corrigir ou atualizar dependências antes da publicação; severidade real depende do alerta. | 4 | Moderado |
| R15 | **Média:** duas oficinas podem usar o mesmo SKU ou placa quando o SaaS crescer. | **Médio:** bloqueia cadastros, mas há migração planejável antes da ampliação. | 4 | Moderado |
| R12 | **Baixa:** PDF A4 e testes de documento reduzem a chance de divergência relevante. | **Médio:** reimpressão ou uso do PDF resolve um caso isolado. | 2 | Baixo |

### Matriz probabilidade × impacto

| Probabilidade \ Impacto | Baixo (1) | Médio (2) | Alto (3) |
| --- | --- | --- | --- |
| **Alta (3)** | Moderado (3) | Crítico (6) | **Crítico (9): R01, R04** |
| **Média (2)** | Baixo (2) | **Moderado (4): R06, R07, R08, R09, R10, R11, R14, R15** | **Crítico (6): R02, R03, R05** |
| **Baixa (1)** | Baixo (1) | **Baixo (2): R12** | **Moderado (3): R13** |

**Síntese:** 5 críticos, 9 moderados e 1 baixo. R01 precede R04 no empate de 9 pontos porque a exposição de dados de terceiros pode ser irreversível e difícil de detectar. A equipe deve tratar os cinco críticos antes de ampliar o uso para empresas externas.

## 4. Estratégias de resposta

As quatro estratégias propostas no enunciado seguem o uso do PMI: **evitar** elimina a exposição por uma escolha de escopo/abordagem; **mitigar** reduz probabilidade ou impacto; **transferir** desloca uma parte identificável da responsabilidade contratual/operacional a terceiro; **aceitar** registra a exposição residual e prevê observação ou reserva. Transferência não elimina a responsabilidade do grupo por integrar e testar o produto. [Referência PMI sobre respostas](https://www.pmi.org/learning/library/practical-risk-management-approach-8248).

| ID | Estratégia e justificativa | Resposta planejada, responsável e prazo relativo |
| --- | --- | --- |
| R01 | **Mitigar:** o cadastro multiempresa é parte do produto; é preciso reduzir a chance de vazamento. | Backend/segurança: ampliar testes de isolamento para GET, POST, edição, PDF, CSV, dashboard e financeiro; revisão de autorização em todo PR, antes do piloto SaaS. |
| R02 | **Mitigar:** não é possível eliminar falha de máquina; cópias verificadas reduzem o impacto. | Dados/infra: backup diário, cópia fora da máquina e restauração mensal em ambiente separado, antes de usar dados reais. |
| R03 | **Mitigar:** estoque é função central, portanto não pode ser excluído. | Estoque/backend: testes de entradas, saídas, OS concorrentes, orçamento repetido e reconciliação de saldos a cada entrega. |
| R04 | **Evitar:** retirar do marco atual funcionalidades futuras sem critério de aceite evita sobrecarga. | Líder do grupo: congelar escopo da entrega, separar “pronto” de “evolução” e exigir troca explícita de prioridade para cada pedido novo, já no próximo planejamento. |
| R05 | **Evitar:** não publicar a versão SaaS usando `ddl-auto=update` e conversão improvisada. | Banco/backend: só autorizar a virada após migração versionada, ensaio em cópia e comparação de contagens/saldos, antes do marco de produção. |
| R06 | **Mitigar:** clarificar regras antes de codificar evita retrabalho. | Requisitos + cliente: escrever exemplos de status de OS, devolução, garantia e lançamento financeiro; obter aceite em demonstração de cada fluxo. |
| R07 | **Mitigar:** build e pacote coerentes são verificáveis. | Integração: executar build frontend + sincronização e smoke test no pacote Spring antes de cada demonstração; comparar hashes dos assets. |
| R08 | **Mitigar:** ambiente é controlável pelo grupo. | Ambiente: registrar Java 17, Maven e pnpm, testar em máquina reserva e guardar um procedimento de inicialização sem credenciais, antes da apresentação. |
| R09 | **Transferir:** contratar/selecionar fornecedor especializado para cobrança ou mensageria desloca a operação do serviço externo e seu SLA. | Produto/integração: cliente escolhe provedor e contrato; grupo valida sandbox, erros, webhooks e suporte antes de prometer o módulo. Sem essa decisão, integração segue fora do marco. |
| R10 | **Mitigar:** revezamento e revisão reduzem a dependência de uma pessoa. | Líder técnico: parear mudanças críticas e manter notas curtas de execução, banco e regras de negócio em cada entrega. |
| R11 | **Mitigar:** validação com usuários reduz erro de operação. | UX/frontend: testar tarefas de balcão em celular/desktop, teclado, foco, contraste e modo escuro antes do aceite visual. |
| R12 | **Aceitar:** testar toda combinação de impressora/driver não é proporcional ao impacto atual. | Documento OS: registrar relato, oferecer PDF A4 como alternativa e corrigir se dois ambientes distintos reproduzirem a falha. |
| R13 | **Evitar:** a implantação atual pode continuar em uma JVM. | Infra/segurança: proibir balanceamento para vários nós até existir sessão/limite compartilhados e teste de revogação entre instâncias. |
| R14 | **Mitigar:** atualizar de forma controlada reduz exposição sem trocar versões às cegas. | Backend: varrer dependências Maven, analisar severidade e aplicabilidade, atualizar e repetir regressões antes de publicação. |
| R15 | **Mitigar:** a regra comercial por empresa precisa de alteração planejada no esquema. | Dados/backend: definir unicidade por empresa, preparar migração e testar colisões legítimas antes de cadastrar a segunda oficina real. |

## 5. Mitigação e contingência dos riscos críticos

### R01 — Exposição de dados entre empresas

**Antes:** o líder de backend mantém uma lista de todas as rotas que leem/alteram dados e identifica em cada uma a origem do `empresaId`, a consulta filtrada e a autorização. Testes com duas empresas devem criar clientes, peças, veículos, OS e lançamentos em uma e tentar listar, consultar por ID, editar, exportar e agregar pela outra. Incluir PDF e endpoints legados. Cada PR que tocar entidade, repositório, relatório, autenticação ou filtro exige revisão de isolamento. **Critério de liberação:** nenhum acesso cruzado retorna dados ou altera registros; a suíte roda no pipeline em banco isolado.

**Gatilho:** resposta com dado de outra empresa, teste de isolamento falho ou nova rota sem teste de autorização. **Se acontecer:** suspender imediatamente a rota ou a liberação externa; preservar logs e uma cópia do banco sem sobrescrever o original; identificar empresas/IDs e janela de exposição; corrigir a consulta ou autorização; testar o caso reproduzido e rotas análogas; revalidar antes de reabrir. O responsável pelo produto avalia comunicação às oficinas afetadas com base nos fatos apurados. **Responsável:** líder de backend/segurança. **Verificação:** toda mudança relevante e revisão semanal da matriz de rotas.

### R02 — Perda do banco ou backup irrestaurável

**Antes:** o responsável por dados agenda backup diário do H2, confere criação, tamanho e integridade do ZIP e copia para local externo ao computador da aplicação. Uma vez por mês, restaura uma cópia em ambiente separado e executa consultas de contagem de clientes, OS, movimentações e financeiro, além de abrir uma OS/PDF de teste. Registra data do último backup e do último restauro bem-sucedido. **Critério de liberação:** nenhum dado real entra sem backup externo e restauração ensaiada.

**Gatilho:** erro de leitura do H2, arquivo ausente, backup diário não gerado ou teste de restauração falho. **Se acontecer:** parar gravações para não piorar a situação; copiar os arquivos existentes para análise; recuperar o último backup validado em instalação separada; comparar movimentos/OS posteriores ao ponto restaurado com comprovantes disponíveis; registrar lacuna de dados e validar com o responsável da oficina antes de voltar a gravar. Não sobrescrever a única cópia original. **Responsável:** dados/infra. **Verificação:** diária do backup e mensal da restauração.

### R03 — Estoque incoerente

**Antes:** manter testes de concorrência para duas retiradas da última unidade, aprovação de orçamento com peça repetida e bloqueio quando disponível < quantidade; ampliar para entradas/saídas intercaladas e várias OS. A cada entrega, reconciliar amostra de peças: saldo inicial + entradas − saídas = saldo final e `0 ≤ disponível ≤ saldo`. Revisar mudanças em `PecasService` e `OrdensService` por outro integrante. **Critério de liberação:** nenhuma operação cria saldo negativo ou duas baixas para a mesma unidade.

**Gatilho:** teste concorrente falho, saldo negativo, diferença entre histórico e saldo, ou reclamação da oficina de peça “disponível” inexistente. **Se acontecer:** interromper novas baixas da peça afetada; registrar OS e movimentações relacionadas; contar o estoque físico; apurar último saldo confiável e transações posteriores; corrigir por ajuste autorizado e auditável, nunca alterando o banco sem registro; informar atendimentos e orçamentos afetados; adicionar teste para a sequência que causou o desvio. **Responsável:** módulo de estoque + representante da oficina. **Verificação:** a cada PR de estoque/OS e conferência operacional periódica.

### R04 — Escopo maior que prazo e capacidade

**Antes:** o líder do grupo fixa o marco acadêmico em um conjunto demonstrável: login/permissões, cadastro, estoque, OS/orçamento, financeiro básico e testes. Cada pedido novo recebe descrição, custo estimado, dependências e critério de aceite; só entra no marco se outro item de esforço semelhante sair ou se o prazo for formalmente revisto. O backlog SaaS (PostgreSQL, cobrança, mensagens, observabilidade) fica em marcos separados. Toda semana, comparar atividades planejadas/concluídas e testes pendentes. **Critério:** não iniciar integração externa sem provedor definido e não apresentar item incompleto como concluído.

**Gatilho:** duas funcionalidades não planejadas entram no mesmo ciclo, mais de 20% das atividades do marco ficam atrasadas, ou testes centrais começam a ser adiados. **Se acontecer:** reunir o grupo e o orientador/cliente; congelar novas funcionalidades; retirar primeiro melhorias opcionais e integrações futuras; concentrar esforço nos fluxos centrais e na qualidade; publicar novo plano de tarefas com responsáveis e evidência do que será demonstrado. Não encurtar validação de dados e permissões para recuperar calendário. **Responsável:** líder do grupo. **Verificação:** semanal e a cada solicitação de mudança.

### R05 — Migração H2 → PostgreSQL com perda ou alteração de dados

**Antes:** criar esquema e migrações versionadas, mapear registros legados e dependências entre empresa, usuário, cliente, veículo, OS, itens, movimentações e financeiro. Ensaiar a migração repetidamente sobre cópia do H2; comparar contagens, somas de saldo, totais das OS e amostra de documentos antes/depois; executar os testes multiempresa e de estoque no banco de destino. Documentar tempo de parada e reversão. **Critério de virada:** script reproduzível, backup restaurável e reconciliação sem diferenças não explicadas.

**Gatilho:** script falha, contagens/totais divergem, FK inválida ou teste funcional passa no H2 e falha no PostgreSQL. **Se acontecer:** suspender a virada e manter o H2 original somente leitura; preservar o banco de destino para diagnóstico; corrigir a migração em ambiente de ensaio, nunca com edição manual irreproduzível em produção; se a virada já começou, restaurar o par aplicação/banco da mesma versão anterior e comunicar a janela de indisponibilidade. Repetir reconciliação e testes antes de nova tentativa. **Responsável:** dados/backend. **Verificação:** em cada ensaio e no checklist de implantação.

## 6. Monitoramento e atualização

O **líder do grupo** é dono do processo e mantém este registro no repositório; cada responsável da tabela é dono da resposta do seu risco. Na reunião semanal, o grupo atualiza para cada ID: probabilidade, impacto, pontuação, estado (aberto, em tratamento, materializado, encerrado), gatilho observado, ação feita, prazo relativo e risco residual. Os cinco críticos aparecem primeiro. Uma mudança de escopo, falha de teste, incidente ou decisão de produção provoca revisão imediata, sem esperar a reunião.

As verificações operacionais são: testes Java e frontend em cada alteração relevante; conferência de build e bundle antes de demonstração; backup diário e restauração mensal; reconciliação de estoque e revisão de acessos antes de cada implantação; levantamento de dependências quando surgir alerta. Um risco é encerrado apenas quando há evidência de que a causa deixou de existir ou o marco que o tornava relevante foi cancelado. “Teste passou” sozinho não encerra risco de operação ou de regra não validada.

Novos riscos serão identificados por retrospectivas, revisão de PR, testes exploratórios dos atendentes, incidentes, reclamações do cliente, falhas de build/CI, mudanças em provedores e análise de dependências. Quem detectar registra evento, causa, consequência e evidência; o líder atribui dono e a próxima reunião classifica P × I. Riscos materializados também entram em um registro de problemas/incidentes, com data, ação imediata e lição aprendida. O PMI descreve o registro como documento continuamente atualizado com gatilhos e ações, abordagem adotada aqui. [PMI — gestão prática de riscos](https://www.pmi.org/learning/library/practical-risk-management-approach-8248).

### Indicadores e gatilhos de acompanhamento

| Indicador | Sinal para agir | Riscos |
| --- | --- | --- |
| Testes de isolamento e rotas sem cobertura | Qualquer teste falho ou rota nova sem caso de duas empresas bloqueia publicação externa. | R01, R13, R15 |
| Último backup/restauração | Mais de 24 horas sem backup ou mais de um mês sem teste de restauração exige ação do responsável. | R02, R05 |
| Reconciliação de estoque | Qualquer saldo negativo ou diferença não explicada exige apuração antes de novas baixas. | R03 |
| Acompanhamento do marco | Mais de 20% das tarefas atrasadas ou duas entradas de escopo não planejadas exigem replanejamento. | R04, R06, R09 |
| Build e ambiente | Falha de build, versão de JDK incompatível ou asset diferente do pacote impede demonstração até correção. | R07, R08, R14 |
| Retorno de usuários | Duas ocorrências do mesmo problema de uso ou impressão geram teste dirigido e revisão da prioridade. | R11, R12 |

## 7. Análise crítica

**1. Qual é o maior risco?** R01, exposição de dados entre oficinas. Ele empata com R04 na pontuação máxima (9), mas lidera pelo potencial de dano permanente e pela dificuldade de perceber rapidamente que uma resposta continha dados de outra empresa.

**2. Por que é o mais perigoso?** O Sputnik combina dados pessoais de clientes, veículos, OS e lançamentos financeiros. A separação por empresa atravessa sessão, filtros, entidades, consultas e relatórios. A auditoria encontrou falhas reais nessa fronteira e as corrigiu; ao adicionar novas rotas ou execução assíncrona, uma omissão pode reintroduzir o problema. Diferentemente de um atraso de funcionalidade, a divulgação a terceiros não é desfeita por um novo deploy.

**3. O que aconteceria sem tratamento?** Uma oficina poderia enxergar, exportar ou modificar registros de outra; valores e indicadores deixariam de ser confiáveis. O grupo teria de suspender o acesso, investigar o período e as pessoas afetadas, corrigir dados quando possível e lidar com perda de confiança do cliente. A expansão SaaS não deveria ser aprovada sem os testes e a revisão de isolamento descritos em R01.

## Referências e evidências do projeto

- Project Management Institute. [PMBOK® Guide, 8ª edição](https://www.pmi.org/standards/pmbok), 2025. Página oficial consultada em 28/09/2026.
- Project Management Institute. [PMI Lexicon of Project Management Terms](https://www.pmi.org/-/media/pmi/documents/registered/pdf/pmbok-standards/pmi-lexicon-pm-terms.pdf?rev=447328d841c249af985d14177ddd5f95), versão disponível em 2026. Definições de plano, registro, responsável e respostas a riscos.
- Project Management Institute. [A practical risk management approach](https://www.pmi.org/learning/library/practical-risk-management-approach-8248). Apoio para registro, matriz, estratégias, gatilhos e contingência.
- Sputnik. [Arquitetura](ARQUITETURA.md), [plano de evolução](PLANO-EVOLUCAO-PRODUTO.md), [auditoria de código de 24/09/2026](AUDITORIA-CODIGO-2026-09-24.md), [configuração da aplicação](../src/main/resources/application.properties), [guia de execução](INTELLIJ.md) e código-fonte do projeto. Consultados em 28/09/2026.

**Nota para apresentação:** substituir os cargos pelos nomes dos integrantes e confirmar com o grupo o prazo do marco acadêmico. Isso não altera a análise técnica, mas torna a atribuição de responsabilidade executável.
