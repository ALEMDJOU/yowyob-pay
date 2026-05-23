"use client";

import { useRouter } from "@/i18n/navigation";
import { ArrowLeft } from "lucide-react";
import { useTranslations } from "next-intl";

type BackButtonProps = {
  fallbackHref?: string;
  className?: string;
};

export function BackButton({
  fallbackHref = "/dashboard",
  className = "",
}: BackButtonProps) {
  const t = useTranslations("common");
  const router = useRouter();

  function handleBack() {
    if (typeof window !== "undefined" && window.history.length > 1) {
      router.back();
      return;
    }
    router.push(fallbackHref);
  }

  return (
    <button
      type="button"
      onClick={handleBack}
      className={`inline-flex shrink-0 items-center gap-2 text-sm font-medium text-primary transition hover:text-primary/80 ${className}`}
      aria-label={t("back")}
    >
      <ArrowLeft className="h-4 w-4 shrink-0" aria-hidden />
      <span>{t("back")}</span>
    </button>
  );
}
