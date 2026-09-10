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
      <CardHeader className="p-5 pb-3">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </ShadcnCard>
  );
}
