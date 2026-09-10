export const title = (v: string) =>
  v === "dashboard"
    ? "Visão geral"
    : v === "movement"
      ? "Movimentar estoque"
      : v === "report"
        ? "Relatório mensal"
        : v === "reposition"
          ? "Reposição"
          : v === "users"
            ? "Usuários"
            : v === "developer"
              ? "Desenvolvedor"
              : v.startsWith("pieces")
                ? v === "pieces-new"
                  ? "Cadastrar peça"
                  : "Peças"
                : v.startsWith("clients")
                  ? v === "clients-new"
                    ? "Cadastrar cliente"
                    : "Clientes"
                  : v.startsWith("vehicles")
                    ? v === "vehicles-new"
                      ? "Cadastrar veículo"
                      : "Veículos"
                    : v === "orders-new"
                      ? "Nova ordem de serviço"
                      : v.startsWith("order-")
                        ? "Detalhes da OS"
                        : "Ordens de serviço";

export const subtitle = (v: string) =>
  v === "dashboard"
    ? "Controle operacional da sua oficina"
    : v === "orders-new"
      ? "Selecione o cliente e o veículo para registrar o atendimento"
      : v === "developer"
        ? "Monitoramento, manutenção e cópias seguras do sistema"
        : "Gestão simples, segura e organizada";
