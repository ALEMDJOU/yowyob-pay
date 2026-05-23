function required(name: string, value: string | undefined): string {
  if (!value) {
    throw new Error(`Variable d'environnement manquante: ${name}`);
  }
  return value;
}

export const serverConfig = {
  paymentBaseUrl:
    process.env.PAYMENT_SERVICE_BASE_URL ?? "http://localhost:8090",
  userPaymentBaseUrl:
    process.env.USER_PAYMENT_BASE_URL ?? "http://localhost:8091",
  paymentInternalApiKey: process.env.PAYMENT_INTERNAL_API_KEY ?? "",
  jwtSecret: process.env.JWT_SECRET ?? "",
  sessionCookieName: process.env.SESSION_COOKIE_NAME ?? "yowyob_session",
  sessionCookieMaxAge: Number(process.env.SESSION_COOKIE_MAX_AGE ?? "3600"),
};

export function assertBffConfig(): void {
  if (process.env.NODE_ENV === "production") {
    required("PAYMENT_INTERNAL_API_KEY", serverConfig.paymentInternalApiKey);
    required("JWT_SECRET", serverConfig.jwtSecret);
  }
}
