import { useCallback, useState } from "react";
import { Platform, ScrollView, View } from "react-native";
import { Stack, useLocalSearchParams } from "expo-router";
import { Image } from "expo-image";
import { LinearGradient } from "expo-linear-gradient";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { errorMessage } from "../../core/errors";
import { formatScore } from "../../core/text";
import { formatMinutes, relativeFromNow } from "../../core/dates";
import { KIND_META, RELEASE_LABEL, STATUS_ICON, statusLabel, unitShort, unitWord } from "../../domain/kinds";
import { doneCount, nextUnit, remainingMinutes, totalFor } from "../../domain/progress";
import { entryStreak } from "../../domain/stats";
import type { Title, TitleDetail } from "../../domain/types";
import { useTitleDetail, useUnits } from "../../queries/catalog";
import { useEntry, useLibrary } from "../../state/library";
import { Cover, useTitleName } from "../../ui/cover";
import { Badge, Card, ErrorNote, FadeIn, IconButton, Pill, Press, Row, Screen, Skeleton, T, TopBar, tap } from "../../ui/primitives";
import { ProgressRing } from "../../ui/progress";
import { MoreSheet, NotesSheet, StatusSheet, Stars } from "../../features/title/EntrySheets";
import { UnitsSection } from "../../features/title/UnitsSection";
import { Characters, InfoGrid, Links, Related, Synopsis } from "../../features/title/sections";

/** Strip detail-only fields so the library stores a light snapshot. */
function toTitle(d: TitleDetail): Title {
  const { relations: _r, recommendations: _rec, characters: _c, links: _l, tags: _t, sources: _s, streaming: _st, startDate: _sd, endDate: _ed, ...title } = d;
  return title;
}

function Loading() {
  return (
    <View style={{ paddingHorizontal: Space.screen, gap: 18, paddingTop: 12 }}>
      <Skeleton height={180} radius={Radius.xl} />
      <Row gap={16}>
        <Skeleton width={110} height={156} radius={Radius.lg} />
        <View style={{ flex: 1, gap: 10 }}>
          <Skeleton height={24} />
          <Skeleton width="60%" height={18} />
          <Skeleton width="40%" height={18} />
        </View>
      </Row>
      <Skeleton height={120} radius={Radius.lg} />
      <Skeleton height={260} radius={Radius.lg} />
    </View>
  );
}

export default function TitleScreen() {
  const { key: rawKey } = useLocalSearchParams<{ key: string }>();
  const key = rawKey ? decodeURIComponent(String(rawKey)) : undefined;
  const detailQ = useTitleDetail(key);
  const entry = useEntry(key);
  const d = detailQ.data;

  if (!d) {
    // Library entries still render offline from their snapshot.
    return (
      <Screen>
        <Stack.Screen options={{ headerShown: false }} />
        <TopBar title={entry?.title.title ?? ""} />
        {detailQ.isError && !entry ? (
          <View style={{ paddingHorizontal: Space.screen, paddingTop: 12 }}>
            <ErrorNote message={errorMessage(detailQ.error)} onRetry={() => detailQ.refetch()} />
          </View>
        ) : entry && detailQ.isError ? (
          <Offline title={entry.title} error={detailQ.error} onRetry={() => detailQ.refetch()} />
        ) : (
          <Loading />
        )}
      </Screen>
    );
  }
  return <Detail d={d} />;
}

function Offline({ title, error, onRetry }: { title: Title; error: unknown; onRetry: () => void }) {
  const d: TitleDetail = { ...title, relations: [], recommendations: [], characters: [], links: [], tags: [], sources: [title.source] };
  return (
    <View style={{ flex: 1 }}>
      <View style={{ paddingHorizontal: Space.screen, paddingBottom: 8 }}>
        <ErrorNote message={`Sem conexão com as fontes. Mostrando dados salvos. (${errorMessage(error)})`} onRetry={onRetry} />
      </View>
      <Detail d={d} embedded />
    </View>
  );
}

function Detail({ d, embedded }: { d: TitleDetail; embedded?: boolean }) {
  const c = useColors();
  const entry = useEntry(d.key);
  const add = useLibrary((s) => s.add);
  const setStatus = useLibrary((s) => s.setStatus);
  const update = useLibrary((s) => s.update);
  const markNext = useLibrary((s) => s.markNext);
  const events = useLibrary((s) => s.events);
  const name = useTitleName(d);
  const meta = KIND_META[d.kind];
  const unitsQ = useUnits(d, d);

  const [statusOpen, setStatusOpen] = useState(false);
  const [notesOpen, setNotesOpen] = useState(false);
  const [moreOpen, setMoreOpen] = useState(false);

  const ensureEntry = useCallback(() => {
    if (!useLibrary.getState().entries[d.key]) add(toTitle(d), "current");
    return d.key;
  }, [add, d]);

  const total = entry ? totalFor(entry) : (d.totalUnits ?? unitsQ.data?.total);
  const done = entry ? doneCount(entry) : 0;
  const next = entry ? nextUnit(entry) : 1;
  const remaining = entry ? remainingMinutes(entry) : undefined;
  const streakDays = entry ? entryStreak(events, entry.key) : 0;
  const word = unitWord(d.medium, true);

  const header = (
    <>
      {/* Banner */}
      <View style={{ marginHorizontal: Space.screen, height: 170, borderRadius: Radius.xl, overflow: "hidden", backgroundColor: c.hero }}>
        {d.banner || d.cover ? (
          <Image source={{ uri: d.banner ?? d.cover }} style={{ width: "100%", height: "100%" }} contentFit="cover" blurRadius={d.banner ? 0 : 18} transition={200} />
        ) : null}
        <LinearGradient colors={["transparent", "rgba(0,0,0,0.35)"]} style={{ position: "absolute", left: 0, right: 0, bottom: 0, height: 90 }} />
      </View>

      {/* Cover + title */}
      <View style={{ flexDirection: "row", gap: 16, paddingHorizontal: Space.screen + 14, marginTop: -64 }}>
        <View style={[{ borderRadius: Radius.lg, borderWidth: 3, borderColor: c.background }, cardShadow(c, 1.4)]}>
          <Cover title={d} width={104} height={148} radius={Radius.lg - 3} />
        </View>
        <View style={{ flex: 1, paddingTop: 72, gap: 6 }}>
          <T variant="h2" numberOfLines={3}>
            {name}
          </T>
          {d.titleNative && d.titleNative !== name ? (
            <T variant="caption" muted numberOfLines={1} style={Platform.OS === "web" ? { fontFamily: "system-ui, sans-serif" } : undefined}>
              {d.titleNative}
            </T>
          ) : null}
        </View>
      </View>

      <Row gap={6} style={{ flexWrap: "wrap", paddingHorizontal: Space.screen, marginTop: 14 }}>
        <Badge label={meta.label} tile={meta.tile} icon={meta.icon} />
        {d.status !== "unknown" ? <Badge label={RELEASE_LABEL[d.status]} /> : null}
        {d.year ? <Badge label={String(d.year)} /> : null}
        {d.score ? <Badge label={`★ ${formatScore(d.score)}`} tile="honey" /> : null}
        {total ? <Badge label={`${total} ${word}`} /> : null}
      </Row>

      {d.nextAiring ? (
        <View style={{ paddingHorizontal: Space.screen, marginTop: 12 }}>
          <Row gap={8} style={{ backgroundColor: c.accent, borderRadius: Radius.md, paddingHorizontal: 14, paddingVertical: 10 }}>
            <Ionicons name="notifications" size={16} color={c.primary} />
            <T variant="small" color={c.accentForeground} style={{ fontFamily: Font.semibold, flex: 1 }}>
              {unitShort(d.medium)} {d.nextAiring.unit} {relativeFromNow(d.nextAiring.at)}
            </T>
          </Row>
        </View>
      ) : null}

      {/* Tracking card */}
      <View style={{ paddingHorizontal: Space.screen, marginTop: 18 }}>
        <Card style={{ padding: 18, gap: 16 }}>
          {entry ? (
            <>
              <Row gap={16}>
                <ProgressRing
                  value={total ? done / total : 0}
                  size={86}
                  label={total ? `${Math.round((done / total) * 100)}%` : String(done)}
                  sublabel={total ? `${done}/${total}` : word}
                />
                <View style={{ flex: 1, gap: 6 }}>
                  <Press
                    onPress={() => setStatusOpen(true)}
                    style={{ flexDirection: "row", alignItems: "center", gap: 6, alignSelf: "flex-start", backgroundColor: c.ink, borderRadius: Radius.pill, paddingHorizontal: 14, height: 34 }}
                  >
                    <Ionicons name={STATUS_ICON[entry.status]} size={15} color={c.inkForeground} />
                    <T variant="label" color={c.inkForeground}>
                      {statusLabel(entry.status, d.medium)}
                    </T>
                    <Ionicons name="chevron-down" size={14} color={c.inkForeground} />
                  </Press>
                  <T variant="caption" muted>
                    {[
                      streakDays > 1 ? `Sequência de ${streakDays} dias` : null,
                      remaining ? `${formatMinutes(remaining)} restantes` : null,
                      entry.repeatCount ? `${entry.repeatCount}× revisto` : null,
                    ]
                      .filter(Boolean)
                      .join(" · ") || "Bora começar"}
                  </T>
                </View>
              </Row>
              {next !== undefined ? (
                <Pill
                  label={`Marcar ${unitShort(d.medium).toLowerCase()} ${next}`}
                  icon="checkmark"
                  full
                  onPress={() => {
                    const n = markNext(entry.key);
                    if (n) tap("success");
                  }}
                />
              ) : (
                <Row gap={8} style={{ justifyContent: "center" }}>
                  <Ionicons name="trophy" size={18} color={c.primary} />
                  <T variant="label">Tudo em dia!</T>
                </Row>
              )}
              <Row style={{ justifyContent: "space-between" }}>
                <Stars value={entry.rating} onChange={(v) => update(entry.key, { rating: v })} />
                <Row gap={8}>
                  <IconButton
                    icon={entry.favorite ? "heart" : "heart-outline"}
                    color={entry.favorite ? c.destructive : undefined}
                    size={38}
                    label="Favoritar"
                    onPress={() => {
                      tap("select");
                      update(entry.key, { favorite: !entry.favorite });
                    }}
                  />
                  <IconButton icon={entry.notes ? "document-text" : "document-text-outline"} size={38} label="Anotações" onPress={() => setNotesOpen(true)} />
                </Row>
              </Row>
            </>
          ) : (
            <>
              <T variant="small" muted>
                Adicione à sua lista para marcar {word} e acompanhar o progresso.
              </T>
              <Row gap={10}>
                <View style={{ flex: 1 }}>
                  <Pill label={statusLabel("current", d.medium)} icon="play" full onPress={() => add(toTitle(d), "current")} />
                </View>
                <View style={{ flex: 1 }}>
                  <Pill label={statusLabel("planning", d.medium)} icon="bookmark-outline" variant="outline" full onPress={() => add(toTitle(d), "planning")} />
                </View>
              </Row>
            </>
          )}
        </Card>
      </View>
    </>
  );

  const content = (
    <View style={{ gap: 30, paddingBottom: 48 }}>
      <FadeIn>{header}</FadeIn>
      <View style={{ paddingHorizontal: Space.screen }}>
        <UnitsSection
          title={d}
          entry={entry}
          list={unitsQ.data}
          loading={unitsQ.isLoading}
          error={unitsQ.error}
          onRetry={() => unitsQ.refetch()}
          ensureEntry={ensureEntry}
        />
      </View>
      <View style={{ paddingHorizontal: Space.screen }}>
        <Synopsis d={d} />
      </View>
      <View style={{ paddingHorizontal: Space.screen }}>
        <InfoGrid d={d} />
      </View>
      <Characters d={d} />
      <Related d={d} />
      <View style={{ paddingHorizontal: Space.screen }}>
        <Links d={d} />
      </View>
    </View>
  );

  const sheets = (
    <>
      <StatusSheet
        open={statusOpen}
        onClose={() => setStatusOpen(false)}
        title={d}
        current={entry?.status}
        onPick={(s) => (entry ? setStatus(entry.key, s) : add(toTitle(d), s))}
      />
      {entry ? <NotesSheet open={notesOpen} onClose={() => setNotesOpen(false)} entry={entry} /> : null}
      <MoreSheet open={moreOpen} onClose={() => setMoreOpen(false)} entry={entry} title={d} />
    </>
  );

  if (embedded) {
    return (
      <ScrollView showsVerticalScrollIndicator={false}>
        {content}
        {sheets}
      </ScrollView>
    );
  }

  return (
    <Screen>
      <Stack.Screen options={{ headerShown: false }} />
      <TopBar title="" right={<IconButton icon="ellipsis-horizontal" label="Mais opções" onPress={() => setMoreOpen(true)} />} />
      <ScrollView showsVerticalScrollIndicator={false}>{content}</ScrollView>
      {sheets}
    </Screen>
  );
}
