# Plano de redesign e reestruturação das views (frontend)

Data: 11/11/2026 · Escopo: `frontend/src/` · Situação: **plano, nada implementado**.

Decisões já tomadas pelo usuário:

- **Direção visual: consolidar o atual.** Identidade, cores e layout ficam como estão. O trabalho é unificar padrões (cabeçalho de tela, formulário, botões, estados de erro/vazio/carregando, tokens de cor).
- **Estrutura: só quebrar os arquivos grandes.** `features/` continua como está; partes das views maiores viram arquivos na mesma pasta e o menu/rotas saem de `App.tsx` para `app/`. Sem renomear pastas.

Regras do plano: reutilizar o que existe em `components/ui`, nenhuma dependência nova, cada etapa pequena, independente e reversível com `git revert`.

---

## 1. Mapa das views atuais

Navegação feita à mão: `App.tsx` guarda a view em `useState` e sincroniza com `history.pushState`; o mapa rota ↔ view está em `app/navigation.ts:1-20`. Não há biblioteca de rotas (e o plano não propõe uma).

| Rota | View | Arquivo (linhas) | `components/ui` usados | API |
|---|---|---|---|---|
| `/login` | — | `features/auth/Login.tsx` (191) | nenhum | `POST /auth/login`, `POST /assinaturas/cadastro` |
| `/sistemas` | `dashboard` | `features/dashboard/Dashboard.tsx` (399) | **nenhum** | `GET /dashboard` (a cada 60 s) |
| `/sistemas/cadastros` | `cadastros` | `features/catalog/CatalogHub.tsx` (111) | `shadcn/tabs` direto | `GET /clientes`, `/veiculos`, `/pecas` (só para contar) |
| `/sistemas/clientes` | `clients` | `features/clients/Clients.tsx` (39) | Button, Loading, Table | `GET /clientes?busca=` |
| `/sistemas/clientes/novo` | `clients-new` | `features/clients/ClientCreate.tsx` (51) | nenhum | `POST /clientes` |
| `/sistemas/veiculos` | `vehicles` | `features/vehicles/Vehicles.tsx` (53) | Button, Loading, Table | `GET /veiculos?busca=` |
| `/sistemas/veiculos/novo` | `vehicles-new` | `features/vehicles/VehicleCreate.tsx` (101) | nenhum | `GET /clientes`, `POST /veiculos` |
| `/sistemas/estoque` | `pieces` | `features/inventory/Pieces.tsx` (128) | Button, Loading, Table | `GET /pecas?busca=` |
| `/sistemas/estoque/nova` | `pieces-new` | `features/inventory/PieceCreate.tsx` (139) | nenhum | `POST /pecas` |
| `/sistemas/estoque/movimentacoes` | `movement` | `features/inventory/StockMovement.tsx` (116) | SearchSelect | `GET /pecas`, `POST /movimentacoes` |
| `/sistemas/estoque/reposicao` | `reposition` | `features/inventory/Reposition.tsx` (71) | Button, Loading, Metric, Table | `GET /reposicao`, download `/reposicao.csv` |
| `/sistemas/ordens` | `orders` | `features/orders/Orders.tsx` (191) | Badge, Button, Loading, Table, confirmar | `GET /ordens`, `PUT /ordens/{id}` (arrastar) |
| `/sistemas/ordens/nova` | `orders-new` | `features/orders/OrderCreate.tsx` (142) | SearchSelect | `GET /clientes`, `/veiculos`, `POST /ordens` |
| `/sistemas/orcamentos/novo` | `quotes-new` | mesmo `OrderCreate` com `asQuote` | SearchSelect | idem |
| `/sistemas/ordens/{id}` | `order-{id}` | `features/orders/OrderDetail.tsx` (418) + `OrderItems.tsx` (141) + `OrderEdit.tsx` (106) + `components/OrderDocumentFields.tsx` (198) | Badge, Button, Card, Empty, Loading, Metric, SearchSelect, confirmar | `GET /ordens/{id}`, `POST /ordens/{id}/aprovar`, itens (`POST`), `PUT /ordens/{id}`, download `/ordens/{id}/pdf`, `GET /pecas` |
| `/sistemas/agenda` | `schedule` | `features/schedule/Schedule.tsx` (137) | Badge, Button, Card, Empty, Loading | `GET /ordens` |
| `/sistemas/relatorios/mensal` | `report` | `features/reports/MonthlyReport.tsx` (42) | Table | `GET /relatorio-mensal` |
| `/sistemas/financeiro` | `finance` | `features/finance/Finance.tsx` (473) | Badge, Button, Card, Empty, Loading, Metric, Table, confirmar | `GET /financeiro`, `GET /ordens`, `POST /financeiro`, `POST /financeiro/{id}/baixar` |
| `/sistemas/usuarios` | `users` | `features/users/Users.tsx` (155) + `UserCreate.tsx` (69) + `UserPermissions.tsx` (44) | Badge, Button, Table, confirmar | `GET/POST /admin/usuarios`, `PUT …/permissoes`, `POST …/resetar-senha`, `DELETE` |
| `/sistemas/desenvolvedor` | `developer` | `features/developer/DeveloperConsole.tsx` (230) | Loading, Table | `GET /admin/desenvolvedor`, `GET/POST …/backups`, download |

Casca: `app/App.tsx` (539 linhas) concentra sessão, tema, atalhos de teclado, menu lateral, cabeçalho com busca global, aviso (`Toast`) e o `switch` das 19 views. Menu em 4 grupos (`App.tsx:55`): Operação (Visão geral, Ordens, Agenda), Estoque (Peças, Movimentar, Reposição), Cadastros (Central), Gestão (Relatório, Financeiro, Usuários, Desenvolvedor).

Estilos: 11 arquivos CSS (≈3.250 linhas) importados em cascata (`styles/index.css:5-16`), mais Tailwind 4. `theme.css` (1.384 linhas) é uma camada de sobrescrita por cima de `base.css`.

Testes (`pnpm test`, `node --test`): 8 arquivos em `frontend/tests/`. Quatro deles **leem código-fonte por caminho** (`layout`, `modal`, `print`, `confirm`), então mover ou renomear arquivo exige ajustar o teste na mesma etapa.

---

## 2. Problemas encontrados

### 2.1 Views grandes demais

- `app/App.tsx:57-539` — um componente só para sessão (`:113-128`), atalhos (`:135-169`), menu (`:216-302`), casca (`:303-443`) e roteamento das views (`:446-534`).
- `features/orders/OrderDetail.tsx:146-339` — 194 linhas de marcação do documento A4 dentro da tela de detalhe; o painel de aprovação (`:111-145`) e a área de trabalho (`:365-415`) ficam no mesmo arquivo.
- `features/finance/Finance.tsx` — cálculo de período e totais (`:51-111`, `:463-473`), formulário (`:297-364`), seleção em lote e tabela (`:145-178`, `:372-458`) e exportação CSV (`:179-202`, que repete a lógica de download de `lib/api.ts:77-93`).
- `features/dashboard/Dashboard.tsx:262-357` — painel de notificações com três blocos quase iguais (`:281-306`, `:307-328`, `:329-350`).
- `app/navigation.ts:39-99` — título e subtítulo em ternários aninhados com 20 níveis.

### 2.2 Duplicação entre `components/ui` e `components/shadcn`

- **Card:** `ui/Card.tsx:12-16` embrulha `ShadcnCard` e anula o estilo dele (`p-0`, classe `.card` reestilizada em `theme.css:331` e `:401`). Além disso há um terceiro e quarto jeito de fazer cartão: `className="card compact-form"` (`Finance.tsx:297`, `OrderEdit.tsx:60`, `OrderItems.tsx:74`) e `className="form-card"` (7 formulários).
- **Botão:** existem `Button` (`shadcn/button.tsx`, classes `.ui-button--*`) e botões crus `<button className="primary|secondary|link">` em 20 pontos, por exemplo `Finance.tsx:363`, `OrderEdit.tsx:101`, `OrderItems.tsx:110` e `:135`, `OrderCreate.tsx:137`, `StockMovement.tsx:111`, `PieceCreate.tsx:134`, `ClientCreate.tsx:48`, `VehicleCreate.tsx:98`, `UserCreate.tsx:66`, `UserPermissions.tsx:41`, `DeveloperConsole.tsx:164` e `:175`, `OrderDocumentFields.tsx:62`, `:165`, `:186`, `Login.tsx:160`. O CSS mantém os dois nomes lado a lado (`styles/buttons.css:34-66`).
- **Abas/alternância:** `shadcn/tabs.tsx` é usado uma única vez (`CatalogHub.tsx:59-87`) e `Orders.tsx:71-86` tem a própria `.view-toggle` para a mesma função.
- **Badge:** `shadcn/badge.tsx:11-15` usa cores fixas do Tailwind; o tema escuro só funciona porque `styles/dark.css:18-67` sobrescreve classes utilitárias (`.bg-white`, `.text-slate-700`…), o que é frágil.
- **Código morto:** `shadcn/chart.tsx` não é importado por ninguém; `recharts` está em `package.json` só por causa dele.
- **Dashboard fora do sistema:** não importa nada de `components/ui`. Tem carregando/erro próprios (`Dashboard.tsx:43-51`), vazio próprio (`.dashboard-empty` em `:259`, `:353`, `:391`), botões próprios (`:144`, `:232`, `:370`) e cartão de indicador próprio (`:185-220`).
- **Casts desnecessários:** `Metric` aceita `ReactNode`, mas há 9 `as any` no valor (`OrderDetail.tsx:341-351`, `Finance.tsx:232-242`, `Reposition.tsx:48`).

### 2.3 Inconsistência visual

- **Dois títulos por tela, com nomes diferentes.** O `h1` da casca (`App.tsx:395`) e um `h2` dentro da view: "Ordens de serviço" × "Atendimentos" (`Orders.tsx:63`), "Financeiro operacional" × "Financeiro" (`Finance.tsx:215`), "Agenda" × "Agenda da oficina" (`Schedule.tsx:55`), "Usuários" × "Acessos do sistema" (`Users.tsx:98`), "Reposição" × "Itens para comprar" (`Reposition.tsx:34`), "Central de cadastros" × "Catálogo e atendimento" (`CatalogHub.tsx:56`). Clientes, veículos, peças e relatório não têm `h2`.
- **Subtítulo genérico.** Clientes, veículos, ordens, relatório, usuários e formulários de cadastro caem em "Gestão simples, segura e organizada" (`navigation.ts:99`).
- **Posição do erro varia.** Acima da barra (`Orders.tsx:56`, `Users.tsx:57`, `CatalogHub.tsx:49`), abaixo dela (`Clients.tsx:22`, `Finance.tsx:224`) ou no meio da página, longe dos botões que o causam (`OrderDetail.tsx:355`, enquanto as ações estão em `:93-109`).
- **Três padrões de mensagem de sucesso.** `Toast` da casca (`App.tsx:444`), `.alert success` local (`OrderDetail.tsx:360`, `Users.tsx:63`) e `.form-feedback` (`OrderItems.tsx:77`).
- **Tailwind solto com cor fixa**, fora dos tokens: `Schedule.tsx:65`, `:87`, `:115`; `Finance.tsx:285`, `:436`; `OrderDetail.tsx:30`.
- **CSS em camadas.** 204 cores em hex/rgb espalhadas por 11 arquivos (72 em `theme.css`, 41 em `base.css`); `.shell` aparece em 36 regras, `.form-card` em 28, `.login` em 20; 46 declarações de fonte ≤ 12px (14 em `dashboard.css`).
- **Formatação ad hoc.** Datas montadas à mão (`Finance.tsx:441`, `Schedule.tsx:120`) em vez de `lib/format`; tipo da movimentação exibido cru (`MonthlyReport.tsx:33`).
- **Perfil.** Todo não administrador aparece como "Balconista" (`App.tsx:365`).

### 2.4 Estados de vazio, erro e carregando

- `MonthlyReport.tsx:9` e `Users.tsx:12` usam `data = []`: enquanto carrega, a tabela mostra "Nenhum registro encontrado. Cadastre um item ou ajuste sua busca." (texto padrão de `Table.tsx:6`), que também não faz sentido para relatório.
- `Reposition.tsx:22-28` — falha ao exportar o CSV substitui a tela inteira pelo erro.
- `Orders.tsx:99-100` — com erro e sem dados, sobra a barra e uma área em branco; não há "Tentar novamente". Só o Dashboard oferece nova tentativa (`Dashboard.tsx:48`).
- `Orders.tsx:103-154` — quadro sem nenhuma OS mostra quatro colunas vazias, sem orientação.
- `Finance.tsx:138-144` e `:447` — "Baixar" individual não confirma nem mostra andamento (o lote confirma, `:156-162`).
- `App.tsx:446-534` — a view é escolhida pela URL sem checar permissão; quem digita `/sistemas/financeiro` sem acesso vê a tela com o erro da API em vez de ser levado à tela inicial. (O backend bloqueia; é só experiência.)
- O padrão `if (!data) return erro ? <alert> : <Loading />` está copiado em 6 views (`OrderDetail.tsx:28-40`, `Finance.tsx:203-210`, `Schedule.tsx:43-50`, `Reposition.tsx:23-29`, `DeveloperConsole.tsx:52-58`, `Dashboard.tsx:43-51`), cada cópia um pouco diferente.

### 2.5 Responsividade

- 12 larguras de quebra diferentes em 9 arquivos (430, 560, 600, 620, 680, 760, 900, 950, 960, 1020, 1100, 1120 px).
- Tabelas só têm rolagem horizontal (`Table.tsx:15`). Peças tem 9 colunas (`Pieces.tsx:71-81`) e Lançamentos 7 (`Finance.tsx:411-419`).
- O quadro de OS depende de arrastar e soltar do HTML5 (`Orders.tsx:130-139`), que não funciona por toque; no celular o quadro abre por padrão (`Orders.tsx:17-19`) mas só serve para leitura.
- Estado que se perde no F5: busca global e peça escolhida para movimentar vivem só no `useState` da casca (`App.tsx:75-77`).

### 2.6 Acessibilidade

- `OrderDocumentFields.tsx:68-79` — modal feito com `div role="dialog"` em portal: fecha com Esc (`:25-32`) mas não prende o foco, não torna o fundo inerte e não devolve o foco ao fechar. O `Confirm.tsx:59` já resolve isso com `<dialog>` nativo.
- `Table.tsx:14-17` — toda tabela vira região focável com rótulo "Tabela de Nome, CPF, Telefone", mesmo sem rolagem; linhas com `key` por índice (`Table.tsx:37`).
- `Orders.tsx:126-149` — mudar status pelo quadro não tem alternativa de teclado; é preciso abrir a OS.
- Campos de busca das listas sem `type="search"` (`Clients.tsx:12`, `Vehicles.tsx:20`, `Pieces.tsx:32`).
- `SearchSelect.tsx:41-73` — um `<label>` envolve dois controles; funciona pelos `aria-label`, mas o clique no rótulo só foca o primeiro.
- Fontes de 10–12px (ver 2.3) em rótulos de indicador e documento.

### 2.7 Navegação

- "Central de cadastros" e "Estoque de peças" mostram a mesma lista de peças por dois caminhos; a da central vem sem o botão "Movimentar" (`CatalogHub.tsx:108` × `App.tsx:454-463`).
- A central baixa as três listas inteiras só para exibir contagens (`CatalogHub.tsx:23-34`) e as abas buscam de novo com `?busca=`.
- Depois de cadastrar cliente ou veículo, e na busca global por placa, a pessoa cai nas telas avulsas `/sistemas/clientes` e `/sistemas/veiculos` (`App.tsx:478`, `:489`; `navigation.ts:114`), fora da central e sem as abas.

---

## 3. Estrutura proposta

Nenhuma pasta nova além de arquivos dentro das pastas que já existem. `★` = arquivo novo extraído de um existente.

```
frontend/src/
  app/
    App.tsx              sessão, tema e casca (cai para ~250 linhas)
    navigation.ts        rotas + tabela { titulo, subtitulo } por view
    menu.tsx           ★ itens e grupos do menu (hoje App.tsx:216-302)
    Views.tsx          ★ escolha da view + checagem de permissão (hoje App.tsx:446-534)
  components/
    ui/                  fonte única: Badge, Button, Card, Confirm, Empty, Loading,
                         Metric, Nav, SearchSelect, Table, Toast
    shadcn/              fica só button.tsx e badge.tsx (card, tabs e chart saem)
  features/
    dashboard/
      Dashboard.tsx
      DashboardNotifications.tsx ★
    finance/
      Finance.tsx          composição + período
      FinanceForm.tsx    ★
      FinanceEntries.tsx ★  tabela + seleção em lote + exportação
      totais.ts          ★  somar / recorte por período / rentabilidade (funções puras)
    orders/
      OrderDetail.tsx      cabeçalho, aprovação, área de trabalho
      components/
        OrderPrintDocument.tsx ★  documento A4 (hoje OrderDetail.tsx:146-339)
        OrderDocumentFields.tsx   (existente)
    (demais features sem mudança de arquivos)
```

### Direção de redesign (consolidação)

1. **Uma tela, um título.** O `h1` da casca é o título; a view não repete `h2`. A barra da view fica com uma frase de orientação (opcional) e as ações.
2. **Ordem fixa dentro da view:** barra de ações → mensagens (erro/sucesso) → indicadores → conteúdo. Erro sempre logo abaixo da barra.
3. **Um botão:** `Button` de `components/ui` em todo lugar; `.primary/.secondary/.link` crus deixam de ser usados.
4. **Um cartão e um formulário:** `Card` para blocos; `.form-card` para formulário de página e `.card.compact-form` só para formulário lateral. Sem novas variantes.
5. **Estados com os componentes existentes:** `Loading` enquanto não há dados, `.alert.error` com "Tentar novamente" quando falha, `Empty` (ou `emptyText` da `Table`) com texto específico da tela.
6. **Cor só por token.** Views não usam `slate-*`, `blue-*`, `emerald-*`; usam as variáveis de `theme.css`. Tailwind fica para espaçamento e layout.
7. **Sem componente novo de UI.** Tudo acima sai dos 11 componentes de `components/ui`.

---

## 4. Etapas

Verificação padrão de toda etapa (rodar em `frontend/`): `pnpm check`, `pnpm test`, `pnpm format:check`, `pnpm build`. Preview: backend em `localhost:8080` e `pnpm dev` (proxy `/api` já configurado em `vite.config.ts`), conferindo a tela afetada em tema claro, tema escuro e largura de celular (375px).

Ordem por prioridade: primeiro o que o usuário percebe com menor risco; por último o que é só organização interna.

### Etapa 1 — Estados de carregando, vazio e erro
- **O quê:** `MonthlyReport` e `Users` passam a mostrar `Loading` antes dos dados e texto de vazio próprio; `Reposition` mostra o erro de exportação sem esconder a tela; `Orders` ganha "Tentar novamente" (`refetch`) e vazio no quadro; "Baixar" individual do financeiro desabilita durante o envio.
- **Arquivos:** `reports/MonthlyReport.tsx`, `users/Users.tsx`, `inventory/Reposition.tsx`, `orders/Orders.tsx`, `finance/Finance.tsx`.
- **Risco:** baixo.
- **Verificar:** no navegador, simular rede lenta e offline (DevTools) em cada tela; banco vazio para os textos de vazio.

### Etapa 2 — Um título por tela
- **O quê:** trocar os ternários de `navigation.ts:39-99` por uma tabela `view → { titulo, subtitulo }`, preencher os subtítulos que hoje caem no genérico e remover os `h2` duplicados das views (a frase de orientação fica).
- **Arquivos:** `app/navigation.ts`, `orders/Orders.tsx`, `finance/Finance.tsx`, `schedule/Schedule.tsx`, `users/Users.tsx`, `inventory/Reposition.tsx`, `catalog/CatalogHub.tsx`, `tests/navigation.test.mjs`.
- **Risco:** baixo. Atenção: os textos novos de subtítulo precisam de aprovação do usuário na revisão da etapa.
- **Verificar:** percorrer todos os itens do menu e as rotas `/novo`; `pnpm test` (navegação).

### Etapa 3 — Botão único
- **O quê:** substituir os 20 `<button className="primary|secondary|link">` por `Button` (variantes `default`, `secondary`, `link`). Quando não sobrar uso, remover os seletores `.primary/.secondary` de `styles/buttons.css`.
- **Arquivos:** os listados em 2.2 e `styles/buttons.css`.
- **Risco:** baixo (o CSS das duas formas já é o mesmo, `buttons.css:34-66`). A tela de login usa o mesmo botão; conferir.
- **Verificar:** `grep -rn 'className="primary\|className="secondary' frontend/src` sem resultado; enviar cada formulário uma vez.

### Etapa 4 — Detalhe da OS em partes
- **O quê:** extrair o documento A4 para `orders/components/OrderPrintDocument.tsx` (só recebe `ordem` e `itens`); subir erro e aviso para logo abaixo da barra de ações; remover os `as any`.
- **Arquivos:** `orders/OrderDetail.tsx`, `orders/components/OrderPrintDocument.tsx` (novo).
- **Risco:** médio — o documento A4 é entrega acadêmica. A marcação e as classes `sputnik-os-*` não mudam, só mudam de arquivo.
- **Verificar:** `pnpm test` (`print.test.mjs`); abrir uma OS e um orçamento, `Ctrl+P` e comparar com a impressão atual; "Baixar PDF"; aprovar um orçamento.

### Etapa 5 — Financeiro em partes
- **O quê:** mover `somar`, recorte por período e rentabilidade para `finance/totais.ts` com um teste no padrão de `tests/format.test.mjs`; extrair `FinanceForm.tsx` e `FinanceEntries.tsx`; exportação CSV reaproveita o trecho de download de `lib/api.ts` em vez de repetir; datas por `lib/format`.
- **Arquivos:** `finance/Finance.tsx`, três arquivos novos na mesma pasta, `lib/api.ts` (exportar a função de download de blob), `tests/finance.test.mjs` (novo).
- **Risco:** médio — envolve valores em dinheiro. Por isso o teste dos totais entra antes de mover o código.
- **Verificar:** conferir os três indicadores e a rentabilidade com os mesmos dados antes e depois; lançar, baixar individual, baixar em lote, exportar CSV e abrir no Excel.

### Etapa 6 — Painel usando os componentes comuns
- **O quê:** `Dashboard` usa `Loading`, `.alert.error` e `Empty`; botões de texto viram `Button variant="link"`; notificações vão para `DashboardNotifications.tsx` com os três blocos repetidos virando uma lista. O cartão de indicador clicável (`.dashboard-metric`) continua próprio, pois `Metric` não é clicável.
- **Arquivos:** `dashboard/Dashboard.tsx`, `dashboard/DashboardNotifications.tsx` (novo), `styles/dashboard.css` (remover classes que ficarem sem uso).
- **Risco:** baixo a médio (tela de entrada do sistema).
- **Verificar:** painel com e sem alertas, com usuário administrador e com usuário só de estoque; atualização automática continua.

### Etapa 7 — Cores por token nas views
- **O quê:** tirar as classes Tailwind de cor de `Schedule.tsx:87` e `:115`, `Finance.tsx:285` e `:436`, e de `shadcn/badge.tsx:11-15`, usando as variáveis de `theme.css`. Em seguida remover de `styles/dark.css:18-67` as sobrescritas que ficarem sem uso.
- **Arquivos:** `schedule/Schedule.tsx`, `finance/Finance.tsx` (ou `FinanceEntries.tsx` se a etapa 5 já passou), `components/shadcn/badge.tsx`, `styles/dark.css`, `styles/theme.css`.
- **Risco:** médio — mexe no tema escuro.
- **Verificar:** todas as telas com badge (ordens, financeiro, usuários, agenda) nos dois temas; contraste dos status.

### Etapa 8 — Casca: menu, views e permissão
- **O quê:** extrair `app/menu.tsx` e `app/Views.tsx` de `App.tsx`; em `Views`, view sem permissão leva à tela inicial do usuário (`landingView`, `App.tsx:103-112`).
- **Arquivos:** `app/App.tsx`, `app/menu.tsx` e `app/Views.tsx` (novos).
- **Risco:** médio — toca login, sessão e todas as rotas. Só mover código; nenhuma mudança de comportamento além do redirecionamento por permissão.
- **Verificar:** `pnpm test`; login e logout; F5 em cada rota; voltar/avançar do navegador; `Ctrl+K` e `N`; menu recolhido e menu no celular; usuário restrito acessando URL de módulo sem permissão.

### Etapa 9 — Limpeza de `components/shadcn`
- **O quê:** `ui/Card` deixa de embrulhar `ShadcnCard` (vira `div.card` direto, mesmo visual); `CatalogHub` usa a `.view-toggle` já existente; apagar `shadcn/card.tsx`, `shadcn/tabs.tsx` e `shadcn/chart.tsx`.
- **Arquivos:** `components/ui/Card.tsx`, `catalog/CatalogHub.tsx`, três arquivos apagados em `components/shadcn/`.
- **Risco:** baixo.
- **Depende de aprovação:** remover `recharts` do `package.json` (só existia para o `chart.tsx`). Se não aprovar, apaga-se só o arquivo.
- **Verificar:** todas as telas com `Card` (detalhe da OS, financeiro, agenda); abas da central; `pnpm build` (o bundle deve diminuir).

### Etapa 10 — Modal dos dados da OS com `<dialog>`
- **O quê:** trocar o `div role="dialog"` de `OrderDocumentFields` por `<dialog>` com `showModal()`, como em `Confirm.tsx`. Ganha foco preso, fundo inerte e retorno do foco sem código novo.
- **Arquivos:** `orders/components/OrderDocumentFields.tsx`, `styles/order-document.css`, `tests/modal.test.mjs` (hoje exige `createPortal`).
- **Risco:** médio — o modal alimenta o documento A4.
- **Verificar:** abrir, navegar só com Tab/Shift+Tab, Esc, clique fora, salvar e conferir os campos na impressão; no celular.

### Etapa 11 — Celular: listas e quadro
- **O quê:** em tela estreita (< 760px) as ordens abrem em lista por padrão (uma condição em `Orders.tsx:17-19`); `type="search"` nos campos de busca; `Table` só fica focável quando há rolagem.
- **Arquivos:** `orders/Orders.tsx`, `clients/Clients.tsx`, `vehicles/Vehicles.tsx`, `inventory/Pieces.tsx`, `components/ui/Table.tsx`.
- **Risco:** baixo.
- **Fora desta etapa (decidir depois):** esconder colunas secundárias no celular e mover status do quadro pelo teclado — os dois pedem mudança na API dos componentes.
- **Verificar:** 375px em ordens, peças e financeiro; navegação por teclado nas tabelas.

### Etapa 12 — Central de cadastros (precisa de aprovação)
- **O quê:** `/sistemas/clientes` e `/sistemas/veiculos` passam a abrir a central já na aba certa, para que o pós-cadastro e a busca por placa não saiam dela; retirar a aba "Peças" da central, já que "Estoque de peças" é a mesma lista com mais ações; tirar as três consultas de contagem.
- **Arquivos:** `catalog/CatalogHub.tsx`, `app/App.tsx` (ou `app/Views.tsx`), `app/navigation.ts`.
- **Risco:** baixo a médio — muda o que o usuário vê no menu "Cadastros". Por alterar navegação apresentada na entrega, **só executar com aprovação explícita**.
- **Verificar:** cadastrar cliente e veículo e conferir onde cai; buscar placa no topo; usuário só com permissão de peças.

### Etapa 13 — CSS: quebras e camadas (opcional, por último)
- **O quê:** reduzir as 12 larguras de quebra para 3 (600, 950, 1120) e mover para os arquivos de origem as regras de `theme.css` que só sobrescrevem `base.css`. Um arquivo CSS por vez.
- **Arquivos:** `styles/*.css`, `tests/layout.test.mjs` (lê os CSS por nome).
- **Risco:** alto — a ordem da cascata é o que mantém o visual atual (`styles/index.css:5`). Sem ganho visível para o usuário; só fazer se as etapas anteriores estiverem estáveis.
- **Verificar:** captura de tela de cada view antes e depois, nos dois temas e em 375/768/1280px; impressão da OS.

---

## 5. Fora do escopo

- Biblioteca de rotas, biblioteca de formulários ou novos componentes de UI.
- Tela `/primeiro-acesso` (servida pelo backend, `App.tsx:188-190`).
- Qualquer mudança de API ou de backend.
- Redesenho de identidade (paleta, tipografia, logotipo) — decisão foi consolidar o atual.

## 6. Pendências para o usuário

1. Textos dos novos subtítulos (etapa 2).
2. Remover a dependência `recharts` (etapa 9).
3. Mudança na central de cadastros (etapa 12).
4. Fazer ou não a etapa 13.
