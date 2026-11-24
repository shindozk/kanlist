import { qs, requestJson } from "../../core/http";

/**
 * Jikan (unofficial MyAnimeList API) — fallback for episode lists (filler/recap flags)
 * and for search when AniList and Kitsu fail. Frequently returns 504 when MAL is down,
 * so callers must treat it as best-effort.
 */
const BASE = "https://api.jikan.moe/v4";

export interface JikanEpisode {
  mal_id: number;
  title: string | null;
  title_romanji: string | null;
  aired: string | null;
  filler: boolean;
  recap: boolean;
}

export interface JikanMedia {
  mal_id: number;
  title: string;
  title_english: string | null;
  title_japanese: string | null;
  type: string | null;
  episodes?: number | null;
  chapters?: number | null;
  volumes?: number | null;
  status: string | null;
  synopsis: string | null;
  score: number | null;
  members: number | null;
  year?: number | null;
  genres: { name: string }[];
  images: { jpg: { image_url?: string; large_image_url?: string } };
  published?: { from: string | null };
  aired?: { from: string | null };
}

export async function episodes(malId: number, maxPages = 10): Promise<JikanEpisode[]> {
  const out: JikanEpisode[] = [];
  for (let page = 1; page <= maxPages; page++) {
    const res = await requestJson<{ data: JikanEpisode[]; pagination: { has_next_page: boolean } }>(
      `${BASE}/anime/${malId}/episodes${qs({ page })}`,
      { source: "jikan", retries: 1, timeoutMs: 10_000 },
    );
    out.push(...res.data);
    if (!res.pagination.has_next_page) break;
  }
  return out;
}

export async function search(type: "anime" | "manga", q: string, limit = 12): Promise<JikanMedia[]> {
  const res = await requestJson<{ data: JikanMedia[] }>(`${BASE}/${type}${qs({ q, limit, sfw: true })}`, {
    source: "jikan",
    retries: 1,
    timeoutMs: 10_000,
  });
  return res.data;
}
