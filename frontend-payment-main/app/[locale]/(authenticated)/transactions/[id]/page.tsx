"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Card } from "@/components/ui/Card";
import { BffError, bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Transaction } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function TransactionDetailPage() {
  const t = useTranslations("transactions");
  const tc = useTranslations("common");
  const te = useTranslations("errors");
  const params = useParams<{ id: string }>();
  const [tx, setTx] = useState<Transaction | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    setError(null);
    bffJson<Transaction>(`/api/bff/payment/transactions/${params.id}`)
      .then(setTx)
      .catch((e: unknown) => {
        setError(
          e instanceof BffError
            ? resolveApiError(e.status, e.body, te)
            : te("network"),
        );
      })
      .finally(() => setLoading(false));
  }, [params.id, te]);

  const backFallback = tx?.walletId
    ? `/wallets/${tx.walletId}/transactions`
    : "/wallets";

  if (loading) {
    return (
      <>
        <AppHeader title={t("detailTitle")} />
        <PageMain centered backFallback="/wallets">
          <p className="text-center text-muted">{tc("loading")}</p>
        </PageMain>
      </>
    );
  }

  if (error || !tx) {
    return (
      <>
        <AppHeader title={t("detailTitle")} />
        <PageMain centered backFallback="/wallets">
          <p className="text-center text-red-600">{error ?? te("notFound")}</p>
        </PageMain>
      </>
    );
  }

  return (
    <>
      <AppHeader title={t("detailTitle")} />
      <PageMain centered backFallback={backFallback}>
        <Card className="space-y-2 text-sm">
          <p>
            <span className="text-muted">{t("type")}:</span> {tx.type}
          </p>
          <p>
            <span className="text-muted">{t("amount")}:</span> {tx.amount}
          </p>
          <p>
            <span className="text-muted">{t("status")}:</span> {tx.status}
          </p>
          <p>
            <span className="text-muted">{t("walletId")}:</span> {tx.walletId}
          </p>
        </Card>
      </PageMain>
    </>
  );
}
