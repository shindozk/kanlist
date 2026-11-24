import { useEffect } from "react";
import { keepPreviousData, useInfiniteQuery, useQuery } from "@tanstack/react-query";
import * as catalog from "../data/repositories/catalog";
import { units } from "../data/repositories/units";
import { translateSynopsis } from "../data/repositories/translate";
import type { KindFilter, Title, TitleDetail } from "../domain/types";
import { useLibrary } from "../state/library";

/** Data hooks for remote catalog data. Screens never call repositories directly. */

export function useSearch(query: string, filter: KindFilter) {
  const settings = useLibrary((s) => s.settings);
  const q = query.trim();
  return useQuery({
    queryKey: ["search", q, filter, settings.showAdult, settings.sources],
    queryFn: () => catalog.search(q, { filter, adult: settings.showAdult, sources: settings.sources }),
    enabled: q.length >= 2,
    placeholderData: keepPreviousData,
    staleTime: 5 * 60_000,
  });
}

export function useBrowse(presetId: string, enabled = true) {
  const adult = useLibrary((s) => s.settings.showAdult);
  return useInfiniteQuery({
    queryKey: ["browse", presetId, adult],
    queryFn: ({ pageParam }) => catalog.browse(presetId, pageParam, adult),
    initialPageParam: 1,
    getNextPageParam: (last, all) => (last.hasNext ? all.length + 1 : undefined),
    enabled,
    staleTime: 20 * 60_000,
  });
}

export function useTitleDetail(key: string | undefined) {
  const refreshTitle = useLibrary((s) => s.refreshTitle);
  const q = useQuery({
    queryKey: ["detail", key],
    queryFn: () => catalog.detail(key!),
    enabled: !!key,
    staleTime: 60 * 60_000,
  });
  // Keep library snapshots fresh (totals, next airing, covers).
  useEffect(() => {
    if (q.data) {
      const { relations: _r, recommendations: _rec, characters: _c, links: _l, tags: _t, sources: _s, streaming: _st, ...title } = q.data;
      refreshTitle(title as Title);
    }
  }, [q.data, refreshTitle]);
  return q;
}

export function useUnits(title: Title | undefined, detail?: TitleDetail) {
  const sources = useLibrary((s) => s.settings.sources);
  const chapterLangs = useLibrary((s) => s.settings.chapterLangs);
  return useQuery({
    queryKey: ["units", title?.key, title?.totalUnits, title?.releasedUnits, chapterLangs, sources, !!detail],
    queryFn: () => units(title!, { sources, chapterLangs, streaming: detail?.streaming }),
    enabled: !!title,
    staleTime: 30 * 60_000,
  });
}

export function useAiring(fromMs: number, toMs: number, anilistIds: number[]) {
  return useQuery({
    queryKey: ["airing", fromMs, toMs, anilistIds.join(",")],
    queryFn: () => catalog.airingBetween(fromMs, toMs, anilistIds),
    enabled: anilistIds.length > 0,
    staleTime: 15 * 60_000,
  });
}

export function useTranslation(key: string | undefined, text: string | undefined, enabled: boolean) {
  return useQuery({
    queryKey: ["translate", key, text?.length],
    queryFn: () => translateSynopsis(key!, text!),
    enabled: enabled && !!key && !!text,
    staleTime: Infinity,
    retry: 1,
  });
}
