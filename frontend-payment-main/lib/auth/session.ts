import { jwtVerify } from "jose";
import { cookies } from "next/headers";
import {
  getSessionCookieName,
  getSessionMaxAge,
  isSessionCookieSecure,
} from "./config";

export type SessionSafe = {
  agentId: string;
  email: string;
  name: string;
};

export async function getJwtFromCookies(): Promise<string | null> {
  const store = await cookies();
  return store.get(getSessionCookieName())?.value ?? null;
}

export async function verifySessionToken(
  token: string,
): Promise<SessionSafe | null> {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    return null;
  }
  try {
    const key = new Uint8Array(Buffer.from(secret, "base64"));
    const { payload } = await jwtVerify(token, key);
    const agentId = payload.agentId;
    if (typeof agentId !== "string" || !agentId) {
      return null;
    }
    const email =
      typeof payload.email === "string"
        ? payload.email
        : typeof payload.sub === "string"
          ? payload.sub
          : null;
    if (!email) {
      return null;
    }
    const name = typeof payload.name === "string" ? payload.name : "";
    return { agentId, email, name };
  } catch {
    return null;
  }
}

export async function getSession(): Promise<SessionSafe | null> {
  const token = await getJwtFromCookies();
  if (!token) {
    return null;
  }
  return verifySessionToken(token);
}

export function buildSetSessionCookieHeader(token: string): string {
  const name = getSessionCookieName();
  const maxAge = getSessionMaxAge();
  const secure = isSessionCookieSecure();
  const parts = [
    `${name}=${encodeURIComponent(token)}`,
    "Path=/",
    "HttpOnly",
    "SameSite=Lax",
    `Max-Age=${maxAge}`,
  ];
  if (secure) {
    parts.push("Secure");
  }
  return parts.join("; ");
}

export function buildClearSessionCookieHeader(): string {
  const name = getSessionCookieName();
  return `${name}=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0`;
}
