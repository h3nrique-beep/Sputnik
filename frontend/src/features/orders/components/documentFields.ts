type Field = {
  key: string;
  label: string;
  type?: string;
  max?: number;
  min?: number;
  length?: number;
};
export const groups: { title: string; help?: string; fields: Field[] }[] = [
  {
    title: "Atendimento e entrega",
    fields: [
      { key: "condicaoPagamento", label: "Condição de pagamento", length: 120 },
      {
        key: "validadeDias",
        label: "Validade do orçamento (dias)",
        type: "number",
        min: 0,
        max: 365,
      },
      {
        key: "previsaoEntrega",
        label: "Previsão de entrega",
        type: "datetime-local",
      },
      {
        key: "previsaoOrcamento",
        label: "Previsão do orçamento",
        type: "datetime-local",
      },
      {
        key: "consultorTecnico",
        label: "Consultor técnico / responsável",
        length: 120,
      },
      {
        key: "prisma",
        label: "Prisma / identificação da chave e cor",
        length: 40,
      },
      {
        key: "retiradaEm",
        label: "Retirada do veículo",
        type: "datetime-local",
      },
    ],
  },
  {
    title: "Proprietário e pagador",
    help: "Sem outro pagador informado, será usado o cliente da OS. Estes complementos ficam salvos nesta ordem, sem alterar o cadastro original.",
    fields: [
      {
        key: "proprietarioEndereco",
        label: "Endereço do proprietário (com cidade, UF e CEP)",
        length: 250,
      },
      {
        key: "proprietarioEmail",
        label: "E-mail do proprietário",
        type: "email",
        length: 160,
      },
      {
        key: "pagadorNome",
        label: "Outro pagador: nome / razão social",
        length: 160,
      },
      { key: "pagadorDocumento", label: "CPF / CNPJ do pagador", length: 30 },
      { key: "pagadorTelefone", label: "Telefone do pagador", length: 80 },
      {
        key: "pagadorEmail",
        label: "E-mail do pagador",
        type: "email",
        length: 160,
      },
      {
        key: "pagadorEndereco",
        label: "Endereço do pagador (com cidade, UF e CEP)",
        length: 250,
      },
    ],
  },
  {
    title: "Veículo e garantia",
    fields: [
      { key: "motor", label: "Número / identificação do motor", length: 80 },
      { key: "cor", label: "Cor do veículo", length: 80 },
      {
        key: "anoModelo",
        label: "Ano-modelo",
        type: "number",
        min: 1900,
        max: 2200,
      },
      {
        key: "combustivelPercentual",
        label: "Combustível na entrada (%)",
        type: "number",
        min: 0,
        max: 100,
      },
      {
        key: "certificadoGarantia",
        label: "Certificado de garantia",
        length: 80,
      },
      {
        key: "reembolso",
        label: "Responsável / informações de reembolso",
        length: 250,
      },
    ],
  },
  {
    title: "Identificação da oficina no documento",
    help: "Preencha os dados reais da oficina. Se o nome ficar vazio, o documento usará Sputnik.",
    fields: [
      { key: "oficinaNome", label: "Nome / razão social", length: 160 },
      { key: "oficinaDocumento", label: "CNPJ / CPF", length: 30 },
      { key: "oficinaEndereco", label: "Endereço completo", length: 250 },
      { key: "oficinaContato", label: "Telefone / contato", length: 80 },
    ],
  },
];
