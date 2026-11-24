import type { ALDetail, ALMedia } from "../sources/anilist";
import { KIND_META } from "../../domain/kinds";
import type { MediaKind, ReleaseStatus, Title, TitleDetail } from "../../domain/types";
import { stripHtml } from "../../core/text";

export function kindFromAniList(m: Pick<ALMedia, "type" | "format" | "countryOfOrigin">): MediaKind {
  const country = m.countryOfOrigin ?? "JP";
  if (m.type === "ANIME") {
    if (m.format === "MOVIE") return "movie";
    if (m.format === "OVA" || m.format === "SPECIAL" || m.format === "MUSIC") return "ova";
    if (country === "CN" || country === "TW") return "donghua";
    return "anime";
  }
  if (m.format === "NOVEL") return "novel";
  if (m.format === "ONE_SHOT") return "oneshot";
  if (country === "KR") return "manhwa";
  if (country === "CN" || country === "TW") return "manhua";
  return "manga";
}

export function releaseFromAniList(s: string | null): ReleaseStatus {
  switch (s) {
    case "RELEASING":
      return "releasing";
    case "FINISHED":
      return "finished";
    case "NOT_YET_RELEASED":
      return "upcoming";
    case "HIATUS":
      return "hiatus";
    case "CANCELLED":
      return "cancelled";
    default:
      return "unknown";
  }
}

const SEASON_PT: Record<string, string> = { WINTER: "Inverno", SPRING: "Primavera", SUMMER: "Verão", FALL: "Outono" };

const dateStr = (d: ALMedia["startDate"]) =>
  d?.year ? [d.year, d.month, d.day].filter(Boolean).map((n) => String(n).padStart(2, "0")).join("-") : undefined;

export function titleFromAniList(m: ALMedia): Title {
  const kind = kindFromAniList(m);
  const medium = KIND_META[kind].medium;
  const total = medium === "watch" ? m.episodes : m.chapters;
  const released =
    medium === "watch" && m.nextAiringEpisode ? Math.max(0, m.nextAiringEpisode.episode - 1) : total ?? undefined;
  return {
    key: `anilist:${m.id}`,
    source: "anilist",
    sourceId: String(m.id),
    kind,
    medium,
    title: m.title.romaji || m.title.english || m.title.native || "Sem título",
    titleAlt: m.title.english && m.title.english !== m.title.romaji ? m.title.english : undefined,
    titleNative: m.title.native ?? undefined,
    cover: m.coverImage?.extraLarge ?? m.coverImage?.large ?? undefined,
    banner: m.bannerImage ?? undefined,
    color: m.coverImage?.color ?? undefined,
    synopsis: stripHtml(m.description),
    synopsisLang: "en",
    genres: m.genres ?? [],
    year: m.seasonYear ?? m.startDate?.year ?? undefined,
    season: m.season ? SEASON_PT[m.season] : undefined,
    status: releaseFromAniList(m.status),
    totalUnits: total ?? undefined,
    totalVolumes: m.volumes ?? undefined,
    releasedUnits: released ?? undefined,
    unitDuration: m.duration ?? undefined,
    score: m.averageScore ?? m.meanScore ?? undefined,
    popularity: m.popularity ?? undefined,
    studios: m.studios?.nodes.map((s) => s.name) ?? [],
    format: m.format ?? undefined,
    country: m.countryOfOrigin ?? undefined,
    isAdult: m.isAdult,
    nextAiring: m.nextAiringEpisode
      ? { unit: m.nextAiringEpisode.episode, at: m.nextAiringEpisode.airingAt * 1000 }
      : undefined,
    externalIds: { anilist: String(m.id), mal: m.idMal ? String(m.idMal) : undefined },
  };
}

const RELATION_PT: Record<string, string> = {
  ADAPTATION: "Adaptação",
  PREQUEL: "Prequel",
  SEQUEL: "Sequência",
  PARENT: "Principal",
  SIDE_STORY: "História paralela",
  CHARACTER: "Personagem",
  SUMMARY: "Resumo",
  ALTERNATIVE: "Alternativo",
  SPIN_OFF: "Spin-off",
  OTHER: "Outro",
  SOURCE: "Original",
  COMPILATION: "Compilação",
  CONTAINS: "Contém",
};

const ROLE_PT: Record<string, string> = { MAIN: "Principal", SUPPORTING: "Coadjuvante", BACKGROUND: "Figurante" };

export function detailFromAniList(d: ALDetail): TitleDetail {
  const base = titleFromAniList(d);
  const authors = (d.staff?.edges ?? [])
    .filter((e) => /story|art|original/i.test(e.role))
    .map((e) => e.node.name.full)
    .filter((n): n is string => !!n);
  return {
    ...base,
    authors: [...new Set(authors)].slice(0, 4),
    relations: (d.relations?.edges ?? [])
      .filter((e) => e.relationType !== "CHARACTER")
      .map((e) => ({ relation: RELATION_PT[e.relationType] ?? e.relationType, title: titleFromAniList(e.node) })),
    recommendations: (d.recommendations?.nodes ?? [])
      .map((n) => n.mediaRecommendation)
      .filter((m): m is ALMedia => !!m)
      .map(titleFromAniList),
    characters: (d.characters?.edges ?? []).map((e) => ({
      name: e.node.name.full ?? "?",
      image: e.node.image?.medium ?? undefined,
      role: ROLE_PT[e.role] ?? e.role,
    })),
    links: (d.externalLinks ?? []).map((l) => ({ site: l.site, url: l.url })),
    tags: (d.tags ?? []).filter((t) => !t.isMediaSpoiler && t.rank >= 60).map((t) => t.name).slice(0, 10),
    startDate: dateStr(d.startDate),
    endDate: dateStr(d.endDate),
    sources: ["anilist"],
    streaming: (d.streamingEpisodes ?? []).map((e) => ({ title: e.title ?? undefined, thumbnail: e.thumbnail ?? undefined })),
  };
}
