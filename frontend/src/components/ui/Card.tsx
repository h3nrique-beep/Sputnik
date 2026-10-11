import { type ReactNode } from "react";
import { CardContent, CardHeader, CardTitle, ShadcnCard } from "../shadcn/card";

export function Card({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <ShadcnCard className="card">
      <CardHeader className="card-heading p-0 pb-3">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="p-0">{children}</CardContent>
    </ShadcnCard>
  );
}
