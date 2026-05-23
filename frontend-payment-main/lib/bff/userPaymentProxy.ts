import {
    getJwtFromCookies,
    getSession,
    type SessionSafe,
} from "@/lib/auth/session";
import type { NextRequest } from "next/server";
import { jsonError, relayBackendResponse } from "./http";

export type SessionAgentGate =
  | { session: SessionSafe; error: null }
  | { session: null; error: Response };

const USER_PAYMENT_BASE =
  process.env.USER_PAYMENT_BASE_URL ?? "http://localhost:8091";

export async function proxyUserPayment(
  method: string,
  path: string,
  options?: {
    body?: string | null;
    bearerFromCookie?: boolean;
    bearerToken?: string;
  },
) {
  const headers: Record<string, string> = {};
  const contentType = options?.body ? "application/json" : undefined;
  if (contentType) {
    headers["Content-Type"] = contentType;
  }

  let bearer = options?.bearerToken;
  if (options?.bearerFromCookie) {
    bearer = (await getJwtFromCookies()) ?? undefined;
  }
  if (bearer) {
    headers.Authorization = `Bearer ${bearer}`;
  }

  const upstream = await fetch(`${USER_PAYMENT_BASE}${path}`, {
    method,
    headers,
    body: options?.body ?? undefined,
    cache: "no-store",
  });
  return relayBackendResponse(upstream);
}

export async function requireSessionAgentMatch(
  _request: NextRequest,
  pathAgentId: string,
): Promise<SessionAgentGate> {
  const session = await getSession();
  if (!session) {
    return {
      session: null,
      error: jsonError(401, "Unauthorized", "Session invalide ou expirée"),
    };
  }
  if (session.agentId !== pathAgentId) {
    return {
      session: null,
      error: jsonError(
        403,
        "Forbidden",
        "L’identifiant agent ne correspond pas à la session",
      ),
    };
  }
  return { session, error: null };
}
