import { buildClearSessionCookieHeader } from "@/lib/auth/session";
import { NextResponse } from "next/server";

export async function POST() {
  return NextResponse.json(
    { ok: true },
    { headers: { "Set-Cookie": buildClearSessionCookieHeader() } },
  );
}
