import { useEffect, useState } from "react";
import { filtrarOpcoes, normalizar, type SearchOption } from "../../lib/search";

/**
 * Lista nativa com um campo de busca acima: filtra por qualquer parte do texto
 * da opção (código, nome, placa, telefone) e seleciona sozinha quando sobra um.
 */
export function SearchSelect({
  label,
  value,
  onChange,
  options,
  searchPlaceholder,
  placeholder = "Selecione",
  required,
  disabled,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: SearchOption[];
  searchPlaceholder: string;
  placeholder?: string;
  required?: boolean;
  disabled?: boolean;
}) {
  const [busca, setBusca] = useState("");
  const termo = normalizar(busca.trim());
  const visiveis = filtrarOpcoes(options, busca, value);
  const encontrados = visiveis.filter((option) => option.value !== value);

  useEffect(() => {
    const unico = termo ? filtrarOpcoes(options, busca) : [];
    if (unico.length === 1 && unico[0].value !== value)
      onChange(unico[0].value);
  }, [termo]);

  return (
    <label className="search-select">
      {label}
      <input
        type="search"
        value={busca}
        disabled={disabled}
        placeholder={searchPlaceholder}
        aria-label={`Buscar ${label.toLowerCase()}`}
        onChange={(e) => setBusca(e.target.value)}
      />
      <select
        required={required}
        disabled={disabled}
        value={value}
        aria-label={label}
        onChange={(e) => onChange(e.target.value)}
      >
        <option value="">
          {termo && !encontrados.length && !value
            ? "Nenhum resultado para a busca"
            : placeholder}
        </option>
        {visiveis.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {termo && (
        <small className="search-select-count" aria-live="polite">
          {visiveis.length} de {options.length}
        </small>
      )}
    </label>
  );
}
