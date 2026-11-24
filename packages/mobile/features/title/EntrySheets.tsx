import { useEffect, useState } from "react";
import { TextInput, View } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { STATUS_ICON, statusLabel, unitWord } from "../../domain/kinds";
import { TRACKING_STATUSES, type LibraryEntry, type Title, type TrackingStatus } from "../../domain/types";
import { useLibrary } from "../../state/library";
import { Pill, Press, Row, T, tap } from "../../ui/primitives";
import { Sheet, SheetOption } from "../../ui/sheet";

export function StatusSheet({
  open,
  onClose,
  title,
  current,
  onPick,
}: {
  open: boolean;
  onClose: () => void;
  title: Title;
  current?: TrackingStatus;
  onPick: (s: TrackingStatus) => void;
}) {
  return (
    <Sheet open={open} onClose={onClose} title="Status">
      {TRACKING_STATUSES.map((s) => (
        <SheetOption
          key={s}
          icon={STATUS_ICON[s]}
          label={statusLabel(s, title.medium)}
          selected={current === s}
          onPress={() => {
            tap("select");
            onPick(s);
            onClose();
          }}
        />
      ))}
    </Sheet>
  );
}

export function Stars({ value, onChange }: { value?: number; onChange: (v: number | undefined) => void }) {
  const c = useColors();
  const v = value ?? 0;
  return (
    <Row gap={4}>
      {[1, 2, 3, 4, 5].map((i) => {
        const full = v >= i * 2;
        const half = !full && v >= i * 2 - 1;
        return (
          <Press
            key={i}
            hitSlop={4}
            onPress={() => {
              tap("select");
              const next = full ? i * 2 - 1 : half ? (v === i * 2 - 1 && i === 1 ? undefined : i * 2) : i * 2;
              onChange(next === v ? undefined : next);
            }}
            accessibilityLabel={`${i} estrelas`}
          >
            <Ionicons name={full ? "star" : half ? "star-half" : "star-outline"} size={24} color={full || half ? c.primary : c.mutedForeground} />
          </Press>
        );
      })}
      {value ? (
        <T variant="label" muted style={{ marginLeft: 6 }}>
          {(value / 2).toFixed(1).replace(".", ",")}
        </T>
      ) : null}
    </Row>
  );
}

const inputStyle = (c: ReturnType<typeof useColors>) => ({
  borderRadius: Radius.md,
  backgroundColor: c.card,
  borderWidth: 1,
  borderColor: c.border,
  paddingHorizontal: 14,
  paddingVertical: 12,
  fontFamily: Font.medium,
  fontSize: 15,
  color: c.foreground,
});

export function NotesSheet({ open, onClose, entry }: { open: boolean; onClose: () => void; entry: LibraryEntry }) {
  const c = useColors();
  const update = useLibrary((s) => s.update);
  const [notes, setNotes] = useState(entry.notes);
  useEffect(() => {
    if (open) setNotes(entry.notes);
  }, [open, entry.notes]);
  return (
    <Sheet open={open} onClose={onClose} title="Anotações">
      <TextInput
        value={notes}
        onChangeText={setNotes}
        multiline
        placeholder="Onde parou, teorias, arcos favoritos…"
        placeholderTextColor={c.mutedForeground}
        style={[inputStyle(c), { minHeight: 140, textAlignVertical: "top" }]}
      />
      <View style={{ marginTop: 14 }}>
        <Pill
          label="Salvar"
          full
          onPress={() => {
            update(entry.key, { notes: notes.trim() });
            onClose();
          }}
        />
      </View>
    </Sheet>
  );
}

export function MoreSheet({ open, onClose, entry, title }: { open: boolean; onClose: () => void; entry?: LibraryEntry; title: Title }) {
  const c = useColors();
  const update = useLibrary((s) => s.update);
  const remove = useLibrary((s) => s.remove);
  const resetProgress = useLibrary((s) => s.resetProgress);
  const collections = useLibrary((s) => s.collections);
  const toggleIn = useLibrary((s) => s.toggleInCollection);
  const [total, setTotal] = useState("");
  const [confirm, setConfirm] = useState(false);
  useEffect(() => {
    if (open) {
      setTotal(entry?.customTotal ? String(entry.customTotal) : "");
      setConfirm(false);
    }
  }, [open, entry?.customTotal]);

  if (!entry) {
    return (
      <Sheet open={open} onClose={onClose} title="Opções">
        <T variant="small" muted>
          Adicione a obra à sua lista para usar coleções, anotações e total personalizado.
        </T>
      </Sheet>
    );
  }

  return (
    <Sheet open={open} onClose={onClose} title="Opções">
      <T variant="label" muted style={{ marginBottom: 8 }}>
        Coleções
      </T>
      {collections.length ? (
        <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 8, marginBottom: 6 }}>
          {collections.map((col) => {
            const on = col.keys.includes(entry.key);
            return (
              <Press
                key={col.id}
                onPress={() => toggleIn(col.id, entry.key)}
                style={{ flexDirection: "row", alignItems: "center", gap: 6, height: 36, paddingHorizontal: 14, borderRadius: Radius.pill, backgroundColor: on ? c.ink : c.card, borderWidth: 1, borderColor: on ? c.ink : c.border }}
              >
                <Ionicons name={on ? "checkmark" : "add"} size={15} color={on ? c.inkForeground : c.mutedForeground} />
                <T variant="label" color={on ? c.inkForeground : c.foreground}>
                  {col.name}
                </T>
              </Press>
            );
          })}
        </View>
      ) : (
        <Press
          onPress={() => {
            onClose();
            router.push("/library");
          }}
        >
          <T variant="small" color={c.primary} style={{ marginBottom: 6 }}>
            Crie coleções na aba Biblioteca →
          </T>
        </Press>
      )}

      <T variant="label" muted style={{ marginTop: 14, marginBottom: 8 }}>
        Total de {unitWord(title.medium, true)} (se a fonte não souber)
      </T>
      <Row>
        <TextInput
          value={total}
          onChangeText={(t) => setTotal(t.replace(/[^0-9]/g, ""))}
          keyboardType="number-pad"
          placeholder={title.totalUnits ? String(title.totalUnits) : "ex.: 120"}
          placeholderTextColor={c.mutedForeground}
          style={[inputStyle(c), { flex: 1 }]}
        />
        <Pill
          label="Salvar"
          variant="ink"
          onPress={() => {
            const n = Number(total);
            update(entry.key, { customTotal: n > 0 ? n : undefined });
            tap("success");
          }}
        />
      </Row>

      <View style={{ marginTop: 18, gap: 2 }}>
        <SheetOption icon="heart-outline" label={entry.favorite ? "Remover dos favoritos" : "Favoritar"} onPress={() => update(entry.key, { favorite: !entry.favorite })} />
        <SheetOption
          icon="refresh-outline"
          label="Zerar progresso"
          hint="Desmarca todos os episódios/capítulos"
          onPress={() => {
            resetProgress(entry.key);
            onClose();
          }}
        />
        <SheetOption
          icon="trash-outline"
          danger
          label={confirm ? "Toque de novo para confirmar" : "Remover da biblioteca"}
          onPress={() => {
            if (!confirm) return setConfirm(true);
            remove(entry.key);
            onClose();
          }}
        />
      </View>
    </Sheet>
  );
}
