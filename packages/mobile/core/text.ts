export function stripHtml(input?: string | null): string | undefined {
  if (!input) return undefined;
  const out = input
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/p>/gi, "\n\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&quot;/g, '"')
    .replace(/&#039;|&apos;/g, "'")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&mdash;/g, "—")
    .replace(/\(Source:[^)]*\)/gi, "")
    .replace(/\[Written by MAL Rewrite\]/gi, "")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
  return out || undefined;
}

/** Normalized title for fuzzy matching across sources. */
export function normalizeTitle(s: string) {
  return s
    .toLowerCase()
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

export function uid() {
  return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 8)}`;
}

export function formatScore(score?: number) {
  if (score === undefined || score === null) return undefined;
  return (score / 10).toFixed(1).replace(".", ",");
}

export function compactNumber(n: number) {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1).replace(".", ",")} mi`;
  if (n >= 1000) return `${(n / 1000).toFixed(1).replace(".", ",")} mil`;
  return String(n);
}

export function unitKey(n: number) {
  return Number.isInteger(n) ? String(n) : String(Number(n.toFixed(2)));
}
