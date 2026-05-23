"use client";

import { BackButton } from "@/components/ui/BackButton";
import type { ReactNode } from "react";

const MAX_WIDTH = {
  md: "max-w-md",
  "3xl": "max-w-3xl",
  "5xl": "max-w-5xl",
} as const;

type PageMainProps = {
  children: ReactNode;
  className?: string;
  maxWidth?: keyof typeof MAX_WIDTH;
  /** Formulaires et cartes isolées : centrage horizontal et vertical */
  centered?: boolean;
  /** Affiche le bouton retour au-dessus du contenu, aligné à gauche */
  backFallback?: string;
};

export function PageMain({
  children,
  className = "",
  maxWidth = "md",
  centered = false,
  backFallback,
}: PageMainProps) {
  const width = MAX_WIDTH[maxWidth];

  const backNav = backFallback ? (
    <BackButton fallbackHref={backFallback} className="self-start" />
  ) : null;

  if (centered) {
    return (
      <main
        className={`flex w-full flex-1 flex-col items-center justify-center px-4 py-8 ${className}`}
      >
        <div className={`flex w-full ${width} flex-col items-start gap-4`}>
          {backNav}
          {children}
        </div>
      </main>
    );
  }

  return (
    <main
      className={`mx-auto flex w-full ${width} flex-1 flex-col items-start gap-4 px-4 py-8 ${className}`}
    >
      {backNav}
      {children}
    </main>
  );
}
