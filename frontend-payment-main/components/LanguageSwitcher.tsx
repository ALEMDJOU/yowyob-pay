"use client";

import { useLocale } from "next-intl";
import { usePathname, useRouter } from "@/i18n/navigation";
import type { AppLocale } from "@/i18n/routing";

export function LanguageSwitcher() {
  const locale = useLocale() as AppLocale;
  const pathname = usePathname();
  const router = useRouter();

  function switchLocale(next: AppLocale) {
    if (next === locale) return;
    router.replace(pathname, { locale: next });
  }

  return (
    <select
      aria-label="Language"
      className="rounded border border-white/30 bg-primary px-2 py-1 text-white"
      value={locale}
      onChange={(e) => switchLocale(e.target.value as AppLocale)}
    >
      <option value="fr">FR</option>
      <option value="en">EN</option>
    </select>
  );
}
