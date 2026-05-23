import { NextResponse } from "next/server";
import {
  buildSetSessionCookieHeader,
  verifySessionToken,
} from "@/lib/auth/session";
import { jsonError, relayBackendResponse } from "@/lib/bff/http";

const USER_PAYMENT_BASE =
  process.env.USER_PAYMENT_BASE_URL ?? "http://localhost:8091";

export async function POST(request: Request) {
  const body = await request.text();
  const upstream = await fetch(`${USER_PAYMENT_BASE}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body,
    cache: "no-store",
  });

  if (!upstream.ok) {
    return relayBackendResponse(upstream);
  }

  const payload = (await upstream.json()) as { token?: string };
  if (!payload.token) {
    return jsonError(502, "Bad Gateway", "Réponse login sans jeton");
  }

  const session = await verifySessionToken(payload.token);
  if (!session) {
    return jsonError(502, "Bad Gateway", "Jeton invalide après authentification");
  }

  return NextResponse.json(session, {
    status: 200,
    headers: {
      "Set-Cookie": buildSetSessionCookieHeader(payload.token),
    },
  });
}
