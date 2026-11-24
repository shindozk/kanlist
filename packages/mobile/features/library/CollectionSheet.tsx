import { useEffect, useState } from "react";
import { TextInput, View } from "react-native";
import { Font, Radius, Tiles, type TileName } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { useLibrary } from "../../state/library";
import { IconTile, Pill, Press, Row, T } from "../../ui/primitives";
import { Sheet } from "../../ui/sheet";

export function CollectionSheet({ open, onClose, editId }: { open: boolean; onClose: () => void; editId?: string }) {
  const c = useColors();
  const create = useLibrary((s) => s.createCollection);
  const updateCol = useLibrary((s) => s.updateCollection);
  const existing = useLibrary((s) => (editId ? s.collections.find((x) => x.id === editId) : undefined));
  const [name, setName] = useState(existing?.name ?? "");
  const [tile, setTile] = useState<string>(existing?.tile ?? "peach");
  const tiles = Object.keys(Tiles) as TileName[];
  useEffect(() => {
    if (open) {
      setName(existing?.name ?? "");
      setTile(existing?.tile ?? "peach");
    }
  }, [open, existing?.name, existing?.tile]);
  return (
    <Sheet open={open} onClose={onClose} title={editId ? "Editar coleção" : "Nova coleção"}>
      <TextInput
        value={name}
        onChangeText={setName}
        placeholder="Ex.: Isekais favoritos"
        placeholderTextColor={c.mutedForeground}
        maxLength={40}
        style={{ borderRadius: Radius.md, backgroundColor: c.card, borderWidth: 1, borderColor: c.border, paddingHorizontal: 14, paddingVertical: 12, fontFamily: Font.medium, fontSize: 15, color: c.foreground }}
      />
      <T variant="label" muted style={{ marginTop: 16, marginBottom: 10 }}>
        Cor
      </T>
      <Row gap={10} style={{ flexWrap: "wrap" }}>
        {tiles.map((t) => (
          <Press key={t} onPress={() => setTile(t)} accessibilityLabel={t}>
            <View style={{ padding: 3, borderRadius: 22, borderWidth: 2, borderColor: tile === t ? c.foreground : "transparent" }}>
              <IconTile icon="albums" tile={t} size={34} radius={17} />
            </View>
          </Press>
        ))}
      </Row>
      <View style={{ marginTop: 18 }}>
        <Pill
          label={editId ? "Salvar" : "Criar coleção"}
          full
          disabled={!name.trim()}
          onPress={() => {
            if (!name.trim()) return;
            if (editId) updateCol(editId, { name: name.trim(), tile });
            else create(name.trim(), tile);
            setName("");
            onClose();
          }}
        />
      </View>
    </Sheet>
  );
}

