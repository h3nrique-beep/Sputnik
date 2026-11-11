import { type RefObject } from "react";
import {
  ArrowRight,
  type LucideIcon,
  Package,
  RefreshCw,
  TriangleAlert,
} from "lucide-react";
import { Empty } from "../../components/ui";
import { type DashboardData } from "../../types/domain";

type Aviso = {
  key: string;
  tone: string;
  icon: LucideIcon;
  titulo: string;
  detalhe: string;
  view: string | false;
  rotulo: string;
};

/** Quantos avisos o painel tem; o sino do topo mostra o mesmo número. */
export const contarAvisos = (data: DashboardData) =>
  (data.criticas > 0 ? 1 : 0) +
  data.aguardandoPeca.length +
  data.retornos.length;

export function DashboardNotifications({
  data,
  go,
  can,
  painel,
}: {
  data: DashboardData;
  go: (view: string) => void;
  can: (permission: string) => boolean;
  painel: RefObject<HTMLElement | null>;
}) {
  const verOrdens = can("ORDENS");
  const avisos: Aviso[] = [
    ...(data.criticas > 0
      ? [
          {
            key: "criticas",
            tone: "amber",
            icon: TriangleAlert,
            titulo: `${data.criticas} ${data.criticas === 1 ? "peça precisa" : "peças precisam"} de reposição`,
            detalhe: "Estoque abaixo do mínimo",
            view: can("REPOSICAO")
              ? "reposition"
              : can("PECAS") && ("pieces" as const),
            rotulo: can("REPOSICAO") ? "Ver reposição" : "Ver estoque",
          },
        ]
      : []),
    ...data.aguardandoPeca.map((order) => ({
      key: `piece-${order.id}`,
      tone: "blue",
      icon: Package,
      titulo: `OS #${order.id} aguardando peça`,
      detalhe: order.cliente.nome,
      view: verOrdens && `order-${order.id}`,
      rotulo: `Abrir OS ${order.id}`,
    })),
    ...data.retornos.map((order) => ({
      key: `return-${order.id}`,
      tone: "violet",
      icon: RefreshCw,
      titulo: `Retorno da OS #${order.id}`,
      detalhe: order.cliente.nome,
      view: verOrdens && `order-${order.id}`,
      rotulo: `Abrir OS ${order.id}`,
    })),
  ];
  return (
    <section
      className="dashboard-panel dashboard-notifications"
      ref={painel}
      aria-labelledby="dashboard-notifications-title"
    >
      <div className="dashboard-panel-heading">
        <div>
          <h3 id="dashboard-notifications-title">Notificações</h3>
          <p>
            {avisos.length
              ? `${avisos.length} ${avisos.length === 1 ? "item precisa" : "itens precisam"} de atenção`
              : "Tudo em dia por aqui"}
          </p>
        </div>
        <span className="dashboard-count">{avisos.length}</span>
      </div>
      {avisos.length ? (
        <div className="dashboard-notification-list">
          {avisos.map(
            ({ key, tone, icon: Icon, titulo, detalhe, view, rotulo }) => (
              <div className="dashboard-notification" key={key}>
                <span className={`dashboard-notification-icon tone-${tone}`}>
                  <Icon size={18} />
                </span>
                <div>
                  <strong>{titulo}</strong>
                  <small>{detalhe}</small>
                </div>
                {view && (
                  <button onClick={() => go(view)} aria-label={rotulo}>
                    <ArrowRight size={17} />
                  </button>
                )}
              </div>
            ),
          )}
        </div>
      ) : (
        <Empty text="Nenhum alerta operacional no momento." />
      )}
    </section>
  );
}
