"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Link, useRouter } from "@/i18n/navigation";
import { BffError, bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Wallet } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function WalletDetailPage() {
  const t = useTranslations("wallets");
  const tc = useTranslations("common");
  const te = useTranslations("errors");
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const id = params.id;
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [canOperate, setCanOperate] = useState<boolean | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    setError(null);
    Promise.all([
      bffJson<Wallet>(`/api/bff/payment/wallets/${id}`),
      bffJson<boolean>(`/api/bff/payment/wallets/${id}/can-operate`),
    ])
      .then(([w, c]) => {
        setWallet(w);
        setCanOperate(c);
      })
      .catch((e: unknown) => {
        setError(
          e instanceof BffError
            ? resolveApiError(e.status, e.body, te)
            : te("network"),
        );
      })
      .finally(() => setLoading(false));
  }, [id, te]);

  async function onDelete() {
    if (!confirm(t("confirmDelete"))) return;
    const res = await fetch(`/api/bff/payment/wallets/${id}`, {
      method: "DELETE",
      credentials: "include",
    });
    if (res.ok) {
      router.push("/wallets");
    }
  }

  if (loading) {
    return (
      <>
        <AppHeader title={t("detail")} />
        <PageMain centered backFallback="/wallets">
          <p className="text-center text-muted">{tc("loading")}</p>
        </PageMain>
      </>
    );
  }

  if (error || !wallet) {
    return (
      <>
        <AppHeader title={t("detail")} />
        <PageMain centered backFallback="/wallets">
          <p className="text-center text-red-600">{error ?? te("notFound")}</p>
        </PageMain>
      </>
    );
  }

  return (
    <>
      <AppHeader title={t("detail")} />
      <PageMain centered backFallback="/wallets">
        <Card className="space-y-2">
          <p className="font-semibold text-primary">{wallet.ownerName}</p>
          <p className="text-sm">{wallet.ownerId}</p>
          <p>
            {t("balance")}: <strong>{wallet.balance}</strong>
          </p>
          <p className="text-sm text-muted">
            {t("canOperate")}:{" "}
            {canOperate === null
              ? "…"
              : canOperate
                ? t("canOperateYes")
                : t("canOperateNo")}
          </p>
        </Card>
        <div className="flex flex-wrap gap-3">
          <Link href={`/wallets/${id}/edit`}>
            <Button variant="secondary">{t("edit")}</Button>
          </Link>
          <Link href={`/wallets/${id}/transactions`}>
            <Button variant="secondary">{t("transactions")}</Button>
          </Link>
          <Button variant="danger" onClick={onDelete}>
            {t("delete")}
          </Button>
        </div>
      </PageMain>
    </>
  );
}
