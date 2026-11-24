import { useMemo, useState } from "react";
import { Image, ScrollView, View } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { Font, Space, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { addDays, dayKey, greeting, longDate, startOfDay, timeHM, weekOf } from "../../core/dates";
import { KIND_META, unitShort } from "../../domain/kinds";
import { minutesPerUnit, nextUnit, totalFor } from "../../domain/progress";
import { activeDays, dayTotal, entryStreak, streak } from "../../domain/stats";
import type { LibraryEntry } from "../../domain/types";
import { useAiring } from "../../queries/catalog";
import { useLibrary } from "../../state/library";
import { openTitle, useTitleName } from "../../ui/cover";
import { FAB, HeroCard, RoutineRow, WeekStrip } from "../../ui/home";
import { EmptyState, FadeIn, IconTile, Press, Row, Screen, SectionHeader, T } from "../../ui/primitives";
import { ProgressBar } from "../../ui/progress";

const hero = require("../../assets/images/hero.png");
const mascot = require("../../assets/images/mascot.png");

function RoutineItem({ entry, first, last }: { entry: LibraryEntry; first: boolean; last: boolean }) {
  const events = useLibrary((s) => s.events);
  const markNext = useLibrary((s) => s.markNext);
  const toggleUnit = useLibrary((s) => s.toggleUnit);
  const name = useTitleName(entry.title);
  const meta = KIND_META[entry.title.kind];
  const today = dayKey(new Date());
  const todayEvents = events.filter((e) => e.key === entry.key && dayKey(e.at) === today);
  const doneToday = todayEvents.length > 0;
  const total = totalFor(entry);
  const next = nextUnit(entry);
  const s = entryStreak(events, entry.key);
  const word = unitShort(entry.title.medium);
  const current = doneToday ? Number(todayEvents.at(-1)!.unit) + todayEvents.at(-1)!.count - 1 : next;

  const subtitle = [
    current ? `${word} ${current}${total ? ` de ${total}` : ""}` : "Tudo em dia",
    s > 0 ? `Sequência ${s} ${s === 1 ? "dia" : "dias"}` : meta.label,
  ].join(" · ");

  const onToggle = () => {
    if (doneToday) {
      const last = todayEvents.at(-1)!;
      if (last.count === 1) toggleUnit(entry.key, last.unit);
      else openTitle(entry.key);
    } else {
      const marked = markNext(entry.key);
      if (!marked) openTitle(entry.key);
    }
  };

  return (
    <RoutineRow
      first={first}
      last={last}
      done={doneToday}
      onToggle={onToggle}
      onPress={() => openTitle(entry.key)}
      tile={<IconTile icon={meta.icon} tile={meta.tile} size={44} />}
      title={name}
      subtitle={subtitle}
      meta={`${Math.round(minutesPerUnit(entry.title))} min`}
    />
  );
}

export default function HomeScreen() {
  const c = useColors();
  const settings = useLibrary((s) => s.settings);
  const entries = useLibrary((s) => s.entries);
  const events = useLibrary((s) => s.events);
  const [selected, setSelected] = useState(() => new Date());
  const selKey = dayKey(selected);
  const todayKey = dayKey(new Date());
  const week = useMemo(() => weekOf(new Date()), []);
  const marked = useMemo(() => activeDays(events), [events]);

  const list = useMemo(() => Object.values(entries), [entries]);
  const routine = useMemo(
    () =>
      list
        .filter((e) => e.status === "current" || e.status === "repeating")
        .sort((a, b) => b.updatedAt - a.updatedAt)
        .slice(0, 8),
    [list],
  );

  // Airing this week for releasing anime in the library.
  const airingIds = useMemo(
    () =>
      list
        .filter((e) => e.title.medium === "watch" && e.title.status === "releasing" && e.title.externalIds.anilist && e.status !== "dropped")
        .map((e) => Number(e.title.externalIds.anilist)),
    [list],
  );
  const from = week[0].getTime();
  const to = addDays(week[6], 1).getTime();
  const airing = useAiring(from, to, airingIds);
  const airingDays = useMemo(() => new Set((airing.data ?? []).map((a) => dayKey(a.at))), [airing.data]);
  const airingSel = (airing.data ?? []).filter((a) => dayKey(a.at) === selKey);
  const airingToday = (airing.data ?? []).filter((a) => dayKey(a.at) === todayKey);

  const dayEvents = useMemo(() => events.filter((e) => dayKey(e.at) === selKey).reverse(), [events, selKey]);
  const today = dayTotal(events, new Date());
  const st = streak(events);
  const isToday = selKey === todayKey;
  const isFuture = startOfDay(selected).getTime() > startOfDay(new Date()).getTime();

  // Hero priority: new episode today → continue → empty library.
  const continueEntry = routine[0];
  const continueName = useTitleName(continueEntry?.title ?? { title: "" });
  let heroProps: { title: string; text: string; action: string; onAction: () => void };
  if (airingToday.length) {
    const a = airingToday[0];
    const isOut = a.at <= Date.now();
    heroProps = {
      title: isOut ? "Episódio novo saiu!" : "Episódio novo hoje",
      text: `${a.title.title} · Ep ${a.unit} ${isOut ? "já disponível" : `às ${timeHM(a.at)}`}${airingToday.length > 1 ? ` e mais ${airingToday.length - 1}` : ""}`,
      action: "Ver episódio",
      onAction: () => openTitle(entries[a.key] ? a.key : a.title.key),
    };
  } else if (continueEntry) {
    const n = nextUnit(continueEntry);
    const w = continueEntry.title.medium === "watch" ? "episódio" : "capítulo";
    heroProps = {
      title: "Continue de onde parou",
      text: n ? `${continueName} · Próximo ${w}: ${n}` : `${continueName} · Você está em dia!`,
      action: "Continuar",
      onAction: () => openTitle(continueEntry.key),
    };
  } else {
    heroProps = {
      title: "Monte sua lista",
      text: "Busque animes, mangás e manhwas e acompanhe cada episódio e capítulo.",
      action: "Explorar",
      onAction: () => router.push("/explore"),
    };
  }

  const goalPct = Math.min(1, today.units / Math.max(1, settings.dailyGoal));
  const allMarked = useMemo(() => new Set([...marked, ...airingDays]), [marked, airingDays]);

  return (
    <Screen>
      <ScrollView contentContainerStyle={{ paddingHorizontal: Space.screen, paddingTop: 12, paddingBottom: 110 }} showsVerticalScrollIndicator={false}>
        {/* Greeting */}
        <FadeIn>
          <Row style={{ justifyContent: "space-between", alignItems: "flex-start" }}>
            <View style={{ flex: 1, gap: 4 }}>
              <Row gap={6} style={{ alignItems: "flex-end", flexWrap: "wrap" }}>
                <T variant="display">{greeting()}</T>
                {settings.name ? (
                  <T variant="h2" style={{ fontFamily: Font.medium, marginBottom: 3 }}>
                    {settings.name}
                  </T>
                ) : null}
              </Row>
              <T variant="small" muted>
                {longDate(new Date())}
              </T>
            </View>
            <Press onPress={() => router.push("/profile")} accessibilityLabel="Perfil">
              <View style={[{ width: 54, height: 54, borderRadius: 27, backgroundColor: c.hero, alignItems: "center", justifyContent: "center", overflow: "hidden" }, cardShadow(c, 0.6)]}>
                <Image source={mascot} style={{ width: 50, height: 50 }} resizeMode="contain" />
              </View>
            </Press>
          </Row>
        </FadeIn>

        {/* Week */}
        <FadeIn delay={60} style={{ marginTop: 24 }}>
          <WeekStrip days={week} selected={selKey} onSelect={setSelected} marked={allMarked} />
        </FadeIn>

        {/* Hero */}
        <FadeIn delay={120} style={{ marginTop: 22 }}>
          <HeroCard image={hero} {...heroProps} />
        </FadeIn>

        {/* Daily goal */}
        <FadeIn delay={160} style={{ marginTop: 16 }}>
          <View style={[{ backgroundColor: c.card, borderRadius: 22, padding: 14, flexDirection: "row", alignItems: "center", gap: 12 }, cardShadow(c, 0.7)]}>
            <IconTile icon="flame" tile="peach" size={40} />
            <View style={{ flex: 1, gap: 6 }}>
              <Row style={{ justifyContent: "space-between" }}>
                <T variant="label">
                  Meta de hoje · {Math.min(today.units, 999)}/{settings.dailyGoal}
                </T>
                <T variant="caption" muted>
                  {st > 0 ? `${st} ${st === 1 ? "dia" : "dias"} seguidos` : "Comece sua sequência"}
                </T>
              </Row>
              <ProgressBar value={goalPct} />
            </View>
          </View>
        </FadeIn>

        {/* Airing on selected day */}
        {airingSel.length ? (
          <FadeIn delay={180} style={{ marginTop: 26 }}>
            <SectionHeader title={isToday ? "Lançamentos de hoje" : "Lançamentos do dia"} />
            <View style={{ gap: 10 }}>
              {airingSel.map((a) => (
                <Press key={`${a.key}-${a.unit}`} onPress={() => openTitle(a.key)}>
                  <View style={[{ backgroundColor: c.card, borderRadius: 22, padding: 12, flexDirection: "row", alignItems: "center", gap: 12 }, cardShadow(c, 0.7)]}>
                    <IconTile icon="notifications-outline" tile="honey" size={44} />
                    <View style={{ flex: 1 }}>
                      <T variant="title" numberOfLines={1} style={{ fontSize: 15.5 }}>
                        {a.title.title}
                      </T>
                      <T variant="caption" muted>
                        Episódio {a.unit} · {a.at <= Date.now() ? "disponível" : "estreia"}
                      </T>
                    </View>
                    <View style={{ width: 1, alignSelf: "stretch", backgroundColor: c.border }} />
                    <View style={{ alignItems: "center", gap: 4, minWidth: 46 }}>
                      <Ionicons name="time" size={17} color={c.foreground} />
                      <T variant="caption" muted style={{ fontSize: 11 }}>
                        {timeHM(a.at)}
                      </T>
                    </View>
                  </View>
                </Press>
              ))}
            </View>
          </FadeIn>
        ) : null}

        {/* Routine / activity */}
        <FadeIn delay={220} style={{ marginTop: 26 }}>
          {isToday ? (
            <>
              <SectionHeader title="Rotina de hoje" action={routine.length ? "Ver tudo" : undefined} onAction={() => router.push("/library")} />
              {routine.length ? (
                <View>
                  {routine.map((e, i) => (
                    <RoutineItem key={e.key} entry={e} first={i === 0} last={i === routine.length - 1} />
                  ))}
                </View>
              ) : (
                <EmptyState
                  icon="sparkles-outline"
                  title="Nada na rotina ainda"
                  text="Obras marcadas como Assistindo ou Lendo aparecem aqui para você marcar o próximo episódio ou capítulo."
                  action="Buscar obra"
                  onAction={() => router.push("/search")}
                />
              )}
            </>
          ) : isFuture ? (
            !airingSel.length ? (
              <EmptyState icon="calendar-outline" title="Sem lançamentos nesse dia" text="Episódios das obras em lançamento da sua biblioteca aparecem aqui." tile="sky" />
            ) : null
          ) : (
            <>
              <SectionHeader title="Atividade do dia" />
              {dayEvents.length ? (
                <View>
                  {dayEvents.map((ev, i) => {
                    const e = entries[ev.key];
                    if (!e) return null;
                    const meta = KIND_META[e.title.kind];
                    const word = unitShort(e.title.medium);
                    const end = Number(ev.unit) + ev.count - 1;
                    return (
                      <RoutineRow
                        key={ev.id}
                        first={i === 0}
                        last={i === dayEvents.length - 1}
                        done
                        onToggle={() => openTitle(e.key)}
                        onPress={() => openTitle(e.key)}
                        tile={<IconTile icon={meta.icon} tile={meta.tile} size={44} />}
                        title={e.title.title}
                        subtitle={ev.count > 1 ? `${word} ${ev.unit}–${end} · ${ev.count} de uma vez` : `${word} ${ev.unit} · ${timeHM(ev.at)}`}
                        meta={`${Math.round(ev.minutes)} min`}
                      />
                    );
                  })}
                </View>
              ) : (
                <EmptyState icon="moon-outline" title="Dia de descanso" text="Nenhum episódio ou capítulo registrado nesse dia." tile="lilac" />
              )}
            </>
          )}
        </FadeIn>
      </ScrollView>
      <FAB label="Adicionar obra" onPress={() => router.push("/search")} />
    </Screen>
  );
}
