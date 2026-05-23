"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Session } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useEffect, useState } from "react";

export default function RechargeKafkaPage() {
  const t = useTranslations("operations");
  const tx = useTranslations("transactions");
  const te = useTranslations("errors");
  const [agentId, setAgentId] = useState<string | null>(null);
  const [targetWalletId, setTargetWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    bffJson<Session>("/api/bff/auth/session").then((s) => setAgentId(s.agentId));
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!agentId) return;
    setError(null);
    setMessage(null);
    const res = await fetch(`/api/bff/agents/${agentId}/recharge`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        targetWalletId,
        amount: Number(amount),
      }),
    });
    if (res.status === 202 || res.ok) {
      setMessage(t("accepted"));
      return;
    }
    const body = await res.json().catch(() => ({}));
    setError(resolveApiError(res.status, body, te));
  }

  return (
    <>
      <AppHeader title={t("rechargeKafkaTitle")} />
      <PageMain centered backFallback="/dashboard">
        <p className="text-sm text-muted">{t("rechargeKafkaHint")}</p>
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label={t("targetWallet")}
              value={targetWalletId}
              onChange={(e) => setTargetWalletId(e.target.value)}
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
            <Button type="submit" className="w-full" disabled={!agentId}>
              {t("submit")}
            </Button>
          </form>
        </Card>
      </PageMain>
    </>
  );
}
