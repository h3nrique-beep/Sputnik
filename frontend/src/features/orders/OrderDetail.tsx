import { useEffect, useState } from "react";
import {
  Badge,
  Button,
  Card,
  Empty,
  Loading,
  Metric,
} from "../../components/ui";
import { api, downloadApi } from "../../lib/api";
import { date, dateTime, label, money } from "../../lib/format";
import { type Order } from "../../types/domain";
import { OrderEdit } from "./OrderEdit";
import { OrderItems } from "./OrderItems";

export function OrderDetail({ id, back }: { id: number; back: () => void }) {
  const [data, setData] = useState<any>(),
    [erro, setErro] = useState(""),
    [baixandoPdf, setBaixandoPdf] = useState(false);
  const reload = () => {
    void api<any>("/ordens/" + id)
      .then((result) => {
        setData(result);
        setErro("");
      })
      .catch((e) => setErro(e.message));
  };
  useEffect(reload, [id]);
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
  const documento = o.documento ?? {};
  const valor = (item: string | number | null | undefined) =>
    item === null || item === undefined || item === "" ? "—" : String(item);
  const dataDocumento = (item: string | number | null | undefined) =>
    item ? dateTime(String(item)) : "—";
  const pecas = data.itens.filter((item: any) => item.tipo === "PECA");
  const servicos = data.itens.filter((item: any) => item.tipo === "SERVICO");
  const totalPecas = pecas.reduce(
    (sum: number, item: any) => sum + item.total,
    0,
  );
  const totalServicos = servicos.reduce(
    (sum: number, item: any) => sum + item.total,
    0,
  );
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
  const aprovar = async () => {
    if (!window.confirm("Aprovar este orçamento e baixar as peças do estoque?"))
      return;
    setErro("");
    try {
      await api(`/ordens/${id}/aprovar`, { method: "POST" });
      reload();
    } catch (e) {
      setErro((e as Error).message);
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
          {eOrcamento && <Button onClick={aprovar}>Aprovar e abrir OS</Button>}
        </span>
      </div>
      <section
        className="sputnik-os-print"
        aria-label="Ordem de serviço para impressão"
      >
        <header className="sputnik-os-head">
          <div className="sputnik-os-brand">
            <img src="/images/logo.jpg" alt="Sputnik" />
            <div>
              <strong>SPUTNIK</strong>
              <span>GESTÃO DE OFICINA</span>
              <small>
                {valor(documento.oficinaNome ?? "Sputnik - Gestão de Oficina")}
              </small>
            </div>
          </div>
          <div className="sputnik-os-number">
            <span>{eOrcamento ? "ORÇAMENTO" : "ORDEM DE SERVIÇO"}</span>
            <strong>Nº {o.id}</strong>
            <small>{label(o.status)}</small>
          </div>
        </header>
        <div className="sputnik-os-top-grid">
          <section className="os-box os-office">
            <h3>OFICINA</h3>
            <p>
              <b>
                {valor(documento.oficinaNome ?? "Sputnik - Gestão de Oficina")}
              </b>
            </p>
            <p>Endereço: {valor(documento.oficinaEndereco)}</p>
            <p>Contato: {valor(documento.oficinaContato)}</p>
            <p>CNPJ / CPF: {valor(documento.oficinaDocumento)}</p>
          </section>
          <section className="os-box os-order-data">
            <h3>DADOS DA ORDEM</h3>
            <p>
              Emissão: <b>{dataDocumento(o.abertura)}</b>
            </p>
            <p>Pagamento: {valor(documento.condicaoPagamento)}</p>
            <p>Validade: {valor(documento.validadeDias)} dia(s)</p>
            <p>Entrega prevista: {dataDocumento(documento.previsaoEntrega)}</p>
            <p>
              Orçamento previsto: {dataDocumento(documento.previsaoOrcamento)}
            </p>
          </section>
        </div>
        <div className="sputnik-os-main-grid">
          <div className="sputnik-os-left">
            <section className="os-box">
              <h3>CLIENTE PAGADOR</h3>
              <div className="os-lines">
                <p>
                  Nome: <b>{valor(documento.pagadorNome ?? o.cliente.nome)}</b>
                </p>
                <p>
                  CPF / CNPJ:{" "}
                  {valor(documento.pagadorDocumento ?? o.cliente.cpf)}
                </p>
                <p>
                  Telefone:{" "}
                  {valor(documento.pagadorTelefone ?? o.cliente.telefone)}
                </p>
                <p>E-mail: {valor(documento.pagadorEmail)}</p>
                <p>Endereço: {valor(documento.pagadorEndereco)}</p>
              </div>
            </section>
            <section className="os-box">
              <h3>PROPRIETÁRIO DO VEÍCULO</h3>
              <div className="os-lines">
                <p>
                  Nome: <b>{o.cliente.nome}</b>{" "}
                  <span>Código: {o.cliente.id}</span>
                </p>
                <p>CPF / CNPJ: {o.cliente.cpf}</p>
                <p>Telefone: {o.cliente.telefone}</p>
                <p>E-mail: {valor(documento.proprietarioEmail)}</p>
                <p>Endereço: {valor(documento.proprietarioEndereco)}</p>
              </div>
            </section>
            <section className="os-box os-services-box">
              <h3>ITENS / SERVIÇOS A EXECUTAR</h3>
              <p className="os-request">{o.descricao}</p>
              {servicos.map((item: any, index: number) => (
                <p className="os-service-row" key={item.id}>
                  {String(index + 1).padStart(3, "0")} · {item.descricao}
                  <b>{money(item.total)}</b>
                </p>
              ))}
            </section>
            <section className="os-box os-observations">
              <h3>OBSERVAÇÕES</h3>
              <p>{valor(documento.observacoes)}</p>
            </section>
          </div>
          <div className="sputnik-os-right">
            <section className="os-box">
              <h3>TIPO DE SERVIÇO</h3>
              {["EXTERNO", "INTERNO", "GARANTIA", "REVISAO"].map((tipo) => (
                <p className="os-check" key={tipo}>
                  <i>
                    {(documento.tipoServico ?? "EXTERNO") === tipo ? "✓" : ""}
                  </i>
                  {tipo === "REVISAO" ? "REVISÃO" : tipo}
                </p>
              ))}
            </section>
            <section className="os-box os-vehicle-box">
              <h3>DADOS DO VEÍCULO</h3>
              <p>
                Modelo: <b>{o.veiculo.marcaModelo}</b>
              </p>
              <p>
                Placa: <b>{o.veiculo.placa}</b>
              </p>
              <p>Chassi: {o.veiculo.chassi}</p>
              <p>Motor: {valor(documento.motor)}</p>
              <p>
                Ano / modelo: {o.veiculo.ano} / {valor(documento.anoModelo)}
              </p>
              <p>Cor: {valor(documento.cor)}</p>
              <p>
                KM de entrada: <b>{o.quilometragem}</b>
              </p>
              <p>Combustível: {valor(documento.combustivelPercentual)}%</p>
              <p>Prisma / cor: {valor(documento.prisma)}</p>
              <p>
                Certificado garantia: {valor(documento.certificadoGarantia)}
              </p>
              <p>
                Consultor técnico: <b>{valor(documento.consultorTecnico)}</b>
              </p>
            </section>
            <section className="os-box os-parts-box">
              <h3>REQUISIÇÃO DE PEÇAS</h3>
              {pecas.length
                ? pecas.map((item: any) => (
                    <p className="os-part-row" key={item.id}>
                      {item.quantidade} × {item.descricao}
                      <b>{money(item.total)}</b>
                    </p>
                  ))
                : Array.from({ length: 5 }, (_, index) => (
                    <p className="os-part-row empty" key={index}>
                      &nbsp;
                    </p>
                  ))}
            </section>
          </div>
        </div>
        <div className="sputnik-os-bottom-grid">
          <section className="os-box os-refund-box">
            <h3>REEMBOLSO / GARANTIA</h3>
            <p>{valor(documento.reembolso)}</p>
          </section>
          <section className="os-box os-expenses-box">
            <h3>DESPESAS</h3>
            <p>
              Serviços / mão de obra <b>{money(totalServicos)}</b>
            </p>
            <p>
              Peças <b>{money(totalPecas)}</b>
            </p>
            <p className="os-grand-total">
              TOTAL <b>{money(totalPecas + totalServicos)}</b>
            </p>
          </section>
        </div>
        <section className="os-signatures">
          <div>
            <p>Declaro ciência dos serviços e valores descritos nesta ordem.</p>
            <span>ASSINATURA DO CLIENTE</span>
          </div>
          <div>
            <p>Caixa: ____/____/________</p>
            <span>VISTO</span>
          </div>
          <div>
            <p>Retirada: {dataDocumento(documento.retiradaEm)}</p>
            <span>ASSINATURA DO CLIENTE</span>
          </div>
        </section>
        <footer className="os-receipt">
          <div>
            <b>COMPROVANTE DE RETIRADA — OS Nº {o.id}</b>
            <br />
            Apresente este comprovante para retirar seu veículo.
          </div>
          <div>
            Placa: <b>{o.veiculo.placa}</b>
            <br />
            Consultor: {valor(documento.consultorTecnico)}
          </div>
        </footer>
      </section>
      <div className="metrics">
        <Metric label="Cliente" value={o.cliente.nome as any} />
        <Metric label="Veículo" value={o.veiculo.placa as any} />
        <Metric label="KM" value={o.quilometragem} />
        {o.agendamento && (
          <Metric label="Agendamento" value={dateTime(o.agendamento) as any} />
        )}
        {o.retornoEm && (
          <Metric label="Retorno" value={date(o.retornoEm) as any} />
        )}
        {o.garantiaAte && (
          <Metric label="Garantia" value={date(o.garantiaAte) as any} />
        )}
      </div>
      <p className="order-description">{o.descricao}</p>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <div className="order-workspace">
        <div className="order-workspace-main">
          <Card title="Itens e custos">
            {data.itens.length ? (
              data.itens.map((i: any) => (
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
          <details className="order-history">
            <summary>Histórico ({data.historico.length})</summary>
            {data.historico.map((h: any, i: number) => (
              <p key={i}>
                <span>
                  {h.descricao}
                  <small>
                    {dateTime(h.data)} · {h.responsavel}
                  </small>
                </span>
              </p>
            ))}
          </details>
        </div>
      </div>
    </>
  );
}
