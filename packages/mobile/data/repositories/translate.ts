import { cached, TTL } from "../../core/cache";
import { toPortuguese } from "../sources/translate";

/** Cached EN → PT-BR translation of a synopsis. */
export function translateSynopsis(key: string, text: string) {
  return cached(`tr:${key}:${text.length}`, { ttlMs: TTL.translate, persist: true }, () => toPortuguese(text));
}
