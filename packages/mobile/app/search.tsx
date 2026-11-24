import { useEffect, useState } from "react";
import { FlatList, KeyboardAvoidingView, Platform, ScrollView, TextInput, View } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space, cardShadow } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { errorMessage } from "../core/errors";
import { KIND_META } from "../domain/kinds";
import { MEDIA_KINDS, type KindFilter, type Title } from "../domain/types";
import { useSearch } from "../queries/catalog";
import { useLibrary } from "../state/library";
import { TitleRow } from "../ui/cover";
import { Chip, EmptyState, ErrorNote, IconButton, Press, Row, Screen, Skeleton, T, tap } from "../ui/primitives";

function useDebounced<T>(value: T, ms: number) {
  const [v, setV] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setV(value), ms);
    return () => clearTimeout(t);
  }, [value, ms]);
  return v;
}

function QuickAdd({ title }: { title: Title }) {
  const c = useColors();
  const inLib = useLibrary((s) => !!s.entries[title.key]);
  const add = useLibrary((s) => s.add);
  return (
    <Press
      hitSlop={8}
      accessibilityLabel={inLib ? "Na biblioteca" : "Adicionar à biblioteca"}
      onPress={() => {
        if (inLib) return;
        tap("success");
        add(title, "planning");
      }}
      style={{ width: 36, height: 36, borderRadius: 18, alignItems: "center", justifyContent: "center", backgroundColor: inLib ? c.primary : c.accent }}
    >
      <Ionicons name={inLib ? "checkmark" : "add"} size={20} color={inLib ? c.primaryForeground : c.accentForeground} />
    </Press>
  );
}

export default function SearchScreen() {
  const c = useColors();
  const [text, setText] = useState("");
  const [filter, setFilter] = useState<KindFilter>("all");
  const q = useDebounced(text, 380);
  const searches = useLibrary((s) => s.searches);
  const addSearch = useLibrary((s) => s.addSearch);
  const clearSearches = useLibrary((s) => s.clearSearches);
  const res = useSearch(q, filter);

  useEffect(() => {
    if (res.data?.items.length && q.trim().length >= 2) addSearch(q);
  }, [res.data, q, addSearch]);

  const items = res.data?.items ?? [];
  const showResults = q.trim().length >= 2;

  return (
    <Screen>
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === "ios" ? "padding" : "height"}>
        <Row style={{ paddingHorizontal: Space.screen, paddingTop: 8, gap: 10 }}>
          <IconButton icon="chevron-back" label="Voltar" onPress={() => (router.canGoBack() ? router.back() : router.replace("/"))} />
          <View style={[{ flex: 1, height: 48, borderRadius: Radius.pill, backgroundColor: c.card, flexDirection: "row", alignItems: "center", paddingHorizontal: 16, gap: 8 }, cardShadow(c, 0.6)]}>
            <Ionicons name="search" size={18} color={c.mutedForeground} />
            <TextInput
              value={text}
              onChangeText={setText}
              placeholder="Anime, mangá, manhwa…"
              placeholderTextColor={c.mutedForeground}
              returnKeyType="search"
              style={{ flex: 1, fontFamily: Font.semibold, fontSize: 15, color: c.foreground, height: 48, outlineStyle: "none" } as object}
            />
            {text ? (
              <Press onPress={() => setText("")} hitSlop={8} accessibilityLabel="Limpar">
                <Ionicons name="close-circle" size={18} color={c.mutedForeground} />
              </Press>
            ) : null}
          </View>
        </Row>

        <View>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 8, paddingVertical: 14 }}>
            <Chip label="Todos" active={filter === "all"} onPress={() => setFilter("all")} />
            {MEDIA_KINDS.map((k) => (
              <Chip key={k} label={KIND_META[k].plural} icon={KIND_META[k].icon} active={filter === k} onPress={() => setFilter(k)} />
            ))}
          </ScrollView>
        </View>

        {!showResults ? (
          <ScrollView contentContainerStyle={{ paddingHorizontal: Space.screen, paddingBottom: 40 }} keyboardShouldPersistTaps="handled">
            {searches.length ? (
              <>
                <Row style={{ justifyContent: "space-between", marginBottom: 10 }}>
                  <T variant="h2">Buscas recentes</T>
                  <Press onPress={clearSearches} hitSlop={8}>
                    <T variant="small" muted>
                      Limpar
                    </T>
                  </Press>
                </Row>
                <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 8 }}>
                  {searches.map((s) => (
                    <Chip key={s} label={s} icon="time-outline" onPress={() => setText(s)} />
                  ))}
                </View>
              </>
            ) : (
              <EmptyState
                icon="search"
                title="O que vamos acompanhar?"
                text="Buscamos ao mesmo tempo no AniList, MangaDex, Kitsu e MyAnimeList (Jikan)."
              />
            )}
          </ScrollView>
        ) : (
          <FlatList
            data={items}
            keyExtractor={(t) => t.key}
            keyboardShouldPersistTaps="handled"
            contentContainerStyle={{ paddingHorizontal: Space.screen, paddingBottom: 40, gap: 10 }}
            renderItem={({ item }) => <TitleRow title={item} right={<QuickAdd title={item} />} />}
            ListHeaderComponent={
              res.data?.used.length ? (
                <T variant="caption" muted style={{ marginBottom: 2 }}>
                  {items.length} resultados · {res.data.used.join(", ")}
                  {res.data.failed.length ? ` · indisponível: ${res.data.failed.join(", ")}` : ""}
                </T>
              ) : undefined
            }
            ListEmptyComponent={
              res.isLoading || res.isFetching ? (
                <View style={{ gap: 10 }}>
                  {[0, 1, 2, 3, 4].map((i) => (
                    <Skeleton key={i} height={100} radius={22} />
                  ))}
                </View>
              ) : res.isError ? (
                <ErrorNote message={errorMessage(res.error)} onRetry={() => res.refetch()} />
              ) : (
                <EmptyState icon="sad-outline" title="Nada encontrado" text="Tente outro nome — o romaji ou o título em inglês costumam funcionar melhor." />
              )
            }
          />
        )}
      </KeyboardAvoidingView>
    </Screen>
  );
}
