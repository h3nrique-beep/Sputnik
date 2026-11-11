import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import {
  Badge,
  Button,
  Card,
  Empty,
  Loading,
  Metric,
} from "../../components/ui";
import { api, downloadApi } from "../../lib/api";
import { date, dateTime, money } from "../../lib/format";
import { type Order, type OrderDetail as Detail } from "../../types/domain";
import { OrderEdit } from "./OrderEdit";
import { OrderItems } from "./OrderItems";
import { OrderPrintDocument } from "./components/OrderPrintDocument";

export function OrderDetail({ id, back }: { id: number; back: () => void }) {
  const consulta = useQuery<Detail>({ queryKey: ["/ordens/" + id] });
  const data = consulta.data;
  const [erroEnvio, setErro] = useState(""),
    [baixandoPdf, setBaixandoPdf] = useState(false),
    [confirmando, setConfirmando] = useState(false),
    [aprovando, setAprovando] = useState(false),
    [aviso, setAviso] = useState("");
  const erro = erroEnvio || consulta.error?.message || "";
  // A gravação já invalida o cache; aqui só some o erro anterior.
  const reload = () => setErro("");
  if (!data)
    return erro ? (
      <div className="space-y-4">
        <div className="alert error" role="alert">
          {erro}
        </div>
        <Button variant="secondary" onClick={back}>
          Voltar para ordens
        </Button>
      </div>
    ) : (
      <Loading />
    );
  const o: Order = data.ordem;
  const eOrcamento = o.status === "ORCAMENTO";
  const pecas = data.itens.filter((item) => item.tipo === "PECA");
  const servicos = data.itens.filter((item) => item.tipo === "SERVICO");
  const totalPecas = pecas.reduce((sum, item) => sum + item.total, 0);
  const totalServicos = servicos.reduce((sum, item) => sum + item.total, 0);
  const baixarPdf = async () => {
    setBaixandoPdf(true);
    setErro("");
    try {
      await downloadApi(`/ordens/${id}/pdf`, `ordem-servico-${id}.pdf`);
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setBaixandoPdf(false);
    }
  };
  const unidades = pecas.reduce((sum, item) => sum + item.quantidade, 0);
  const aprovar = async () => {
    setErro("");
    setAprovando(true);
    try {
      await api(`/ordens/${id}/aprovar`, { method: "POST" });
      setConfirmando(false);
      setAviso(
        unidades
          ? `Orçamento aprovado. OS #${id} aberta e ${unidades} ${unidades === 1 ? "unidade baixada" : "unidades baixadas"} do estoque.`
          : `Orçamento aprovado. OS #${id} aberta.`,
      );
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setAprovando(false);
    }
  };
  return (
    <>
      <div className="toolbar">
        <div>
          <Button variant="link" size="sm" onClick={back}>
            ← Voltar
          </Button>
          <h2 className="order-title">
            {eOrcamento ? "Orçamento" : "OS"} #{o.id}{" "}
            <Badge status={o.status} />
          </h2>
        </div>
        <span className="toolbar-actions">
          <Button variant="secondary" onClick={() => window.print()}>
            Imprimir
          </Button>
          <Button
            variant="secondary"
            onClick={baixarPdf}
            disabled={baixandoPdf}
          >
            {baixandoPdf ? "Gerando PDF..." : "Baixar PDF"}
          </Button>
          {eOrcamento && (
            <Button onClick={() => setConfirmando(true)}>
              Aprovar e abrir OS
            </Button>
          )}
        </span>
      </div>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      {aviso && (
        <div className="alert success" role="status">
          {aviso}
        </div>
      )}
      {eOrcamento && confirmando && (
        <section className="approve-panel" aria-labelledby="aprovar-titulo">
          <h3 id="aprovar-titulo">Aprovar o orçamento #{o.id}?</h3>
          {pecas.length ? (
            <>
              <p>Estas peças saem do estoque assim que você confirmar:</p>
              <ul>
                {pecas.map((item) => (
                  <li key={item.id}>
                    <b>{item.quantidade}×</b> {item.descricao}
                  </li>
                ))}
              </ul>
            </>
          ) : (
            <p>Este orçamento não tem peças: nada sai do estoque.</p>
          )}
          <p className="muted">
            Se faltar saldo de alguma peça, a aprovação é recusada e nada é
            baixado.
          </p>
          <span className="row-actions">
            <Button onClick={aprovar} disabled={aprovando}>
              {aprovando ? "Aprovando..." : "Confirmar aprovação"}
            </Button>
            <Button
              variant="secondary"
              onClick={() => setConfirmando(false)}
              disabled={aprovando}
            >
              Cancelar
            </Button>
          </span>
        </section>
      )}
      <OrderPrintDocument data={data} />
      <div className="metrics">
        <Metric label="Cliente" value={o.cliente.nome} />
        <Metric label="Veículo" value={o.veiculo.placa} />
        <Metric label="KM" value={o.quilometragem} />
        {o.agendamento && (
          <Metric label="Agendamento" value={dateTime(o.agendamento)} />
        )}
        {o.retornoEm && <Metric label="Retorno" value={date(o.retornoEm)} />}
        {o.garantiaAte && (
          <Metric label="Garantia" value={date(o.garantiaAte)} />
        )}
      </div>
      <p className="order-description">{o.descricao}</p>
      <div className="order-workspace">
        <div className="order-workspace-main">
          <Card title="Itens e custos">
            {data.itens.length ? (
              data.itens.map((i) => (
                <p key={i.id}>
                  <span>
                    {i.quantidade}× {i.descricao}
                  </span>
                  <b>{money(i.total)}</b>
                </p>
              ))
            ) : (
              <Empty text="Nenhum item lançado. Use o formulário abaixo." />
            )}
            <p className="order-total">
              <span>Total</span>
              <b>{money(totalPecas + totalServicos)}</b>
            </p>
          </Card>
          <OrderItems orderId={id} saved={reload} failed={setErro} />
        </div>
        <div className="order-workspace-side">
          {!eOrcamento && (
            <OrderEdit order={o} saved={reload} failed={setErro} />
          )}
          <section className="order-history" aria-labelledby="historico-titulo">
            <h2 id="historico-titulo">
              Histórico <small>({data.historico.length})</small>
            </h2>
            <ol className="order-timeline">
              {data.historico.map((h, i) => (
                <li
                  key={i}
                  className={
                    h.descricao.startsWith("Status alterado")
                      ? "marco"
                      : undefined
                  }
                >
                  {h.descricao}
                  <small>
                    <time dateTime={h.data}>{dateTime(h.data)}</time> ·{" "}
                    {h.responsavel}
                  </small>
                </li>
              ))}
            </ol>
          </section>
        </div>
      </div>
    </>
  );
}
