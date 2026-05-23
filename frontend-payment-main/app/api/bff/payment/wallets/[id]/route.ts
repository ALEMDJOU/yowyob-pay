import type { NextRequest } from "next/server";
import {
  proxyPayment,
  readRequestBodyText,
} from "@/lib/bff/paymentProxy";

type Params = { params: Promise<{ id: string }> };

export async function GET(request: NextRequest, { params }: Params) {
  const { id } = await params;
  return proxyPayment(request, "GET", `/api/v1/wallets/${id}`);
}

export async function PATCH(request: NextRequest, { params }: Params) {
  const { id } = await params;
  const body = await readRequestBodyText(request);
  return proxyPayment(request, "PATCH", `/api/v1/wallets/${id}`, {
    body,
    requireIdempotency: true,
  });
}

export async function DELETE(request: NextRequest, { params }: Params) {
  const { id } = await params;
  return proxyPayment(request, "DELETE", `/api/v1/wallets/${id}`);
}
