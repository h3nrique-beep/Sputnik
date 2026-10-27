import { type components } from "./api";

type Schemas = components["schemas"];

/** Contratos da API gerados de openapi.json (npm run api:types); não edite api.ts à mão. */
export type User = Schemas["UsuarioDto"];
export type Piece = Schemas["PecaDto"];
export type Client = Schemas["ClienteDto"];
export type Vehicle = Schemas["VeiculoDto"];
export type Order = Schemas["OrdemResumoDto"];
export type OrderDetail = Schemas["OrdemDetalheDto"];
export type OrderItem = Schemas["ItemDto"];
export type FinanceEntry = Schemas["LancamentoDto"];
export type Restock = Schemas["ReposicaoResumoDto"];
export type DashboardData = Schemas["DashboardDto"];
export type Movement = Schemas["MovimentacaoDto"];
export type SystemUser = Schemas["UsuarioSistemaDto"];

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
