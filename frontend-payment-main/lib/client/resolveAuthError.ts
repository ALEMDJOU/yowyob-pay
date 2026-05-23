type AuthErrorTranslator = (
  key: string,
  values?: Record<string, string | number>,
) => string;

type ProblemBody = {
  title?: string;
  detail?: string;
  status?: number;
  fieldErrors?: Record<string, string>;
  code?: string;
};

const DETAIL_TO_KEY: Record<string, string> = {
  "Identifiants incorrects": "errorInvalidCredentials",
  "Agent introuvable": "errorInvalidCredentials",
  "Email déjà utilisé": "errorEmailTaken",
  "Requête invalide": "errorValidationSummary",
};

const FIELD_MESSAGE_TO_KEY: Record<string, string> = {
  "email est obligatoire": "validation.emailRequired",
  "email invalide": "validation.emailInvalid",
  "password est obligatoire": "validation.passwordRequired",
  "name est obligatoire": "validation.nameRequired",
  "name trop long": "validation.nameTooLong",
  "password : entre 8 et 128 caractères": "validation.passwordLength",
};

const FIELD_LABEL_KEY: Record<string, string> = {
  email: "fieldEmail",
  password: "fieldPassword",
  name: "fieldName",
};

function translateFieldMessage(
  raw: string,
  t: AuthErrorTranslator,
): string {
  const key = FIELD_MESSAGE_TO_KEY[raw.trim()];
  return key ? t(key) : raw;
}

function formatFieldErrors(
  fieldErrors: Record<string, string>,
  t: AuthErrorTranslator,
): string {
  const lines = Object.entries(fieldErrors).map(([field, message]) => {
    const labelKey = FIELD_LABEL_KEY[field];
    const label = labelKey ? t(labelKey) : field;
    return `${label} : ${translateFieldMessage(message, t)}`;
  });
  return lines.join(" · ");
}

function mapDetailToMessage(
  detail: string,
  t: AuthErrorTranslator,
): string | null {
  const trimmed = detail.trim();
  const key = DETAIL_TO_KEY[trimmed];
  if (key) {
    return t(key);
  }
  return null;
}

function translateDetailFields(
  detail: string,
  t: AuthErrorTranslator,
): string | null {
  if (!detail.includes(": ")) {
    return null;
  }
  const pseudo: Record<string, string> = {};
  for (const part of detail.split(";").map((p) => p.trim())) {
    const idx = part.indexOf(": ");
    if (idx > 0) {
      pseudo[part.slice(0, idx)] = part.slice(idx + 2);
    }
  }
  if (Object.keys(pseudo).length === 0) {
    return null;
  }
  return formatFieldErrors(pseudo, t);
}

export function resolveAuthError(
  status: number,
  body: unknown,
  t: AuthErrorTranslator,
  context: "login" | "register",
): string {
  const problem =
    body && typeof body === "object" ? (body as ProblemBody) : {};

  if (problem.code && typeof problem.code === "string") {
    const codeKey = `codes.${problem.code}`;
    const translated = t(codeKey);
    if (translated !== codeKey) {
      return translated;
    }
  }

  if (problem.fieldErrors && Object.keys(problem.fieldErrors).length > 0) {
    return formatFieldErrors(problem.fieldErrors, t);
  }

  if (typeof problem.detail === "string" && problem.detail.trim()) {
    const mapped = mapDetailToMessage(problem.detail, t);
    if (mapped) {
      return mapped;
    }
    const fromFields = translateDetailFields(problem.detail, t);
    if (fromFields) {
      return fromFields;
    }
    if (problem.detail !== "Requête invalide") {
      return problem.detail;
    }
    return t("errorValidationSummary");
  }

  if (status === 401) {
    return t("errorInvalidCredentials");
  }
  if (status === 409) {
    return t("errorEmailTaken");
  }
  if (status === 400) {
    return context === "login"
      ? t("errorGenericLogin")
      : t("errorGenericRegister");
  }
  if (status >= 500) {
    return t("errorServer");
  }

  return context === "login"
    ? t("errorGenericLogin")
    : t("errorGenericRegister");
}
