export function getSessionCookieName(): string {
  return process.env.SESSION_COOKIE_NAME ?? "yowyob_session";
}

export function getSessionMaxAge(): number {
  const raw = process.env.SESSION_MAX_AGE_SECONDS;
  const parsed = raw ? Number.parseInt(raw, 10) : 3600;
  return Number.isFinite(parsed) ? parsed : 3600;
}

export function isSessionCookieSecure(): boolean {
  return process.env.SESSION_COOKIE_SECURE === "true";
}
