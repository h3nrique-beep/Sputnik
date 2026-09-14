import { type FormEvent, useEffect, useMemo, useState } from "react";
import {
  Badge,
  Button,
  Card,
  Empty,
  Loading,
  Metric,
  Table,
} from "../../components/ui";
import { api } from "../../lib/api";
import { money } from "../../lib/format";
import { type Order } from "../../types/domain";

type Entry = {
  id: number;
  tipo: "RECEITA" | "DESPESA";
  status: "PENDENTE" | "PAGO";
  descricao: string;
  categoria: string;
  valor: number;
  vencimento: string;
  ordemServicoId: number | "";
};
const hoje = new Date().toISOString().slice(0, 10);

export function Finance({ go }: { go: (view: string) => void }) {
  const [entries, setEntries] = useState<Entry[]>(),
    [orders, setOrders] = useState<Order[]>([]);
  const [form, setForm] = useState({
    tipo: "RECEITA",
    descricao: "",
    categoria: "",
    valor: "",
    vencimento: hoje,
    ordemServicoId: "",
  });
  const [erro, setErro] = useState("");
  const load = () =>
    api<Entry[]>("/financeiro")
      .then(setEntries)
      .catch((e) => setErro(e.message));
  useEffect(() => {
    void load();
    api<Order[]>("/ordens").then(setOrders);
  }, []);
  const totals = useMemo(() => {
    const all = entries ?? [];
    const total = (tipo: Entry["tipo"], status: Entry["status"]) =>
      all
        .filter((entry) => entry.tipo === tipo && entry.status === status)
        .reduce((sum, entry) => sum + Number(entry.valor), 0);
    return {
      receber: total("RECEITA", "PENDENTE"),
      pagar: total("DESPESA", "PENDENTE"),
      recebido: total("RECEITA", "PAGO"),
    };
  }, [entries]);
  const profitability = useMemo(
    () =>
      Object.values(
        (entries ?? [])
          .filter((entry) => entry.ordemServicoId && entry.status === "PAGO")
          .reduce<
            Record<
              string,
              { orderId: number; receita: number; despesa: number }
            >
          >((acc, entry) => {
            const orderId = Number(entry.ordemServicoId);
            const current = acc[orderId] ?? { orderId, receita: 0, despesa: 0 };
            current[entry.tipo === "RECEITA" ? "receita" : "despesa"] += Number(
              entry.valor,
            );
            acc[orderId] = current;
            return acc;
          }, {}),
      ),
    [entries],
  );
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setErro("");
    try {
      await api("/financeiro", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          valor: Number(form.valor),
          ordemServicoId: form.ordemServicoId
            ? Number(form.ordemServicoId)
            : null,
        }),
      });
      setForm({
        tipo: "RECEITA",
        descricao: "",
        categoria: "",
        valor: "",
        vencimento: hoje,
        ordemServicoId: "",
      });
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  const baixar = async (id: number) => {
    try {
      await api("/financeiro/" + id + "/baixar", { method: "POST" });
      load();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  if (!entries) return <Loading />;
  return (
    <div className="module-shell space-y-5">
      <div className="toolbar">
        <div>
          <h2>Financeiro</h2>
          <p>Controle contas a receber, despesas e pagamentos efetivados.</p>
        </div>
        <Button variant="secondary" onClick={() => go("orders")}>
          Ver ordens
        </Button>
      </div>
      {erro && <div className="alert error">{erro}</div>}
      <div className="metrics">
        <Metric label="A receber" value={money(totals.receber) as any} />
        <Metric label="A pagar" value={money(totals.pagar) as any} />
        <Metric label="Recebido" value={money(totals.recebido) as any} />
      </div>
      <Card title="Rentabilidade por OS baixada">
        {profitability.length ? (
          <Table
            headers={["OS", "Receitas", "Despesas", "Resultado"]}
            rows={profitability.map((item) => {
              const order = orders.find(
                (candidate) => candidate.id === item.orderId,
              );
              const result = item.receita - item.despesa;
              return [
                "#" + item.orderId + (order ? " · " + order.cliente.nome : ""),
                money(item.receita),
                money(item.despesa),
                <b
                  key={item.orderId}
                  className={result >= 0 ? "text-emerald-700" : "text-red-700"}
                >
                  {money(result)}
                </b>,
              ];
            })}
          />
        ) : (
          <Empty text="Baixe receitas e despesas vinculadas a uma OS para acompanhar sua rentabilidade." />
        )}
      </Card>
      <div className="cards">
        <form className="card compact-form" onSubmit={submit}>
          <h2>Novo lançamento</h2>
          <label>
            Tipo
            <select
              value={form.tipo}
              onChange={(e) => setForm({ ...form, tipo: e.target.value })}
            >
              <option value="RECEITA">Receita</option>
              <option value="DESPESA">Despesa</option>
            </select>
          </label>
          <label>
            Descrição
            <input
              required
              value={form.descricao}
              onChange={(e) => setForm({ ...form, descricao: e.target.value })}
            />
          </label>
          <label>
            Categoria
            <input
              value={form.categoria}
              onChange={(e) => setForm({ ...form, categoria: e.target.value })}
              placeholder="Ex.: mão de obra, aluguel"
            />
          </label>
          <label>
            Valor
            <input
              required
              min="0.01"
              step="0.01"
              type="number"
              value={form.valor}
              onChange={(e) => setForm({ ...form, valor: e.target.value })}
            />
          </label>
          <label>
            Vencimento
            <input
              required
              type="date"
              value={form.vencimento}
              onChange={(e) => setForm({ ...form, vencimento: e.target.value })}
            />
          </label>
          <label>
            OS vinculada (opcional)
            <select
              value={form.ordemServicoId}
              onChange={(e) =>
                setForm({ ...form, ordemServicoId: e.target.value })
              }
            >
              <option value="">Nenhuma</option>
              {orders.map((order) => (
                <option key={order.id} value={order.id}>
                  OS #{order.id} · {order.cliente.nome}
                </option>
              ))}
            </select>
          </label>
          <button className="primary">Adicionar lançamento</button>
        </form>
        <Card title="Como usar">
          <p>
            Registre receitas previstas e despesas da oficina. Ao receber ou
            pagar, use “Baixar” para marcar a movimentação no dia atual.
          </p>
        </Card>
      </div>
      <Card title="Lançamentos">
        {entries.length ? (
          <Table
            headers={[
              "Tipo",
              "Descrição",
              "Vencimento",
              "Valor",
              "Status",
              "Ação",
            ]}
            rows={entries.map((entry) => [
              entry.tipo === "RECEITA" ? "Receita" : "Despesa",
              <span key={entry.id}>
                {entry.descricao}
                <small className="block text-slate-500">
                  {entry.categoria || "Sem categoria"}
                  {entry.ordemServicoId ? " · OS #" + entry.ordemServicoId : ""}
                </small>
              </span>,
              new Date(entry.vencimento + "T12:00:00").toLocaleDateString(
                "pt-BR",
              ),
              money(entry.valor),
              <Badge key="status" status={entry.status} />,
              entry.status === "PENDENTE" ? (
                <Button key="baixar" size="sm" onClick={() => baixar(entry.id)}>
                  Baixar
                </Button>
              ) : (
                "Pago"
              ),
            ])}
          />
        ) : (
          <Empty text="Nenhum lançamento financeiro cadastrado." />
        )}
      </Card>
    </div>
  );
}
