import {
  Ban,
  CircleCheck,
  CircleDot,
  Clock,
  FileText,
  PackageSearch,
  ShieldCheck,
  User,
  Wrench,
  type LucideIcon,
} from "lucide-react";
import { label } from "../../lib/format";
import { ShadcnBadge, type ShadcnBadgeProps } from "../shadcn/badge";

type Variant = ShadcnBadgeProps["variant"];

// Cada status tem ícone próprio: a cor nunca é o único sinal.
const ESTILOS: Record<string, [Variant, LucideIcon]> = {
  ORCAMENTO: ["warning", FileText],
  ABERTA: ["outline", CircleDot],
  EM_ANDAMENTO: ["default", Wrench],
  AGUARDANDO_PECA: ["warning", PackageSearch],
  CONCLUIDA: ["success", CircleCheck],
  CANCELADA: ["destructive", Ban],
  PENDENTE: ["warning", Clock],
  PAGO: ["success", CircleCheck],
  ATIVA: ["success", CircleCheck],
  EM_TESTE: ["warning", Clock],
  SUSPENSA: ["destructive", Ban],
  ADMIN: ["default", ShieldCheck],
  BALCONISTA: ["outline", User],
};

export function Badge({ status }: { status: string }) {
  const code = status.toUpperCase();
  const [variant, Icon] = ESTILOS[code] ?? ["outline", CircleDot];
  return (
    <ShadcnBadge variant={variant} className="gap-1">
      <Icon size={12} aria-hidden="true" />
      {label(code)}
    </ShadcnBadge>
  );
}
