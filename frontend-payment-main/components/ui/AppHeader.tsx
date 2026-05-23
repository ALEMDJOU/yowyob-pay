"use client";

import { LanguageSwitcher } from "@/components/LanguageSwitcher";
import type { ReactNode } from "react";

export function AppHeader({
  title,
  actions,
}: {
  title: string;
  actions?: ReactNode;
}) {
  return (
    <header className="shrink-0 bg-primary text-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-4">
        <h1 className="min-w-0 truncate text-lg font-bold tracking-tight">{title}</h1>
        <div className="flex shrink-0 items-center gap-3">
          {actions}
          <LanguageSwitcher />
        </div>
      </div>
    </header>
  );
}
