import type { NextRequest } from "next/server";
import { proxyPayment } from "@/lib/bff/paymentProxy";

type Params = { params: Promise<{ walletId: string }> };

export async function GET(request: NextRequest, { params }: Params) {
  const { walletId } = await params;
  return proxyPayment(
    request,
    "GET",
    `/api/v1/transactions/Wallet/${walletId}`,
  );
}
