import { TriangleAlert } from "lucide-react";
import { useEffect, useRef, useSyncExternalStore } from "react";
import { Button } from "../shadcn/button";

type Pedido = {
  titulo: string;
  mensagem: string;
  acao: string;
  perigo: boolean;
  responder: (ok: boolean) => void;
};

let atual: Pedido | null = null;
const ouvintes = new Set<() => void>();
const avisar = () => ouvintes.forEach((ouvinte) => ouvinte());

/** Substitui o confirm() do navegador: `if (!(await confirmar(...))) return;` */
export function confirmar(
  titulo: string,
  mensagem: string,
  { acao = "Confirmar", perigo = false } = {},
) {
  // Um novo pedido cancela o anterior em vez de empilhar diálogos.
  atual?.responder(false);
  return new Promise<boolean>((resolve) => {
    atual = {
      titulo,
      mensagem,
      acao,
      perigo,
      responder: (ok) => {
        atual = null;
        avisar();
        resolve(ok);
      },
    };
    avisar();
  });
}

/** Montado uma vez na raiz do app. */
export function ConfirmDialog() {
  const pedido = useSyncExternalStore(
    (ouvinte) => {
      ouvintes.add(ouvinte);
      return () => ouvintes.delete(ouvinte);
    },
    () => atual,
  );
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialogo = ref.current;
    if (pedido && !dialogo?.open) dialogo?.showModal();
    if (!pedido && dialogo?.open) dialogo.close();
  }, [pedido]);
  return (
    <dialog
      ref={ref}
      className="confirm-dialog"
      aria-labelledby="confirm-titulo"
      aria-describedby="confirm-mensagem"
      // Esc e clique fora contam como "cancelar".
      onCancel={() => pedido?.responder(false)}
      onClick={(event) => {
        if (event.target === event.currentTarget) pedido?.responder(false);
      }}
    >
      {pedido && (
        <div className="confirm-body">
          <span
            className={`confirm-icon${pedido.perigo ? " perigo" : ""}`}
            aria-hidden="true"
          >
            <TriangleAlert size={20} />
          </span>
          <div>
            <h2 id="confirm-titulo">{pedido.titulo}</h2>
            <p id="confirm-mensagem">{pedido.mensagem}</p>
          </div>
          <div className="confirm-actions">
            <Button
              variant="secondary"
              onClick={() => pedido.responder(false)}
              autoFocus
            >
              Cancelar
            </Button>
            <Button
              variant={pedido.perigo ? "destructive" : "default"}
              onClick={() => pedido.responder(true)}
            >
              {pedido.acao}
            </Button>
          </div>
        </div>
      )}
    </dialog>
  );
}
