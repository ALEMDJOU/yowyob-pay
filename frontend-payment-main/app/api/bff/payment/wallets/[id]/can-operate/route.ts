import type { NextRequest } from "next/server";
import { proxyPayment } from "@/lib/bff/paymentProxy";

type Params = { params: Promise<{ id: string }> };

export async function GET(request: NextRequest, { params }: Params) {
  const { id } = await params;
  return proxyPayment(request, "GET", `/api/v1/wallets/${id}/can-operate`);
}
