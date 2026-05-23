import { NextResponse } from "next/server";

export async function relayBackendResponse(
  upstream: Response,
): Promise<NextResponse> {
  const contentType =
    upstream.headers.get("content-type") ?? "application/json";
  const body = await upstream.text();
  return new NextResponse(body || null, {
    status: upstream.status,
    headers: { "Content-Type": contentType },
  });
}

export function jsonError(
  status: number,
  title: string,
  detail: string,
): NextResponse {
  return NextResponse.json({ title, detail }, { status });
}
