export async function proxyResponse(upstream: Response): Promise<Response> {
  const body = await upstream.text();
  const contentType =
    upstream.headers.get("Content-Type") ?? "application/json";
  return new Response(body, {
    status: upstream.status,
    headers: { "Content-Type": contentType },
  });
}
