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
  const registerResponse = await fetch(
    `${USER_PAYMENT_BASE}/api/v1/auth/register`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body,
      cache: "no-store",
    },
  );

  if (!registerResponse.ok) {
    return relayBackendResponse(registerResponse);
  }

  const agent = await registerResponse.json();

  const loginResponse = await fetch(`${USER_PAYMENT_BASE}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body,
    cache: "no-store",
  });

  if (!loginResponse.ok) {
    return NextResponse.json(agent, { status: 201 });
  }

  const loginPayload = (await loginResponse.json()) as { token?: string };
  if (!loginPayload.token) {
    return NextResponse.json(agent, { status: 201 });
  }

  const session = await verifySessionToken(loginPayload.token);
  if (!session) {
    return jsonError(502, "Bad Gateway", "Jeton invalide après inscription");
  }

  return NextResponse.json(session, {
    status: 201,
    headers: {
      "Set-Cookie": buildSetSessionCookieHeader(loginPayload.token),
    },
  });
}
