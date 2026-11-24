import { useMemo, useState } from "react";
import { ScrollView, View } from "react-native";
import { Image } from "expo-image";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { errorMessage } from "../../core/errors";
import { shortDate } from "../../core/dates";
import { unitShort, unitWord } from "../../domain/kinds";
import type { LibraryEntry, Title, Unit, UnitList } from "../../domain/types";
import { useLibrary } from "../../state/library";
import { Chip, ErrorNote, Pill, Press, Row, SectionHeader, Skeleton, T, tap } from "../../ui/primitives";
import { Sheet, SheetOption } from "../../ui/sheet";

const PAGE = 50;
const SOURCE_LABEL: Record<string, string> = { anilist: "AniList", kitsu: "Kitsu", jikan: "MyAnimeList", mangadex: "MangaDex", local: "Local" };

function UnitRow({ unit, done, onToggle, onLong, medium }: { unit: Unit; done: boolean; onToggle: () => void; onLong: () => void; medium: Title["medium"] }) {
  const c = useColors();
  const word = unitShort(medium);
  const info = [
    unit.volume ? `Vol. ${unit.volume}` : null,
    unit.airedAt ? shortDate(unit.airedAt) : null,
    unit.langs?.length ? unit.langs.map((l) => l.toUpperCase()).join(" ") : null,
  ]
    .filter(Boolean)
    .join(" · ");
  return (
    <Press
      onPress={() => {
        tap(done ? "light" : "success");
        onToggle();
      }}
      onLongPress={onLong}
      delayLongPress={350}
      scale={0.99}
      disabled={unit.upcoming}
    >
      <View style={{ flexDirection: "row", alignItems: "center", gap: 12, paddingVertical: 9, opacity: unit.upcoming ? 0.5 : 1 }}>
        <View
          style={{
            width: 24,
            height: 24,
            borderRadius: 12,
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: done ? c.primary : "transparent",
            borderWidth: done ? 0 : 1.5,
            borderColor: c.mutedForeground,
          }}
        >
          {done ? <Ionicons name="checkmark" size={15} color={c.primaryForeground} /> : null}
        </View>
        {unit.thumbnail ? (
          <Image source={{ uri: unit.thumbnail }} style={{ width: 72, height: 42, borderRadius: 10, backgroundColor: c.muted }} contentFit="cover" />
        ) : null}
        <View style={{ flex: 1 }}>
          <T variant="label" numberOfLines={1} style={{ fontSize: 14 }} color={done ? c.mutedForeground : c.foreground}>
            {word} {unit.key}
            {unit.title ? ` · ${unit.title}` : ""}
          </T>
          {info || unit.filler || unit.recap || unit.upcoming ? (
            <Row gap={6}>
              {unit.upcoming ? (
                <T variant="caption" color={c.primary}>
                  Em breve
                </T>
              ) : null}
              {unit.filler ? (
                <T variant="caption" color={c.warning} style={{ fontFamily: Font.bold }}>
                  Filler
                </T>
              ) : null}
              {unit.recap ? (
                <T variant="caption" color={c.warning} style={{ fontFamily: Font.bold }}>
                  Recap
                </T>
              ) : null}
              {info ? (
                <T variant="caption" muted numberOfLines={1} style={{ flexShrink: 1 }}>
                  {info}
                </T>
              ) : null}
            </Row>
          ) : null}
        </View>
      </View>
    </Press>
  );
}

export function UnitsSection({
  title,
  entry,
  list,
  loading,
  error,
  onRetry,
  ensureEntry,
}: {
  title: Title;
  entry?: LibraryEntry;
  list?: UnitList;
  loading: boolean;
  error: unknown;
  onRetry: () => void;
  ensureEntry: () => string;
}) {
  const c = useColors();
  const toggleUnit = useLibrary((s) => s.toggleUnit);
  const markMany = useLibrary((s) => s.markMany);
  const unmarkMany = useLibrary((s) => s.unmarkMany);
  const [range, setRange] = useState(-1);
  const [hideDone, setHideDone] = useState(false);
  const [desc, setDesc] = useState(false);
  const [menu, setMenu] = useState<Unit | null>(null);

  const units = useMemo(() => list?.units ?? [], [list]);
  const ranges = useMemo(() => {
    const out: { start: number; end: number; label: string }[] = [];
    for (let i = 0; i < units.length; i += PAGE) {
      const a = units[i];
      const b = units[Math.min(units.length, i + PAGE) - 1];
      out.push({ start: i, end: Math.min(units.length, i + PAGE), label: `${a.key}–${b.key}` });
    }
    return out;
  }, [units]);

  // Start on the range containing the next unit.
  const done = entry?.done ?? {};
  const firstOpen = units.findIndex((u) => done[u.key] === undefined && !u.upcoming);
  const defaultRange = firstOpen >= 0 ? Math.floor(firstOpen / PAGE) : 0;
  const active = ranges.length ? Math.min(range === -1 ? defaultRange : range, ranges.length - 1) : 0;
  const cur = ranges[active];
  const chips = ranges.map((r, i) => ({ ...r, i }));
  if (desc) chips.reverse();
  let shown = cur ? units.slice(cur.start, cur.end) : [];
  if (desc) shown = [...shown].reverse();
  if (hideDone) shown = shown.filter((u) => done[u.key] === undefined);

  const word = unitWord(title.medium, true);
  const doneCount = Object.keys(done).length;

  const toggle = (u: Unit) => toggleUnit(ensureEntry(), u.key);
  const markUpTo = (u: Unit) => {
    const key = ensureEntry();
    const keys = units.filter((x) => x.number <= u.number && !x.upcoming).map((x) => x.key);
    markMany(key, keys);
    tap("success");
  };
  const unmarkFrom = (u: Unit) => {
    if (!entry) return;
    unmarkMany(entry.key, units.filter((x) => x.number >= u.number).map((x) => x.key));
  };

  return (
    <View>
      <SectionHeader title={`${word[0].toUpperCase()}${word.slice(1)}${units.length ? ` (${units.length})` : ""}`} />
      {loading ? (
        <View style={{ gap: 10 }}>
          {[0, 1, 2, 3].map((i) => (
            <Skeleton key={i} height={40} />
          ))}
        </View>
      ) : error && !units.length ? (
        <ErrorNote message={errorMessage(error)} onRetry={onRetry} />
      ) : !units.length ? (
        <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg, padding: 16, gap: 8 }, cardShadow(c, 0.7)]}>
          <T variant="small" muted>
            As fontes ainda não informam quantos {word} existem. Defina o total no menu (⋯) para acompanhar o progresso.
          </T>
        </View>
      ) : (
        <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg, paddingHorizontal: 14, paddingVertical: 10 }, cardShadow(c, 0.7)]}>
          <Row style={{ justifyContent: "space-between", marginBottom: 4 }}>
            <T variant="caption" muted>
              {doneCount} de {list?.total ?? units.length} · {list?.sources.map((s) => SOURCE_LABEL[s] ?? s).join(" + ")}
              {list?.estimated ? " · lista estimada" : ""}
            </T>
            <Row gap={14}>
              <Press onPress={() => setHideDone((v) => !v)} hitSlop={8} accessibilityLabel="Ocultar concluídos">
                <Ionicons name={hideDone ? "eye-off" : "eye-outline"} size={18} color={c.mutedForeground} />
              </Press>
              <Press onPress={() => setDesc((v) => !v)} hitSlop={8} accessibilityLabel="Inverter ordem">
                <Ionicons name={desc ? "arrow-up" : "arrow-down"} size={18} color={c.mutedForeground} />
              </Press>
            </Row>
          </Row>
          {ranges.length > 1 ? (
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8, paddingVertical: 8 }}>
              {chips.map((r) => (
                <Chip key={r.label} label={r.label} active={r.i === active} onPress={() => setRange(r.i)} />
              ))}
            </ScrollView>
          ) : null}
          {shown.map((u) => (
            <UnitRow key={u.key} unit={u} medium={title.medium} done={done[u.key] !== undefined} onToggle={() => toggle(u)} onLong={() => setMenu(u)} />
          ))}
          {!shown.length ? (
            <T variant="small" muted style={{ paddingVertical: 12, textAlign: "center" }}>
              Tudo marcado neste intervalo.
            </T>
          ) : null}
          <T variant="caption" muted style={{ textAlign: "center", paddingTop: 6 }}>
            Toque para marcar · segure para mais opções
          </T>
        </View>
      )}

      <Sheet open={!!menu} onClose={() => setMenu(null)} title={menu ? `${unitShort(title.medium)} ${menu.key}` : undefined}>
        {menu ? (
          <View style={{ gap: 2 }}>
            <SheetOption icon="checkmark-done" label={`Marcar até o ${menu.key}`} hint={`Marca todos os ${word} anteriores`} onPress={() => { markUpTo(menu); setMenu(null); }} />
            <SheetOption icon="checkmark-circle-outline" label={done[menu.key] !== undefined ? "Desmarcar" : "Marcar só este"} onPress={() => { toggle(menu); setMenu(null); }} />
            {entry ? (
              <SheetOption icon="arrow-undo-outline" label={`Desmarcar a partir do ${menu.key}`} onPress={() => { unmarkFrom(menu); setMenu(null); }} />
            ) : null}
            <View style={{ marginTop: 8 }}>
              <Pill label="Fechar" variant="outline" full onPress={() => setMenu(null)} />
            </View>
          </View>
        ) : null}
      </Sheet>
    </View>
  );
}
