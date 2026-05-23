type ErrorTranslator = (
  key: string,
  values?: Record<string, string | number>,
) => string;

type ProblemBody = {
  title?: string;
  detail?: string;
  code?: string;
  fieldErrors?: Record<string, string>;
};

export function resolveApiError(
  status: number,
  body: unknown,
  t: ErrorTranslator,
): string {
  const problem =
    body && typeof body === "object" ? (body as ProblemBody) : {};

  if (typeof problem.detail === "string" && problem.detail.trim()) {
    if (status >= 500) {
      return t("serverWithDetail", { detail: problem.detail });
    }
    return problem.detail;
  }

  if (typeof problem.title === "string" && problem.title.trim() && status < 500) {
    return problem.title;
  }

  if (status === 401) {
    return t("unauthorized");
  }
  if (status === 403) {
    return t("forbidden");
  }
  if (status === 404) {
    return t("notFound");
  }
  if (status === 409) {
    return t("idempotencyConflict");
  }
  if (status >= 500) {
    return t("server");
  }
  if (status >= 400) {
    return t("genericWithStatus", { status });
  }

  return t("generic");
}
