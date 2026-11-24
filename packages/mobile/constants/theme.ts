import { Platform } from "react-native";

/**
 * Kanlist color tokens (see design.md at the app root).
 * Every screen reads these through `useColors()`, so editing a value restyles the app.
 */
export interface Palette {
  background: string;
  foreground: string;
  card: string;
  cardForeground: string;
  primary: string;
  primaryForeground: string;
  secondary: string;
  secondaryForeground: string;
  muted: string;
  mutedForeground: string;
  accent: string;
  accentForeground: string;
  border: string;
  destructive: string;
  success: string;
  warning: string;
  /** Selected day / dark pills */
  ink: string;
  inkForeground: string;
  /** Hero button + FAB */
  brown: string;
  brownForeground: string;
  /** Highlight card (peach) */
  hero: string;
  heroForeground: string;
  heroMuted: string;
  shadow: string;
  overlay: string;
  track: string;
}

export const Colors: { light: Palette; dark: Palette } = {
  light: {
    background: "#F6F1EE",
    foreground: "#1E1B19",
    card: "#FFFFFF",
    cardForeground: "#1E1B19",
    primary: "#F07C1F",
    primaryForeground: "#FFFFFF",
    secondary: "#F1EAE5",
    secondaryForeground: "#2A2624",
    muted: "#F1EAE5",
    mutedForeground: "#8E8782",
    accent: "#FFE9DA",
    accentForeground: "#5C2A0D",
    border: "#EEE6E1",
    destructive: "#D9483B",
    success: "#4E9A3A",
    warning: "#D98A1A",
    ink: "#2A2624",
    inkForeground: "#FFFFFF",
    brown: "#5C2A0D",
    brownForeground: "#FFFFFF",
    hero: "#FFD6BA",
    heroForeground: "#3A2214",
    heroMuted: "#7A5440",
    shadow: "#5C2A0D",
    overlay: "rgba(30, 20, 14, 0.42)",
    track: "#EFE6DF",
  },
  dark: {
    background: "#151210",
    foreground: "#F4ECE6",
    card: "#211C19",
    cardForeground: "#F4ECE6",
    primary: "#FF8B2E",
    primaryForeground: "#1A100A",
    secondary: "#2A2420",
    secondaryForeground: "#F4ECE6",
    muted: "#2A2420",
    mutedForeground: "#9E948D",
    accent: "#3A251A",
    accentForeground: "#FFE3CF",
    border: "#2E2723",
    destructive: "#F0645A",
    success: "#7BC062",
    warning: "#F2A93B",
    ink: "#F4ECE6",
    inkForeground: "#1A1512",
    brown: "#FF8B2E",
    brownForeground: "#1A100A",
    hero: "#3A251A",
    heroForeground: "#FFE3CF",
    heroMuted: "#D4AE96",
    shadow: "#000000",
    overlay: "rgba(0, 0, 0, 0.6)",
    track: "#2E2723",
  },
};

export type ColorScheme = keyof typeof Colors;
export type ThemeColors = Palette;

/** Pastel tile colors (bg / fg) per media kind — light and dark variants. */
export const Tiles = {
  peach: { light: ["#FFDCC4", "#E8741A"], dark: ["#3D2718", "#FF9A4D"] },
  honey: { light: ["#FDE3A7", "#B7791F"], dark: ["#3A2E16", "#F2C25C"] },
  coral: { light: ["#FFD9D2", "#D9534F"], dark: ["#3D201E", "#FF8A80"] },
  rose: { light: ["#FBD5EE", "#C2449B"], dark: ["#3A1D30", "#F28BD2"] },
  sage: { light: ["#E3EFCB", "#5E8B23"], dark: ["#26301A", "#A5D46A"] },
  sky: { light: ["#D7E9F7", "#2F7BBF"], dark: ["#18293A", "#7DB8F0"] },
  lilac: { light: ["#E6DEFA", "#6C4FD0"], dark: ["#262040", "#A994F5"] },
  sand: { light: ["#F3E6D8", "#8A5A2B"], dark: ["#33281E", "#D9A877"] },
  mint: { light: ["#DDF1EC", "#2C8C78"], dark: ["#173029", "#6BD2BB"] },
  blush: { light: ["#F7DDE4", "#B24668"], dark: ["#3A1E27", "#EF8BA9"] },
} as const;

export type TileName = keyof typeof Tiles;

export const Radius = { sm: 10, md: 14, lg: 22, xl: 26, xxl: 28, pill: 999 } as const;
export const Space = { screen: 22, gap: 12, section: 28 } as const;

/** Urbanist families, loaded in app/_layout.tsx. */
export const Font = {
  regular: "Urbanist_400Regular",
  medium: "Urbanist_500Medium",
  semibold: "Urbanist_600SemiBold",
  bold: "Urbanist_700Bold",
  extrabold: "Urbanist_800ExtraBold",
} as const;

export const Fonts = Platform.select({
  ios: { sans: Font.medium, serif: "ui-serif", rounded: Font.medium, mono: "ui-monospace" },
  default: { sans: Font.medium, serif: "serif", rounded: Font.medium, mono: "monospace" },
  web: { sans: Font.medium, serif: "Georgia, serif", rounded: Font.medium, mono: "monospace" },
});

export function cardShadow(colors: Palette, strength = 1) {
  return Platform.select({
    web: { boxShadow: `0 6px 18px rgba(92, 42, 13, ${0.06 * strength})` } as object,
    default: {
      shadowColor: colors.shadow,
      shadowOpacity: 0.07 * strength,
      shadowRadius: 14,
      shadowOffset: { width: 0, height: 6 },
      elevation: 2 * strength,
    },
  });
}
