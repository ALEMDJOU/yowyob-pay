import { redirect } from "@/i18n/navigation";
import { getSession } from "@/lib/auth/session";

export default async function AuthenticatedLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  const session = await getSession();
  if (!session) {
    redirect({ href: "/auth/login", locale });
  }
  return <>{children}</>;
}
