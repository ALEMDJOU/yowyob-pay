import { randomUUID } from "node:crypto";
import type { NextRequest } from "next/server";
import { getSession } from "@/lib/auth/session";
import { jsonError, relayBackendResponse } from "./http";

const PAYMENT_BASE =
  process.env.PAYMENT_SERVICE_BASE_URL ?? "http://localhost:8090";
const API_KEY = process.env.PAYMENT_INTERNAL_API_KEY ?? "";

export function resolveIdempotencyKey(
  request: NextRequest,
  explicit?: string | null,
): string {
  const fromHeader =
    explicit?.trim() || request.headers.get("Idempotency-Key")?.trim();
  if (fromHeader) {
    return fromHeader;
  }
  return randomUUID();
}

export async function requireSessionOr401(): Promise<
  | { session: Awaited<ReturnType<typeof getSession>> & object; error: null }
  | { session: null; error: Response }
> {
  const session = await getSession();
  if (!session) {
    return {
      session: null,
      error: jsonError(401, "Unauthorized", "Session invalide ou expirée"),
    };
  }
  return { session, error: null };
}

export async function proxyPayment(
  request: NextRequest,
  method: string,
  path: string,
  options?: {
    body?: string | null;
    requireIdempotency?: boolean;
    idempotencyKey?: string | null;
    requireSession?: boolean;
  },
) {
  if (options?.requireSession !== false) {
    const gate = await requireSessionOr401();
    if (gate.error) {
      return gate.error;
    }
  }

  const search = request.nextUrl.search;
  const url = `${PAYMENT_BASE}${path}${search}`;
  const headers: Record<string, string> = {
    "X-Internal-Api-Key": API_KEY,
  };
  const contentType = request.headers.get("content-type");
  if (contentType) {
    headers["Content-Type"] = contentType;
  }
  if (options?.requireIdempotency) {
    headers["Idempotency-Key"] = resolveIdempotencyKey(
      request,
      options.idempotencyKey,
    );
  }

  const upstream = await fetch(url, {
    method,
    headers,
    body: options?.body ?? undefined,
    cache: "no-store",
  });
  return relayBackendResponse(upstream);
}

export async function readRequestBodyText(
  request: NextRequest,
): Promise<string | null> {
  const method = request.method.toUpperCase();
  if (method === "GET" || method === "HEAD" || method === "DELETE") {
    return null;
  }
  return request.text();
}
