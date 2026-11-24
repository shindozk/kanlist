import type { JikanMedia } from "../sources/jikan";
import type { MediaKind, ReleaseStatus, Title } from "../../domain/types";
import { stripHtml } from "../../core/text";

export function kindFromJikan(type: "anime" | "manga", t: string | null): MediaKind {
  const s = (t ?? "").toLowerCase();
  if (type === "anime") {
    if (s === "movie") return "movie";
    if (s === "ova" || s === "special" || s === "music") return "ova";
    return "anime";
  }
  if (s === "manhwa") return "manhwa";
  if (s === "manhua") return "manhua";
  if (s.includes("novel")) return "novel";
  if (s === "one-shot") return "oneshot";
  if (s === "doujinshi") return "doujin";
  return "manga";
}

const STATUS: Record<string, ReleaseStatus> = {
  "Currently Airing": "releasing",
  Publishing: "releasing",
  "Finished Airing": "finished",
  Finished: "finished",
  "Not yet aired": "upcoming",
  "On Hiatus": "hiatus",
  Discontinued: "cancelled",
};

export function titleFromJikan(type: "anime" | "manga", m: JikanMedia): Title {
  const medium = type === "anime" ? "watch" : "read";
  const from = m.aired?.from ?? m.published?.from;
  return {
    key: `jikan:${type}-${m.mal_id}`,
    source: "jikan",
    sourceId: `${type}-${m.mal_id}`,
    kind: kindFromJikan(type, m.type),
    medium,
    title: m.title,
    titleAlt: m.title_english ?? undefined,
    titleNative: m.title_japanese ?? undefined,
    cover: m.images?.jpg?.large_image_url ?? m.images?.jpg?.image_url,
    synopsis: stripHtml(m.synopsis),
    synopsisLang: "en",
    genres: m.genres.map((g) => g.name),
    year: m.year ?? (from ? Number(from.slice(0, 4)) : undefined),
    status: STATUS[m.status ?? ""] ?? "unknown",
    totalUnits: (medium === "watch" ? m.episodes : m.chapters) ?? undefined,
    totalVolumes: m.volumes ?? undefined,
    score: m.score ? Math.round(m.score * 10) : undefined,
    popularity: m.members ?? undefined,
    externalIds: { mal: String(m.mal_id) },
  };
}
