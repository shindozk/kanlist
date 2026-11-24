import { AppError } from "./errors";

/**
 * Network layer used by every data source:
 * - timeout via AbortController
 * - retry with exponential backoff + jitter (429 / 5xx / network), honoring Retry-After
 * - per-source rate limiting (min interval between requests)
 * - in-flight de-duplication
 * - circuit breaker: a source failing repeatedly is skipped for a cool-down window
 */

export interface RequestOptions {
  source: string;
  method?: "GET" | "POST";
  headers?: Record<string, string>;
  body?: unknown;
  timeoutMs?: number;
  retries?: number;
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

class RateLimiter {
  private next = 0;
  constructor(private readonly intervalMs: number) {}
  async acquire() {
    const now = Date.now();
    const slot = Math.max(now, this.next);
    this.next = slot + this.intervalMs;
    if (slot > now) await sleep(slot - now);
  }
}

/** Minimum spacing per source (ms). Jikan = 3 req/s, MangaDex = 5 req/s. */
const LIMITS: Record<string, number> = { jikan: 380, mangadex: 220, anilist: 120, kitsu: 60, translate: 150 };
const limiters = new Map<string, RateLimiter>();
const limiterFor = (source: string) => {
  let l = limiters.get(source);
  if (!l) {
    l = new RateLimiter(LIMITS[source] ?? 0);
    limiters.set(source, l);
  }
  return l;
};

// ---- circuit breaker -------------------------------------------------------
const FAIL_THRESHOLD = 4;
const COOLDOWN_MS = 45_000;
const circuits = new Map<string, { fails: number; openUntil: number }>();

function assertCircuit(source: string) {
  const c = circuits.get(source);
  if (c && c.openUntil > Date.now()) {
    throw new AppError("unavailable", `${source} temporarily disabled`, { source });
  }
}
function recordResult(source: string, ok: boolean) {
  const c = circuits.get(source) ?? { fails: 0, openUntil: 0 };
  if (ok) {
    c.fails = 0;
    c.openUntil = 0;
  } else {
    c.fails += 1;
    if (c.fails >= FAIL_THRESHOLD) c.openUntil = Date.now() + COOLDOWN_MS;
  }
  circuits.set(source, c);
}
export function sourceHealth(source: string) {
  const c = circuits.get(source);
  return { fails: c?.fails ?? 0, open: !!c && c.openUntil > Date.now() };
}

// ---- request ---------------------------------------------------------------
const inflight = new Map<string, Promise<unknown>>();

export function requestJson<T>(url: string, opts: RequestOptions): Promise<T> {
  const dedupeKey = `${opts.method ?? "GET"} ${url} ${opts.body ? JSON.stringify(opts.body) : ""}`;
  const existing = inflight.get(dedupeKey);
  if (existing) return existing as Promise<T>;
  const p = run<T>(url, opts).finally(() => inflight.delete(dedupeKey));
  inflight.set(dedupeKey, p);
  return p;
}

async function run<T>(url: string, opts: RequestOptions): Promise<T> {
  const retries = opts.retries ?? 2;
  let attempt = 0;
  assertCircuit(opts.source);
  for (;;) {
    try {
      const result = await once<T>(url, opts);
      recordResult(opts.source, true);
      return result;
    } catch (e) {
      const err = e instanceof AppError ? e : new AppError("network", String(e), { source: opts.source });
      if (!err.retryable || attempt >= retries) {
        if (err.retryable) recordResult(opts.source, false);
        throw err;
      }
      const backoff = err.retryAfterMs ?? 450 * 2 ** attempt + Math.random() * 250;
      await sleep(Math.min(backoff, 6000));
      attempt += 1;
    }
  }
}

async function once<T>(url: string, opts: RequestOptions): Promise<T> {
  await limiterFor(opts.source).acquire();
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), opts.timeoutMs ?? 12_000);
  let res: Response;
  try {
    res = await fetch(url, {
      method: opts.method ?? "GET",
      headers: {
        Accept: "application/json",
        ...(opts.body ? { "Content-Type": "application/json" } : {}),
        ...opts.headers,
      },
      body: opts.body ? JSON.stringify(opts.body) : undefined,
      signal: ctrl.signal,
    });
  } catch (e) {
    if (ctrl.signal.aborted) throw new AppError("timeout", `Timeout ${url}`, { source: opts.source });
    throw new AppError("network", e instanceof Error ? e.message : "network error", { source: opts.source });
  } finally {
    clearTimeout(timer);
  }

  if (!res.ok) {
    if (res.status === 429) {
      const ra = Number(res.headers.get("retry-after"));
      throw new AppError("rate_limit", "Rate limited", {
        status: 429,
        source: opts.source,
        retryAfterMs: Number.isFinite(ra) && ra > 0 ? ra * 1000 : undefined,
      });
    }
    if (res.status === 404) throw new AppError("not_found", "Not found", { status: 404, source: opts.source });
    throw new AppError("http", `HTTP ${res.status}`, { status: res.status, source: opts.source });
  }
  try {
    return (await res.json()) as T;
  } catch {
    throw new AppError("parse", "Invalid JSON", { source: opts.source });
  }
}

/** Builds a query string supporting repeated keys (`includes[]=a&includes[]=b`). */
export function qs(params: Record<string, string | number | boolean | (string | number)[] | undefined | null>) {
  const parts: string[] = [];
  for (const [k, v] of Object.entries(params)) {
    if (v === undefined || v === null || v === "") continue;
    const values = Array.isArray(v) ? v : [v];
    for (const item of values) parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(String(item))}`);
  }
  return parts.length ? `?${parts.join("&")}` : "";
}

/** Runs async tasks with bounded concurrency. */
export async function pool<T, R>(items: T[], limit: number, fn: (item: T) => Promise<R>): Promise<R[]> {
  const out: R[] = Array.from({ length: items.length });
  let i = 0;
  const workers = Array.from({ length: Math.min(limit, items.length) }, async () => {
    while (i < items.length) {
      const idx = i++;
      out[idx] = await fn(items[idx]);
    }
  });
  await Promise.all(workers);
  return out;
}
