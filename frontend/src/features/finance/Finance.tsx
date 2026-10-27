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
import { localDate, money, paraCsv, variacao } from "../../lib/format";
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

export function Finance({
  go,
  canOrders,
}: {
  go: (view: string) => void;
  canOrders: boolean;
}) {
  const hoje = localDate(new Date());
  const [entries, setEntries] = useState<Entry[]>(),
    [orders, setOrders] = useState<Order[]>([]);
  const [period, setPeriod] = useState<"all" | "month" | "next30">("month");
  const [form, setForm] = useState({
    tipo: "RECEITA",
    descricao: "",
    categoria: "",
    valor: "",
    vencimento: hoje,
    ordemServicoId: "",
  });
  const [erro, setErro] = useState("");
  const [selecionados, setSelecionados] = useState<Set<number>>(new Set());
  const [baixandoLote, setBaixandoLote] = useState(false);
  const load = () =>
    api<Entry[]>("/financeiro")
      .then(setEntries)
      .catch((e) => setErro(e.message));
  useEffect(() => {
    void load();
    if (canOrders)
      api<Order[]>("/ordens")
        .then(setOrders)
        .catch((e: Error) =>
          setErro(
            "Não foi possível carregar as ordens para vínculo: " + e.message,
          ),
        );
  }, [canOrders]);
  const entriesInPeriod = useMemo(() => {
    const date = new Date();
    const monthStart = localDate(
      new Date(date.getFullYear(), date.getMonth(), 1),
    );
    const monthEnd = localDate(
      new Date(date.getFullYear(), date.getMonth() + 1, 1),
    );
    const next30 = new Date(date);
    next30.setDate(date.getDate() + 30);
    const limit = localDate(next30);
    return (entries ?? []).filter((entry) =>
      period === "month"
        ? entry.vencimento >= monthStart && entry.vencimento < monthEnd
        : period === "next30"
          ? entry.vencimento >= hoje && entry.vencimento <= limit
          : true,
    );
  }, [entries, period, hoje]);
  const totals = useMemo(() => somar(entriesInPeriod), [entriesInPeriod]);
  // Mesmo recorte do mês corrente aplicado ao mês anterior, para comparação.
  const anterior = useMemo(() => {
    const date = new Date();
    const inicio = new Date(date.getFullYear(), date.getMonth() - 1, 1);
    const de = localDate(inicio);
    const ate = localDate(new Date(date.getFullYear(), date.getMonth(), 1));
    return {
      rotulo: inicio
        .toLocaleDateString("pt-BR", { month: "short" })
        .replace(".", ""),
      ...somar(
        (entries ?? []).filter((e) => e.vencimento >= de && e.vencimento < ate),
      ),
    };
  }, [entries]);
  const comparar = (chave: keyof ReturnType<typeof somar>) =>
    period === "month"
      ? variacao(totals[chave], anterior[chave], anterior.rotulo)
      : "";
  const profitability = useMemo(
    () =>
      Object.values(
        entriesInPeriod
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
    [entriesInPeriod],
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
  const pendentes = entriesInPeriod.filter((e) => e.status === "PENDENTE");
  const marcados = pendentes.filter((e) => selecionados.has(e.id));
  const somaMarcados = marcados.reduce((sum, e) => sum + Number(e.valor), 0);
  const alternar = (id: number) =>
    setSelecionados((atual) => {
      const proximo = new Set(atual);
      if (!proximo.delete(id)) proximo.add(id);
      return proximo;
    });
  const baixarSelecionados = async () => {
    if (
      !confirm(
        `Baixar ${marcados.length} ${marcados.length === 1 ? "lançamento" : "lançamentos"} (${money(somaMarcados)}) com a data de hoje?`,
      )
    )
      return;
    setErro("");
    setBaixandoLote(true);
    const falhas: string[] = [];
    // Um por vez: cada baixa é independente e uma falha não desfaz as outras.
    for (const entry of marcados) {
      try {
        await api("/financeiro/" + entry.id + "/baixar", { method: "POST" });
      } catch (e) {
        falhas.push(`${entry.descricao}: ${(e as Error).message}`);
      }
    }
    setBaixandoLote(false);
    setSelecionados(new Set());
    if (falhas.length) setErro("Não baixados: " + falhas.join(" · "));
    load();
  };
  const exportar = () => {
    const linhas = (marcados.length ? marcados : entriesInPeriod).map((e) => [
      e.tipo === "RECEITA" ? "Receita" : "Despesa",
      e.descricao,
      e.categoria,
      e.vencimento,
      Number(e.valor).toFixed(2).replace(".", ","),
      e.status === "PAGO" ? "Pago" : "Pendente",
      e.ordemServicoId ? "#" + e.ordemServicoId : "",
    ]);
    const csv = paraCsv([
      ["Tipo", "Descrição", "Categoria", "Vencimento", "Valor", "Status", "OS"],
      ...linhas,
    ]);
    // BOM para o Excel reconhecer UTF-8 e manter os acentos.
    const url = URL.createObjectURL(
      new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8" }),
    );
    const link = document.createElement("a");
    link.href = url;
    link.download = `lancamentos-${hoje}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  };
  if (!entries)
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
          <h2>Financeiro</h2>
          <p>Controle contas a receber, despesas e pagamentos efetivados.</p>
        </div>
        {canOrders && (
          <Button variant="secondary" onClick={() => go("orders")}>
            Ver ordens
          </Button>
        )}
      </div>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <div className="metrics">
        <Metric
          label="A receber"
          value={money(totals.receber) as any}
          detail={comparar("receber")}
        />
        <Metric
          label="A pagar"
          value={money(totals.pagar) as any}
          detail={comparar("pagar")}
        />
        <Metric
          label="Recebido"
          value={money(totals.recebido) as any}
          detail={comparar("recebido")}
        />
      </div>
      <div className="toolbar compact-toolbar">
        <p>
          Indicadores e lista por vencimento:{" "}
          <b>
            {period === "month"
              ? "este mês"
              : period === "next30"
                ? "próximos 30 dias"
                : "todo o histórico"}
          </b>
          .
        </p>
        <label className="field-inline">
          Período
          <select
            value={period}
            onChange={(event) => setPeriod(event.target.value as typeof period)}
          >
            <option value="month">Este mês</option>
            <option value="next30">Próximos 30 dias</option>
            <option value="all">Todo o histórico</option>
          </select>
        </label>
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
          {canOrders && (
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
          )}
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
        {entriesInPeriod.length > 0 && (
          <div className="bulk-bar" role="group" aria-label="Ações em lote">
            <span aria-live="polite">
              {marcados.length
                ? `${marcados.length} selecionado(s) · ${money(somaMarcados)}`
                : "Marque os pendentes para baixar de uma vez."}
            </span>
            {pendentes.length > 0 && (
              <Button
                size="sm"
                variant="secondary"
                onClick={() =>
                  setSelecionados(
                    marcados.length === pendentes.length
                      ? new Set()
                      : new Set(pendentes.map((e) => e.id)),
                  )
                }
              >
                {marcados.length === pendentes.length
                  ? "Limpar seleção"
                  : "Selecionar pendentes"}
              </Button>
            )}
            <Button size="sm" variant="secondary" onClick={exportar}>
              {marcados.length ? "Exportar selecionados" : "Exportar CSV"}
            </Button>
            <Button
              size="sm"
              onClick={baixarSelecionados}
              disabled={!marcados.length || baixandoLote}
            >
              {baixandoLote ? "Baixando..." : "Baixar selecionados"}
            </Button>
          </div>
        )}
        {entriesInPeriod.length ? (
          <Table
            headers={[
              "Seleção",
              "Tipo",
              "Descrição",
              "Vencimento",
              "Valor",
              "Status",
              "Ação",
            ]}
            rows={entriesInPeriod.map((entry) => [
              entry.status === "PENDENTE" ? (
                <input
                  key="sel"
                  type="checkbox"
                  className="row-check"
                  checked={selecionados.has(entry.id)}
                  onChange={() => alternar(entry.id)}
                  aria-label={`Selecionar ${entry.descricao}`}
                />
              ) : (
                ""
              ),
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
          <Empty text="Nenhum lançamento para o período selecionado." />
        )}
      </Card>
    </div>
  );
}

function somar(lista: Entry[]) {
  const total = (tipo: Entry["tipo"], status: Entry["status"]) =>
    lista
      .filter((entry) => entry.tipo === tipo && entry.status === status)
      .reduce((sum, entry) => sum + Number(entry.valor), 0);
  return {
    receber: total("RECEITA", "PENDENTE"),
    pagar: total("DESPESA", "PENDENTE"),
    recebido: total("RECEITA", "PAGO"),
  };
}
