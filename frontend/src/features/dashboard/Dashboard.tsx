import { useEffect, useRef, useState } from "react";
import {
  ArrowRight,
  Bell,
  ClipboardList,
  Package,
  PackagePlus,
  PackageSearch,
  RefreshCw,
  Search,
  TriangleAlert,
  Warehouse,
} from "lucide-react";
import { api } from "../../lib/api";
import { label } from "../../lib/format";
import { type DashboardData } from "../../types/domain";

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
  const verEstoque =
    can("PECAS") || can("REPOSICAO") || can("RELATORIO_MENSAL");
  const attention = [
    verEstoque && {
      label: "Estoque crítico",
      value: data.criticas,
      detail: data.criticas
        ? "Peças abaixo do mínimo"
        : "Nenhuma peça abaixo do mínimo",
      icon: TriangleAlert,
      tone: "amber",
      view: can("REPOSICAO") ? "reposition" : can("PECAS") ? "pieces" : null,
      cta: can("REPOSICAO") ? "Ver reposição" : "Ver estoque",
    },
    can("ORDENS") && {
      label: "Aguardando peça",
      value: data.aguardandoPeca.length,
      detail: data.aguardandoPeca.length
        ? "Ordens paradas por falta de peça"
        : "Nenhuma ordem parada",
      icon: PackageSearch,
      tone: "amber",
      view: "orders",
      cta: "Ver ordens",
    },
    can("ORDENS") && {
      label: "Retornos a contatar",
      value: data.retornos.length,
      detail: data.retornos.length
        ? "Clientes com retorno preventivo"
        : "Nenhum retorno pendente",
      icon: RefreshCw,
      tone: "violet",
      view: "orders",
      cta: "Ver ordens",
    },
    can("ORDENS") && {
      label: "Ordens em aberto",
      value: data.ordensAbertas,
      detail: `${data.ordensConcluidasMes.toLocaleString("pt-BR")} concluídas no mês`,
      icon: ClipboardList,
      tone: "blue",
      view: "orders",
      cta: "Ver ordens",
    },
  ].filter((item) => item !== false);

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
      {actions.length > 0 && (
        <nav className="dashboard-quick" aria-label="Ações rápidas">
          {actions.map(({ label, view, icon: Icon }) => (
            <button key={view} type="button" onClick={() => go(view)}>
              <Icon size={17} aria-hidden="true" />
              {label}
            </button>
          ))}
        </nav>
      )}
      <div className="dashboard-metrics">
        {attention.map(
          ({ label, value, detail, icon: Icon, tone, view, cta }) => {
            const content = (
              <>
                <span className="dashboard-metric-icon">
                  <Icon size={19} aria-hidden="true" />
                </span>
                <span className="dashboard-metric-label">{label}</span>
                <strong>{value.toLocaleString("pt-BR")}</strong>
                <small>{detail}</small>
                {view && value > 0 && (
                  <span className="dashboard-metric-cta">
                    {cta} <ArrowRight size={14} aria-hidden="true" />
                  </span>
                )}
              </>
            );
            const className = `dashboard-metric tone-${value > 0 ? tone : "clear"}`;
            return view ? (
              <button
                type="button"
                className={className}
                key={label}
                onClick={() => go(view)}
              >
                {content}
              </button>
            ) : (
              <article className={className} key={label}>
                {content}
              </article>
            );
          },
        )}
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
                    <span>{label(status)}</span>
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
      <div className="dashboard-bottom">
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
                  <span>
                    <code className="dashboard-code">{piece.codigo}</code>{" "}
                    {piece.nome}
                  </span>
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
      </div>
    </div>
  );
}
