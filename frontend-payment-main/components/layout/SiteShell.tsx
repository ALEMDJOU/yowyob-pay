import { AppFooter } from "@/components/ui/AppFooter";
import type { ReactNode } from "react";

export function SiteShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-dvh flex-col">
      <div className="flex min-h-0 flex-1 flex-col">{children}</div>
      <AppFooter />
    </div>
  );
}
