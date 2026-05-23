"use client";

import { useRouter } from "@/i18n/navigation";
import { useTranslations } from "next-intl";
import { useEffect, useState } from "react";

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const t = useTranslations("common");
  const [ready, setReady] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetch("/api/bff/auth/session", { credentials: "include" })
      .then((res) => {
        if (cancelled) return;
        if (!res.ok) {
          router.replace("/auth/login");
          return;
        }
        setReady(true);
      })
      .catch(() => {
        if (!cancelled) router.replace("/auth/login");
      });
    return () => {
      cancelled = true;
    };
  }, [router]);

  if (!ready) {
    return (
      <main className="flex flex-1 items-center justify-center p-8">
        <p className="text-center text-primary/70">{t("loading")}</p>
      </main>
    );
  }

  return <>{children}</>;
}
