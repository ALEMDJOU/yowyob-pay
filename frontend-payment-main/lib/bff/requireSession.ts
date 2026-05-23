import { getSession, type SessionSafe } from "@/lib/auth/session";

export type SessionGuard =
  | { session: SessionSafe }
  | { response: Response };

export async function requireSession(): Promise<SessionGuard> {
  const session = await getSession();
  if (!session) {
    return {
      response: Response.json(
        { title: "Unauthorized", detail: "Session invalide ou expirée" },
        { status: 401 },
      ),
    };
  }
  return { session };
}
