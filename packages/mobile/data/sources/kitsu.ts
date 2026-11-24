import { qs, requestJson } from "../../core/http";

/**
 * Kitsu JSON:API — episode metadata (titles, thumbnails, air dates) and a
 * catalog fallback when AniList is unavailable.
 */
const BASE = "https://kitsu.io/api/edge";
const HEADERS = { Accept: "application/vnd.api+json" };

export interface KitsuMediaAttrs {
  canonicalTitle: string;
  titles: Record<string, string | undefined>;
  synopsis: string | null;
  startDate: string | null;
  endDate: string | null;
  status: string | null;
  subtype: string | null;
  averageRating: string | null;
  userCount: number | null;
  episodeCount?: number | null;
  episodeLength?: number | null;
  chapterCount?: number | null;
  volumeCount?: number | null;
  posterImage: { small?: string; medium?: string; large?: string; original?: string } | null;
  coverImage: { large?: string; original?: string } | null;
  nsfw?: boolean;
}

export interface KitsuMedia {
  id: string;
  type: "anime" | "manga";
  attributes: KitsuMediaAttrs;
}

export interface KitsuEpisode {
  id: string;
  attributes: {
    number: number | null;
    canonicalTitle: string | null;
    titles: Record<string, string | undefined> | null;
    airdate: string | null;
    length: number | null;
    thumbnail: { original?: string } | null;
  };
}

export async function search(type: "anime" | "manga", text: string, limit = 12): Promise<KitsuMedia[]> {
  const res = await requestJson<{ data: KitsuMedia[] }>(
    `${BASE}/${type}${qs({ "filter[text]": text, "page[limit]": limit })}`,
    { source: "kitsu", headers: HEADERS },
  );
  return res.data;
}

export async function trending(type: "anime" | "manga", limit = 20): Promise<KitsuMedia[]> {
  const res = await requestJson<{ data: KitsuMedia[] }>(`${BASE}/trending/${type}${qs({ limit })}`, {
    source: "kitsu",
    headers: HEADERS,
  });
  return res.data;
}

export async function get(type: "anime" | "manga", id: string): Promise<KitsuMedia> {
  const res = await requestJson<{ data: KitsuMedia }>(`${BASE}/${type}/${id}`, { source: "kitsu", headers: HEADERS });
  return res.data;
}

/** Resolves a Kitsu id from a MyAnimeList id. */
export async function idFromMal(type: "anime" | "manga", malId: string | number): Promise<string | null> {
  const res = await requestJson<{ data: { id: string }[]; included?: { id: string; type: string }[] }>(
    `${BASE}/mappings${qs({
      "filter[externalSite]": `myanimelist/${type}`,
      "filter[externalId]": malId,
      include: "item",
    })}`,
    { source: "kitsu", headers: HEADERS },
  );
  const item = res.included?.find((i) => i.type === type);
  return item?.id ?? null;
}

/** All episodes (paged, 20 per request, capped at 2000). */
export async function episodes(animeId: string, max = 2000): Promise<KitsuEpisode[]> {
  const out: KitsuEpisode[] = [];
  let offset = 0;
  const limit = 20;
  for (;;) {
    const res = await requestJson<{ data: KitsuEpisode[]; meta?: { count?: number } }>(
      `${BASE}/anime/${animeId}/episodes${qs({ "page[limit]": limit, "page[offset]": offset, sort: "number" })}`,
      { source: "kitsu", headers: HEADERS },
    );
    out.push(...res.data);
    const count = res.meta?.count ?? 0;
    offset += limit;
    if (res.data.length < limit || offset >= count || offset >= max) break;
  }
  return out;
}
