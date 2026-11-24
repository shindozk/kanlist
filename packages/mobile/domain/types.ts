/**
 * Kanlist domain model. Pure types — no React, no network, no storage.
 * Every data source maps into these shapes (see data/mappers).
 */

export const MEDIA_KINDS = [
  "anime",
  "movie",
  "ova",
  "donghua",
  "manga",
  "manhwa",
  "manhua",
  "novel",
  "oneshot",
  "doujin",
] as const;
export type MediaKind = (typeof MEDIA_KINDS)[number];

/** Watch = episodes, Read = chapters. */
export type Medium = "watch" | "read";

export type SourceName = "anilist" | "mangadex" | "kitsu" | "jikan" | "local";

export type ReleaseStatus = "releasing" | "finished" | "upcoming" | "hiatus" | "cancelled" | "unknown";

export type TrackingStatus = "current" | "planning" | "completed" | "paused" | "dropped" | "repeating";

export const TRACKING_STATUSES: TrackingStatus[] = [
  "current",
  "planning",
  "completed",
  "paused",
  "dropped",
  "repeating",
];

export interface ExternalIds {
  anilist?: string;
  mal?: string;
  kitsu?: string;
  mangadex?: string;
}

/** Normalized catalog item. `key` = `${source}:${sourceId}` and is the app-wide id. */
export interface Title {
  key: string;
  source: SourceName;
  sourceId: string;
  kind: MediaKind;
  medium: Medium;
  title: string;
  titleAlt?: string;
  titleNative?: string;
  cover?: string;
  banner?: string;
  color?: string;
  synopsis?: string;
  synopsisLang?: "pt" | "en";
  genres: string[];
  year?: number;
  season?: string;
  status: ReleaseStatus;
  /** Episodes or chapters, when known. */
  totalUnits?: number;
  totalVolumes?: number;
  /** Already released units (airing anime). */
  releasedUnits?: number;
  /** Minutes per unit. */
  unitDuration?: number;
  /** 0–100 */
  score?: number;
  popularity?: number;
  studios?: string[];
  authors?: string[];
  format?: string;
  country?: string;
  isAdult?: boolean;
  nextAiring?: { unit: number; at: number };
  externalIds: ExternalIds;
}

export interface Character {
  name: string;
  image?: string;
  role?: string;
}

export interface RelatedTitle {
  relation: string;
  title: Title;
}

export interface TitleDetail extends Title {
  relations: RelatedTitle[];
  recommendations: Title[];
  characters: Character[];
  links: { site: string; url: string }[];
  tags: string[];
  startDate?: string;
  endDate?: string;
  /** Which sources contributed to this record. */
  sources: SourceName[];
  /** Per-episode thumbnails/titles from streaming partners (AniList). */
  streaming?: { title?: string; thumbnail?: string }[];
}

/** One episode or chapter. */
export interface Unit {
  number: number;
  key: string;
  title?: string;
  thumbnail?: string;
  airedAt?: string;
  volume?: string;
  filler?: boolean;
  recap?: boolean;
  langs?: string[];
  upcoming?: boolean;
}

export interface UnitList {
  medium: Medium;
  units: Unit[];
  /** Data sources used for titles / numbers. */
  sources: SourceName[];
  /** True when the list was generated from a count (no per-unit metadata). */
  estimated: boolean;
  /** Total known (may be larger than units when unreleased). */
  total?: number;
}

export interface LibraryEntry {
  key: string;
  title: Title;
  status: TrackingStatus;
  /** unit key -> timestamp watched/read */
  done: Record<string, number>;
  volumes: number;
  rating?: number; // 0–10
  notes: string;
  favorite: boolean;
  repeatCount: number;
  /** User-defined total when sources don't know it. */
  customTotal?: number;
  addedAt: number;
  updatedAt: number;
  startedAt?: number;
  finishedAt?: number;
}

export interface ActivityEvent {
  id: string;
  /** Library entry key */
  key: string;
  kind: MediaKind;
  /** First unit key affected (bulk events cover `count` units). */
  unit: string;
  count: number;
  at: number;
  minutes: number;
}

export interface Collection {
  id: string;
  name: string;
  description: string;
  tile: string;
  keys: string[];
  createdAt: number;
  updatedAt: number;
}

export type ThemeMode = "system" | "light" | "dark";
export type TitleLanguage = "romaji" | "english" | "native";

export interface Settings {
  name: string;
  onboarded: boolean;
  theme: ThemeMode;
  dailyGoal: number;
  titleLanguage: TitleLanguage;
  chapterLangs: string[];
  showAdult: boolean;
  autoTranslate: boolean;
  haptics: boolean;
  interests: MediaKind[];
  sources: { kitsu: boolean; jikan: boolean; mangadex: boolean; translate: boolean };
}

export type KindFilter = MediaKind | "all";
