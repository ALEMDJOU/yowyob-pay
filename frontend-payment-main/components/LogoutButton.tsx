"use client";

import { useTranslations } from "next-intl";
import { useLocale } from "next-intl";
import { useRouter } from "@/i18n/navigation";
import { Button } from "@/components/ui/Button";

export function LogoutButton() {
  const t = useTranslations("nav");
  const router = useRouter();
  const locale = useLocale();

  async function logout() {
    await fetch("/api/bff/auth/logout", { method: "POST", credentials: "include" });
    router.push("/auth/login", { locale });
    router.refresh();
  }

  return (
    <Button
      variant="secondary"
      className="!border-white/30 !bg-transparent !text-white hover:!bg-white/10"
      onClick={logout}
    >
      {t("logout")}
    </Button>
  );
}
