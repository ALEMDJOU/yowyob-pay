"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Card } from "@/components/ui/Card";
import { Link } from "@/i18n/navigation";
import { BffError, bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Transaction } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function WalletTransactionsPage() {
  const t = useTranslations("transactions");
  const tc = useTranslations("common");
  const te = useTranslations("errors");
  const params = useParams<{ id: string }>();
  const walletId = params.id;
  const [items, setItems] = useState<Transaction[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    setError(null);
    bffJson<Transaction[]>(`/api/bff/payment/transactions/by-wallet/${walletId}`)
      .then(setItems)
      .catch((e: unknown) => {
        setError(
          e instanceof BffError
            ? resolveApiError(e.status, e.body, te)
            : te("network"),
        );
      })
      .finally(() => setLoading(false));
  }, [walletId, te]);

  return (
    <>
      <AppHeader title={t("title")} />
      <PageMain maxWidth="3xl" backFallback={`/wallets/${walletId}`}>
        {loading ? (
          <p className="text-muted">{tc("loading")}</p>
        ) : error ? (
          <p className="text-red-600">{error}</p>
        ) : items.length === 0 ? (
          <p className="text-muted">{t("empty")}</p>
        ) : (
          <ul className="space-y-3">
            {items.map((tx) => (
              <li key={tx.id}>
                <Card className="flex justify-between gap-2">
                  <div>
                    <p className="font-medium text-primary">
                      {tx.type} - {tx.amount}
                    </p>
                    <p className="text-sm text-muted">{tx.status}</p>
                  </div>
                  <Link
                    href={`/transactions/${tx.id}`}
                    className="text-sm text-primary underline"
                  >
                    {t("detail")}
                  </Link>
                </Card>
              </li>
            ))}
          </ul>
        )}
      </PageMain>
    </>
  );
}
