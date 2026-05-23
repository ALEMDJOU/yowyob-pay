"use client";

import { PageMain } from "@/components/layout/PageMain";
import { LogoutButton } from "@/components/LogoutButton";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Link } from "@/i18n/navigation";
import { BffError, bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { PagedWallets } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useEffect, useState } from "react";

export default function WalletsPage() {
  const t = useTranslations("wallets");
  const tc = useTranslations("common");
  const te = useTranslations("errors");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PagedWallets | null>(null);
  const [error, setError] = useState<string | null>(null);
  const size = 10;

  useEffect(() => {
    let cancelled = false;
    bffJson<PagedWallets>(`/api/bff/payment/wallets?page=${page}&size=${size}`)
      .then((d) => {
        if (!cancelled) {
          setData(d);
          setError(null);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(
            e instanceof BffError
              ? resolveApiError(e.status, e.body, te)
              : te("network"),
          );
        }
      });
    return () => {
      cancelled = true;
    };
  }, [page, te]);

  return (
    <>
      <AppHeader title={t("title")} actions={<LogoutButton />} />
      <PageMain maxWidth="5xl" backFallback="/dashboard">
        <Link href="/wallets/new">
          <Button>{t("new")}</Button>
        </Link>
        {error ? <p className="text-red-600">{error}</p> : null}
        {!data && !error ? (
          <p className="text-muted">{tc("loading")}</p>
        ) : !data || data.content.length === 0 ? (
          <p className="text-muted">{t("empty")}</p>
        ) : (
          <ul className="space-y-3">
            {data.content.map((w) => (
              <li key={w.id}>
                <Card className="flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <p className="font-semibold text-primary">{w.ownerName}</p>
                    <p className="text-sm text-muted">
                      {t("balance")}: {w.balance}
                    </p>
                  </div>
                  <Link href={`/wallets/${w.id}`} className="text-sm font-medium text-primary underline">
                    {t("detail")}
                  </Link>
                </Card>
              </li>
            ))}
          </ul>
        )}
        {data && data.totalPages > 1 ? (
          <div className="flex items-center justify-center gap-4 pt-4">
            <Button
              variant="secondary"
              disabled={page <= 0}
              onClick={() => setPage((p) => p - 1)}
            >
              {t("prev")}
            </Button>
            <span className="text-sm text-muted">
              {t("page", { page: page + 1, total: data.totalPages })}
            </span>
            <Button
              variant="secondary"
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              {t("next")}
            </Button>
          </div>
        ) : null}
      </PageMain>
    </>
  );
}
