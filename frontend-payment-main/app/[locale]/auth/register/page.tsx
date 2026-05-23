"use client";

import { PageMain } from "@/components/layout/PageMain";
import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Link, useRouter } from "@/i18n/navigation";
import { BffError } from "@/lib/client/bff";
import { resolveAuthError } from "@/lib/client/resolveAuthError";
import { useTranslations } from "next-intl";
import { useState } from "react";

export default function RegisterPage() {
  const t = useTranslations("auth");
  const nav = useTranslations("nav");
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/bff/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new BffError(res.status, body);
      }
      router.push("/auth/login");
      router.refresh();
    } catch (err) {
      if (err instanceof BffError) {
        setError(resolveAuthError(err.status, err.body, t, "register"));
      } else {
        setError(t("errorNetwork"));
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <AppHeader title={t("registerTitle")} />
      <PageMain centered backFallback="/">
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
            <Input
              label={t("name")}
              name="name"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <Input
              label={t("email")}
              name="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <Input
              label={t("password")}
              name="password"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {error ? <p className="text-sm text-red-600">{error}</p> : null}
            <Button type="submit" disabled={loading} className="w-full">
              {t("submitRegister")}
            </Button>
          </form>
          <p className="mt-4 text-sm text-muted">
            {t("hasAccount")}{" "}
            <Link href="/auth/login" className="font-medium text-primary underline">
              {nav("login")}
            </Link>
          </p>
        </Card>
      </PageMain>
    </>
  );
}
