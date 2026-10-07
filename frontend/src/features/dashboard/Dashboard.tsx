import { useEffect, useRef, useState } from "react";
import {
  ArrowRight,
  Bell,
  CheckCircle2,
  ClipboardList,
  Package,
  PackagePlus,
  RefreshCw,
  Search,
  TriangleAlert,
  Warehouse,
} from "lucide-react";
import { api } from "../../lib/api";
import { type Order, type Piece } from "../../types/domain";

type DashboardData = {
  pecas: number;
  ordensAbertas: number;
  ordensConcluidasMes: number;
  criticas: number;
  porStatus: Record<string, number>;
  aguardandoPeca: Order[];
  retornos: Order[];
  pecasMaisUsadas: Piece[];
};

const statusName = (status: string) =>
  status.replaceAll("_", " ").toLocaleLowerCase("pt-BR");

export function Dashboard({
  go,
  can,
}: {
  go: (view: string) => void;
  can: (permission: string) => boolean;
}) {
  const [data, setData] = useState<DashboardData | null>(null);
  const [error, setError] = useState("");
  const [refreshing, setRefreshing] = useState(false);
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null);
  const notifications = useRef<HTMLElement>(null);

  const refresh = async () => {
    setRefreshing(true);
    try {
      setData(await api<DashboardData>("/dashboard"));
      setUpdatedAt(new Date());
      setError("");
    } catch {
      setError("Não foi possível atualizar o painel. Tente novamente.");
    } finally {
      setRefreshing(false);
    }
  };
  useEffect(() => {
    void refresh();
    const timer = window.setInterval(() => {
      if (document.visibilityState === "visible") void refresh();
    }, 60_000);
    return () => window.clearInterval(timer);
  }, []);

  if (!data)
    return (
      <div className="dashboard-state" role="status">
        <p>{error || "Carregando visão geral..."}</p>
        {error && (
          <button onClick={() => void refresh()}>Tentar novamente</button>
        )}
      </div>
    );

  const statusEntries = Object.entries(data.porStatus).filter(
    ([, count]) => count > 0,
  );
  const maxStatus = Math.max(1, ...statusEntries.map(([, count]) => count));
  const notificationCount =
    (data.criticas > 0 ? 1 : 0) +
    data.aguardandoPeca.length +
    data.retornos.length;
  const actions = [
    can("ORDENS") && {
      label: "Nova ordem de serviço",
      view: "orders-new",
      icon: ClipboardList,
    },
    can("PECAS") && {
      label: "Consultar estoque",
      view: "pieces",
      icon: Search,
    },
    can("PECAS") && {
      label: "Cadastrar peça",
      view: "pieces-new",
      icon: PackagePlus,
    },
    can("PECAS") && {
      label: "Movimentar estoque",
      view: "movement",
      icon: Warehouse,
    },
  ].filter((action) => action !== false);

  return (
    <div className="dashboard">
      <div className="dashboard-intro">
        <div>
          <h2>O que precisa da sua atenção hoje</h2>
          <p>
            Disponibilidade de peças e atendimento para decidir o próximo passo.
          </p>
        </div>
        <div className="dashboard-toolbar">
          <span className="dashboard-updated">
            {updatedAt &&
              `Atualizado às ${updatedAt.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}`}
          </span>
          <button
            type="button"
            className="dashboard-icon-button"
            onClick={() => void refresh()}
            disabled={refreshing}
            aria-label="Atualizar painel"
            title="Atualizar painel"
          >
            <RefreshCw size={18} aria-hidden="true" />
          </button>
          <button
            type="button"
            className="dashboard-icon-button dashboard-bell"
            onClick={() =>
              notifications.current?.scrollIntoView({ behavior: "smooth" })
            }
            aria-label={`Ver notificações: ${notificationCount}`}
            title="Ver notificações"
          >
            <Bell size={18} aria-hidden="true" />
            {notificationCount > 0 && (
              <span aria-hidden="true">{notificationCount}</span>
            )}
          </button>
        </div>
      </div>
      {error && (
        <p className="dashboard-error" role="alert">
          {error}
        </p>
      )}
      <div className="dashboard-metrics">
        {[
          {
            label: "Peças no catálogo",
            value: data.pecas,
            detail: "Itens de estoque automotivo",
            icon: Package,
            tone: "blue",
          },
          {
            label: "Ordens em aberto",
            value: data.ordensAbertas,
            detail: "Na fila de atendimento",
            icon: ClipboardList,
            tone: "blue",
          },
          {
            label: "Concluídas no mês",
            value: data.ordensConcluidasMes,
            detail: "Atendimentos finalizados",
            icon: CheckCircle2,
            tone: "green",
          },
          {
            label: "Estoque crítico",
            value: data.criticas,
            detail: "Itens abaixo do mínimo",
            icon: TriangleAlert,
            tone: "amber",
          },
        ].map(({ label, value, detail, icon: Icon, tone }) => (
          <article className={`dashboard-metric tone-${tone}`} key={label}>
            <span className="dashboard-metric-icon">
              <Icon size={19} aria-hidden="true" />
            </span>
            <span className="dashboard-metric-label">{label}</span>
            <strong>{value.toLocaleString("pt-BR")}</strong>
            <small>{detail}</small>
          </article>
        ))}
      </div>
      <div className="dashboard-grid">
        <section
          className="dashboard-panel"
          aria-labelledby="dashboard-status-title"
        >
          <div className="dashboard-panel-heading">
            <div>
              <h3 id="dashboard-status-title">Fila de atendimento</h3>
              <p>Ordens de serviço por etapa</p>
            </div>
            {can("ORDENS") && (
              <button
                className="dashboard-text-button"
                onClick={() => go("orders")}
              >
                Ver ordens <ArrowRight size={15} />
              </button>
            )}
          </div>
          {statusEntries.length ? (
            <div className="dashboard-status-list">
              {statusEntries.map(([status, count]) => (
                <div className="dashboard-status-row" key={status}>
                  <div>
                    <span>{statusName(status)}</span>
                    <strong>{count}</strong>
                  </div>
                  <div className="dashboard-status-track">
                    <span
                      style={{
                        width: `${Math.max(6, (count / maxStatus) * 100)}%`,
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="dashboard-empty">Nenhuma ordem registrada ainda.</p>
          )}
        </section>
        <section
          className="dashboard-panel dashboard-notifications"
          ref={notifications}
          aria-labelledby="dashboard-notifications-title"
        >
          <div className="dashboard-panel-heading">
            <div>
              <h3 id="dashboard-notifications-title">Notificações</h3>
              <p>
                {notificationCount
                  ? `${notificationCount} ${notificationCount === 1 ? "item precisa" : "itens precisam"} de atenção`
                  : "Tudo em dia por aqui"}
              </p>
            </div>
            <span className="dashboard-count">{notificationCount}</span>
          </div>
          {notificationCount ? (
            <div className="dashboard-notification-list">
              {data.criticas > 0 && (
                <div className="dashboard-notification">
                  <span className="dashboard-notification-icon tone-amber">
                    <TriangleAlert size={18} />
                  </span>
                  <div>
                    <strong>
                      {data.criticas}{" "}
                      {data.criticas === 1 ? "peça precisa" : "peças precisam"}{" "}
                      de reposição
                    </strong>
                    <small>Estoque abaixo do mínimo</small>
                  </div>
                  {(can("REPOSICAO") || can("PECAS")) && (
                    <button
                      onClick={() =>
                        go(can("REPOSICAO") ? "reposition" : "pieces")
                      }
                      aria-label={
                        can("REPOSICAO") ? "Ver reposição" : "Ver estoque"
                      }
                    >
                      <ArrowRight size={17} />
                    </button>
                  )}
                </div>
              )}
              {data.aguardandoPeca.map((order) => (
                <div
                  className="dashboard-notification"
                  key={`piece-${order.id}`}
                >
                  <span className="dashboard-notification-icon tone-blue">
                    <Package size={18} />
                  </span>
                  <div>
                    <strong>OS #{order.id} aguardando peça</strong>
                    <small>{order.cliente.nome}</small>
                  </div>
                  {can("ORDENS") && (
                    <button
                      onClick={() => go(`order-${order.id}`)}
                      aria-label={`Abrir OS ${order.id}`}
                    >
                      <ArrowRight size={17} />
                    </button>
                  )}
                </div>
              ))}
              {data.retornos.map((order) => (
                <div
                  className="dashboard-notification"
                  key={`return-${order.id}`}
                >
                  <span className="dashboard-notification-icon tone-violet">
                    <RefreshCw size={18} />
                  </span>
                  <div>
                    <strong>Retorno da OS #{order.id}</strong>
                    <small>{order.cliente.nome}</small>
                  </div>
                  {can("ORDENS") && (
                    <button
                      onClick={() => go(`order-${order.id}`)}
                      aria-label={`Abrir OS ${order.id}`}
                    >
                      <ArrowRight size={17} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <p className="dashboard-empty">
              Nenhum alerta operacional no momento.
            </p>
          )}
        </section>
      </div>
      <div className="dashboard-grid dashboard-bottom">
        <section
          className="dashboard-panel"
          aria-labelledby="dashboard-pieces-title"
        >
          <div className="dashboard-panel-heading">
            <div>
              <h3 id="dashboard-pieces-title">Peças mais utilizadas</h3>
              <p>Disponibilidade dos itens com maior uso</p>
            </div>
            {can("PECAS") && (
              <button
                className="dashboard-text-button"
                onClick={() => go("pieces")}
              >
                Ver estoque <ArrowRight size={15} />
              </button>
            )}
          </div>
          {data.pecasMaisUsadas.length ? (
            <div className="dashboard-piece-list">
              {data.pecasMaisUsadas.slice(0, 5).map((piece) => (
                <div key={piece.id}>
                  <span>{piece.nome}</span>
                  <strong>{piece.disponivel} disponíveis</strong>
                </div>
              ))}
            </div>
          ) : (
            <p className="dashboard-empty">
              O uso de peças aparecerá aqui após os primeiros atendimentos.
            </p>
          )}
        </section>
        <section
          className="dashboard-panel"
          aria-labelledby="dashboard-actions-title"
        >
          <div className="dashboard-panel-heading">
            <div>
              <h3 id="dashboard-actions-title">Ações rápidas</h3>
              <p>Acesse as rotinas mais frequentes</p>
            </div>
          </div>
          <div className="dashboard-action-list">
            {actions.length ? (
              actions.map(({ label, view, icon: Icon }) => (
                <button key={view} onClick={() => go(view)}>
                  <Icon size={18} aria-hidden="true" />
                  <span>{label}</span>
                  <ArrowRight size={16} aria-hidden="true" />
                </button>
              ))
            ) : (
              <p className="dashboard-empty">
                Use o menu para acessar seus módulos.
              </p>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
