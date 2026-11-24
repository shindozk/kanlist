import { View, type StyleProp, type ViewStyle } from "react-native";
import { Image } from "expo-image";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { Font, Radius, cardShadow } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { formatScore } from "../core/text";
import { KIND_META } from "../domain/kinds";
import type { Title } from "../domain/types";
import { useLibrary } from "../state/library";
import { Press, T, useTile } from "./primitives";
import { ProgressBar } from "./progress";

export const openTitle = (key: string) => router.push({ pathname: "/title/[key]", params: { key } });

/** Displayed name honoring the user's title-language preference. */
export function useTitleName(t: Pick<Title, "title" | "titleAlt" | "titleNative">) {
  const pref = useLibrary((s) => s.settings.titleLanguage);
  if (pref === "english") return t.titleAlt ?? t.title;
  if (pref === "native") return t.titleNative ?? t.title;
  return t.title;
}

export function Cover({
  title,
  width,
  height,
  radius = Radius.md,
  style,
}: {
  title: Pick<Title, "cover" | "kind" | "color">;
  width: number | `${number}%`;
  height: number;
  radius?: number;
  style?: StyleProp<ViewStyle>;
}) {
  const meta = KIND_META[title.kind];
  const tile = useTile(meta.tile);
  return (
    <View style={[{ width, height, borderRadius: radius, overflow: "hidden", backgroundColor: tile.bg }, style]}>
      {title.cover ? (
        <Image source={{ uri: title.cover }} style={{ width: "100%", height: "100%" }} contentFit="cover" transition={180} />
      ) : (
        <View style={{ flex: 1, alignItems: "center", justifyContent: "center" }}>
          <Ionicons name={meta.icon} size={28} color={tile.fg} />
        </View>
      )}
    </View>
  );
}

/** Poster card used in rails and grids. */
export function PosterCard({ title, width = 128, showKind = true }: { title: Title; width?: number; showKind?: boolean }) {
  const c = useColors();
  const name = useTitleName(title);
  const entry = useLibrary((s) => s.entries[title.key]);
  const meta = KIND_META[title.kind];
  const tile = useTile(meta.tile);
  const total = entry ? (entry.customTotal ?? entry.title.totalUnits) : undefined;
  const done = entry ? Object.keys(entry.done).length : 0;
  return (
    <Press onPress={() => openTitle(title.key)} style={{ width }}>
      <View style={[{ borderRadius: Radius.lg }, cardShadow(c, 0.8)]}>
        <Cover title={title} width={width} height={width * 1.42} radius={Radius.lg} />
        {showKind ? (
          <View style={{ position: "absolute", top: 8, left: 8, backgroundColor: tile.bg, borderRadius: Radius.pill, paddingHorizontal: 8, height: 22, justifyContent: "center" }}>
            <T variant="caption" color={tile.fg} style={{ fontFamily: Font.bold, fontSize: 11 }}>
              {meta.label}
            </T>
          </View>
        ) : null}
        {entry ? (
          <View style={{ position: "absolute", top: 8, right: 8, width: 24, height: 24, borderRadius: 12, backgroundColor: c.primary, alignItems: "center", justifyContent: "center" }}>
            <Ionicons name="bookmark" size={12} color={c.primaryForeground} />
          </View>
        ) : null}
      </View>
      <T variant="label" numberOfLines={2} style={{ marginTop: 8, fontSize: 13.5 }}>
        {name}
      </T>
      {entry && total ? (
        <View style={{ marginTop: 6 }}>
          <ProgressBar value={done / total} height={4} />
        </View>
      ) : (
        <T variant="caption" muted numberOfLines={1} style={{ marginTop: 2 }}>
          {[title.year, title.score ? `★ ${formatScore(title.score)}` : null].filter(Boolean).join(" · ") || meta.label}
        </T>
      )}
    </Press>
  );
}

/** Compact horizontal row (search results, relations). */
export function TitleRow({ title, subtitle, right }: { title: Title; subtitle?: string; right?: React.ReactNode }) {
  const c = useColors();
  const name = useTitleName(title);
  const meta = KIND_META[title.kind];
  const tile = useTile(meta.tile);
  const inLib = useLibrary((s) => !!s.entries[title.key]);
  const units = title.totalUnits ? `${title.totalUnits} ${title.medium === "watch" ? "eps" : "caps"}` : null;
  return (
    <Press onPress={() => openTitle(title.key)}>
      <View style={[{ flexDirection: "row", gap: 14, padding: 10, backgroundColor: c.card, borderRadius: Radius.lg, alignItems: "center" }, cardShadow(c, 0.7)]}>
        <Cover title={title} width={58} height={80} radius={Radius.md} />
        <View style={{ flex: 1, gap: 4 }}>
          <T variant="title" numberOfLines={2} style={{ fontSize: 15 }}>
            {name}
          </T>
          <View style={{ flexDirection: "row", alignItems: "center", gap: 6, flexWrap: "wrap" }}>
            <View style={{ backgroundColor: tile.bg, borderRadius: Radius.pill, paddingHorizontal: 8, height: 20, justifyContent: "center" }}>
              <T variant="caption" color={tile.fg} style={{ fontFamily: Font.bold, fontSize: 11 }}>
                {meta.label}
              </T>
            </View>
            <T variant="caption" muted numberOfLines={1}>
              {subtitle ?? [title.year, units, title.score ? `★ ${formatScore(title.score)}` : null].filter(Boolean).join(" · ")}
            </T>
          </View>
        </View>
        {right ?? (inLib ? <Ionicons name="bookmark" size={18} color={c.primary} /> : <Ionicons name="chevron-forward" size={18} color={c.mutedForeground} />)}
      </View>
    </Press>
  );
}
