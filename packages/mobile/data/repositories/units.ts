import { cached, TTL } from "../../core/cache";
import { unitKey } from "../../core/text";
import * as kitsu from "../sources/kitsu";
import * as jikan from "../sources/jikan";
import * as mangadex from "../sources/mangadex";
import type { SourceName, Title, TitleDetail, Unit, UnitList } from "../../domain/types";
import type { SourceToggles } from "./catalog";

/**
 * Builds the episode / chapter list for a title by merging every source that knows
 * something about it. Each source is best-effort: a failure just drops its metadata.
 */

export interface UnitOptions {
  sources: SourceToggles;
  chapterLangs: string[];
  streaming?: TitleDetail["streaming"];
}

const MAX_GENERATED = 3000;

function generated(n: number, released?: number): Unit[] {
  const count = Math.min(n, MAX_GENERATED);
  return Array.from({ length: count }, (_, i) => ({
    number: i + 1,
    key: String(i + 1),
    upcoming: released !== undefined ? i + 1 > released : undefined,
  }));
}

// ---------------------------------------------------------------- episodes

async function episodes(t: Title, opts: UnitOptions): Promise<UnitList> {
  const sources: SourceName[] = [];
  const byNumber = new Map<number, Unit>();
  const upsert = (n: number, patch: Partial<Unit>) => {
    if (!Number.isFinite(n) || n <= 0) return;
    const cur = byNumber.get(n) ?? { number: n, key: unitKey(n) };
    byNumber.set(n, { ...cur, ...Object.fromEntries(Object.entries(patch).filter(([, v]) => v !== undefined && v !== null && v !== "")) });
  };

  if (t.kind === "movie") {
    return { medium: "watch", units: [{ number: 1, key: "1", title: "Filme completo" }], sources: [t.source], estimated: false, total: 1 };
  }

  const mal = t.externalIds.mal;
  const tasks: Promise<void>[] = [];

  if (mal && opts.sources.kitsu) {
    tasks.push(
      (async () => {
        const kid =
          t.externalIds.kitsu ??
          (await cached(`kitsu-map:anime:${mal}`, { ttlMs: TTL.mapping, persist: true }, () => kitsu.idFromMal("anime", mal)));
        if (!kid) return;
        const eps = await kitsu.episodes(kid, Math.max(40, (t.totalUnits ?? t.releasedUnits ?? 100) + 20));
        for (const e of eps) {
          const a = e.attributes;
          if (a.number === null) continue;
          const title = a.canonicalTitle || a.titles?.en_us || a.titles?.en || undefined;
          upsert(a.number, {
            title: title && !/^episode \d+$/i.test(title) ? title : undefined,
            thumbnail: a.thumbnail?.original,
            airedAt: a.airdate ?? undefined,
          });
        }
        if (eps.length) sources.push("kitsu");
      })().catch(() => {}),
    );
  }

  if (mal && opts.sources.jikan) {
    tasks.push(
      jikan
        .episodes(Number(mal), 4)
        .then((eps) => {
          for (const e of eps) {
            const existing = byNumber.get(e.mal_id);
            upsert(e.mal_id, {
              title: existing?.title ?? e.title ?? undefined,
              airedAt: existing?.airedAt ?? e.aired?.slice(0, 10) ?? undefined,
              filler: e.filler || undefined,
              recap: e.recap || undefined,
            });
          }
          if (eps.length) sources.push("jikan");
        })
        .catch(() => {}),
    );
  }

  await Promise.all(tasks);

  // AniList streaming episodes carry nice thumbnails ("Episode 3 - Title").
  const streaming = opts.streaming ?? [];
  for (const s of streaming) {
    const m = s.title?.match(/episode\s+(\d+(?:\.\d+)?)\s*[-–:]?\s*(.*)$/i);
    if (!m) continue;
    const n = Number(m[1]);
    const cur = byNumber.get(n);
    upsert(n, { title: cur?.title ?? (m[2] || undefined), thumbnail: s.thumbnail ?? cur?.thumbnail });
  }
  if (streaming.length) sources.unshift("anilist");

  const released = t.releasedUnits ?? (t.status === "finished" ? t.totalUnits : undefined);
  const known = Math.max(0, ...byNumber.keys());
  const total = t.totalUnits ?? (t.status === "releasing" ? undefined : known || undefined);
  const upTo = Math.max(total ?? 0, released ?? 0, known);

  if (!upTo) return { medium: "watch", units: [], sources, estimated: true, total };

  const base = generated(upTo, t.status === "releasing" ? released ?? known : undefined);
  const units = base.map((u) => ({ ...u, ...byNumber.get(u.number), upcoming: u.upcoming }));
  return { medium: "watch", units, sources: sources.length ? sources : [t.source], estimated: byNumber.size === 0, total };
}

// ---------------------------------------------------------------- chapters

async function resolveMangaDexId(t: Title): Promise<string | null> {
  if (t.externalIds.mangadex) return t.externalIds.mangadex;
  if (!t.externalIds.anilist) return null;
  return cached(`md-map:${t.externalIds.anilist}`, { ttlMs: TTL.mapping, persist: true }, async () => {
    const m = await mangadex.findByAnilist(t.externalIds.anilist!, [t.title, t.titleAlt ?? ""]);
    return m?.id ?? null;
  });
}

async function chapters(t: Title, opts: UnitOptions): Promise<UnitList> {
  const total = t.totalUnits;
  if (opts.sources.mangadex) {
    try {
      const id = await resolveMangaDexId(t);
      if (id) {
        let agg = await mangadex.aggregate(id, opts.chapterLangs);
        let langs = opts.chapterLangs;
        if (!Object.keys(agg.volumes ?? {}).length) {
          agg = await mangadex.aggregate(id, []);
          langs = [];
        }
        const byNumber = new Map<number, Unit>();
        for (const vol of Object.values(agg.volumes ?? {})) {
          for (const ch of Object.values(vol.chapters)) {
            const n = Number(ch.chapter);
            if (!Number.isFinite(n) || n < 0) continue;
            byNumber.set(n, { number: n, key: unitKey(n), volume: vol.volume !== "none" ? vol.volume : undefined });
          }
        }
        // Titles + dates + languages from the feed (best-effort).
        if (byNumber.size && byNumber.size <= 1500) {
          try {
            const feed = await mangadex.feed(id, langs.length ? langs : ["en", "pt-br", "es-la", "es"], 1500);
            for (const c of feed) {
              const n = Number(c.attributes.chapter);
              const u = byNumber.get(n);
              if (!u) continue;
              u.title = u.title ?? (c.attributes.title || undefined);
              u.airedAt = u.airedAt ?? c.attributes.publishAt?.slice(0, 10);
              u.langs = [...new Set([...(u.langs ?? []), c.attributes.translatedLanguage])];
            }
          } catch {
            // aggregate alone is enough
          }
        }
        const fromDex = [...byNumber.values()].sort((a, b) => a.number - b.number);
        // Fill the gaps up to the official total so progress is never capped by scanlation coverage.
        if (total && total > (fromDex.at(-1)?.number ?? 0)) {
          const have = new Set(fromDex.map((u) => u.key));
          for (const u of generated(total)) if (!have.has(u.key)) fromDex.push(u);
          fromDex.sort((a, b) => a.number - b.number);
        }
        if (fromDex.length) {
          return {
            medium: "read",
            units: fromDex,
            sources: ["mangadex"],
            estimated: false,
            total: total ?? fromDex.at(-1)?.number,
          };
        }
      }
    } catch {
      // fall through to generated list
    }
  }
  if (t.kind === "oneshot") return { medium: "read", units: generated(1), sources: [t.source], estimated: true, total: 1 };
  return { medium: "read", units: total ? generated(total) : [], sources: [t.source], estimated: true, total };
}

export async function units(t: Title, opts: UnitOptions): Promise<UnitList> {
  const key = `units:${t.key}:${t.medium === "read" ? opts.chapterLangs.join(",") : ""}:${Object.values(opts.sources).join("")}`;
  return cached(key, { ttlMs: TTL.units, persist: true }, () => (t.medium === "watch" ? episodes(t, opts) : chapters(t, opts)));
}
