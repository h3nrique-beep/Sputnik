import { useQuery } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { api } from "../../lib/api";
import { type Client } from "../../types/domain";

export function VehicleCreate({ done }: { done: () => void }) {
  const lista = useQuery<Client[]>({ queryKey: ["/clientes"] });
  const clients = lista.data ?? [];
  const [form, setForm] = useState<any>({}),
    [erroEnvio, setErro] = useState("");
  const erro = erroEnvio || lista.error?.message || "";
  const set = (key: string, value: any) => setForm({ ...form, [key]: value });
  const submit = async (e: FormEvent) => {
    e.preventDefault();
    try {
      await api("/veiculos", {
        method: "POST",
        body: JSON.stringify({
          ...form,
          ano: Number(form.ano),
          quilometragemAtual: Number(form.quilometragemAtual),
          clienteId: Number(form.clienteId),
        }),
      });
      done();
    } catch (e) {
      setErro((e as Error).message);
    }
  };
  return (
    <form className="form-card grid-form" onSubmit={submit}>
      <h2>Dados do veículo</h2>
      <p>O proprietário precisa estar cadastrado antes do vínculo.</p>
      {erro && (
        <div className="alert error" role="alert">
          {erro}
        </div>
      )}
      <label>
        Cliente
        <select
          required
          value={form.clienteId || ""}
          onChange={(e) => set("clienteId", e.target.value)}
        >
          <option value="">Selecione</option>
          {clients.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome} · {c.cpf}
            </option>
          ))}
        </select>
      </label>
      <label>
        Modelo
        <input
          required
          value={form.marcaModelo || ""}
          onChange={(e) => set("marcaModelo", e.target.value)}
        />
      </label>
      <label>
        Placa
        <input
          required
          value={form.placa || ""}
          onChange={(e) => set("placa", e.target.value.toUpperCase())}
        />
      </label>
      <label>
        Chassi
        <input
          required
          value={form.chassi || ""}
          onChange={(e) => set("chassi", e.target.value.toUpperCase())}
        />
      </label>
      <label>
        Ano
        <input
          required
          type="number"
          min="1900"
          value={form.ano || ""}
          onChange={(e) => set("ano", e.target.value)}
        />
      </label>
      <label>
        Quilometragem atual
        <input
          required
          type="number"
          min="0"
          value={form.quilometragemAtual || ""}
          onChange={(e) => set("quilometragemAtual", e.target.value)}
        />
      </label>
      <button className="primary">Cadastrar veículo</button>
    </form>
  );
}
