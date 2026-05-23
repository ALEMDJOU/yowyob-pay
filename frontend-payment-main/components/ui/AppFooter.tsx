import { getTranslations } from "next-intl/server";

export async function AppFooter() {
  const t = await getTranslations("footer");
  const year = new Date().getFullYear();

  return (
    <footer className="shrink-0 border-t border-primary/10 py-6 text-center text-sm text-primary/60">
      <p>{t("copyright", { year })}</p>
    </footer>
  );
}
