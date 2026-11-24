import { useMemo, useState } from "react";
import { FlatList, ScrollView, TextInput, View } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { normalizeTitle } from "../../core/text";
import { KIND_META, STATUS_ICON, statusLabel, unitShort } from "../../domain/kinds";
import { doneCount, lastDone, progressOf, totalFor } from "../../domain/progress";
import { MEDIA_KINDS, TRACKING_STATUSES, type LibraryEntry, type MediaKind, type TrackingStatus } from "../../domain/types";
import { useLibrary } from "../../state/library";
import { Cover, openTitle, useTitleName } from "../../ui/cover";
import { Chip, EmptyState, FadeIn, IconButton, Pill, Press, Row, Screen, T, tap, useTile } from "../../ui/primitives";
import { ProgressBar } from "../../ui/progress";
import { Sheet, SheetOption } from "../../ui/sheet";
import { CollectionSheet } from "../../features/library/CollectionSheet";

type Sort = "updated" | "added" | "title" | "progress" | "rating";
const SORTS: { id: Sort; label: string; icon: React.ComponentProps<typeof Ionicons>["name"] }[] = [
  { id: "updated", label: "Atividade recente", icon: "time-outline" },
  { id: "added", label: "Adicionados recentemente", icon: "add-circle-outline" },
  { id: "title", label: "Título (A–Z)", icon: "text-outline" },
  { id: "progress", label: "Progresso", icon: "pie-chart-outline" },
  { id: "rating", label: "Minha nota", icon: "star-outline" },
];

const STATUS_PLAIN: Record<TrackingStatus, string> = {
  current: "Em andamento",
  planning: "Planejado",
  completed: "Completo",
  paused: "Pausado",
  dropped: "Abandonado",
  repeating: "Revendo",
};

function EntryRow({ e }: { e: LibraryEntry }) {
  const c = useColors();
  const name = useTitleName(e.title);
  const meta = KIND_META[e.title.kind];
  const tile = useTile(meta.tile);
  const markNext = useLibrary((s) => s.markNext);
  const total = totalFor(e);
  const done = doneCount(e);
  const p = progressOf(e);
  const canNext = (e.status === "current" || e.status === "repeating") && (!total || done < total);
  return (
    <Press onPress={() => openTitle(e.key)}>
      <View style={[{ flexDirection: "row", gap: 14, padding: 10, backgroundColor: c.card, borderRadius: Radius.lg, alignItems: "center" }, cardShadow(c, 0.7)]}>
        <Cover title={e.title} width={58} height={80} radius={Radius.md} />
        <View style={{ flex: 1, gap: 6 }}>
          <T variant="title" numberOfLines={2} style={{ fontSize: 15 }}>
            {name}
          </T>
          <Row gap={6}>
            <View style={{ backgroundColor: tile.bg, borderRadius: Radius.pill, paddingHorizontal: 8, height: 20, justifyContent: "center" }}>
              <T variant="caption" color={tile.fg} style={{ fontFamily: Font.bold, fontSize: 11 }}>
                {meta.label}
              </T>
            </View>
            <Ionicons name={STATUS_ICON[e.status]} size={13} color={c.mutedForeground} />
            <T variant="caption" muted numberOfLines={1} style={{ flex: 1 }}>
              {statusLabel(e.status, e.title.medium)}
              {e.rating ? ` · ★ ${(e.rating / 2).toFixed(1).replace(".", ",")}` : ""}
            </T>
          </Row>
          <Row gap={8}>
            <View style={{ flex: 1 }}>
              <ProgressBar value={p ?? (done ? 0.08 : 0)} height={5} />
            </View>
            <T variant="caption" style={{ fontFamily: Font.semibold }}>
              {total ? `${done}/${total}` : done ? `${unitShort(e.title.medium)} ${lastDone(e)}` : "—"}
            </T>
          </Row>
        </View>
        {canNext ? (
          <Press
            hitSlop={8}
            accessibilityLabel="Marcar próximo"
            onPress={() => {
              if (markNext(e.key)) tap("success");
            }}
            style={{ width: 38, height: 38, borderRadius: 19, borderWidth: 2, borderColor: c.primary, alignItems: "center", justifyContent: "center" }}
          >
            <Ionicons name="add" size={20} color={c.primary} />
          </Press>
        ) : e.favorite ? (
          <Ionicons name="heart" size={18} color={c.destructive} />
        ) : null}
      </View>
    </Press>
  );
}

function CollectionCard({ id }: { id: string }) {
  const c = useColors();
  const col = useLibrary((s) => s.collections.find((x) => x.id === id));
  const entries = useLibrary((s) => s.entries);
  const tile = useTile(col?.tile ?? "peach");
  if (!col) return null;
  const covers = col.keys.map((k) => entries[k]?.title).filter(Boolean).slice(0, 3);
  return (
    <Press onPress={() => router.push({ pathname: "/collection/[id]", params: { id } })} style={{ flex: 1 }}>
      <View style={[{ backgroundColor: tile.bg, borderRadius: Radius.xl, padding: 16, minHeight: 160, justifyContent: "space-between" }, cardShadow(c, 0.5)]}>
        <View style={{ flexDirection: "row", height: 70 }}>
          {covers.length ? (
            covers.map((t, i) => (
              <View key={t!.key} style={{ marginLeft: i ? -18 : 0, transform: [{ rotate: `${(i - 1) * 6}deg` }], borderRadius: 10, borderWidth: 2, borderColor: tile.bg }}>
                <Cover title={t!} width={48} height={66} radius={9} />
              </View>
            ))
          ) : (
            <Ionicons name="albums" size={34} color={tile.fg} />
          )}
        </View>
        <View>
          <T variant="title" color={tile.fg} numberOfLines={1} style={{ fontFamily: Font.bold }}>
            {col.name}
          </T>
          <T variant="caption" color={tile.fg} style={{ opacity: 0.75 }}>
            {col.keys.length} {col.keys.length === 1 ? "obra" : "obras"}
          </T>
        </View>
      </View>
    </Press>
  );
}

export default function LibraryScreen() {
  const c = useColors();
  const entriesMap = useLibrary((s) => s.entries);
  const collections = useLibrary((s) => s.collections);
  const [tab, setTab] = useState<"list" | "collections">("list");
  const [status, setStatus] = useState<TrackingStatus | "all" | "favorites">("current");
  const [kind, setKind] = useState<MediaKind | "all">("all");
  const [sort, setSort] = useState<Sort>("updated");
  const [sortOpen, setSortOpen] = useState(false);
  const [kindOpen, setKindOpen] = useState(false);
  const [newCol, setNewCol] = useState(false);
  const [query, setQuery] = useState("");

  const all = useMemo(() => Object.values(entriesMap), [entriesMap]);
  const counts = useMemo(() => {
    const m: Record<string, number> = { all: all.length, favorites: all.filter((e) => e.favorite).length };
    for (const s of TRACKING_STATUSES) m[s] = all.filter((e) => e.status === s).length;
    return m;
  }, [all]);

  const list = useMemo(() => {
    const q = normalizeTitle(query);
    const out = all.filter(
      (e) =>
        (status === "all" || (status === "favorites" ? e.favorite : e.status === status)) &&
        (kind === "all" || e.title.kind === kind) &&
        (!q || normalizeTitle([e.title.title, e.title.titleAlt, e.title.titleNative].filter(Boolean).join(" ")).includes(q)),
    );
    const by: Record<Sort, (a: LibraryEntry, b: LibraryEntry) => number> = {
      updated: (a, b) => b.updatedAt - a.updatedAt,
      added: (a, b) => b.addedAt - a.addedAt,
      title: (a, b) => a.title.title.localeCompare(b.title.title),
      progress: (a, b) => (progressOf(b) ?? 0) - (progressOf(a) ?? 0),
      rating: (a, b) => (b.rating ?? -1) - (a.rating ?? -1),
    };
    return out.sort(by[sort]);
  }, [all, status, kind, sort, query]);

  const statusChips: { id: TrackingStatus | "all" | "favorites"; label: string }[] = [
    { id: "current", label: STATUS_PLAIN.current },
    { id: "planning", label: STATUS_PLAIN.planning },
    { id: "completed", label: STATUS_PLAIN.completed },
    { id: "repeating", label: STATUS_PLAIN.repeating },
    { id: "paused", label: STATUS_PLAIN.paused },
    { id: "dropped", label: STATUS_PLAIN.dropped },
    { id: "favorites", label: "Favoritos" },
    { id: "all", label: "Tudo" },
  ];

  return (
    <Screen>
      <View style={{ paddingHorizontal: Space.screen, paddingTop: 12, gap: 16 }}>
        <Row style={{ justifyContent: "space-between" }}>
          <T variant="display">Biblioteca</T>
          <IconButton icon="add" label="Adicionar obra" onPress={() => router.push("/search")} bg={c.brown} color={c.brownForeground} />
        </Row>
        {/* Segmented */}
        <Row gap={0} style={{ backgroundColor: c.muted, borderRadius: Radius.pill, padding: 4 }}>
          {(
            [
              ["list", "Minha lista"],
              ["collections", "Coleções"],
            ] as const
          ).map(([id, label]) => (
            <Press
              key={id}
              onPress={() => {
                tap("select");
                setTab(id);
              }}
              style={[{ flex: 1, height: 38, borderRadius: Radius.pill, alignItems: "center", justifyContent: "center" }, tab === id && [{ backgroundColor: c.card }, cardShadow(c, 0.5)]]}
            >
              <T variant="label" color={tab === id ? c.foreground : c.mutedForeground}>
                {label}
              </T>
            </Press>
          ))}
        </Row>
      </View>

      {tab === "list" ? (
        <FlatList
          data={list}
          keyExtractor={(e) => e.key}
          contentContainerStyle={{ paddingHorizontal: Space.screen, paddingBottom: 40, gap: 12 }}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
          ListHeaderComponent={
            <View style={{ gap: 12, paddingTop: 16, paddingBottom: 4 }}>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8, paddingHorizontal: Space.screen }} style={{ marginHorizontal: -Space.screen }}>
                {statusChips.map((s) => (
                  <Chip key={s.id} label={s.label} count={counts[s.id]} active={status === s.id} onPress={() => setStatus(s.id)} />
                ))}
              </ScrollView>
              <Row gap={8}>
                <Row gap={8} style={{ flex: 1, height: 42, backgroundColor: c.card, borderRadius: Radius.pill, paddingHorizontal: 14, borderWidth: 1, borderColor: c.border }}>
                  <Ionicons name="search" size={16} color={c.mutedForeground} />
                  <TextInput
                    value={query}
                    onChangeText={setQuery}
                    placeholder="Filtrar na lista"
                    placeholderTextColor={c.mutedForeground}
                    style={{ flex: 1, fontFamily: Font.medium, fontSize: 14, color: c.foreground, paddingVertical: 0 }}
                  />
                </Row>
                <IconButton icon="options-outline" label="Tipo" onPress={() => setKindOpen(true)} bg={kind !== "all" ? c.ink : undefined} color={kind !== "all" ? c.inkForeground : undefined} />
                <IconButton icon="swap-vertical" label="Ordenar" onPress={() => setSortOpen(true)} />
              </Row>
              {kind !== "all" ? (
                <Row>
                  <Chip label={KIND_META[kind].plural} icon="close" active onPress={() => setKind("all")} />
                </Row>
              ) : null}
            </View>
          }
          renderItem={({ item, index }) => (
            <FadeIn delay={Math.min(index, 8) * 30}>
              <EntryRow e={item} />
            </FadeIn>
          )}
          ListEmptyComponent={
            all.length ? (
              <EmptyState icon="filter" title="Nada por aqui" text="Nenhuma obra com esses filtros." tile="sky" />
            ) : (
              <EmptyState
                icon="library-outline"
                title="Sua biblioteca está vazia"
                text="Busque um anime, mangá ou manhwa e comece a marcar o que você assiste e lê."
                action="Buscar obras"
                onAction={() => router.push("/search")}
              />
            )
          }
        />
      ) : (
        <ScrollView contentContainerStyle={{ padding: Space.screen, paddingBottom: 40, gap: 12 }} showsVerticalScrollIndicator={false}>
          {collections.length ? (
            <>
              {Array.from({ length: Math.ceil(collections.length / 2) }).map((_, i) => (
                <Row key={i} gap={12} style={{ alignItems: "stretch" }}>
                  <CollectionCard id={collections[i * 2].id} />
                  {collections[i * 2 + 1] ? <CollectionCard id={collections[i * 2 + 1].id} /> : <View style={{ flex: 1 }} />}
                </Row>
              ))}
              <View style={{ marginTop: 8 }}>
                <Pill label="Nova coleção" icon="add" variant="outline" full onPress={() => setNewCol(true)} />
              </View>
            </>
          ) : (
            <EmptyState
              icon="albums-outline"
              tile="lilac"
              title="Organize do seu jeito"
              text="Crie coleções como “Para maratonar” ou “Manhwas de ação”."
              action="Criar coleção"
              onAction={() => setNewCol(true)}
            />
          )}
        </ScrollView>
      )}

      <Sheet open={sortOpen} onClose={() => setSortOpen(false)} title="Ordenar por">
        {SORTS.map((s) => (
          <SheetOption
            key={s.id}
            icon={s.icon}
            label={s.label}
            selected={sort === s.id}
            onPress={() => {
              setSort(s.id);
              setSortOpen(false);
            }}
          />
        ))}
      </Sheet>
      <Sheet open={kindOpen} onClose={() => setKindOpen(false)} title="Tipo de obra">
        <SheetOption
          icon="apps-outline"
          label="Todos os tipos"
          selected={kind === "all"}
          onPress={() => {
            setKind("all");
            setKindOpen(false);
          }}
        />
        {MEDIA_KINDS.map((k) => (
          <SheetOption
            key={k}
            icon={KIND_META[k].icon}
            label={KIND_META[k].plural}
            hint={`${all.filter((e) => e.title.kind === k).length} na lista`}
            selected={kind === k}
            onPress={() => {
              setKind(k);
              setKindOpen(false);
            }}
          />
        ))}
      </Sheet>
      {newCol ? <CollectionSheet open={newCol} onClose={() => setNewCol(false)} /> : null}
    </Screen>
  );
}
