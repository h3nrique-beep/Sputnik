import { Button } from "../../../components/ui";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { X } from "lucide-react";
import { type OrderDocumentData } from "../../../types/order-document";
import { groups } from "./documentFields";

export function OrderDocumentFields({
  value,
  onChange,
}: {
  value: OrderDocumentData;
  onChange: (v: OrderDocumentData) => void;
}) {
  const [open, setOpen] = useState(false);
  const [step, setStep] = useState(0);
  const [draft, setDraft] = useState<OrderDocumentData>(value);
  const configured = Object.values(value).filter(
    (item) => item !== null && item !== "" && item !== undefined,
  ).length;

  useEffect(() => {
    if (!open) setDraft(value);
  }, [value, open]);

  // <dialog> nativo: foco preso, fundo inerte e retorno do foco ao fechar.
  const dialogo = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    if (open) dialogo.current?.showModal();
  }, [open]);

  const set = (key: string, next: string | number | null) =>
    setDraft((current) => ({ ...current, [key]: next }));
  const close = () => {
    // close() antes de desmontar devolve o foco ao botão que abriu.
    dialogo.current?.close();
    setDraft(value);
    setStep(0);
    setOpen(false);
  };
  const save = () => {
    dialogo.current?.close();
    onChange(draft);
    setStep(0);
    setOpen(false);
  };
  const openModal = () => {
    setDraft(value);
    setStep(0);
    setOpen(true);
  };

  return (
    <div className="os-document-control">
      <div>
        <strong>Dados da OS impressa</strong>
        <small>
          {configured
            ? `${configured} campo(s) complementar(es) preenchido(s)`
            : "Preencha pagamento, entrega, veículo, consultor e dados da oficina"}
        </small>
      </div>
      <Button variant="secondary" type="button" onClick={openModal}>
        {configured ? "Editar dados da OS" : "Preencher dados da OS"}
      </Button>

      {open &&
        createPortal(
          <dialog
            ref={dialogo}
            aria-labelledby="os-document-title"
            className="os-document-modal"
            // Esc e clique no fundo contam como "cancelar".
            onCancel={(event) => {
              event.preventDefault();
              close();
            }}
            onMouseDown={(event) => {
              if (event.target === event.currentTarget) close();
            }}
          >
            <header>
              <div>
                <h2 id="os-document-title">Dados do documento</h2>
                <span>Preencha só o que fará parte da OS impressa.</span>
              </div>
              <button
                aria-label="Fechar"
                className="modal-close"
                type="button"
                onClick={close}
              >
                <X size={18} aria-hidden="true" />
              </button>
            </header>
            <nav aria-label="Etapas do documento" className="os-document-steps">
              {groups.map((group, index) => (
                <button
                  aria-current={step === index ? "step" : undefined}
                  className={step === index ? "active" : ""}
                  key={group.title}
                  type="button"
                  onClick={() => setStep(index)}
                >
                  <em>{index + 1}</em>
                  {group.title}
                </button>
              ))}
            </nav>
            <div className="os-document-body">
              <label className="document-service-type">
                Tipo de serviço
                <select
                  value={draft.tipoServico ?? "EXTERNO"}
                  onChange={(e) => set("tipoServico", e.target.value)}
                >
                  <option value="EXTERNO">Externo</option>
                  <option value="INTERNO">Interno</option>
                  <option value="GARANTIA">Garantia</option>
                  <option value="REVISAO">Revisão</option>
                </select>
              </label>
              <fieldset>
                <legend>{groups[step].title}</legend>
                {groups[step].help && <p>{groups[step].help}</p>}
                <div className="os-document-grid">
                  {groups[step].fields.map((field) => (
                    <label key={field.key}>
                      {field.label}
                      <input
                        type={field.type ?? "text"}
                        min={field.min}
                        max={field.max}
                        maxLength={field.length}
                        value={draft[field.key] ?? ""}
                        onChange={(e) =>
                          set(
                            field.key,
                            e.target.value === ""
                              ? null
                              : field.type === "number"
                                ? Number(e.target.value)
                                : e.target.value,
                          )
                        }
                      />
                    </label>
                  ))}
                </div>
              </fieldset>
              {step === 0 && (
                <label className="document-observations">
                  Observações da OS
                  <textarea
                    maxLength={1200}
                    rows={3}
                    value={draft.observacoes ?? ""}
                    onChange={(e) => set("observacoes", e.target.value)}
                  />
                </label>
              )}
            </div>
            <footer>
              <button className="link" type="button" onClick={close}>
                Cancelar
              </button>
              <span>
                <Button
                  variant="secondary"
                  disabled={step === 0}
                  type="button"
                  onClick={() => setStep(step - 1)}
                >
                  Voltar
                </Button>
                {step < groups.length - 1 ? (
                  <Button type="button" onClick={() => setStep(step + 1)}>
                    Próximo
                  </Button>
                ) : (
                  <Button type="button" onClick={save}>
                    Salvar dados
                  </Button>
                )}
              </span>
            </footer>
          </dialog>,
          document.body,
        )}
    </div>
  );
}
