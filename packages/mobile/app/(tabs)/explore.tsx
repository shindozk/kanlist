import { useState } from "react";
import { FlatList, ScrollView, View } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { errorMessage } from "../../core/errors";
import { EXPLORE_RAILS, PRESETS, genrePreset, type BrowsePreset } from "../../domain/browse";
import { EXPLORE_GENRES, genrePt } from "../../domain/kinds";
import { useBrowse } from "../../queries/catalog";
import { PosterCard } from "../../ui/cover";
import { Chip, ErrorNote, FadeIn, IconTile, Press, Row, Screen, SectionHeader, Skeleton, T } from "../../ui/primitives";

const openPreset = (id: string) => router.push({ pathname: "/browse/[id]", params: { id } });

function Rail({ preset }: { preset: BrowsePreset }) {
  const q = useBrowse(preset.id);
  const items = q.data?.pages[0]?.items ?? [];
  return (
    <View style={{ marginTop: 28 }}>
      <View style={{ paddingHorizontal: Space.screen }}>
        <SectionHeader title={preset.label} action="Ver tudo" onAction={() => openPreset(preset.id)} />
      </View>
      {q.isError ? (
        <View style={{ paddingHorizontal: Space.screen }}>
          <ErrorNote message={errorMessage(q.error)} onRetry={() => q.refetch()} />
        </View>
      ) : (
        <FlatList
          horizontal
          data={q.isLoading ? [] : items.slice(0, 15)}
          keyExtractor={(t) => t.key}
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 14, paddingBottom: 4 }}
          renderItem={({ item }) => <PosterCard title={item} width={124} />}
          ListEmptyComponent={
            q.isLoading ? (
              <Row gap={14}>
                {[0, 1, 2].map((i) => (
                  <Skeleton key={i} width={124} height={176} radius={22} />
                ))}
              </Row>
            ) : undefined
          }
        />
      )}
    </View>
  );
}

export default function ExploreScreen() {
  const c = useColors();
  const [medium, setMedium] = useState<"watch" | "read">("watch");
  const categories = PRESETS.filter((p) => !EXPLORE_RAILS.slice(0, 3).includes(p.id));

  return (
    <Screen>
      <ScrollView contentContainerStyle={{ paddingTop: 12, paddingBottom: 40 }} showsVerticalScrollIndicator={false}>
        <FadeIn style={{ paddingHorizontal: Space.screen, gap: 16 }}>
          <T variant="display">Explorar</T>
          <Press onPress={() => router.push("/search")}>
            <View style={[{ height: 52, borderRadius: Radius.pill, backgroundColor: c.card, flexDirection: "row", alignItems: "center", paddingHorizontal: 18, gap: 10 }, cardShadow(c, 0.7)]}>
              <Ionicons name="search" size={19} color={c.mutedForeground} />
              <T variant="body" muted style={{ flex: 1 }}>
                Buscar anime, mangá, manhwa, donghua…
              </T>
            </View>
          </Press>
        </FadeIn>

        {/* Categories */}
        <FadeIn delay={60}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 12, paddingTop: 22, paddingBottom: 6 }}>
            {categories.map((p) => (
              <Press key={p.id} onPress={() => openPreset(p.id)}>
                <View style={[{ width: 148, backgroundColor: c.card, borderRadius: Radius.lg, padding: 14, gap: 12 }, cardShadow(c, 0.7)]}>
                  <IconTile icon={p.icon} tile={p.tile} size={40} radius={13} />
                  <View>
                    <T variant="label" numberOfLines={1}>
                      {p.label}
                    </T>
                    <T variant="caption" muted numberOfLines={1}>
                      {p.subtitle}
                    </T>
                  </View>
                </View>
              </Press>
            ))}
          </ScrollView>
        </FadeIn>

        {EXPLORE_RAILS.map((id) => {
          const p = PRESETS.find((x) => x.id === id);
          return p ? <Rail key={id} preset={p} /> : null;
        })}

        {/* Genres */}
        <View style={{ paddingHorizontal: Space.screen, marginTop: 30 }}>
          <SectionHeader title="Por gênero" />
          <Row style={{ marginBottom: 14 }}>
            <Chip label="Para assistir" icon="tv-outline" active={medium === "watch"} onPress={() => setMedium("watch")} />
            <Chip label="Para ler" icon="book-outline" active={medium === "read"} onPress={() => setMedium("read")} />
          </Row>
          <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 10 }}>
            {EXPLORE_GENRES.map((g, i) => {
              const p = genrePreset(g, medium);
              return (
                <Press key={g} onPress={() => openPreset(p.id)} style={{ width: "31%", flexGrow: 1 }}>
                  <View style={[{ backgroundColor: c.card, borderRadius: Radius.md, paddingVertical: 14, paddingHorizontal: 12, gap: 8 }, cardShadow(c, 0.5)]}>
                    <View style={{ width: 22, height: 6, borderRadius: 3, backgroundColor: c.primary, opacity: 0.25 + ((i % 4) * 0.2) }} />
                    <T variant="label" numberOfLines={1} style={{ fontFamily: Font.bold }}>
                      {genrePt(g)}
                    </T>
                  </View>
                </Press>
              );
            })}
          </View>
        </View>
      </ScrollView>
    </Screen>
  );
}
