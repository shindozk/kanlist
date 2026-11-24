import { useState } from "react";
import { FlatList, Linking, View } from "react-native";
import { Image } from "expo-image";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, Space, cardShadow } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { formatMinutes, shortDate } from "../../core/dates";
import { KIND_META, RELEASE_LABEL, genrePt, unitWord } from "../../domain/kinds";
import type { TitleDetail } from "../../domain/types";
import { useTranslation } from "../../queries/catalog";
import { useLibrary } from "../../state/library";
import { PosterCard } from "../../ui/cover";
import { Badge, Press, Row, SectionHeader, T } from "../../ui/primitives";

// ---------------------------------------------------------------- synopsis
export function Synopsis({ d }: { d: TitleDetail }) {
  const c = useColors();
  const settings = useLibrary((s) => s.settings);
  const wantsPt = d.synopsisLang === "en" && settings.autoTranslate && settings.sources.translate;
  const [showOriginal, setShowOriginal] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const tr = useTranslation(d.key, d.synopsis, wantsPt);
  if (!d.synopsis) return null;

  const translated = wantsPt && tr.data && !showOriginal;
  const text = translated ? tr.data! : d.synopsis;
  return (
    <View>
      <SectionHeader title="Sinopse" />
      <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg, padding: 16, gap: 10 }, cardShadow(c, 0.7)]}>
        <T variant="body" numberOfLines={expanded ? undefined : 6} style={{ lineHeight: 22 }}>
          {text}
        </T>
        <Row style={{ justifyContent: "space-between" }}>
          <Press onPress={() => setExpanded((v) => !v)} hitSlop={8}>
            <T variant="label" color={c.primary}>
              {expanded ? "Mostrar menos" : "Ler mais"}
            </T>
          </Press>
          {wantsPt ? (
            tr.isLoading ? (
              <T variant="caption" muted>
                Traduzindo…
              </T>
            ) : tr.data ? (
              <Press onPress={() => setShowOriginal((v) => !v)} hitSlop={8}>
                <Row gap={4}>
                  <Ionicons name="language-outline" size={14} color={c.mutedForeground} />
                  <T variant="caption" muted>
                    {showOriginal ? "Ver tradução" : "Ver original (EN)"}
                  </T>
                </Row>
              </Press>
            ) : (
              <T variant="caption" muted>
                Tradução indisponível
              </T>
            )
          ) : null}
        </Row>
      </View>
    </View>
  );
}

// ---------------------------------------------------------------- info grid
export function InfoGrid({ d }: { d: TitleDetail }) {
  const c = useColors();
  const meta = KIND_META[d.kind];
  const rows: [string, string | undefined][] = [
    ["Tipo", meta.label + (d.format ? ` · ${d.format.replace(/_/g, " ")}` : "")],
    ["Status", RELEASE_LABEL[d.status]],
    [unitWord(d.medium, true).replace(/^./, (x) => x.toUpperCase()), d.totalUnits ? String(d.totalUnits) : d.releasedUnits ? `${d.releasedUnits}+` : "?"],
    ["Volumes", d.totalVolumes ? String(d.totalVolumes) : undefined],
    ["Duração", d.unitDuration ? `${d.unitDuration} min/ep` : undefined],
    ["Total", d.unitDuration && d.totalUnits ? formatMinutes(d.unitDuration * d.totalUnits) : undefined],
    ["Temporada", d.season && d.year ? `${d.season} ${d.year}` : undefined],
    ["Início", d.startDate ? shortDate(d.startDate) : undefined],
    ["Fim", d.endDate ? shortDate(d.endDate) : undefined],
    ["Estúdio", d.studios?.length ? d.studios.join(", ") : undefined],
    ["Autores", d.authors?.length ? d.authors.join(", ") : undefined],
    ["País", d.country],
  ];
  const visible = rows.filter(([, v]) => !!v) as [string, string][];
  return (
    <View>
      <SectionHeader title="Informações" />
      <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg, padding: 6, flexDirection: "row", flexWrap: "wrap" }, cardShadow(c, 0.7)]}>
        {visible.map(([k, v]) => (
          <View key={k} style={{ width: "50%", padding: 10, gap: 2 }}>
            <T variant="caption" muted>
              {k}
            </T>
            <T variant="label" numberOfLines={2}>
              {v}
            </T>
          </View>
        ))}
      </View>
      {d.genres.length || d.tags.length ? (
        <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 6, marginTop: 14 }}>
          {d.genres.map((g) => (
            <Badge key={g} label={genrePt(g)} tile={meta.tile} />
          ))}
          {d.tags.map((t) => (
            <Badge key={t} label={t} />
          ))}
        </View>
      ) : null}
    </View>
  );
}

// ---------------------------------------------------------------- characters
export function Characters({ d }: { d: TitleDetail }) {
  const c = useColors();
  if (!d.characters.length) return null;
  return (
    <View>
      <View style={{ paddingHorizontal: Space.screen }}>
        <SectionHeader title="Personagens" />
      </View>
      <FlatList
        horizontal
        data={d.characters}
        keyExtractor={(x, i) => `${x.name}-${i}`}
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 14 }}
        renderItem={({ item }) => (
          <View style={{ width: 78, alignItems: "center", gap: 6 }}>
            <Image source={item.image ? { uri: item.image } : undefined} style={{ width: 66, height: 66, borderRadius: 33, backgroundColor: c.muted }} contentFit="cover" />
            <T variant="caption" numberOfLines={2} style={{ textAlign: "center", fontFamily: Font.semibold }}>
              {item.name}
            </T>
            {item.role ? (
              <T variant="caption" muted style={{ fontSize: 10.5, marginTop: -4 }}>
                {item.role}
              </T>
            ) : null}
          </View>
        )}
      />
    </View>
  );
}

// ---------------------------------------------------------------- relations & recommendations
export function Related({ d }: { d: TitleDetail }) {
  return (
    <View style={{ gap: 28 }}>
      {d.relations.length ? (
        <View>
          <View style={{ paddingHorizontal: Space.screen }}>
            <SectionHeader title="Relacionados" />
          </View>
          <FlatList
            horizontal
            data={d.relations}
            keyExtractor={(r) => r.title.key}
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 14 }}
            renderItem={({ item }) => (
              <View>
                <T variant="caption" muted style={{ marginBottom: 6, fontFamily: Font.semibold }}>
                  {item.relation}
                </T>
                <PosterCard title={item.title} width={112} />
              </View>
            )}
          />
        </View>
      ) : null}
      {d.recommendations.length ? (
        <View>
          <View style={{ paddingHorizontal: Space.screen }}>
            <SectionHeader title="Se você gostou…" />
          </View>
          <FlatList
            horizontal
            data={d.recommendations}
            keyExtractor={(t) => t.key}
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={{ paddingHorizontal: Space.screen, gap: 14 }}
            renderItem={({ item }) => <PosterCard title={item} width={112} />}
          />
        </View>
      ) : null}
    </View>
  );
}

// ---------------------------------------------------------------- links
export function Links({ d }: { d: TitleDetail }) {
  const c = useColors();
  const links = d.links.filter((l, i) => d.links.findIndex((x) => x.url === l.url) === i).slice(0, 10);
  if (!links.length) return null;
  return (
    <View>
      <SectionHeader title="Onde encontrar" />
      <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 8 }}>
        {links.map((l) => (
          <Press
            key={l.url}
            onPress={() => void Linking.openURL(l.url)}
            style={{ flexDirection: "row", alignItems: "center", gap: 6, height: 36, paddingHorizontal: 14, borderRadius: Radius.pill, backgroundColor: c.card, borderWidth: 1, borderColor: c.border }}
          >
            <Ionicons name="open-outline" size={14} color={c.mutedForeground} />
            <T variant="label">{l.site}</T>
          </Press>
        ))}
      </View>
      <T variant="caption" muted style={{ marginTop: 14 }}>
        Dados: {d.sources.map((s) => ({ anilist: "AniList", mangadex: "MangaDex", kitsu: "Kitsu", jikan: "MyAnimeList", local: "Local" })[s]).join(", ")}
      </T>
    </View>
  );
}
