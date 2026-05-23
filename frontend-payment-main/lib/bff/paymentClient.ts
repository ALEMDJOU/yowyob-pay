import { serverConfig } from "@/lib/config";
import { randomUUID } from "crypto";

export type PaymentFetchInit = RequestInit & {
  idempotencyKey?: string | null;
};

export async function paymentFetch(
  path: string,
  init: PaymentFetchInit = {},
): Promise<Response> {
  const headers = new Headers(init.headers);
  headers.set("X-Internal-Api-Key", serverConfig.paymentInternalApiKey);
  if (init.method === "POST" || init.method === "PATCH") {
    const key = init.idempotencyKey ?? randomUUID();
    headers.set("Idempotency-Key", key);
  }
  const { idempotencyKey, ...fetchInit } = init;
  void idempotencyKey;
  return fetch(`${serverConfig.paymentBaseUrl}${path}`, {
    ...fetchInit,
    headers,
  });
}
