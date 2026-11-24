import { useState } from "react";
import { Image, KeyboardAvoidingView, Platform, ScrollView, TextInput, View } from "react-native";
import { router } from "expo-router";
import { Font, Radius, Space, cardShadow } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { KIND_META } from "../domain/kinds";
import { MEDIA_KINDS, type MediaKind } from "../domain/types";
import { useLibrary } from "../state/library";
import { Chip, FadeIn, Pill, Screen, T } from "../ui/primitives";

const GOALS = [1, 3, 5, 10];

export default function Onboarding() {
  const c = useColors();
  const update = useLibrary((s) => s.updateSettings);
  const settings = useLibrary((s) => s.settings);
  const [name, setName] = useState(settings.name);
  const [interests, setInterests] = useState<MediaKind[]>(settings.interests);
  const [goal, setGoal] = useState(settings.dailyGoal);

  const toggle = (k: MediaKind) =>
    setInterests((cur) => (cur.includes(k) ? cur.filter((x) => x !== k) : [...cur, k]));

  const finish = () => {
    update({ name: name.trim(), interests: interests.length ? interests : ["anime", "manga"], dailyGoal: goal, onboarded: true });
    router.replace("/");
  };

  return (
    <Screen edges={["top", "left", "right", "bottom"]}>
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === "ios" ? "padding" : "height"}>
        <ScrollView contentContainerStyle={{ padding: Space.screen, paddingTop: 28, gap: 26 }} keyboardShouldPersistTaps="handled">
          <FadeIn style={{ alignItems: "center", gap: 14 }}>
            <View style={[{ width: 112, height: 112, borderRadius: 56, backgroundColor: c.hero, alignItems: "center", justifyContent: "center" }, cardShadow(c, 1)]}>
              <Image source={require("../assets/images/mascot.png")} style={{ width: 100, height: 100 }} resizeMode="contain" />
            </View>
            <T variant="display" style={{ textAlign: "center" }}>
              Bem-vindo ao Kanlist
            </T>
            <T variant="body" muted style={{ textAlign: "center", maxWidth: 300 }}>
              Sua rotina de animes, mangás, manhwas e donghuas — episódio por episódio, capítulo por capítulo.
            </T>
          </FadeIn>

          <FadeIn delay={80} style={{ gap: 10 }}>
            <T variant="h2">Como podemos te chamar?</T>
            <TextInput
              value={name}
              onChangeText={setName}
              placeholder="Seu nome ou apelido"
              placeholderTextColor={c.mutedForeground}
              maxLength={24}
              returnKeyType="done"
              style={[
                { height: 52, borderRadius: Radius.lg, backgroundColor: c.card, paddingHorizontal: 18, fontFamily: Font.semibold, fontSize: 16, color: c.foreground },
                cardShadow(c, 0.6),
              ]}
            />
          </FadeIn>

          <FadeIn delay={140} style={{ gap: 10 }}>
            <T variant="h2">O que você acompanha?</T>
            <View style={{ flexDirection: "row", flexWrap: "wrap", gap: 8 }}>
              {MEDIA_KINDS.map((k) => (
                <Chip key={k} label={KIND_META[k].plural} icon={KIND_META[k].icon} active={interests.includes(k)} onPress={() => toggle(k)} />
              ))}
            </View>
          </FadeIn>

          <FadeIn delay={200} style={{ gap: 10 }}>
            <T variant="h2">Meta diária</T>
            <T variant="small" muted>
              Quantos episódios ou capítulos por dia mantêm sua sequência viva?
            </T>
            <View style={{ flexDirection: "row", gap: 8 }}>
              {GOALS.map((g) => (
                <Chip key={g} label={String(g)} active={goal === g} onPress={() => setGoal(g)} />
              ))}
            </View>
          </FadeIn>

          <FadeIn delay={260}>
            <Pill label="Começar" icon="arrow-forward" onPress={finish} full />
          </FadeIn>
        </ScrollView>
      </KeyboardAvoidingView>
    </Screen>
  );
}
