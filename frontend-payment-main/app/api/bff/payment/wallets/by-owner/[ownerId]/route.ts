import type { NextRequest } from "next/server";
import { proxyPayment } from "@/lib/bff/paymentProxy";

type Params = { params: Promise<{ ownerId: string }> };

export async function GET(request: NextRequest, { params }: Params) {
  const { ownerId } = await params;
  return proxyPayment(request, "GET", `/api/v1/wallets/owner/${ownerId}`);
}
