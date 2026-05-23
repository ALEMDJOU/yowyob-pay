import { serverConfig } from "@/lib/config";

export async function userPaymentFetch(
  path: string,
  init: RequestInit = {},
  bearerToken?: string | null,
): Promise<Response> {
  const headers = new Headers(init.headers);
  if (bearerToken) {
    headers.set("Authorization", `Bearer ${bearerToken}`);
  }
  return fetch(`${serverConfig.userPaymentBaseUrl}${path}`, {
    ...init,
    headers,
  });
}
