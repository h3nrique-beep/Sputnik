export type User = {
  id: number;
  empresaId?: number | null;
  empresaNome?: string | null;
  nome: string;
  perfil: string;
  permissoes: string[];
  trocaSenhaObrigatoria: boolean;
};

export type Piece = {
  id: number;
  codigo: string;
  nome: string;
  categoria: string;
  preco: number;
  saldo: number;
  disponivel: number;
  minimo: number;
  locacao: string;
};

export type Client = {
  id: number;
  nome: string;
  cpf: string;
  telefone: string;
};

export type Vehicle = {
  id: number;
  placa: string;
  marcaModelo: string;
  ano: number;
  chassi: string;
  quilometragemAtual: number;
  cliente?: Client;
};

export type Order = {
  documento?: import("./order-document").OrderDocumentData | null;
  id: number;
  status: string;
  abertura: string;
  agendamento?: string | null;
  retornoEm?: string | null;
  garantiaAte?: string | null;
  quilometragem: number;
  descricao: string;
  cliente: Client;
  veiculo: Vehicle;
};

export type BackupEntry = {
  nome: string;
  tamanhoBytes: number;
  criadoEm: number;
};

export type DeveloperDiagnostics = {
  versao: string;
  java: string;
  sistemaOperacional: string;
  memoriaUsadaMb: number;
  memoriaMaximaMb: number;
  discoTotalMb: number;
  discoLivreMb: number;
  bancoMb: number;
  backups: number;
};
