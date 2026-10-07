export const money = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    value || 0,
  );

export const dateTime = (value: string) =>
  new Date(value).toLocaleString("pt-BR");

/** Data civil local, sem a conversão UTC que pode antecipar o vencimento. */
export const localDate = (date: Date) =>
  [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0"),
  ].join("-");
