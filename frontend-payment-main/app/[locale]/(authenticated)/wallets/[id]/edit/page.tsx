"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { useRouter } from "@/i18n/navigation";
import { BffError, bffJson } from "@/lib/client/bff";
import { resolveApiError } from "@/lib/client/resolveApiError";
import type { Wallet } from "@/lib/types";
import { useTranslations } from "next-intl";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function EditWalletPage() {
  const t = useTranslations("wallets");
  const te = useTranslations("errors");
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const id = params.id;
  const [ownerId, setOwnerId] = useState("");
  const [ownerName, setOwnerName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    bffJson<Wallet>(`/api/bff/payment/wallets/${id}`).then((w) => {
      setOwnerId(w.ownerId);
      setOwnerName(w.ownerName);
    });
  }, [id]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/bff/payment/wallets/${id}`, {
        method: "PATCH",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": crypto.randomUUID(),
        },
        body: JSON.stringify({ ownerId, ownerName }),
      });
      if (res.status === 409) {
        setError(t("idempotencyConflict"));
        return;
      }
      if (!res.ok) throw new BffError(res.status, await res.json());
      router.push(`/wallets/${id}`);
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
      <AppHeader title={t("edit")} />
      <PageMain centered backFallback={`/wallets/${id}`}>
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label={t("ownerId")}
              value={ownerId}
              onChange={(e) => setOwnerId(e.target.value)}
              required
            />
            <Input
              label={t("ownerName")}
              value={ownerName}
              onChange={(e) => setOwnerName(e.target.value)}
              required
            />
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" disabled={loading} className="w-full">
              {t("save")}
            </Button>
          </form>
        </Card>
      </PageMain>
    </>
  );
}
