import { DashboardNav } from "@/components/dashboard/DashboardNav";
import { LogoutButton } from "@/components/LogoutButton";
import { AppHeader } from "@/components/ui/AppHeader";
import { BackButton } from "@/components/ui/BackButton";
import { getSession } from "@/lib/auth/session";
import { getTranslations } from "next-intl/server";

export default async function DashboardPage() {
  const session = await getSession();
  const t = await getTranslations("dashboard");

  return (
    <>
      <AppHeader title={t("title")} actions={<LogoutButton />} />
      <main className="flex w-full flex-1 flex-col px-4 py-8 lg:px-8">
        <BackButton fallbackHref="/" className="mb-6" />
        <div className="flex w-full flex-col gap-10 lg:flex-row lg:items-start lg:justify-between">
          <p className="text-2xl font-semibold text-primary lg:max-w-xl">
            {t("welcome", { name: session?.name ?? "" })}
          </p>
          <div className="w-full lg:flex lg:justify-end">
            <DashboardNav />
          </div>
        </div>
      </main>
    </>
  );
}
