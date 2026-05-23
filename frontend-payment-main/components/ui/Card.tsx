import type { ReactNode } from "react";

export function Card({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={`rounded-xl border border-primary/10 bg-surface-muted p-6 shadow-sm ${className}`}
    >
      {children}
    </div>
  );
}
