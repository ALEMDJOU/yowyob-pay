"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { useRouter } from "@/i18n/navigation";
import { BffError } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Wallet } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useState } from "react";

export default function NewWalletPage() {
  const t = useTranslations("wallets");
  const te = useTranslations("errors");
  const router = useRouter();
  const [ownerId, setOwnerId] = useState("");
  const [ownerName, setOwnerName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const idempotencyKey = crypto.randomUUID();
    try {
      const res = await fetch("/api/bff/payment/wallets", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": idempotencyKey,
        },
        body: JSON.stringify({ ownerId, ownerName }),
      });
      if (res.status === 409) {
        setError(t("idempotencyConflict"));
        return;
      }
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new BffError(res.status, body);
      }
      const wallet = (await res.json()) as Wallet;
      router.push(`/wallets/${wallet.id}`);
    } catch (err) {
      if (err instanceof BffError) {
        if (err.status === 409) {
          setError(t("idempotencyConflict"));
        } else {
          setError(resolveApiError(err.status, err.body, te));
        }
      } else {
        setError(te("network"));
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <AppHeader title={t("new")} />
      <PageMain centered backFallback="/wallets">
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label={t("ownerId")}
              name="ownerId"
              required
              value={ownerId}
              onChange={(e) => setOwnerId(e.target.value)}
            />
            <Input
              label={t("ownerName")}
              name="ownerName"
              required
              value={ownerName}
              onChange={(e) => setOwnerName(e.target.value)}
            />
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" disabled={loading} className="w-full">
              {t("create")}
            </Button>
          </form>
        </Card>
      </PageMain>
    </>
  );
}
