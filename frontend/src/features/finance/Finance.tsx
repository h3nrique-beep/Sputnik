import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import {
  Button,
  Card,
  Empty,
  Loading,
  Metric,
  Table,
} from "../../components/ui";
import { localDate, money, variacao } from "../../lib/format";
import { type FinanceEntry as Entry, type Order } from "../../types/domain";
import { FinanceEntries } from "./FinanceEntries";
import { FinanceForm } from "./FinanceForm";
import { rentabilidade, somar } from "./totais";

export function Finance({
  go,
  canOrders,
}: {
  go: (view: string) => void;
  canOrders: boolean;
}) {
  const hoje = localDate(new Date());
  const lancamentos = useQuery<Entry[]>({ queryKey: ["/financeiro"] });
  const ordens = useQuery<Order[]>({
    queryKey: ["/ordens"],
    enabled: canOrders,
  });
  const entries = lancamentos.data,
    orders = ordens.data ?? [];
  const [period, setPeriod] = useState<"all" | "month" | "next30">("month");
  const [erroEnvio, setErro] = useState("");
  const erro =
    erroEnvio ||
    lancamentos.error?.message ||
    (ordens.error
      ? "Não foi possível carregar as ordens para vínculo: " +
        ordens.error.message
      : "");
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
    () => rentabilidade(entriesInPeriod),
    [entriesInPeriod],
  );
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
          value={money(totals.receber)}
          detail={comparar("receber")}
        />
        <Metric
          label="A pagar"
          value={money(totals.pagar)}
          detail={comparar("pagar")}
        />
        <Metric
          label="Recebido"
          value={money(totals.recebido)}
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
        <FinanceForm orders={canOrders ? orders : undefined} failed={setErro} />
        <Card title="Como usar">
          <p>
            Registre receitas previstas e despesas da oficina. Ao receber ou
            pagar, use “Baixar” para marcar a movimentação no dia atual.
          </p>
        </Card>
      </div>
      <FinanceEntries entries={entriesInPeriod} failed={setErro} />
    </div>
  );
}
