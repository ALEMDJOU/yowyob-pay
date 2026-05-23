"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { resolveApiError } from "@/lib/client/resolveApiError";
import { useTranslations } from "next-intl";
import { useState } from "react";

export default function RechargeRestPage() {
  const t = useTranslations("operations");
  const tx = useTranslations("transactions");
  const te = useTranslations("errors");
  const [walletId, setWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    const res = await fetch("/api/bff/payment/transactions/recharge", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        "Idempotency-Key": crypto.randomUUID(),
      },
      body: JSON.stringify({
        walletId,
        amount: Number(amount),
        type: "RECHARGE",
      }),
    });
    if (res.ok) {
      setMessage(t("accepted"));
      return;
    }
    const body = await res.json().catch(() => ({}));
    setError(resolveApiError(res.status, body, te));
  }

  return (
    <>
      <AppHeader title={t("rechargeRestTitle")} />
      <PageMain centered backFallback="/dashboard">
        <p className="text-sm text-muted">{t("rechargeRestHint")}</p>
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label={tx("walletId")}
              value={walletId}
              onChange={(e) => setWalletId(e.target.value)}
              required
            />
            <Input
              label={tx("amount")}
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
            {message ? <p className="text-sm text-green-700">{message}</p> : null}
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" className="w-full">
              {t("submit")}
            </Button>
          </form>
        </Card>
      </PageMain>
    </>
  );
}
