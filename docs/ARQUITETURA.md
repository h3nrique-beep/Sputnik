# Arquitetura e lógica do Sputnik

## Visão geral

O Sputnik utiliza React/TypeScript na interface principal e Spring Boot na API. O Spring entrega o frontend compilado em `/`; as chamadas `/api` passam pelos controladores, serviços e repositórios até o H2. A única página renderizada no servidor é a troca obrigatória de senha do primeiro acesso (Thymeleaf).

```text
React (features) → cliente HTTP → contexto da empresa → /api → serviços → repositórios → H2
```

## Organização dos pacotes

| Pacote | Responsabilidade |
| --- | --- |
| `auth` | Login, sessão e autenticação de usuários persistidos. |
| `config` | Beans técnicos, dados iniciais e migrações locais de dados legados. |
| `controller` | Entrega do React (`SpaController`), primeiro acesso e tratamento de erros da API. |
| `controller/api` | API React: autenticação, dashboard, estoque, cadastros, usuários, desenvolvedor e OS. |
| `controller/api/dto` | Contratos de requisição e resposta, com validações preservadas. |
| `controller/api/mapper` | Conversões de entidades em respostas JSON. |
| `domain` | Entidades JPA e enums que representam o negócio. |
| `repository` | Interfaces Spring Data JPA para leitura e gravação no H2. |
| `service` | Regras de negócio, autorização centralizada, financeiro, PDF da OS e backups. |
| `security` | Token anti-CSRF, cabeçalhos, limite de requisições e de tentativas de login. |
| `tenant` | Contexto da empresa autenticada e filtro de isolamento de dados operacionais. |
| `web` | Objetos de entrada validados recebidos pela API e pelo primeiro acesso. |

## Inicialização

`SputnikApplication` é o ponto de entrada. A anotação `@SpringBootApplication` ativa a configuração automática do Spring Boot, encontra os componentes e inicia o servidor na porta 8080.

Na primeira inicialização, `DataInitializer` inclui seis peças de demonstração. `DisponivelMigration` preenche o campo `estoqueDisponivel` de registros antigos que ainda não o possuam. `UsuarioInitializer` cria a empresa e o administrador inicial caso a base não possua usuários. `MigracaoMultiempresa` associa os dados locais anteriores à empresa padrão.

## Empresa e isolamento de dados

`EmpresaContextFilter` identifica a empresa do usuário autenticado em cada requisição e ativa o filtro Hibernate `empresaAtual`. Clientes, veículos, peças, movimentações, ordens de serviço e lançamentos financeiros herdam `EntidadeDaEmpresa`: a empresa é atribuída no `@PrePersist` e as consultas da sessão recebem apenas os registros da empresa atual.

O teste de integração `empresaDaSessaoIsolaDadosOperacionais` confirma que uma empresa não lista nem movimenta uma peça cadastrada por outra. Para a implantação SaaS, a expansão dessa cobertura para cada rota, exportação e agregação permanece prevista no [plano de evolução](PLANO-EVOLUCAO-PRODUTO.md).

## Autenticação e sessão

`AuthApiController` recebe o login React em `POST /api/auth/login` e usa `UsuarioService`, que:

1. Normaliza o usuário para minúsculas.
2. Localiza o usuário na tabela `usuarios_sistema`.
3. Compara a senha informada com o hash BCrypt armazenado.
4. Retorna um `UsuarioAutenticado` com empresa, nome, perfil e módulos permitidos.

Quando a autenticação é válida, `SessaoAutenticacao.iniciar` grava esse objeto na `HttpSession`. Cada rota da API consulta essa sessão antes de responder. O logout invalida a sessão inteira.

### Perfis atuais

| Login | Perfil | Permissões |
| --- | --- | --- |
| `balconista@sputnik.com.br` | `BALCONISTA` | Peças, movimentações, pessoas, veículos e ordens de serviço. |
| `admin` | `ADMIN` | Todas as permissões, relatório mensal, reposição, CSV e valor em estoque. |

As senhas são armazenadas como hash BCrypt. Usuários, perfis e permissões são persistidos no H2; os acessos iniciais só são criados se não existir nenhum usuário na base.

Ao cadastrar um Balconista, o Admin define uma senha temporária. No primeiro login, o usuário é impedido de abrir as demais telas até criar uma senha definitiva com no mínimo oito caracteres, letra maiúscula, minúscula, número e caractere especial.

## Controladores da API

| Classe | Responsabilidade |
| --- | --- |
| `AuthApiController` | Login, logout, sessão atual e token anti-CSRF. |
| `DashboardApiController` | Indicadores do painel, filtrados por permissão. |
| `EstoqueApiController` | Peças, movimentações, relatório mensal, reposição e CSV. |
| `CadastrosApiController` | Clientes, veículos e vínculo entre ambos. |
| `OrdensApiController` | OS e orçamentos: abertura, edição, itens, aprovação, PDF e impressão. |
| `FinanceiroApiController` | Lançamentos e baixa. |
| `UsuariosApiController` | Contas, permissões, reset de senha e exclusão (somente Admin). |
| `EmpresaApiController` e `AssinaturasApiController` | Assinatura da empresa e cadastro de nova empresa. |
| `DesenvolvedorApiController` | Diagnóstico do ambiente e backups (somente Admin da empresa principal). |
| `PrimeiroAcessoController` | Troca obrigatória de senha, fora da API. |

## Autorização

Cada controlador protege suas rotas verificando a sessão. As ações funcionais chamam `temAcesso(session, modulo)`, que aceita o módulo liberado ou o perfil `ADMIN`; sem permissão, a API responde HTTP 403. As rotas de gestão de usuários exigem `ADMIN`.

O menu do React esconde os itens sem permissão, mas a regra principal está no servidor: chamar a rota diretamente também recebe 403. Enquanto a troca de senha do primeiro acesso não é feita, `PrimeiroAcessoInterceptor` só libera a sessão, o logout e a própria tela de troca.

## Entidades e relacionamentos

```text
Cliente 1 ─── N Veículo
Cliente 1 ─── N OrdemServico
Veículo 1 ─── N OrdemServico
Peça    1 ─── N Movimentação
```

- `Peca`: cadastro, preço, locação, saldo contábil, disponível e mínimo.
- `Movimentacao`: entrada ou saída, quantidade, saldo resultante, data, operador e observação.
- `Cliente`: pessoa responsável pela ordem.
- `Veiculo`: dados técnicos do carro e vínculo opcional com um cliente já cadastrado.
- `OrdemServico`: registro do atendimento, ligado a uma pessoa e a um carro.

## Regras de negócio

As regras estão em `PecasService` (estoque), `CadastrosService` (clientes e veículos), `OrdensService` (atendimento) e `FinanceiroService` (lançamentos). Os controladores da API chamam esses serviços diretamente.

| Regra | Comportamento |
| --- | --- |
| RN02 | O código da peça é normalizado para maiúsculas e não pode se repetir. |
| RN03/RN05 | Uma saída é bloqueada quando a quantidade é maior que o saldo contábil. |
| Estoque disponível | É inicializado com o saldo e atualizado junto a cada movimentação. |
| Estoque crítico | Uma peça é crítica quando o disponível é menor que o mínimo. |
| Sugestão de reposição | Calcula o necessário para chegar ao dobro do estoque mínimo, com mínimo de uma unidade. |
| Ordem de serviço | O veículo selecionado precisa pertencer à pessoa selecionada. |

`@Transactional` é usado nas operações que alteram mais de um registro lógico, como movimentar estoque e abrir ordem. Assim, uma falha evita que a operação fique parcialmente gravada.

## Fluxos principais

### Cadastro e consulta de peça

1. O React envia a peça para `POST /api/pecas`.
2. A validação Bean Validation verifica os campos obrigatórios.
3. O serviço verifica duplicidade de código e persiste no H2.
4. A listagem aceita busca por nome, código ou fabricante e filtros de estoque baixo ou zerado.

### Movimentação de estoque

1. O formulário envia peça, tipo, quantidade e observação.
2. O serviço busca a peça e valida o saldo antes de uma saída.
3. O saldo contábil e o disponível são atualizados.
4. Uma `Movimentacao` é gravada para auditoria.

### Ordem de serviço

1. O atendente seleciona o cliente cadastrado.
2. A tela apresenta apenas os veículos vinculados àquele cliente.
3. Se não houver cliente ou veículo, a tela direciona ao cadastro adequado.
4. Cliente e veículo selecionados aparecem em um resumo de conferência; a seleção só pode ser alterada pelo botão específico.
5. A quilometragem e a descrição do serviço são liberadas após a confirmação visual.
6. O servidor valida novamente o vínculo antes de salvar a ordem como `ABERTA`.

## Rotas

Todas as rotas de dados ficam em `/api` e respondem JSON (exceto PDF, impressão e CSV).

| Rota | Finalidade | Permissão |
| --- | --- | --- |
| `GET /api/security/csrf` · `POST /api/auth/login` · `POST /api/auth/logout` · `GET /api/auth/session` | Sessão e token | Público / autenticado |
| `GET /api/dashboard` | Painel | Qualquer módulo operacional |
| `GET/POST /api/pecas` · `POST /api/movimentacoes` | Peças e estoque | Peças |
| `GET /api/relatorio-mensal` | Movimentações do mês | Relatório mensal |
| `GET /api/reposicao` · `GET /api/reposicao.csv` | Itens críticos e exportação | Reposição |
| `GET/POST /api/clientes` · `GET/POST /api/veiculos` | Cadastros | Peças ou Ordens |
| `GET/POST /api/ordens` · `GET/PUT /api/ordens/{id}` | OS e orçamentos | Ordens |
| `POST /api/ordens/{id}/itens/peca` · `/itens/servico` · `/aprovar` | Itens e aprovação | Ordens |
| `GET /api/ordens/{id}/pdf` · `/impressao` | Documento da OS | Ordens |
| `GET/POST /api/financeiro` · `POST /api/financeiro/{id}/baixar` | Financeiro | Financeiro |
| `/api/admin/usuarios/**` | Usuários e permissões | Admin |
| `/api/admin/desenvolvedor/**` | Diagnóstico e backups | Admin da empresa principal |
| `GET /api/empresa/assinatura` · `POST /api/assinaturas/cadastro` | Assinatura e cadastro de empresa | Admin / público quando habilitado |
| `GET/POST /primeiro-acesso` | Troca obrigatória de senha (Thymeleaf) | Sessão com troca pendente |

## Interface

As telas principais ficam em `frontend/src/features`, agrupadas por funcionalidade. `app/App.tsx` compõe a navegação e as telas; `main.tsx` apenas inicia o React. Os componentes compartilhados ficam em `components/ui` e os estilos em `styles`, preservando a ordem de importação.

O único template restante é `src/main/resources/templates/primeiro-acesso.html`, estilizado por `static/app.css`. A validação definitiva de permissões, estoque e vínculo entre cliente e veículo continua no servidor.

## Persistência e limitações atuais

O banco H2 é gravado em `data/sputnik`; peças, ordens, usuários e dados complementares do documento sobrevivem ao reinício. Reset de senha, troca obrigatória, edição de permissões e histórico da OS continuam implementados. No H2 local, o Hibernate atualiza o esquema sozinho (`ddl-auto=update`) para preservar os dados existentes. No perfil `postgres`, o esquema vem apenas das migrações Flyway (`db/migration/V1__esquema_inicial.sql`, com índices em `empresa_id` e nas chaves estrangeiras) e o Hibernate só valida que ele corresponde às entidades. `PostgresMigracaoIntegrationTest` comprova isso contra um PostgreSQL real a cada `mvn test`.
