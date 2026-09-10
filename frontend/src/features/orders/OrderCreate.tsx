import { type FormEvent, useEffect, useState } from "react";
import { api } from "../../lib/api";
import { type Client, type Vehicle } from "../../types/domain";
import { type OrderDocumentData } from "../../types/order-document";
import { OrderDocumentFields } from "./components/OrderDocumentFields";

export function OrderCreate({ done }: { done: () => void }) {
  const [documento, setDocumento] = useState<OrderDocumentData>({});
  const [clients, setClients] = useState<Client[]>([]),
    [vehicles, setVehicles] = useState<Vehicle[]>([]),
    [client, setClient] = useState<number>(),
    [vehicle, setVehicle] = useState<number>(),
    [km, setKm] = useState(""),
    [descricao, setDescricao] = useState(""),
    [erro, setErro] = useState("");
  useEffect(() => {
    api<Client[]>("/clientes").then(setClients);
    api<Vehicle[]>("/veiculos").then(setVehicles);
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
        }),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card" onSubmit={submit}>
      <h2>Abrir atendimento</h2>
      {erro && <div className="alert error">{erro}</div>}
      <label>
        Cliente
        <select
          required
          value={client || ""}
          onChange={(e) => {
            setClient(Number(e.target.value));
            setVehicle(undefined);
          }}
        >
          <option value="">Selecione</option>
          {clients.map((c) => (
            <option value={c.id} key={c.id}>
              {c.nome} · {c.telefone}
            </option>
          ))}
        </select>
      </label>
      <label>
        Veículo vinculado
        <select
          required
          disabled={!client}
          value={vehicle || ""}
          onChange={(e) => setVehicle(Number(e.target.value))}
        >
          <option value="">Selecione</option>
          {filtered.map((v) => (
            <option value={v.id} key={v.id}>
              {v.placa} · {v.marcaModelo}
            </option>
          ))}
        </select>
      </label>
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
        Descrição
        <textarea
          required
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
      </label>
      <OrderDocumentFields value={documento} onChange={setDocumento} />
      <button className="primary">Abrir ordem de serviço</button>
    </form>
  );
}
