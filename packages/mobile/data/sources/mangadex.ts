import Constants from "expo-constants";
import { Platform } from "react-native";
import { requestJson, qs } from "../../core/http";
import { AppError } from "../../core/errors";
import { client } from "../../lib/api";

/**
 * MangaDex — chapter structure (aggregate), chapter titles (feed) and a catalog
 * source for manga / manhwa / manhua / doujinshi that AniList may not list.
 * Browsers are blocked by MangaDex CORS, so on web we go through the oRPC proxy.
 */
const BASE = "https://api.mangadex.org";
export const COVER_BASE = "https://uploads.mangadex.org/covers";
export const TAG_DOUJINSHI = "b13b2a48-c720-44a9-9c77-39c9979373fb";
export const TAG_ONESHOT = "0234a31e-a729-4e28-9d6a-3f87c4966b9e";

type Query = Record<string, string | number | boolean | (string | number)[]>;

async function get<T>(path: string, query: Query = {}): Promise<T> {
  if (Platform.OS === "web") {
    try {
      return (await client.mangadex.get({ path, query })) as T;
    } catch (e) {
      const code = (e as { code?: string })?.code;
      if (code === "NOT_FOUND") throw new AppError("not_found", "Not found", { source: "mangadex" });
      if (code === "TOO_MANY_REQUESTS") throw new AppError("rate_limit", "Rate limited", { source: "mangadex" });
      throw new AppError("network", e instanceof Error ? e.message : "proxy error", { source: "mangadex" });
    }
  }
  return requestJson<T>(`${BASE}${path}${qs(query)}`, { source: "mangadex" });
}

export interface MDRel {
  id: string;
  type: string;
  attributes?: { fileName?: string; name?: string };
}

export interface MDManga {
  id: string;
  attributes: {
    title: Record<string, string>;
    altTitles: Record<string, string>[];
    description: Record<string, string>;
    originalLanguage: string;
    lastChapter: string | null;
    lastVolume: string | null;
    status: string | null;
    year: number | null;
    contentRating: string;
    publicationDemographic: string | null;
    tags: { id: string; attributes: { name: Record<string, string>; group: string } }[];
    links: Record<string, string> | null;
  };
  relationships: MDRel[];
}

export interface MDChapter {
  id: string;
  attributes: {
    chapter: string | null;
    title: string | null;
    volume: string | null;
    translatedLanguage: string;
    publishAt: string | null;
    pages: number;
  };
}

export interface MDAggregate {
  volumes: Record<
    string,
    { volume: string; count: number; chapters: Record<string, { chapter: string; id: string; count: number }> }
  >;
}

const SAFE = ["safe", "suggestive"];

export async function search(title: string, opts: { limit?: number; adult?: boolean; doujin?: boolean } = {}) {
  const res = await get<{ data: MDManga[] }>("/manga", {
    title,
    limit: opts.limit ?? 12,
    "includes[]": ["cover_art", "author"],
    "contentRating[]": opts.adult ? [...SAFE, "erotica"] : SAFE,
    "order[relevance]": "desc",
    ...(opts.doujin ? { "includedTags[]": [TAG_DOUJINSHI] } : {}),
  });
  return res.data;
}

export async function popular(opts: { limit?: number; offset?: number; language?: string; doujin?: boolean } = {}) {
  const res = await get<{ data: MDManga[]; total: number }>("/manga", {
    limit: opts.limit ?? 20,
    offset: opts.offset ?? 0,
    "includes[]": ["cover_art", "author"],
    "contentRating[]": SAFE,
    "order[followedCount]": "desc",
    "hasAvailableChapters": true,
    ...(opts.language ? { "originalLanguage[]": [opts.language] } : {}),
    ...(opts.doujin ? { "includedTags[]": [TAG_DOUJINSHI] } : {}),
  });
  return res;
}

export async function manga(id: string) {
  const res = await get<{ data: MDManga }>(`/manga/${id}`, { "includes[]": ["cover_art", "author", "artist"] });
  return res.data;
}

export async function aggregate(id: string, langs: string[]) {
  return get<MDAggregate>(`/manga/${id}/aggregate`, langs.length ? { "translatedLanguage[]": langs } : {});
}

/** Chapter feed (titles + dates), up to `max` chapters ordered ascending. */
export async function feed(id: string, langs: string[], max = 1500): Promise<MDChapter[]> {
  const out: MDChapter[] = [];
  const limit = 500;
  for (let offset = 0; offset < max; offset += limit) {
    const res = await get<{ data: MDChapter[]; total: number }>(`/manga/${id}/feed`, {
      limit,
      offset,
      "translatedLanguage[]": langs,
      "order[chapter]": "asc",
      "contentRating[]": [...SAFE, "erotica", "pornographic"],
      includeExternalUrl: 1,
    });
    out.push(...res.data);
    if (offset + limit >= res.total) break;
  }
  return out;
}

/** Finds the MangaDex entry linked to an AniList id (falls back to the best title match). */
export async function findByAnilist(anilistId: string, titles: string[]): Promise<MDManga | null> {
  for (const t of titles.filter(Boolean).slice(0, 2)) {
    const list = await search(t, { limit: 10, adult: true });
    const linked = list.find((m) => m.attributes.links?.al === anilistId);
    if (linked) return linked;
  }
  return null;
}

export function coverUrl(m: MDManga, size: 256 | 512 = 512) {
  const file = m.relationships.find((r) => r.type === "cover_art")?.attributes?.fileName;
  if (!file) return undefined;
  const url = `${COVER_BASE}/${m.id}/${file}.${size}.jpg`;
  if (Platform.OS !== "web") return url;
  const base = String(Constants.expoConfig?.extra?.apiUrl ?? "").replace(/\/+$/, "");
  return base ? `${base}/api/img?u=${encodeURIComponent(url)}` : url;
}
