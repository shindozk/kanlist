import type { ComponentProps } from "react";
import type { Ionicons } from "@expo/vector-icons";
import type { TileName } from "../constants/theme";
import type { MediaKind, Medium, ReleaseStatus, TrackingStatus } from "./types";

type IconName = ComponentProps<typeof Ionicons>["name"];

export interface KindMeta {
  label: string;
  plural: string;
  medium: Medium;
  icon: IconName;
  tile: TileName;
  /** Default minutes per unit when the source doesn't say. */
  minutes: number;
}

export const KIND_META: Record<MediaKind, KindMeta> = {
  anime: { label: "Anime", plural: "Animes", medium: "watch", icon: "tv-outline", tile: "peach", minutes: 24 },
  movie: { label: "Filme", plural: "Filmes", medium: "watch", icon: "film-outline", tile: "honey", minutes: 105 },
  ova: { label: "OVA/Especial", plural: "OVAs", medium: "watch", icon: "sparkles-outline", tile: "coral", minutes: 25 },
  donghua: { label: "Donghua", plural: "Donghuas", medium: "watch", icon: "flower-outline", tile: "rose", minutes: 20 },
  manga: { label: "Mangá", plural: "Mangás", medium: "read", icon: "book-outline", tile: "sage", minutes: 6 },
  manhwa: { label: "Manhwa", plural: "Manhwas", medium: "read", icon: "phone-portrait-outline", tile: "sky", minutes: 5 },
  manhua: { label: "Manhua", plural: "Manhuas", medium: "read", icon: "library-outline", tile: "lilac", minutes: 5 },
  novel: { label: "Novel", plural: "Novels", medium: "read", icon: "document-text-outline", tile: "sand", minutes: 20 },
  oneshot: { label: "One-shot", plural: "One-shots", medium: "read", icon: "flash-outline", tile: "mint", minutes: 15 },
  doujin: { label: "Doujin", plural: "Doujins", medium: "read", icon: "brush-outline", tile: "blush", minutes: 8 },
};

export const unitWord = (medium: Medium, plural = false) =>
  medium === "watch" ? (plural ? "episódios" : "episódio") : plural ? "capítulos" : "capítulo";
export const unitShort = (medium: Medium) => (medium === "watch" ? "Ep" : "Cap");

export function statusLabel(status: TrackingStatus, medium: Medium): string {
  const w = medium === "watch";
  switch (status) {
    case "current":
      return w ? "Assistindo" : "Lendo";
    case "planning":
      return w ? "Quero assistir" : "Quero ler";
    case "completed":
      return "Completo";
    case "paused":
      return "Pausado";
    case "dropped":
      return "Abandonado";
    case "repeating":
      return w ? "Reassistindo" : "Relendo";
  }
}

export const STATUS_ICON: Record<TrackingStatus, IconName> = {
  current: "play-circle",
  planning: "bookmark",
  completed: "checkmark-circle",
  paused: "pause-circle",
  dropped: "close-circle",
  repeating: "repeat",
};

export const RELEASE_LABEL: Record<ReleaseStatus, string> = {
  releasing: "Em lançamento",
  finished: "Finalizado",
  upcoming: "Em breve",
  hiatus: "Em hiato",
  cancelled: "Cancelado",
  unknown: "—",
};

const GENRES_PT: Record<string, string> = {
  Action: "Ação",
  Adventure: "Aventura",
  Comedy: "Comédia",
  Drama: "Drama",
  Ecchi: "Ecchi",
  Fantasy: "Fantasia",
  Horror: "Terror",
  "Mahou Shoujo": "Garota mágica",
  "Magical Girls": "Garota mágica",
  Mecha: "Mecha",
  Music: "Música",
  Mystery: "Mistério",
  Psychological: "Psicológico",
  Romance: "Romance",
  "Sci-Fi": "Ficção científica",
  "Slice of Life": "Cotidiano",
  Sports: "Esportes",
  Supernatural: "Sobrenatural",
  Thriller: "Suspense",
  Historical: "Histórico",
  "Isekai": "Isekai",
  "Martial Arts": "Artes marciais",
  "School Life": "Vida escolar",
  School: "Escolar",
  Crime: "Crime",
  Tragedy: "Tragédia",
  Medical: "Médico",
  Military: "Militar",
  Philosophical: "Filosófico",
  Superhero: "Super-herói",
  Wuxia: "Wuxia",
  Survival: "Sobrevivência",
  Cooking: "Culinária",
  Gourmet: "Culinária",
  "Award Winning": "Premiado",
  Suspense: "Suspense",
  "Boys' Love": "BL",
  "Girls' Love": "GL",
};

export const genrePt = (g: string) => GENRES_PT[g] ?? g;

/** Genres offered in Explore (AniList canonical names). */
export const EXPLORE_GENRES = [
  "Action",
  "Romance",
  "Fantasy",
  "Comedy",
  "Slice of Life",
  "Drama",
  "Mystery",
  "Sci-Fi",
  "Sports",
  "Psychological",
  "Supernatural",
  "Horror",
  "Adventure",
  "Mecha",
  "Music",
  "Thriller",
];
