import { cached, TTL } from "../../core/cache";
import { AppError, toAppError } from "../../core/errors";
import { normalizeTitle } from "../../core/text";
import * as anilist from "../sources/anilist";
import * as kitsu from "../sources/kitsu";
import * as jikan from "../sources/jikan";
import * as mangadex from "../sources/mangadex";
import {
  detailFromAniList,
  detailFromMangaDex,
  titleFromAniList,
  titleFromJikan,
  titleFromKitsu,
  titleFromMangaDex,
} from "../mappers";
import { presetById } from "../../domain/browse";
import { KIND_META } from "../../domain/kinds";
import type { KindFilter, Settings, Title, TitleDetail } from "../../domain/types";

/**
 * Catalog repository — the only entry point screens use for remote catalog data.
 * Orchestrates sources with fallbacks: AniList → Kitsu → Jikan, plus MangaDex for
 * comics AniList doesn't index (doujinshi, scanlation-only manhwa) and chapter data.
 */

export type SourceToggles = Settings["sources"];

export interface SearchOptions {
  filter: KindFilter;
  adult: boolean;
  sources: SourceToggles;
}

export interface SearchResult {
  items: Title[];
  /** Sources that answered. */
  used: string[];
  /** Sources that failed (shown as a soft warning). */
  failed: string[];
}

function rank(items: Title[], query: string) {
  const q = normalizeTitle(query);
  const score = (t: Title) => {
    const names = [t.title, t.titleAlt, t.titleNative].filter(Boolean).map((n) => normalizeTitle(n!));
    let s = 0;
    if (names.some((n) => n === q)) s += 1000;
    else if (names.some((n) => n.startsWith(q))) s += 500;
    else if (names.some((n) => n.includes(q))) s += 200;
    s += Math.log10((t.popularity ?? 0) + 1) * 40;
    if (t.source === "anilist") s += 30;
    return s;
  };
  return [...items].sort((a, b) => score(b) - score(a));
}

/** Removes duplicates across sources (linked ids or identical normalized titles + kind). */
function dedupe(items: Title[]) {
  const seenAni = new Set<string>();
  const seenMal = new Set<string>();
  const seenName = new Set<string>();
  const out: Title[] = [];
  for (const t of items) {
    const ani = t.externalIds.anilist;
    const mal = t.externalIds.mal ? `${t.medium}:${t.externalIds.mal}` : undefined;
    const name = `${t.medium}:${normalizeTitle(t.title)}`;
    if ((ani && seenAni.has(ani)) || (mal && seenMal.has(mal)) || seenName.has(name)) continue;
    if (ani) seenAni.add(ani);
    if (mal) seenMal.add(mal);
    seenName.add(name);
    out.push(t);
  }
  return out;
}

const matchesFilter = (t: Title, f: KindFilter) => f === "all" || t.kind === f;

export async function search(query: string, opts: SearchOptions): Promise<SearchResult> {
  const q = query.trim();
  if (q.length < 2) return { items: [], used: [], failed: [] };
  const key = `search:${opts.filter}:${opts.adult ? 1 : 0}:${Object.values(opts.sources).join("")}:${normalizeTitle(q)}`;
  return cached(key, { ttlMs: TTL.search }, async () => {
    const medium = opts.filter === "all" ? null : KIND_META[opts.filter].medium;
    const types: anilist.ALType[] = medium === "watch" ? ["ANIME"] : medium === "read" ? ["MANGA"] : ["ANIME", "MANGA"];
    const used: string[] = [];
    const failed: string[] = [];
    let primary: Title[] = [];

    const tasks: Promise<void>[] = [];

    // 1) AniList, falling back to Kitsu then Jikan.
    tasks.push(
      (async () => {
        try {
          const pages = await Promise.all(
            types.map((type) =>
              anilist.browse({
                type,
                search: q,
                perPage: types.length > 1 ? 15 : 25,
                isAdult: opts.adult ? undefined : false,
              }),
            ),
          );
          primary = pages.flatMap((p) => p.media.map(titleFromAniList));
          used.push("AniList");
          return;
        } catch {
          failed.push("AniList");
        }
        if (opts.sources.kitsu) {
          try {
            const lists = await Promise.all(types.map((t) => kitsu.search(t === "ANIME" ? "anime" : "manga", q)));
            primary = lists.flat().map(titleFromKitsu);
            used.push("Kitsu");
            return;
          } catch {
            failed.push("Kitsu");
          }
        }
        if (opts.sources.jikan) {
          try {
            const lists = await Promise.all(
              types.map((t) => {
                const type = t === "ANIME" ? "anime" : "manga";
                return jikan.search(type, q).then((r) => r.map((m) => titleFromJikan(type, m)));
              }),
            );
            primary = lists.flat();
            used.push("Jikan");
          } catch {
            failed.push("Jikan");
          }
        }
      })(),
    );

    // 2) MangaDex in parallel for comics.
    let extra: Title[] = [];
    if (opts.sources.mangadex && medium !== "watch") {
      tasks.push(
        mangadex
          .search(q, { limit: 15, adult: opts.adult, doujin: opts.filter === "doujin" })
          .then((list) => {
            extra = list.map(titleFromMangaDex);
            used.push("MangaDex");
          })
          .catch(() => {
            failed.push("MangaDex");
          }),
      );
    }

    await Promise.all(tasks);
    if (!used.length) throw new AppError("unavailable", "All sources failed");

    const merged = dedupe([...primary, ...extra])
      .filter((t) => matchesFilter(t, opts.filter))
      .filter((t) => opts.adult || !t.isAdult);
    return { items: rank(merged, q), used, failed };
  });
}

export interface BrowsePage {
  items: Title[];
  hasNext: boolean;
}

export async function browse(presetId: string, page = 1, adult = false): Promise<BrowsePage> {
  const preset = presetById(presetId);
  if (!preset) throw new AppError("not_found", "Unknown list");
  return cached(`browse:${presetId}:${page}:${adult ? 1 : 0}`, { ttlMs: TTL.discover, persist: page === 1 }, async () => {
    if (preset.source === "mangadex") {
      const res = await mangadex.popular({ limit: 20, offset: (page - 1) * 20, ...preset.mangadex });
      return { items: res.data.map(titleFromMangaDex), hasNext: page * 20 < res.total };
    }
    try {
      const res = await anilist.browse({ ...preset.params, page, perPage: 20, isAdult: adult ? undefined : false });
      return { items: res.media.map(titleFromAniList), hasNext: res.hasNextPage };
    } catch (e) {
      // Trending fallback through Kitsu when AniList is down.
      if (page === 1 && (presetId === "trending-anime" || presetId === "trending-manga")) {
        const list = await kitsu.trending(presetId === "trending-anime" ? "anime" : "manga");
        return { items: list.map(titleFromKitsu), hasNext: false };
      }
      throw e;
    }
  });
}

export function parseKey(key: string): { source: Title["source"]; id: string } {
  const i = key.indexOf(":");
  return { source: key.slice(0, i) as Title["source"], id: key.slice(i + 1) };
}

function emptyDetail(t: Title, source: TitleDetail["sources"][number]): TitleDetail {
  return { ...t, relations: [], recommendations: [], characters: [], links: [], tags: [], sources: [source] };
}

export async function detail(key: string): Promise<TitleDetail> {
  const { source, id } = parseKey(key);
  return cached(`detail:${key}`, { ttlMs: TTL.detail, persist: true }, async () => {
    switch (source) {
      case "anilist":
        return detailFromAniList(await anilist.detail(Number(id)));
      case "mangadex": {
        const m = await mangadex.manga(id);
        const md = detailFromMangaDex(m);
        if (md.externalIds.anilist) {
          // Enrich with AniList metadata, but keep the MangaDex identity (chapters come from there).
          try {
            const al = detailFromAniList(await anilist.detail(Number(md.externalIds.anilist)));
            return {
              ...al,
              key: md.key,
              source: md.source,
              sourceId: md.sourceId,
              kind: md.kind === "doujin" ? "doujin" : al.kind,
              externalIds: { ...al.externalIds, ...md.externalIds },
              links: [...md.links, ...al.links],
              synopsis: md.synopsisLang === "pt" ? md.synopsis : al.synopsis,
              synopsisLang: md.synopsisLang === "pt" ? "pt" : al.synopsisLang,
              sources: ["mangadex", "anilist"],
            };
          } catch {
            return md;
          }
        }
        return md;
      }
      case "kitsu": {
        const [type, kid] = id.split("-") as ["anime" | "manga", string];
        const t = titleFromKitsu(await kitsu.get(type, kid));
        return { ...emptyDetail(t, "kitsu"), links: [{ site: "Kitsu", url: `https://kitsu.app/${type}/${kid}` }] };
      }
      case "jikan": {
        const [type, mal] = id.split("-") as ["anime" | "manga", string];
        const al = await anilist.byMalId(Number(mal), type === "anime" ? "ANIME" : "MANGA");
        if (!al) throw new AppError("not_found", "Not found");
        const d = detailFromAniList(await anilist.detail(al.id));
        return { ...d, key, source: "jikan", sourceId: id, sources: ["anilist", "jikan"] };
      }
      default:
        throw new AppError("not_found", "Unknown source");
    }
  });
}

export interface AiringItem {
  key: string;
  title: Title;
  unit: number;
  at: number;
}

/** Episodes airing between two timestamps (ms) for the given AniList ids. */
export async function airingBetween(fromMs: number, toMs: number, anilistIds: number[]): Promise<AiringItem[]> {
  if (!anilistIds.length) return [];
  const ids = [...anilistIds].sort((a, b) => a - b);
  const from = Math.floor(fromMs / 1000);
  const to = Math.floor(toMs / 1000);
  return cached(`airing:${from}:${to}:${ids.join(",")}`, { ttlMs: TTL.airing }, async () => {
    const list = await anilist.airing(from, to, ids);
    return list.map((a) => {
      const t = titleFromAniList(a.media);
      return { key: t.key, title: t, unit: a.episode, at: a.airingAt * 1000 };
    });
  });
}

export async function safe<T>(p: Promise<T>): Promise<{ ok: true; value: T } | { ok: false; error: AppError }> {
  try {
    return { ok: true, value: await p };
  } catch (e) {
    return { ok: false, error: toAppError(e) };
  }
}
