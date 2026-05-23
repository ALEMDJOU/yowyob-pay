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

export default function LoginPage() {
  const t = useTranslations("auth");
  const nav = useTranslations("nav");
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("/api/bff/auth/login", {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new BffError(res.status, body);
      }
      router.push("/dashboard");
      router.refresh();
    } catch (err) {
      if (err instanceof BffError) {
        setError(resolveAuthError(err.status, err.body, t, "login"));
      } else {
        setError(t("errorNetwork"));
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <AppHeader title={t("loginTitle")} />
      <PageMain centered backFallback="/">
        <Card>
          <form onSubmit={onSubmit} className="space-y-4">
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
              {t("submitLogin")}
            </Button>
          </form>
          <p className="mt-4 text-sm text-muted">
            {t("noAccount")}{" "}
            <Link href="/auth/register" className="font-medium text-primary underline">
              {nav("register")}
            </Link>
          </p>
        </Card>
      </PageMain>
    </>
  );
}
