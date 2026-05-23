import type { NextRequest } from "next/server";
import {
  proxyPayment,
  readRequestBodyText,
} from "@/lib/bff/paymentProxy";

export async function GET(request: NextRequest) {
  return proxyPayment(request, "GET", "/api/v1/wallets");
}

export async function POST(request: NextRequest) {
  const body = await readRequestBodyText(request);
  return proxyPayment(request, "POST", "/api/v1/wallets", {
    body,
    requireIdempotency: true,
  });
}
