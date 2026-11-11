import { dateTime, label, money } from "../../../lib/format";
import { type OrderDetail as Detail } from "../../../types/domain";

const valor = (item: string | number | null | undefined) =>
  item === null || item === undefined || item === "" ? "—" : String(item);
const dataDocumento = (item: string | number | null | undefined) =>
  item ? dateTime(String(item)) : "—";

/** Via A4 da OS: só aparece na impressão (layout-print.css). */
export function OrderPrintDocument({ data }: { data: Detail }) {
  const o = data.ordem;
  const eOrcamento = o.status === "ORCAMENTO";
  const documento = o.documento ?? {};
  const pecas = data.itens.filter((item) => item.tipo === "PECA");
  const servicos = data.itens.filter((item) => item.tipo === "SERVICO");
  const totalPecas = pecas.reduce((sum, item) => sum + item.total, 0);
  const totalServicos = servicos.reduce((sum, item) => sum + item.total, 0);
  return (
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
                CPF / CNPJ: {valor(documento.pagadorDocumento ?? o.cliente.cpf)}
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
            {servicos.map((item, index) => (
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
            <p>Certificado garantia: {valor(documento.certificadoGarantia)}</p>
            <p>
              Consultor técnico: <b>{valor(documento.consultorTecnico)}</b>
            </p>
          </section>
          <section className="os-box os-parts-box">
            <h3>REQUISIÇÃO DE PEÇAS</h3>
            {pecas.length
              ? pecas.map((item) => (
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
  );
}
