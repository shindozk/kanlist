import { coverUrl, TAG_DOUJINSHI, TAG_ONESHOT, type MDManga } from "../sources/mangadex";
import type { MediaKind, ReleaseStatus, Title, TitleDetail } from "../../domain/types";
import { stripHtml } from "../../core/text";

export function kindFromMangaDex(m: MDManga): MediaKind {
  const tags = m.attributes.tags.map((t) => t.id);
  if (tags.includes(TAG_DOUJINSHI)) return "doujin";
  if (tags.includes(TAG_ONESHOT)) return "oneshot";
  const lang = m.attributes.originalLanguage;
  if (lang === "ko") return "manhwa";
  if (lang === "zh" || lang === "zh-hk") return "manhua";
  return "manga";
}

const STATUS: Record<string, ReleaseStatus> = {
  ongoing: "releasing",
  completed: "finished",
  hiatus: "hiatus",
  cancelled: "cancelled",
};

const pick = (rec: Record<string, string> | undefined, ...langs: string[]) => {
  if (!rec) return undefined;
  for (const l of langs) if (rec[l]) return rec[l];
  return Object.values(rec)[0];
};

export function titleFromMangaDex(m: MDManga): Title {
  const a = m.attributes;
  const alt = a.altTitles ?? [];
  const altEn = alt.find((t) => t.en)?.en;
  const native = alt.find((t) => t["ja"] || t["ko"] || t["zh"]);
  const main = pick(a.title, "en", "ja-ro", "ko-ro", "zh-ro") ?? "Sem título";
  const lastCh = a.lastChapter ? Number(a.lastChapter) : undefined;
  const ptDesc = a.description?.["pt-br"];
  return {
    key: `mangadex:${m.id}`,
    source: "mangadex",
    sourceId: m.id,
    kind: kindFromMangaDex(m),
    medium: "read",
    title: main,
    titleAlt: altEn && altEn !== main ? altEn : undefined,
    titleNative: native ? Object.values(native)[0] : undefined,
    cover: coverUrl(m),
    synopsis: stripHtml(ptDesc ?? pick(a.description, "en")),
    synopsisLang: ptDesc ? "pt" : "en",
    genres: a.tags.filter((t) => t.attributes.group === "genre").map((t) => t.attributes.name.en ?? ""),
    year: a.year ?? undefined,
    status: STATUS[a.status ?? ""] ?? "unknown",
    totalUnits: a.status === "completed" && lastCh && Number.isFinite(lastCh) ? lastCh : undefined,
    totalVolumes: a.lastVolume ? Number(a.lastVolume) || undefined : undefined,
    authors: m.relationships.filter((r) => r.type === "author" || r.type === "artist").map((r) => r.attributes?.name ?? "").filter(Boolean),
    country: a.originalLanguage?.toUpperCase(),
    isAdult: a.contentRating === "erotica" || a.contentRating === "pornographic",
    externalIds: {
      mangadex: m.id,
      anilist: a.links?.al,
      mal: a.links?.mal,
      kitsu: a.links?.kt,
    },
  };
}

export function detailFromMangaDex(m: MDManga): TitleDetail {
  const t = titleFromMangaDex(m);
  const links: { site: string; url: string }[] = [{ site: "MangaDex", url: `https://mangadex.org/title/${m.id}` }];
  const l = m.attributes.links ?? {};
  if (l.al) links.push({ site: "AniList", url: `https://anilist.co/manga/${l.al}` });
  if (l.mal) links.push({ site: "MyAnimeList", url: `https://myanimelist.net/manga/${l.mal}` });
  if (l.raw?.startsWith("http")) links.push({ site: "Original", url: l.raw });
  if (l.engtl?.startsWith("http")) links.push({ site: "Oficial (EN)", url: l.engtl });
  return {
    ...t,
    authors: [...new Set(t.authors)],
    relations: [],
    recommendations: [],
    characters: [],
    links,
    tags: m.attributes.tags.filter((x) => x.attributes.group !== "genre").map((x) => x.attributes.name.en ?? "").slice(0, 10),
    startDate: m.attributes.year ? String(m.attributes.year) : undefined,
    sources: ["mangadex"],
  };
}
