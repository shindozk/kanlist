import { useState } from "react";
import { FlatList, View } from "react-native";
import { Stack, router, useLocalSearchParams } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { useLibrary } from "../../state/library";
import { TitleRow } from "../../ui/cover";
import { EmptyState, IconButton, Press, Row, Screen, T, TopBar, useTile } from "../../ui/primitives";
import { Sheet, SheetOption } from "../../ui/sheet";
import { CollectionSheet } from "../../features/library/CollectionSheet";

export default function CollectionScreen() {
  const c = useColors();
  const { id } = useLocalSearchParams<{ id: string }>();
  const col = useLibrary((s) => s.collections.find((x) => x.id === id));
  const entries = useLibrary((s) => s.entries);
  const toggleIn = useLibrary((s) => s.toggleInCollection);
  const deleteCol = useLibrary((s) => s.deleteCollection);
  const tile = useTile(col?.tile ?? "peach");
  const [menu, setMenu] = useState(false);
  const [edit, setEdit] = useState(false);
  const [adding, setAdding] = useState(false);

  if (!col) {
    return (
      <Screen>
        <Stack.Screen options={{ headerShown: false }} />
        <TopBar />
        <EmptyState icon="albums-outline" title="Coleção não encontrada" />
      </Screen>
    );
  }

  const items = col.keys.map((k) => entries[k]).filter(Boolean);
  const candidates = Object.values(entries)
    .filter((e) => !col.keys.includes(e.key))
    .sort((a, b) => b.updatedAt - a.updatedAt);

  return (
    <Screen>
      <Stack.Screen options={{ headerShown: false }} />
      <TopBar title="" right={<IconButton icon="ellipsis-horizontal" label="Opções" onPress={() => setMenu(true)} />} />
      <FlatList
        data={items}
        keyExtractor={(e) => e.key}
        contentContainerStyle={{ paddingHorizontal: Space.screen, paddingBottom: 40, gap: 12 }}
        ListHeaderComponent={
          <View style={{ backgroundColor: tile.bg, borderRadius: Radius.xl, padding: 20, marginBottom: 8, gap: 6 }}>
            <Ionicons name="albums" size={28} color={tile.fg} />
            <T variant="h1" color={tile.fg}>
              {col.name}
            </T>
            <T variant="small" color={tile.fg} style={{ opacity: 0.8 }}>
              {items.length} {items.length === 1 ? "obra" : "obras"}
            </T>
            <Press
              onPress={() => setAdding(true)}
              style={{ alignSelf: "flex-start", marginTop: 8, backgroundColor: c.brown, borderRadius: Radius.pill, paddingHorizontal: 16, height: 36, flexDirection: "row", alignItems: "center", gap: 6 }}
            >
              <Ionicons name="add" size={16} color={c.brownForeground} />
              <T variant="label" color={c.brownForeground}>
                Adicionar obras
              </T>
            </Press>
          </View>
        }
        renderItem={({ item }) => (
          <TitleRow
            title={item!.title}
            right={
              <Press hitSlop={10} onPress={() => toggleIn(col.id, item!.key)} accessibilityLabel="Remover da coleção">
                <Ionicons name="remove-circle-outline" size={22} color={c.mutedForeground} />
              </Press>
            }
          />
        )}
        ListEmptyComponent={<EmptyState icon="add-circle-outline" title="Coleção vazia" text="Adicione obras da sua biblioteca." tile="sand" />}
      />

      <Sheet open={menu} onClose={() => setMenu(false)} title="Coleção">
        <SheetOption icon="create-outline" label="Renomear / cor" onPress={() => { setMenu(false); setEdit(true); }} />
        <SheetOption
          icon="trash-outline"
          label="Excluir coleção"
          hint="As obras continuam na biblioteca"
          danger
          onPress={() => {
            setMenu(false);
            deleteCol(col.id);
            router.back();
          }}
        />
      </Sheet>
      <Sheet open={adding} onClose={() => setAdding(false)} title="Adicionar à coleção">
        {candidates.length ? (
          candidates.map((e) => (
            <Press key={e.key} onPress={() => toggleIn(col.id, e.key)} style={{ paddingVertical: 10 }}>
              <Row gap={12}>
                <Ionicons name="add-circle" size={22} color={c.primary} />
                <T variant="title" numberOfLines={1} style={{ flex: 1, fontFamily: Font.semibold, fontSize: 15 }}>
                  {e.title.title}
                </T>
              </Row>
            </Press>
          ))
        ) : (
          <T variant="small" muted>
            Todas as obras da biblioteca já estão aqui.
          </T>
        )}
      </Sheet>
      {edit ? <CollectionSheet open={edit} onClose={() => setEdit(false)} editId={col.id} /> : null}
    </Screen>
  );
}
