import { ActivityIndicator, FlatList, View, useWindowDimensions } from "react-native";
import { useLocalSearchParams } from "expo-router";
import { Space } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { errorMessage } from "../../core/errors";
import { presetById } from "../../domain/browse";
import { useBrowse } from "../../queries/catalog";
import { PosterCard } from "../../ui/cover";
import { EmptyState, ErrorNote, Screen, Skeleton, T, TopBar } from "../../ui/primitives";

export default function BrowseScreen() {
  const c = useColors();
  const { id } = useLocalSearchParams<{ id: string }>();
  const preset = presetById(String(id));
  const q = useBrowse(String(id), !!preset);
  const { width } = useWindowDimensions();
  const cols = width > 700 ? 4 : 3;
  const gap = 12;
  const cardW = Math.floor((Math.min(width, 900) - Space.screen * 2 - gap * (cols - 1)) / cols);

  const items = (q.data?.pages ?? []).flatMap((p) => p.items);
  const unique = items.filter((t, i) => items.findIndex((x) => x.key === t.key) === i);

  return (
    <Screen>
      <TopBar title={preset?.label ?? "Lista"} />
      <FlatList
        data={unique}
        key={cols}
        numColumns={cols}
        keyExtractor={(t) => t.key}
        columnWrapperStyle={{ gap }}
        contentContainerStyle={{ paddingHorizontal: Space.screen, paddingBottom: 40, gap: 18 }}
        renderItem={({ item }) => <PosterCard title={item} width={cardW} />}
        onEndReachedThreshold={0.6}
        onEndReached={() => {
          if (q.hasNextPage && !q.isFetchingNextPage) void q.fetchNextPage();
        }}
        ListHeaderComponent={
          preset ? (
            <T variant="small" muted style={{ marginBottom: 2 }}>
              {preset.subtitle}
            </T>
          ) : undefined
        }
        ListEmptyComponent={
          !preset ? (
            <EmptyState icon="help-circle-outline" title="Lista não encontrada" />
          ) : q.isLoading ? (
            <View style={{ flexDirection: "row", flexWrap: "wrap", gap }}>
              {Array.from({ length: 9 }, (_, i) => (
                <Skeleton key={i} width={cardW} height={cardW * 1.42} radius={22} />
              ))}
            </View>
          ) : q.isError ? (
            <ErrorNote message={errorMessage(q.error)} onRetry={() => q.refetch()} />
          ) : (
            <EmptyState icon="file-tray-outline" title="Nada por aqui" />
          )
        }
        ListFooterComponent={q.isFetchingNextPage ? <ActivityIndicator color={c.primary} style={{ marginTop: 10 }} /> : undefined}
      />
    </Screen>
  );
}
