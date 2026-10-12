export type SearchOption = { value: string; label: string };

/** Minúsculas e sem acentos: "Óleo" e "oleo" encontram a mesma peça. */
export const normalizar = (texto: string) =>
  texto
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();

/** Opções cujo texto contém a busca; a opção já escolhida nunca some da lista. */
export const filtrarOpcoes = (
  options: SearchOption[],
  busca: string,
  selecionado = "",
) => {
  const termo = normalizar(busca.trim());
  return termo
    ? options.filter(
        (option) =>
          option.value === selecionado ||
          normalizar(option.label).includes(termo),
      )
    : options;
};
