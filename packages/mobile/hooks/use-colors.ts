import { Colors, type ColorScheme, type ThemeColors } from "../constants/theme";
import { useColorScheme } from "./use-color-scheme";
import { useLibrary } from "../state/library";

/** Active scheme: user preference (Settings → Tema) or the system setting. */
export function useScheme(): ColorScheme {
  const system = useColorScheme() ?? "light";
  const pref = useLibrary((s) => s.settings.theme);
  if (pref === "light" || pref === "dark") return pref;
  return system === "dark" ? "dark" : "light";
}

/** Returns the color palette for the active scheme. */
export function useColors(): ThemeColors {
  return Colors[useScheme()];
}
