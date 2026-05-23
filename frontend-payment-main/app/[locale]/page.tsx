import { AppHeader } from "@/components/ui/AppHeader";
import { Button } from "@/components/ui/Button";
import { Link } from "@/i18n/navigation";
import { useTranslations } from "next-intl";

export default function LandingPage() {
  const t = useTranslations("landing");

  return (
    <>
      <AppHeader title={t("title")} />
      <main className="mx-auto flex w-full max-w-5xl flex-1 flex-col items-center justify-center px-4 py-16">
        <section className="max-w-2xl space-y-4">
          <h2 className="text-3xl font-bold text-primary">{t("title")}</h2>
          <p className="text-lg text-primary/80">{t("subtitle")}</p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Link href="/auth/login">
              <Button type="button">{t("ctaLogin")}</Button>
            </Link>
            <Link href="/auth/register">
              <Button variant="secondary" type="button">
                {t("ctaRegister")}
              </Button>
            </Link>
          </div>
        </section>
      </main>
    </>
  );
}
