# Arquitetura e lógica do Sputnik

## Visão geral

O Sputnik utiliza React/TypeScript na interface principal e Spring Boot na API. O Spring entrega o frontend compilado em `/`; as chamadas `/api` passam pelos controladores, serviços e repositórios até o H2. As páginas Thymeleaf anteriores continuam presentes para compatibilidade, incluindo os fluxos de acesso existentes.

```text
React (features) → cliente HTTP → contexto da empresa → /api → serviços → repositórios → H2
Thymeleaf (legado) → controladores → EstoqueService → serviços especializados
```

## Organização dos pacotes

| Pacote | Responsabilidade |
| --- | --- |
| `auth` | Login, sessão e autenticação de usuários persistidos. |
| `config` | Beans técnicos, dados iniciais e migrações locais de dados legados. |
| `controller` | Páginas Thymeleaf legadas. |
| `controller/api` | API React: autenticação, dashboard, estoque, cadastros, usuários, desenvolvedor e OS. |
| `controller/api/dto` | Contratos de requisição e resposta, com validações preservadas. |
| `controller/api/mapper` | Conversões de entidades em respostas JSON. |
| `domain` | Entidades JPA e enums que representam o negócio. |
| `repository` | Interfaces Spring Data JPA para leitura e gravação no H2. |
| `service` | Regras de negócio, autorização centralizada e preparação dos dados comuns das views. |
| `tenant` | Contexto da empresa autenticada e filtro de isolamento de dados operacionais. |
| `web` | Objetos de formulário usados nas telas de movimentação e ordem de serviço. |

## Inicialização

`SputnikApplication` é o ponto de entrada. A anotação `@SpringBootApplication` ativa a configuração automática do Spring Boot, encontra os componentes e inicia o servidor na porta 8080.

Na primeira inicialização, `DataInitializer` inclui seis peças de demonstração. `DisponivelMigration` preenche o campo `estoqueDisponivel` de registros antigos que ainda não o possuam. `UsuarioInitializer` cria a empresa e o administrador inicial caso a base não possua usuários. `MigracaoMultiempresa` associa os dados locais anteriores à empresa padrão.

## Empresa e isolamento de dados

`EmpresaContextFilter` identifica a empresa do usuário autenticado em cada requisição e ativa o filtro Hibernate `empresaAtual`. Clientes, veículos, peças, movimentações, ordens de serviço e lançamentos financeiros herdam `EntidadeDaEmpresa`: a empresa é atribuída no `@PrePersist` e as consultas da sessão recebem apenas os registros da empresa atual.

O teste de integração `empresaDaSessaoIsolaDadosOperacionais` confirma que uma empresa não lista nem movimenta uma peça cadastrada por outra. Para a implantação SaaS, a expansão dessa cobertura para cada rota, exportação e agregação permanece prevista no [plano de evolução](PLANO-EVOLUCAO-PRODUTO.md).

## Autenticação e sessão

`AuthApiController` recebe o login React em `POST /api/auth/login`; `AuthController` atende o formulário legado em `POST /login`. Ambos utilizam `UsuarioService`, que:

1. Normaliza o usuário para minúsculas.
2. Localiza o usuário na tabela `usuarios_sistema`.
3. Compara a senha informada com o hash BCrypt armazenado.
4. Retorna um `UsuarioAutenticado` com empresa, nome, perfil e módulos permitidos.

Quando a autenticação é válida, `SessaoAutenticacao.iniciar` grava esse objeto na `HttpSession`. As páginas consultam essa sessão antes de responder. O logout invalida a sessão inteira.

### Perfis atuais

| Login | Perfil | Permissões |
| --- | --- | --- |
| `balconista@sputnik.com.br` | `BALCONISTA` | Peças, movimentações, pessoas, veículos e ordens de serviço. |
| `admin` | `ADMIN` | Todas as permissões, relatório mensal, reposição, CSV e valor em estoque. |

As senhas são armazenadas como hash BCrypt. Usuários, perfis e permissões são persistidos no H2; os acessos iniciais só são criados se não existir nenhum usuário na base.

Ao cadastrar um Balconista, o Admin define uma senha temporária. No primeiro login, o usuário é impedido de abrir as demais telas até criar uma senha definitiva com no mínimo oito caracteres, letra maiúscula, minúscula, número e caractere especial.

## Controladores por módulo

| Classe | Responsabilidade |
| --- | --- |
| `DashboardController` | Página inicial e indicadores gerenciais. |
| `PecaController` | Consulta e cadastro de peças. |
| `CadastroController` | Clientes, veículos e vínculo entre ambos. |
| `OrdemServicoController` | Seleção e abertura das ordens de serviço. |
| `EstoqueController` | Movimentações, relatório mensal, reposição e CSV. |
| `AuthController` e `PrimeiroAcessoController` | Login, logout e troca obrigatória de senha. |
| `UsuarioAdminController` | Gestão de contas de Balconista e permissões. |

## Autorização

Cada controlador protege suas próprias rotas verificando a sessão. As ações funcionais chamam `temAcesso(session, modulo)`, que aceita o módulo liberado ou o perfil `ADMIN`. As rotas de gestão de usuários exigem `ADMIN`.

O menu esconde os itens administrativos do Balconista, mas a regra principal está no servidor: se ele tentar abrir a URL diretamente, é redirecionado para o início com a mensagem de acesso negado. A exportação CSV retorna HTTP 403 para usuário autenticado sem permissão e HTTP 401 para usuário não autenticado.

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

As regras estão em `PecasService` (estoque), `CadastrosService` (clientes e veículos) e `OrdensService` (atendimento). `EstoqueService` é uma fachada que encaminha as chamadas anteriores aos serviços especializados, preservando as interfaces e transações.

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

1. O React envia a peça para `POST /api/pecas`; o formulário legado mantém `POST /pecas`.
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

A tabela abaixo descreve as rotas legadas. O React utiliza `/api/auth/*`, `/api/dashboard`, `/api/pecas`, `/api/clientes`, `/api/veiculos`, `/api/movimentacoes`, `/api/ordens`, `/api/relatorio-mensal`, `/api/reposicao` e `/api/admin/*`. O documento da OS usa `/api/ordens/{id}/pdf` e `/api/ordens/{id}/impressao`. A organização em classes não altera os caminhos públicos.

| Rota | Finalidade | Perfil |
| --- | --- | --- |
| `GET/POST /login` | Entrar no sistema | Público |
| `POST /logout` | Encerrar sessão | Autenticado |
| `GET /inicio` | Painel principal | Balconista ou Admin |
| `GET/POST /pecas` | Listar e cadastrar peças | Balconista ou Admin |
| `GET/POST /movimentacoes` | Registrar entrada ou saída | Balconista ou Admin |
| `GET/POST /pessoas` e `/veiculos` | Preparar atendimento | Balconista ou Admin |
| `GET/POST /ordens` | Listar e abrir ordens | Balconista ou Admin |
| `GET /relatorio-mensal` | Histórico do mês | Módulo Relatório mensal ou Admin |
| `GET /reposicao` | Itens críticos e custo | Módulo Reposição ou Admin |
| `GET /reposicao.csv` | Exportar reposição | Módulo Reposição ou Admin |
| `GET/POST /admin/usuarios` | Listar e cadastrar usuários | Admin |

## Interface

As telas principais ficam em `frontend/src/features`, agrupadas por funcionalidade. `app/App.tsx` compõe a navegação e as telas; `main.tsx` apenas inicia o React. Os componentes compartilhados ficam em `components/ui` e os estilos em `styles`, preservando a ordem de importação.

Os templates antigos ficam em `src/main/resources/templates`, com `static/app.css` e `fragments.html`. A validação definitiva de permissões, estoque e vínculo entre cliente e veículo continua no servidor.

## Persistência e limitações atuais

O banco H2 é gravado em `data/sputnik`; peças, ordens, usuários e dados complementares do documento sobrevivem ao reinício. Reset de senha, troca obrigatória, edição de permissões e histórico da OS continuam implementados. O Hibernate atualiza localmente o esquema e inclui `empresa_id` nos registros operacionais principais; para produção, a migração deve ser versionada com Flyway e executada em PostgreSQL.
