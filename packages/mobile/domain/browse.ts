import type { ComponentProps } from "react";
import type { Ionicons } from "@expo/vector-icons";
import type { TileName } from "../constants/theme";
import { currentSeason } from "../core/dates";
import type { BrowseParams } from "../data/sources/anilist";
import { genrePt } from "./kinds";

type IconName = ComponentProps<typeof Ionicons>["name"];

/** A discoverable list. AniList presets carry params; MangaDex presets are handled separately. */
export interface BrowsePreset {
  id: string;
  label: string;
  subtitle: string;
  icon: IconName;
  tile: TileName;
  source: "anilist" | "mangadex";
  params?: BrowseParams;
  mangadex?: { language?: string; doujin?: boolean };
}

const season = currentSeason();

export const PRESETS: BrowsePreset[] = [
  {
    id: "trending-anime",
    label: "Animes em alta",
    subtitle: "O que todo mundo está assistindo",
    icon: "flame-outline",
    tile: "peach",
    source: "anilist",
    params: { type: "ANIME", sort: ["TRENDING_DESC"], formatNotIn: ["MUSIC"] },
  },
  {
    id: "season",
    label: `Temporada de ${season.label}`,
    subtitle: `Lançamentos de ${season.label.toLowerCase()} ${season.year}`,
    icon: "leaf-outline",
    tile: "sage",
    source: "anilist",
    params: { type: "ANIME", season: season.season, seasonYear: season.year, sort: ["POPULARITY_DESC"] },
  },
  {
    id: "trending-manga",
    label: "Mangás em alta",
    subtitle: "Os mais lidos agora",
    icon: "book-outline",
    tile: "honey",
    source: "anilist",
    params: { type: "MANGA", country: "JP", sort: ["TRENDING_DESC"], formatNotIn: ["NOVEL"] },
  },
  {
    id: "manhwa",
    label: "Manhwas populares",
    subtitle: "Webtoons coreanos",
    icon: "phone-portrait-outline",
    tile: "sky",
    source: "anilist",
    params: { type: "MANGA", country: "KR", sort: ["TRENDING_DESC", "POPULARITY_DESC"] },
  },
  {
    id: "donghua",
    label: "Donghuas",
    subtitle: "Animações chinesas",
    icon: "flower-outline",
    tile: "rose",
    source: "anilist",
    params: { type: "ANIME", country: "CN", sort: ["TRENDING_DESC", "POPULARITY_DESC"] },
  },
  {
    id: "manhua",
    label: "Manhuas",
    subtitle: "Quadrinhos chineses",
    icon: "library-outline",
    tile: "lilac",
    source: "anilist",
    params: { type: "MANGA", country: "CN", sort: ["TRENDING_DESC", "POPULARITY_DESC"] },
  },
  {
    id: "movies",
    label: "Filmes de anime",
    subtitle: "Os melhores avaliados",
    icon: "film-outline",
    tile: "coral",
    source: "anilist",
    params: { type: "ANIME", formatIn: ["MOVIE"], sort: ["SCORE_DESC"] },
  },
  {
    id: "top-anime",
    label: "Animes mais bem avaliados",
    subtitle: "Clássicos e favoritos",
    icon: "trophy-outline",
    tile: "honey",
    source: "anilist",
    params: { type: "ANIME", sort: ["SCORE_DESC"], formatIn: ["TV", "ONA"] },
  },
  {
    id: "novels",
    label: "Light novels",
    subtitle: "Histórias em prosa",
    icon: "document-text-outline",
    tile: "sand",
    source: "anilist",
    params: { type: "MANGA", formatIn: ["NOVEL"], sort: ["POPULARITY_DESC"] },
  },
  {
    id: "oneshots",
    label: "One-shots",
    subtitle: "Histórias de um capítulo só",
    icon: "flash-outline",
    tile: "mint",
    source: "anilist",
    params: { type: "MANGA", formatIn: ["ONE_SHOT"], sort: ["POPULARITY_DESC"] },
  },
  {
    id: "doujin",
    label: "Doujinshis",
    subtitle: "Obras independentes (MangaDex)",
    icon: "brush-outline",
    tile: "blush",
    source: "mangadex",
    mangadex: { doujin: true },
  },
  {
    id: "mangadex-popular",
    label: "Mais seguidos no MangaDex",
    subtitle: "Comunidade de scanlation",
    icon: "people-outline",
    tile: "sky",
    source: "mangadex",
    mangadex: {},
  },
];

/** Presets shown as horizontal rails in Explore, in order. */
export const EXPLORE_RAILS = ["trending-anime", "season", "trending-manga", "manhwa", "donghua", "movies", "manhua", "doujin"];

export function genrePreset(genre: string, medium: "watch" | "read"): BrowsePreset {
  return {
    id: `genre:${medium}:${genre}`,
    label: genrePt(genre),
    subtitle: medium === "watch" ? "Animes do gênero" : "Mangás, manhwas e manhuas do gênero",
    icon: "pricetag-outline",
    tile: "peach",
    source: "anilist",
    params: { type: medium === "watch" ? "ANIME" : "MANGA", genre, sort: ["POPULARITY_DESC"] },
  };
}

export function presetById(id: string): BrowsePreset | undefined {
  if (id.startsWith("genre:")) {
    const [, medium, ...rest] = id.split(":");
    return genrePreset(rest.join(":"), medium === "read" ? "read" : "watch");
  }
  return PRESETS.find((p) => p.id === id);
}
