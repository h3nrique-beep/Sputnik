import { type FormEvent, useEffect, useState } from "react";
import { SearchSelect } from "../../components/ui";
import { api } from "../../lib/api";
import { type Client, type Vehicle } from "../../types/domain";
import { type OrderDocumentData } from "../../types/order-document";
import { OrderDocumentFields } from "./components/OrderDocumentFields";

export function OrderCreate({
  done,
  asQuote = false,
}: {
  done: () => void;
  asQuote?: boolean;
}) {
  const [documento, setDocumento] = useState<OrderDocumentData>({});
  const [clients, setClients] = useState<Client[]>([]),
    [vehicles, setVehicles] = useState<Vehicle[]>([]),
    [client, setClient] = useState<number>(),
    [vehicle, setVehicle] = useState<number>(),
    [km, setKm] = useState(""),
    [agendamento, setAgendamento] = useState(""),
    [retornoEm, setRetornoEm] = useState(""),
    [garantiaAte, setGarantiaAte] = useState(""),
    [descricao, setDescricao] = useState(""),
    [erro, setErro] = useState("");
  useEffect(() => {
    api<Client[]>("/clientes")
      .then(setClients)
      .catch((e: Error) => setErro(e.message));
    api<Vehicle[]>("/veiculos")
      .then(setVehicles)
      .catch((e: Error) => setErro(e.message));
  }, []);
  const filtered = vehicles.filter((v) => v.cliente?.id === client);
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api("/ordens", {
        method: "POST",
        body: JSON.stringify({
          clienteId: client,
          documento,
          veiculoId: vehicle,
          quilometragemAtual: Number(km),
          descricao,
          orcamento: asQuote,
          agendamento: agendamento || null,
          retornoEm: retornoEm || null,
          garantiaAte: garantiaAte || null,
        }),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card" onSubmit={submit}>
      <h2>{asQuote ? "Novo orçamento" : "Abrir atendimento"}</h2>
      {asQuote && (
        <p>
          Inclua os itens previstos. O estoque só será baixado quando o cliente
          aprovar.
        </p>
      )}
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <SearchSelect
        label="Cliente"
        required
        value={client ? String(client) : ""}
        onChange={(value) => {
          setClient(value ? Number(value) : undefined);
          setVehicle(undefined);
        }}
        searchPlaceholder="Nome, telefone ou CPF"
        options={clients.map((c) => ({
          value: String(c.id),
          label: `${c.nome} · ${c.telefone} · ${c.cpf}`,
        }))}
      />
      <SearchSelect
        label="Veículo vinculado"
        required
        disabled={!client}
        value={vehicle ? String(vehicle) : ""}
        onChange={(value) => setVehicle(value ? Number(value) : undefined)}
        placeholder={client ? "Selecione" : "Escolha o cliente primeiro"}
        searchPlaceholder="Placa ou modelo"
        options={filtered.map((v) => ({
          value: String(v.id),
          label: `${v.placa} · ${v.marcaModelo}`,
        }))}
      />
      <label>
        Quilometragem atual
        <input
          required
          type="number"
          min="0"
          value={km}
          onChange={(e) => setKm(e.target.value)}
        />
      </label>
      <label>
        Agendamento (opcional)
        <input
          type="datetime-local"
          value={agendamento}
          onChange={(e) => setAgendamento(e.target.value)}
        />
      </label>
      <label>
        Retorno preventivo
        <input
          type="date"
          value={retornoEm}
          onChange={(e) => setRetornoEm(e.target.value)}
        />
      </label>
      <label>
        Garantia até
        <input
          type="date"
          value={garantiaAte}
          onChange={(e) => setGarantiaAte(e.target.value)}
        />
      </label>
      <label>
        Descrição
        <textarea
          required
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
      </label>
      <OrderDocumentFields value={documento} onChange={setDocumento} />
      <button className="primary">
        {asQuote ? "Criar orçamento" : "Abrir ordem de serviço"}
      </button>
    </form>
  );
}
