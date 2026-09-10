import { useEffect, useState } from "react";
import { ClipboardPlus, PackagePlus, Search, Warehouse } from "lucide-react";
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, Tooltip, XAxis, YAxis } from "recharts";
import { Button, Card, Empty, Loading, Metric } from "../../components/ui";
import { CardContent, CardHeader, CardTitle, ShadcnCard } from "../../components/shadcn/card";
import { ChartContainer, type ChartConfig } from "../../components/shadcn/chart";
import { api } from "../../lib/api";
import { type Order, type Piece } from "../../types/domain";

export function Dashboard({ go }: { go: (v: string) => void }) {
  const [data, setData] = useState<any>();
  useEffect(() => {
    api<any>("/dashboard").then(setData);
  }, []);
  if (!data) return <Loading />;
  const statusData = Object.entries(data.porStatus).map(([status, total]) => ({
    status: status.replaceAll("_", " "),
    total: Number(total),
  }));
  const statusConfig = Object.fromEntries(
    statusData.map((item, index) => [
      item.status,
      { label: item.status, color: ["#2563eb", "#f59e0b", "#10b981", "#ef4444", "#7c3aed"][index % 5] },
    ]),
  ) satisfies ChartConfig;
  const pieceData = data.pecasMaisUsadas.slice(0, 5).map((piece: Piece) => ({
    nome: piece.nome,
    disponivel: piece.disponivel,
  }));
  const pieceConfig = { disponivel: { label: "Disponível", color: "#2563eb" } } satisfies ChartConfig;
  return (
    <>
      <div className="restock-banner">
        <span>!</span>
        <div>
          <b>{data.criticas} itens exigem reposição</b>
          <small>Estão abaixo do estoque mínimo configurado.</small>
        </div>
        <Button size="sm" variant="secondary" onClick={() => go("reposition")}>
          Ver reposição
        </Button>
      </div>
      <div className="metrics">
        <Metric
          icon="▣"
          label="Peças cadastradas"
          value={data.pecas}
          detail="SKUs ativos no catálogo"
        />
        <Metric
          icon="↗"
          label="Ordens abertas"
          value={data.ordensAbertas}
          detail="Serviços em andamento"
        />
        <Metric
          icon="✓"
          label="Concluídas no mês"
          value={data.ordensConcluidasMes}
          detail="Atendimentos finalizados"
        />
        <Metric
          icon="!"
          label="Estoque crítico"
          value={data.criticas}
          detail="Itens abaixo do mínimo"
        />
      </div>
      <section className="mb-6 grid gap-4 lg:grid-cols-5">
        <ShadcnCard className="lg:col-span-3">
          <CardHeader>
            <CardTitle>Ordens por status</CardTitle>
            <p className="text-sm text-slate-500">Visão rápida da fila de atendimento atual.</p>
          </CardHeader>
          <CardContent>
            <ChartContainer config={statusConfig} className="min-h-[260px]">
              <BarChart accessibilityLayer data={statusData} margin={{ top: 8, right: 8, left: -22, bottom: 8 }}>
                <CartesianGrid vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="status" tickLine={false} axisLine={false} tickMargin={10} tick={{ fontSize: 10 }} />
                <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
                <Tooltip cursor={{ fill: "#f8fafc" }} formatter={(value) => [value, "Ordens"]} />
                <Bar dataKey="total" radius={[6, 6, 0, 0]}>
                  {statusData.map((item) => <Cell key={item.status} fill={statusConfig[item.status].color} />)}
                </Bar>
              </BarChart>
            </ChartContainer>
          </CardContent>
        </ShadcnCard>
        <ShadcnCard className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Distribuição da operação</CardTitle>
            <p className="text-sm text-slate-500">Cada faixa representa um status de OS.</p>
          </CardHeader>
          <CardContent>
            <ChartContainer config={statusConfig} className="min-h-[260px]">
              <PieChart>
                <Tooltip formatter={(value, _, item) => [value, item.payload.status]} />
                <Legend verticalAlign="bottom" height={34} wrapperStyle={{ fontSize: 10 }} />
                <Pie data={statusData} dataKey="total" nameKey="status" cx="50%" cy="45%" innerRadius={48} outerRadius={78} paddingAngle={3}>
                  {statusData.map((item) => <Cell key={item.status} fill={statusConfig[item.status].color} />)}
                </Pie>
              </PieChart>
            </ChartContainer>
          </CardContent>
        </ShadcnCard>
      </section>
      <section className="mb-6 grid gap-4 lg:grid-cols-5">
        <ShadcnCard className="lg:col-span-3">
          <CardHeader><CardTitle>Estoque das peças mais utilizadas</CardTitle><p className="text-sm text-slate-500">Quantidade disponível dos cinco itens com maior uso.</p></CardHeader>
          <CardContent>
            <ChartContainer config={pieceConfig} className="min-h-[230px]">
              <BarChart accessibilityLayer data={pieceData} layout="vertical" margin={{ top: 0, right: 8, left: 18, bottom: 0 }}>
                <CartesianGrid horizontal={false} stroke="#e2e8f0" />
                <XAxis type="number" allowDecimals={false} tickLine={false} axisLine={false} />
                <YAxis type="category" dataKey="nome" width={105} tickLine={false} axisLine={false} tick={{ fontSize: 10 }} />
                <Tooltip cursor={{ fill: "#f8fafc" }} formatter={(value) => [value, "Disponível"]} />
                <Bar dataKey="disponivel" fill="var(--color-disponivel)" radius={[0, 6, 6, 0]} />
              </BarChart>
            </ChartContainer>
          </CardContent>
        </ShadcnCard>
        <ShadcnCard className="lg:col-span-2">
          <CardHeader><CardTitle>Ações rápidas</CardTitle><p className="text-sm text-slate-500">Atalhos para as rotinas mais frequentes.</p></CardHeader>
          <CardContent className="grid gap-2">
            <Button variant="outline" className="justify-start" onClick={() => go("pieces-new")}><PackagePlus size={16} />Cadastrar peça</Button>
            <Button variant="outline" className="justify-start" onClick={() => go("pieces")}><Search size={16} />Consultar estoque</Button>
            <Button variant="outline" className="justify-start" onClick={() => go("movement")}><Warehouse size={16} />Movimentar estoque</Button>
            <Button className="justify-start" onClick={() => go("orders-new")}><ClipboardPlus size={16} />Abrir nova ordem</Button>
          </CardContent>
        </ShadcnCard>
      </section>
      <div className="cards dashboard-cards">
        <Card title="Ordens por status">
          {Object.entries(data.porStatus).map(([s, n]) => (
            <p key={s}>
              <span>{s.replace("_", " ")}</span>
              <b>{String(n)}</b>
            </p>
          ))}
        </Card>
        <Card title="Alertas">
          {data.aguardandoPeca.length || data.retornos.length ? (
            [...data.aguardandoPeca, ...data.retornos].map((o: Order) => (
              <p key={o.id}>
                <span>
                  OS #{o.id} · {o.cliente.nome}
                </span>
                <b>{o.status}</b>
              </p>
            ))
          ) : (
            <Empty text="Nenhum alerta no momento." />
          )}
        </Card>
        <Card title="Peças mais utilizadas">
          {data.pecasMaisUsadas.length ? (
            data.pecasMaisUsadas.map((p: Piece) => (
              <p key={p.id}>
                <span>{p.nome}</span>
                <b>{p.disponivel} disp.</b>
              </p>
            ))
          ) : (
            <Empty text="Nenhuma peça utilizada ainda." />
          )}
        </Card>
      </div>
    </>
  );
}
