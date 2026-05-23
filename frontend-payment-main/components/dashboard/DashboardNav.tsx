"use client";

import { Link } from "@/i18n/navigation";
import {
    CreditCard,
    Radio,
    Wallet,
    Zap,
    type LucideIcon,
} from "lucide-react";
import { useTranslations } from "next-intl";

type NavRow = {
  href: "/wallets" | "/operations/recharge-kafka" | "/operations/recharge-rest" | "/operations/payment";
  labelKey: "walletsLink" | "rechargeKafka" | "rechargeRest" | "payment";
  icon: LucideIcon;
};

const ROWS: NavRow[] = [
  { href: "/wallets", labelKey: "walletsLink", icon: Wallet },
  { href: "/operations/recharge-kafka", labelKey: "rechargeKafka", icon: Radio },
  { href: "/operations/recharge-rest", labelKey: "rechargeRest", icon: Zap },
  { href: "/operations/payment", labelKey: "payment", icon: CreditCard },
];

export function DashboardNav() {
  const t = useTranslations("dashboard");

  return (
    <table className="w-full max-w-md border-collapse">
      <tbody>
        {ROWS.map(({ href, labelKey, icon: Icon }) => (
          <tr key={href}>
            <td className="py-3">
              <Link
                href={href}
                className="inline-flex items-center gap-3 text-base font-medium text-primary transition hover:text-primary/80"
              >
                <Icon className="h-5 w-5 shrink-0 text-accent" aria-hidden />
                {t(labelKey)}
              </Link>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
