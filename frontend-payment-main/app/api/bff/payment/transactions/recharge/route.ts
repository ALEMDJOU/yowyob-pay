import type { NextRequest } from "next/server";
import {
  proxyPayment,
  readRequestBodyText,
} from "@/lib/bff/paymentProxy";

export async function POST(request: NextRequest) {
  const body = await readRequestBodyText(request);
  return proxyPayment(request, "POST", "/api/v1/transactions", {
    body,
    requireIdempotency: true,
  });
}
