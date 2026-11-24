import type { KitsuMedia } from "../sources/kitsu";
import type { MediaKind, ReleaseStatus, Title } from "../../domain/types";
import { stripHtml } from "../../core/text";

export function kindFromKitsu(m: KitsuMedia): MediaKind {
  const st = m.attributes.subtype ?? "";
  if (m.type === "anime") {
    if (st === "movie") return "movie";
    if (st === "OVA" || st === "special" || st === "music") return "ova";
    return "anime";
  }
  if (st === "manhwa") return "manhwa";
  if (st === "manhua") return "manhua";
  if (st === "novel") return "novel";
  if (st === "oneshot") return "oneshot";
  if (st === "doujin") return "doujin";
  return "manga";
}

const STATUS: Record<string, ReleaseStatus> = {
  current: "releasing",
  finished: "finished",
  upcoming: "upcoming",
  unreleased: "upcoming",
  tba: "upcoming",
};

export function titleFromKitsu(m: KitsuMedia): Title {
  const a = m.attributes;
  const kind = kindFromKitsu(m);
  const medium = m.type === "anime" ? "watch" : "read";
  const rating = a.averageRating ? Number(a.averageRating) : undefined;
  return {
    key: `kitsu:${m.type}-${m.id}`,
    source: "kitsu",
    sourceId: `${m.type}-${m.id}`,
    kind,
    medium,
    title: a.titles?.en_jp || a.canonicalTitle,
    titleAlt: a.titles?.en && a.titles.en !== a.canonicalTitle ? a.titles.en : undefined,
    titleNative: a.titles?.ja_jp,
    cover: a.posterImage?.large ?? a.posterImage?.medium ?? a.posterImage?.original,
    banner: a.coverImage?.large ?? a.coverImage?.original,
    synopsis: stripHtml(a.synopsis),
    synopsisLang: "en",
    genres: [],
    year: a.startDate ? Number(a.startDate.slice(0, 4)) : undefined,
    status: STATUS[a.status ?? ""] ?? "unknown",
    totalUnits: (medium === "watch" ? a.episodeCount : a.chapterCount) ?? undefined,
    totalVolumes: a.volumeCount ?? undefined,
    unitDuration: a.episodeLength ?? undefined,
    score: rating && Number.isFinite(rating) ? Math.round(rating) : undefined,
    popularity: a.userCount ?? undefined,
    isAdult: !!a.nsfw,
    externalIds: { kitsu: m.id },
  };
}
