import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/session";
import { jsonError } from "@/lib/bff/http";

export async function GET() {
  const session = await getSession();
  if (!session) {
    return jsonError(401, "Unauthorized", "Session invalide ou expirée");
  }
  return NextResponse.json(session);
}
