import { qs, requestJson } from "../../core/http";

/**
 * MyMemory translation (EN → PT-BR). Free tier accepts ~500 bytes per request,
 * so long synopses are split on sentence boundaries.
 */
const BASE = "https://api.mymemory.translated.net/get";
const CHUNK = 420;

function chunks(text: string): string[] {
  const sentences = text.match(/[^.!?\n]+[.!?]*[\s\n]*/g) ?? [text];
  const out: string[] = [];
  let cur = "";
  for (const s of sentences) {
    if ((cur + s).length > CHUNK && cur) {
      out.push(cur);
      cur = "";
    }
    cur += s.length > CHUNK ? s.slice(0, CHUNK) : s;
  }
  if (cur.trim()) out.push(cur);
  return out;
}

export async function toPortuguese(text: string, maxChunks = 8): Promise<string> {
  const parts = chunks(text).slice(0, maxChunks);
  const translated: string[] = [];
  for (const part of parts) {
    const trimmed = part.trim();
    if (!trimmed) continue;
    const res = await requestJson<{ responseStatus: number | string; responseData: { translatedText: string } }>(
      `${BASE}${qs({ q: trimmed, langpair: "en|pt-BR" })}`,
      { source: "translate", retries: 1 },
    );
    const t = res.responseData?.translatedText;
    if (Number(res.responseStatus) !== 200 || !t || /MYMEMORY WARNING|QUERY LENGTH LIMIT/i.test(t)) {
      throw new Error("translation unavailable");
    }
    translated.push(t + (part.endsWith("\n\n") ? "\n\n" : part.endsWith("\n") ? "\n" : " "));
  }
  return translated.join("").trim();
}
