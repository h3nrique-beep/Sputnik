export const money = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    value || 0,
  );

export const dateTime = (value: string) =>
  new Date(value).toLocaleString("pt-BR");
