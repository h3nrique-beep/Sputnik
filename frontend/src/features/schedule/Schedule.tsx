import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Empty, Loading } from "../../components/ui";
import { api } from "../../lib/api";
import { dateTime } from "../../lib/format";
import { type Order } from "../../types/domain";

export function Schedule({ go }: { go: (view: string) => void }) {
  const [orders, setOrders] = useState<Order[]>();
  const [filter, setFilter] = useState("");
  const [erro, setErro] = useState("");
  useEffect(() => {
    api<Order[]>("/ordens")
      .then(setOrders)
      .catch((e: Error) => setErro(e.message));
  }, []);
  const scheduled = useMemo(
    () =>
      (orders ?? [])
        .filter(
          (order) =>
            order.agendamento &&
            !["CONCLUIDA", "CANCELADA"].includes(order.status),
        )
        .filter(
          (order) => !filter || order.agendamento?.slice(0, 10) === filter,
        )
        .sort((a, b) =>
          String(a.agendamento).localeCompare(String(b.agendamento)),
        ),
    [orders, filter],
  );
  const followUps = useMemo(
    () =>
      (orders ?? [])
        .flatMap((order) => [
          order.retornoEm
            ? { order, tipo: "Retorno preventivo", data: order.retornoEm }
            : null,
          order.garantiaAte
            ? { order, tipo: "Garantia até", data: order.garantiaAte }
            : null,
        ])
        .filter(Boolean) as { order: Order; tipo: string; data: string }[],
    [orders],
  );
  if (!orders)
    return erro ? (
      <div className="alert error" role="alert">
        {erro}
      </div>
    ) : (
      <Loading />
    );
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <div>
          <h2>Agenda da oficina</h2>
          <p>
            Organize os próximos atendimentos e abra a OS diretamente da fila.
          </p>
        </div>
        <Button onClick={() => go("orders-new")}>＋ Agendar atendimento</Button>
      </div>
      <Card title="Próximos atendimentos">
        <div className="mb-4 flex flex-wrap items-end gap-3">
          <label className="max-w-xs">
            Data
            <input
              type="date"
              value={filter}
              onChange={(event) => setFilter(event.target.value)}
            />
          </label>
          {filter && (
            <Button variant="ghost" size="sm" onClick={() => setFilter("")}>
              Limpar filtro
            </Button>
          )}
        </div>
        {scheduled.length ? (
          <div className="space-y-3">
            {scheduled.map((order) => (
              <button
                type="button"
                key={order.id}
                onClick={() => go("order-" + order.id)}
                className="flex w-full flex-col gap-2 rounded-lg border border-slate-200 bg-white p-4 text-left transition hover:border-blue-300 hover:bg-blue-50 sm:flex-row sm:items-center sm:justify-between"
              >
                <div>
                  <b>{dateTime(String(order.agendamento))}</b>
                  <p className="text-sm text-slate-600">
                    {order.cliente.nome} · {order.veiculo.marcaModelo} ·{" "}
                    {order.veiculo.placa}
                  </p>
                  <small className="text-slate-500">{order.descricao}</small>
                </div>
                <Badge status={order.status} />
              </button>
            ))}
          </div>
        ) : (
          <Empty text="Nenhum atendimento agendado para este período." />
        )}
      </Card>
      <Card title="Retornos e garantias">
        {followUps.length ? (
          <div className="space-y-3">
            {followUps
              .sort((a, b) => a.data.localeCompare(b.data))
              .map(({ order, tipo, data }) => (
                <button
                  type="button"
                  key={tipo + order.id}
                  onClick={() => go("order-" + order.id)}
                  className="flex w-full items-center justify-between rounded-lg border border-slate-200 bg-white p-4 text-left transition hover:border-blue-300"
                >
                  <div>
                    <b>
                      {tipo}:{" "}
                      {new Date(data + "T12:00:00").toLocaleDateString("pt-BR")}
                    </b>
                    <p className="text-sm text-slate-600">
                      OS #{order.id} · {order.cliente.nome} ·{" "}
                      {order.veiculo.placa}
                    </p>
                  </div>
                  <Badge status={order.status} />
                </button>
              ))}
          </div>
        ) : (
          <Empty text="Nenhum retorno ou garantia registrado." />
        )}
      </Card>
    </div>
  );
}
