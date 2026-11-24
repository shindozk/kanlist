import { requestJson } from "../../core/http";
import { AppError } from "../../core/errors";

/**
 * AniList GraphQL client — primary catalog source for anime and manga
 * (incl. manhwa / manhua via countryOfOrigin, novels, one-shots).
 */
const ENDPOINT = "https://graphql.anilist.co";

export type ALType = "ANIME" | "MANGA";
export type ALSort =
  | "TRENDING_DESC"
  | "POPULARITY_DESC"
  | "SCORE_DESC"
  | "SEARCH_MATCH"
  | "START_DATE_DESC"
  | "UPDATED_AT_DESC";

export interface ALMedia {
  id: number;
  idMal: number | null;
  type: ALType;
  format: string | null;
  status: string | null;
  episodes: number | null;
  chapters: number | null;
  volumes: number | null;
  duration: number | null;
  countryOfOrigin: string | null;
  isAdult: boolean;
  title: { romaji: string | null; english: string | null; native: string | null };
  synonyms: string[] | null;
  coverImage: { large: string | null; extraLarge: string | null; color: string | null } | null;
  bannerImage: string | null;
  description: string | null;
  genres: string[] | null;
  season: string | null;
  seasonYear: number | null;
  startDate: { year: number | null; month: number | null; day: number | null } | null;
  endDate: { year: number | null; month: number | null; day: number | null } | null;
  averageScore: number | null;
  meanScore: number | null;
  popularity: number | null;
  studios: { nodes: { name: string }[] } | null;
  nextAiringEpisode: { episode: number; airingAt: number } | null;
}

export interface ALDetail extends ALMedia {
  relations: { edges: { relationType: string; node: ALMedia }[] } | null;
  recommendations: { nodes: { mediaRecommendation: ALMedia | null }[] } | null;
  characters: {
    edges: { role: string; node: { name: { full: string | null }; image: { medium: string | null } | null } }[];
  } | null;
  staff: { edges: { role: string; node: { name: { full: string | null } } }[] } | null;
  streamingEpisodes: { title: string | null; thumbnail: string | null; url: string | null; site: string | null }[] | null;
  externalLinks: { site: string; url: string }[] | null;
  tags: { name: string; rank: number; isMediaSpoiler: boolean }[] | null;
}

const MEDIA = `
  id idMal type format status episodes chapters volumes duration countryOfOrigin isAdult
  title { romaji english native } synonyms
  coverImage { large extraLarge color } bannerImage
  description(asHtml: false) genres season seasonYear
  startDate { year month day } endDate { year month day }
  averageScore meanScore popularity
  studios(isMain: true) { nodes { name } }
  nextAiringEpisode { episode airingAt }
`;

async function gql<T>(query: string, variables: Record<string, unknown>): Promise<T> {
  const res = await requestJson<{ data?: T; errors?: { message: string; status?: number }[] }>(ENDPOINT, {
    source: "anilist",
    method: "POST",
    body: { query, variables },
    timeoutMs: 12_000,
  });
  if (res.errors?.length && !res.data) {
    const status = res.errors[0].status;
    throw new AppError(status === 404 ? "not_found" : "http", res.errors[0].message, { status, source: "anilist" });
  }
  if (!res.data) throw new AppError("parse", "Empty AniList response", { source: "anilist" });
  return res.data;
}

export interface BrowseParams {
  type?: ALType;
  search?: string;
  sort?: ALSort[];
  season?: string;
  seasonYear?: number;
  genre?: string;
  country?: string;
  formatIn?: string[];
  formatNotIn?: string[];
  status?: string;
  isAdult?: boolean;
  page?: number;
  perPage?: number;
}

export interface ALPage {
  media: ALMedia[];
  hasNextPage: boolean;
  total: number;
}

export async function browse(p: BrowseParams): Promise<ALPage> {
  const query = `
    query ($page:Int,$perPage:Int,$type:MediaType,$search:String,$sort:[MediaSort],$season:MediaSeason,$seasonYear:Int,
           $genre:String,$country:CountryCode,$formatIn:[MediaFormat],$formatNotIn:[MediaFormat],$status:MediaStatus,$isAdult:Boolean) {
      Page(page:$page, perPage:$perPage) {
        pageInfo { hasNextPage total }
        media(type:$type, search:$search, sort:$sort, season:$season, seasonYear:$seasonYear, genre:$genre,
              countryOfOrigin:$country, format_in:$formatIn, format_not_in:$formatNotIn, status:$status, isAdult:$isAdult) { ${MEDIA} }
      }
    }`;
  const data = await gql<{ Page: { pageInfo: { hasNextPage: boolean; total: number }; media: ALMedia[] } }>(query, {
    page: p.page ?? 1,
    perPage: p.perPage ?? 20,
    type: p.type,
    search: p.search || undefined,
    sort: p.sort ?? (p.search ? ["SEARCH_MATCH"] : ["TRENDING_DESC"]),
    season: p.season,
    seasonYear: p.seasonYear,
    genre: p.genre,
    country: p.country,
    formatIn: p.formatIn,
    formatNotIn: p.formatNotIn,
    status: p.status,
    isAdult: p.isAdult,
  });
  return { media: data.Page.media, hasNextPage: data.Page.pageInfo.hasNextPage, total: data.Page.pageInfo.total };
}

export async function detail(id: number): Promise<ALDetail> {
  const query = `
    query ($id:Int) {
      Media(id:$id) {
        ${MEDIA}
        relations { edges { relationType node { ${MEDIA} } } }
        recommendations(perPage: 12, sort: RATING_DESC) { nodes { mediaRecommendation { ${MEDIA} } } }
        characters(perPage: 12, sort: [ROLE, RELEVANCE]) { edges { role node { name { full } image { medium } } } }
        staff(perPage: 8, sort: RELEVANCE) { edges { role node { name { full } } } }
        streamingEpisodes { title thumbnail url site }
        externalLinks { site url }
        tags { name rank isMediaSpoiler }
      }
    }`;
  const data = await gql<{ Media: ALDetail | null }>(query, { id });
  if (!data.Media) throw new AppError("not_found", "Media not found", { source: "anilist" });
  return data.Media;
}

export async function byMalId(idMal: number, type: ALType): Promise<ALMedia | null> {
  const query = `query ($idMal:Int,$type:MediaType) { Media(idMal:$idMal, type:$type) { ${MEDIA} } }`;
  try {
    const data = await gql<{ Media: ALMedia | null }>(query, { idMal, type });
    return data.Media;
  } catch (e) {
    if (e instanceof AppError && e.kind === "not_found") return null;
    throw e;
  }
}

export interface ALAiring {
  episode: number;
  airingAt: number;
  media: ALMedia;
}

/** Airing schedule between two unix-second timestamps, optionally restricted to ids. */
export async function airing(from: number, to: number, ids?: number[]): Promise<ALAiring[]> {
  const query = `
    query ($page:Int,$from:Int,$to:Int,$ids:[Int]) {
      Page(page:$page, perPage:50) {
        pageInfo { hasNextPage }
        airingSchedules(airingAt_greater:$from, airingAt_lesser:$to, mediaId_in:$ids, sort:TIME) {
          episode airingAt media { ${MEDIA} }
        }
      }
    }`;
  const out: ALAiring[] = [];
  for (let page = 1; page <= 4; page++) {
    const data = await gql<{ Page: { pageInfo: { hasNextPage: boolean }; airingSchedules: ALAiring[] } }>(query, {
      page,
      from,
      to,
      ids: ids?.length ? ids : undefined,
    });
    out.push(...data.Page.airingSchedules);
    if (!data.Page.pageInfo.hasNextPage) break;
  }
  return out;
}
