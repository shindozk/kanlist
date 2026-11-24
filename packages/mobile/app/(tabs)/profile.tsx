import { useEffect, useMemo, useState, type ReactNode } from "react";
import { Image, ScrollView, Switch, TextInput, View } from "react-native";
import * as Clipboard from "expo-clipboard";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { cacheCount, clearCache } from "../../core/cache";
import { formatMinutes } from "../../core/dates";
import { KIND_META } from "../../domain/kinds";
import { bestStreak, lastDays, libraryStats, streak } from "../../domain/stats";
import type { Settings, ThemeMode, TitleLanguage } from "../../domain/types";
import { useLibrary } from "../../state/library";
import { Card, Chip, FadeIn, IconTile, Pill, Press, Row, Screen, SectionHeader, T, tap } from "../../ui/primitives";
import { StatTile } from "../../ui/home";
import { Sheet } from "../../ui/sheet";

const LANGS: { id: string; label: string }[] = [
  { id: "pt-br", label: "Português" },
  { id: "en", label: "Inglês" },
  { id: "es-la", label: "Espanhol" },
  { id: "ja", label: "Japonês" },
];

function SettingRow({ icon, tile, label, hint, right, onPress }: { icon: React.ComponentProps<typeof Ionicons>["name"]; tile: string; label: string; hint?: string; right?: ReactNode; onPress?: () => void }) {
  const c = useColors();
  const body = (
    <Row gap={14} style={{ paddingVertical: 10 }}>
      <IconTile icon={icon} tile={tile} size={38} radius={12} />
      <View style={{ flex: 1 }}>
        <T variant="title" style={{ fontSize: 15 }}>
          {label}
        </T>
        {hint ? (
          <T variant="caption" muted>
            {hint}
          </T>
        ) : null}
      </View>
      {right ?? (onPress ? <Ionicons name="chevron-forward" size={18} color={c.mutedForeground} /> : null)}
    </Row>
  );
  return onPress ? <Press onPress={onPress}>{body}</Press> : body;
}

function Toggle({ value, onChange }: { value: boolean; onChange: (v: boolean) => void }) {
  const c = useColors();
  return (
    <Switch
      value={value}
      onValueChange={(v) => {
        tap("select");
        onChange(v);
      }}
      trackColor={{ true: c.primary, false: c.track }}
      thumbColor="#FFFFFF"
    />
  );
}

function Segmented<V extends string>({ value, options, onChange }: { value: V; options: [V, string][]; onChange: (v: V) => void }) {
  const c = useColors();
  return (
    <Row gap={0} style={{ backgroundColor: c.muted, borderRadius: Radius.pill, padding: 3 }}>
      {options.map(([id, label]) => (
        <Press
          key={id}
          onPress={() => {
            tap("select");
            onChange(id);
          }}
          style={{ flex: 1, height: 34, borderRadius: Radius.pill, alignItems: "center", justifyContent: "center", backgroundColor: value === id ? c.card : "transparent" }}
        >
          <T variant="label" color={value === id ? c.foreground : c.mutedForeground} style={{ fontSize: 12.5 }}>
            {label}
          </T>
        </Press>
      ))}
    </Row>
  );
}

function ActivityChart() {
  const c = useColors();
  const events = useLibrary((s) => s.events);
  const goal = useLibrary((s) => s.settings.dailyGoal);
  const days = useMemo(() => lastDays(events, 14), [events]);
  const max = Math.max(goal, ...days.map((d) => d.units), 1);
  return (
    <View>
      <Row gap={6} style={{ alignItems: "flex-end", height: 110 }}>
        {days.map((d, i) => {
          const h = Math.max(4, (d.units / max) * 100);
          const hit = d.units >= goal;
          return (
            <View key={d.day} style={{ flex: 1, alignItems: "center", justifyContent: "flex-end", height: 110 }}>
              <View style={{ width: "100%", height: h, borderRadius: 6, backgroundColor: d.units ? (hit ? c.primary : c.hero) : c.track, opacity: i === days.length - 1 ? 1 : 0.92 }} />
            </View>
          );
        })}
      </Row>
      <Row style={{ justifyContent: "space-between", marginTop: 8 }}>
        <T variant="caption" muted>
          14 dias atrás
        </T>
        <T variant="caption" muted>
          Hoje
        </T>
      </Row>
    </View>
  );
}

export default function ProfileScreen() {
  const c = useColors();
  const settings = useLibrary((s) => s.settings);
  const entries = useLibrary((s) => s.entries);
  const events = useLibrary((s) => s.events);
  const updateSettings = useLibrary((s) => s.updateSettings);
  const exportJson = useLibrary((s) => s.exportJson);
  const importJson = useLibrary((s) => s.importJson);
  const resetAll = useLibrary((s) => s.resetAll);
  const stats = useMemo(() => libraryStats(Object.values(entries)), [entries]);
  const cur = streak(events);
  const best = bestStreak(events);

  const [name, setName] = useState(settings.name);
  const [cache, setCache] = useState<number | null>(null);
  const [importOpen, setImportOpen] = useState(false);
  const [importText, setImportText] = useState("");
  const [msg, setMsg] = useState<string | null>(null);
  const [confirmReset, setConfirmReset] = useState(false);

  useEffect(() => {
    void cacheCount().then(setCache).catch(() => setCache(null));
  }, []);

  const set = (p: Partial<Settings>) => updateSettings(p);
  const flash = (m: string) => {
    setMsg(m);
    setTimeout(() => setMsg(null), 2600);
  };

  return (
    <Screen>
      <ScrollView contentContainerStyle={{ padding: Space.screen, paddingTop: 12, paddingBottom: 60, gap: 26 }} showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
        <FadeIn>
          <Row gap={14}>
            <View style={{ width: 64, height: 64, borderRadius: 32, backgroundColor: c.hero, overflow: "hidden", alignItems: "center", justifyContent: "center" }}>
              <Image source={require("../../assets/images/mascot.png")} style={{ width: 60, height: 60 }} resizeMode="contain" />
            </View>
            <View style={{ flex: 1 }}>
              <T variant="h1">{settings.name || "Otaku"}</T>
              <T variant="small" muted>
                {stats.entries} obras · sequência de {cur} {cur === 1 ? "dia" : "dias"}
              </T>
            </View>
          </Row>
        </FadeIn>

        {/* Stats */}
        <FadeIn delay={60} style={{ gap: 12 }}>
          <Row gap={12}>
            <StatTile icon="tv" tile="peach" value={String(stats.episodes)} label="Episódios" />
            <StatTile icon="book" tile="sage" value={String(stats.chapters)} label="Capítulos" />
          </Row>
          <Row gap={12}>
            <StatTile icon="time" tile="sky" value={formatMinutes(stats.minutes)} label="Tempo total" />
            <StatTile icon="flame" tile="coral" value={`${best}`} label="Melhor sequência" />
          </Row>
        </FadeIn>

        <Card style={{ padding: 18 }}>
          <SectionHeader title="Atividade" />
          <ActivityChart />
          <Row gap={14} style={{ marginTop: 14 }}>
            <Row gap={6}>
              <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: c.primary }} />
              <T variant="caption" muted>
                Meta batida
              </T>
            </Row>
            <Row gap={6}>
              <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: c.hero }} />
              <T variant="caption" muted>
                Abaixo da meta
              </T>
            </Row>
          </Row>
        </Card>

        {stats.byKind.length ? (
          <Card style={{ padding: 18, gap: 10 }}>
            <SectionHeader title="Por tipo" />
            {stats.byKind.map((k) => (
              <Row key={k.kind} gap={12}>
                <IconTile icon={KIND_META[k.kind].icon} tile={KIND_META[k.kind].tile} size={34} radius={11} />
                <T variant="title" style={{ flex: 1, fontSize: 15 }}>
                  {KIND_META[k.kind].plural}
                </T>
                <T variant="small" muted>
                  {k.count} · {k.units} {KIND_META[k.kind].medium === "watch" ? "eps" : "caps"}
                </T>
              </Row>
            ))}
            {stats.meanRating ? (
              <T variant="caption" muted style={{ marginTop: 4 }}>
                Nota média: ★ {(stats.meanRating / 2).toFixed(1).replace(".", ",")} · {stats.completed} completas · {stats.favorites} favoritas
              </T>
            ) : null}
          </Card>
        ) : null}

        {/* Settings */}
        <View>
          <SectionHeader title="Preferências" />
          <Card style={{ paddingHorizontal: 16, paddingVertical: 8 }}>
            <View style={{ paddingVertical: 10, gap: 8 }}>
              <T variant="label" muted>
                Seu nome
              </T>
              <TextInput
                value={name}
                onChangeText={setName}
                onBlur={() => set({ name: name.trim() })}
                onSubmitEditing={() => set({ name: name.trim() })}
                placeholder="Como te chamamos?"
                placeholderTextColor={c.mutedForeground}
                style={{ borderRadius: Radius.md, backgroundColor: c.muted, paddingHorizontal: 14, paddingVertical: 10, fontFamily: Font.medium, fontSize: 15, color: c.foreground }}
              />
            </View>
            <View style={{ paddingVertical: 10, gap: 8 }}>
              <T variant="label" muted>
                Tema
              </T>
              <Segmented<ThemeMode>
                value={settings.theme}
                onChange={(v) => set({ theme: v })}
                options={[
                  ["system", "Sistema"],
                  ["light", "Claro"],
                  ["dark", "Escuro"],
                ]}
              />
            </View>
            <View style={{ paddingVertical: 10, gap: 8 }}>
              <T variant="label" muted>
                Idioma dos títulos
              </T>
              <Segmented<TitleLanguage>
                value={settings.titleLanguage}
                onChange={(v) => set({ titleLanguage: v })}
                options={[
                  ["romaji", "Romaji"],
                  ["english", "Inglês"],
                  ["native", "Original"],
                ]}
              />
            </View>
            <SettingRow
              icon="flag"
              tile="honey"
              label="Meta diária"
              hint={`${settings.dailyGoal} episódios/capítulos por dia`}
              right={
                <Row gap={8}>
                  <Press hitSlop={8} onPress={() => set({ dailyGoal: Math.max(1, settings.dailyGoal - 1) })} accessibilityLabel="Diminuir meta">
                    <Ionicons name="remove-circle-outline" size={26} color={c.mutedForeground} />
                  </Press>
                  <T variant="title" style={{ minWidth: 22, textAlign: "center" }}>
                    {settings.dailyGoal}
                  </T>
                  <Press hitSlop={8} onPress={() => set({ dailyGoal: Math.min(50, settings.dailyGoal + 1) })} accessibilityLabel="Aumentar meta">
                    <Ionicons name="add-circle" size={26} color={c.primary} />
                  </Press>
                </Row>
              }
            />
            <View style={{ paddingVertical: 10, gap: 8 }}>
              <T variant="label" muted>
                Idiomas dos capítulos (MangaDex)
              </T>
              <Row gap={8} style={{ flexWrap: "wrap" }}>
                {LANGS.map((l) => {
                  const on = settings.chapterLangs.includes(l.id);
                  return (
                    <Chip
                      key={l.id}
                      label={l.label}
                      active={on}
                      onPress={() => {
                        const next = on ? settings.chapterLangs.filter((x) => x !== l.id) : [...settings.chapterLangs, l.id];
                        if (next.length) set({ chapterLangs: next });
                      }}
                    />
                  );
                })}
              </Row>
            </View>
            <SettingRow icon="language" tile="sky" label="Traduzir sinopses" hint="Inglês → português automaticamente" right={<Toggle value={settings.autoTranslate} onChange={(v) => set({ autoTranslate: v })} />} />
            <SettingRow icon="phone-portrait" tile="mint" label="Vibração" right={<Toggle value={settings.haptics} onChange={(v) => set({ haptics: v })} />} />
            <SettingRow icon="eye-off" tile="blush" label="Conteúdo adulto" hint="Mostra obras +18 na busca" right={<Toggle value={settings.showAdult} onChange={(v) => set({ showAdult: v })} />} />
          </Card>
        </View>

        <View>
          <SectionHeader title="Fontes de dados" />
          <Card style={{ paddingHorizontal: 16, paddingVertical: 8 }}>
            <SettingRow icon="server" tile="lilac" label="AniList" hint="Fonte principal · sempre ativa" />
            <SettingRow icon="book" tile="sage" label="MangaDex" hint="Capítulos e obras de leitura" right={<Toggle value={settings.sources.mangadex} onChange={(v) => set({ sources: { ...settings.sources, mangadex: v } })} />} />
            <SettingRow icon="film" tile="peach" label="Kitsu" hint="Títulos e miniaturas dos episódios" right={<Toggle value={settings.sources.kitsu} onChange={(v) => set({ sources: { ...settings.sources, kitsu: v } })} />} />
            <SettingRow icon="planet" tile="sky" label="Jikan (MyAnimeList)" hint="Fillers e recaps" right={<Toggle value={settings.sources.jikan} onChange={(v) => set({ sources: { ...settings.sources, jikan: v } })} />} />
            <SettingRow icon="chatbubbles" tile="honey" label="MyMemory" hint="Tradução das sinopses" right={<Toggle value={settings.sources.translate} onChange={(v) => set({ sources: { ...settings.sources, translate: v } })} />} />
          </Card>
        </View>

        <View>
          <SectionHeader title="Dados" />
          <Card style={{ paddingHorizontal: 16, paddingVertical: 8 }}>
            <SettingRow
              icon="cloud-upload"
              tile="sage"
              label="Exportar backup"
              hint="Copia sua biblioteca em JSON"
              onPress={async () => {
                await Clipboard.setStringAsync(exportJson());
                tap("success");
                flash("Backup copiado para a área de transferência.");
              }}
            />
            <SettingRow
              icon="cloud-download"
              tile="sky"
              label="Importar backup"
              hint="Cole um JSON exportado do Kanlist"
              onPress={async () => {
                const clip = await Clipboard.getStringAsync().catch(() => "");
                setImportText(clip.trim().startsWith("{") ? clip : "");
                setImportOpen(true);
              }}
            />
            <SettingRow
              icon="trash-bin"
              tile="sand"
              label="Limpar cache"
              hint={cache !== null ? `${cache} respostas salvas` : "Respostas das APIs salvas"}
              onPress={async () => {
                await clearCache();
                setCache(0);
                flash("Cache limpo.");
              }}
            />
            <SettingRow icon="warning" tile="coral" label="Apagar tudo" hint="Remove biblioteca, histórico e ajustes" onPress={() => setConfirmReset(true)} />
          </Card>
          {msg ? (
            <T variant="small" color={c.success} style={{ marginTop: 10, textAlign: "center" }}>
              {msg}
            </T>
          ) : null}
        </View>

        <T variant="caption" muted style={{ textAlign: "center" }}>
          Kanlist · tudo fica salvo só neste aparelho
        </T>
      </ScrollView>

      <Sheet open={importOpen} onClose={() => setImportOpen(false)} title="Importar backup">
        <TextInput
          value={importText}
          onChangeText={setImportText}
          multiline
          placeholder='{"version":2,...}'
          placeholderTextColor={c.mutedForeground}
          style={{ minHeight: 140, maxHeight: 240, textAlignVertical: "top", borderRadius: Radius.md, backgroundColor: c.card, borderWidth: 1, borderColor: c.border, padding: 14, fontFamily: Font.medium, fontSize: 13, color: c.foreground }}
        />
        <Row gap={10} style={{ marginTop: 14 }}>
          {(["merge", "replace"] as const).map((mode) => (
            <View key={mode} style={{ flex: 1 }}>
              <Pill
                label={mode === "merge" ? "Mesclar" : "Substituir"}
                variant={mode === "merge" ? "brown" : "outline"}
                full
                disabled={!importText.trim()}
                onPress={() => {
                  try {
                    const n = importJson(importText, mode);
                    setImportOpen(false);
                    flash(`${n} obras importadas.`);
                  } catch {
                    flash("JSON inválido — nada foi alterado.");
                    setImportOpen(false);
                  }
                }}
              />
            </View>
          ))}
        </Row>
      </Sheet>
      <Sheet open={confirmReset} onClose={() => setConfirmReset(false)} title="Apagar tudo?">
        <T variant="small" muted style={{ marginBottom: 16 }}>
          Isso remove permanentemente sua biblioteca, coleções e histórico deste aparelho. Exporte um backup antes se quiser guardar.
        </T>
        <Pill
          label="Apagar definitivamente"
          variant="danger"
          full
          onPress={async () => {
            setConfirmReset(false);
            await resetAll();
          }}
        />
      </Sheet>
    </Screen>
  );
}
