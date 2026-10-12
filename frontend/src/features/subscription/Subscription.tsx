import { useEffect, useState } from "react";
import { Card, Loading, Metric } from "../../components/ui";
import { api } from "../../lib/api";
import { label } from "../../lib/format";

type Subscription = {
  nome: string;
  slug: string;
  plano: string;
  status: string;
  periodoTesteAte: string;
  limiteUsuarios: number;
  usuariosAtivos: number;
};
export function Subscription() {
  const [data, setData] = useState<Subscription>();
  const [erro, setErro] = useState("");
  useEffect(() => {
    api<Subscription>("/empresa/assinatura")
      .then(setData)
      .catch((e: Error) => setErro(e.message));
  }, []);
  if (!data)
    return erro ? (
      <div className="alert error" role="alert">
        {erro}
      </div>
    ) : (
      <Loading />
    );
  return (
    <div className="module-shell space-y-5">
      <div className="metrics">
        <Metric label="Plano" value={label(data.plano) as any} />
        <Metric label="Status" value={label(data.status) as any} />
        <Metric
          label="Usuários"
          value={`${data.usuariosAtivos} / ${data.limiteUsuarios}` as any}
        />
      </div>
      <Card title={data.nome}>
        <p>
          <b>Endereço:</b> {data.slug}.sputnik
        </p>
        <p>
          <b>Teste até:</b>{" "}
          {data.periodoTesteAte
            ? new Date(data.periodoTesteAte + "T12:00:00").toLocaleDateString(
                "pt-BR",
              )
            : "—"}
        </p>
        <p>
          Para ampliar usuários ou trocar de plano, conecte este módulo ao seu
          provedor de cobrança (Stripe, Mercado Pago ou Asaas) antes de liberar
          alterações automáticas.
        </p>
      </Card>
    </div>
  );
}
