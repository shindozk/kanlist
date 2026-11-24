export type ErrorKind = "network" | "timeout" | "http" | "rate_limit" | "parse" | "not_found" | "unavailable";

/** Single error type that crosses layer boundaries. */
export class AppError extends Error {
  readonly kind: ErrorKind;
  readonly status?: number;
  readonly source?: string;
  readonly retryAfterMs?: number;

  constructor(kind: ErrorKind, message: string, opts: { status?: number; source?: string; retryAfterMs?: number } = {}) {
    super(message);
    this.name = "AppError";
    this.kind = kind;
    this.status = opts.status;
    this.source = opts.source;
    this.retryAfterMs = opts.retryAfterMs;
  }

  get retryable() {
    return (
      this.kind === "network" ||
      this.kind === "timeout" ||
      this.kind === "rate_limit" ||
      (this.kind === "http" && (this.status ?? 0) >= 500)
    );
  }
}

export function toAppError(e: unknown, source?: string): AppError {
  if (e instanceof AppError) return e;
  const msg = e instanceof Error ? e.message : String(e);
  return new AppError("network", msg, { source });
}

/** Human message in PT-BR for any error. */
export function errorMessage(e: unknown): string {
  const err = toAppError(e);
  switch (err.kind) {
    case "network":
      return "Sem conexão com as fontes de dados. Verifique sua internet.";
    case "timeout":
      return "A fonte demorou demais para responder. Tente de novo.";
    case "rate_limit":
      return "Muitas buscas seguidas. Aguarde alguns segundos.";
    case "not_found":
      return "Não encontramos essa obra.";
    case "unavailable":
      return "Todas as fontes estão indisponíveis agora.";
    default:
      return "Algo deu errado ao carregar os dados.";
  }
}
