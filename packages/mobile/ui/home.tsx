import { useEffect, useRef, type ReactNode } from "react";
import { Animated, Image, View, type ImageSourcePropType } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { Font, Radius, cardShadow } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { dayKey, weekdayShort } from "../core/dates";
import { IconTile, Press, T, tap } from "./primitives";

// ---------------------------------------------------------------- WeekStrip
export function WeekStrip({
  days,
  selected,
  onSelect,
  marked,
}: {
  days: Date[];
  selected: string;
  onSelect: (d: Date) => void;
  marked: Set<string>;
}) {
  const c = useColors();
  const today = dayKey(new Date());
  return (
    <View style={{ flexDirection: "row", justifyContent: "space-between" }}>
      {days.map((d) => {
        const k = dayKey(d);
        const isSel = k === selected;
        const isToday = k === today;
        return (
          <Press
            key={k}
            onPress={() => {
              tap("select");
              onSelect(d);
            }}
            style={{ alignItems: "center", gap: 10 }}
            accessibilityLabel={`${weekdayShort(d)} ${d.getDate()}`}
          >
            <T variant="small" color={isToday ? c.foreground : c.mutedForeground} style={{ fontFamily: isToday ? Font.bold : Font.medium }}>
              {weekdayShort(d)}
            </T>
            <View
              style={[
                {
                  width: 44,
                  height: 44,
                  borderRadius: 22,
                  alignItems: "center",
                  justifyContent: "center",
                  backgroundColor: isSel ? c.ink : c.card,
                },
                !isSel && cardShadow(c, 0.5),
              ]}
            >
              <T variant="title" color={isSel ? c.inkForeground : c.foreground} style={{ fontSize: 15, fontFamily: Font.semibold }}>
                {d.getDate()}
              </T>
            </View>
            <View style={{ width: 5, height: 5, borderRadius: 3, backgroundColor: marked.has(k) ? c.primary : "transparent", marginTop: -4 }} />
          </Press>
        );
      })}
    </View>
  );
}

// ---------------------------------------------------------------- HeroCard
export function HeroCard({
  title,
  text,
  action,
  onAction,
  image,
  footer,
}: {
  title: string;
  text: string;
  action: string;
  onAction: () => void;
  image: ImageSourcePropType;
  footer?: ReactNode;
}) {
  const c = useColors();
  return (
    <View style={{ backgroundColor: c.hero, borderRadius: Radius.xl, padding: 20, paddingRight: 0, flexDirection: "row", overflow: "hidden", minHeight: 152 }}>
      <View style={{ flex: 1, paddingRight: 6, justifyContent: "space-between", gap: 12 }}>
        <View style={{ gap: 6 }}>
          <T variant="h2" color={c.heroForeground} numberOfLines={2}>
            {title}
          </T>
          <T variant="small" color={c.heroMuted} numberOfLines={3}>
            {text}
          </T>
        </View>
        {footer}
        <Press
          haptic
          onPress={onAction}
          style={{ alignSelf: "flex-start", backgroundColor: c.brown, borderRadius: Radius.pill, paddingHorizontal: 18, height: 38, justifyContent: "center" }}
        >
          <T variant="label" color={c.brownForeground}>
            {action}
          </T>
        </Press>
      </View>
      <Image source={image} style={{ width: 128, height: 128, alignSelf: "center", marginRight: 4 }} resizeMode="contain" />
    </View>
  );
}

// ---------------------------------------------------------------- RoutineRow
export function RoutineRow({
  done,
  onToggle,
  tile,
  title,
  subtitle,
  meta,
  metaIcon = "time",
  onPress,
  first,
  last,
}: {
  done: boolean;
  onToggle: () => void;
  tile: ReactNode;
  title: string;
  subtitle: string;
  meta: string;
  metaIcon?: React.ComponentProps<typeof Ionicons>["name"];
  onPress?: () => void;
  first?: boolean;
  last?: boolean;
}) {
  const c = useColors();
  const scale = useRef(new Animated.Value(1)).current;
  useEffect(() => {
    if (!done) return;
    scale.setValue(0.75);
    Animated.spring(scale, { toValue: 1, friction: 4, useNativeDriver: false }).start();
  }, [done, scale]);

  return (
    <View style={{ flexDirection: "row", alignItems: "stretch", gap: 12 }}>
      {/* timeline */}
      <View style={{ width: 24, alignItems: "center" }}>
        <View style={{ flex: 1, width: 0, borderLeftWidth: 1.5, borderStyle: "dashed", borderColor: first ? "transparent" : c.border }} />
        <Press
          onPress={() => {
            tap(done ? "light" : "success");
            onToggle();
          }}
          hitSlop={12}
          accessibilityRole="checkbox"
          accessibilityState={{ checked: done }}
          accessibilityLabel={`Marcar ${title}`}
        >
          <Animated.View
            style={{
              width: 22,
              height: 22,
              borderRadius: 11,
              alignItems: "center",
              justifyContent: "center",
              backgroundColor: done ? c.primary : "transparent",
              borderWidth: done ? 0 : 1.5,
              borderColor: c.mutedForeground,
              transform: [{ scale }],
            }}
          >
            {done ? <Ionicons name="checkmark" size={14} color={c.primaryForeground} /> : null}
          </Animated.View>
        </Press>
        <View style={{ flex: 1, width: 0, borderLeftWidth: 1.5, borderStyle: "dashed", borderColor: last ? "transparent" : c.border }} />
      </View>
      {/* card */}
      <Press onPress={onPress} style={{ flex: 1, paddingVertical: 6 }} scale={0.985}>
        <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg, padding: 12, flexDirection: "row", alignItems: "center", gap: 12 }, cardShadow(c, 0.8)]}>
          {tile}
          <View style={{ flex: 1, gap: 3 }}>
            <T variant="title" numberOfLines={1} style={{ fontSize: 15.5 }}>
              {title}
            </T>
            <T variant="caption" muted numberOfLines={1}>
              {subtitle}
            </T>
          </View>
          <View style={{ width: 1, alignSelf: "stretch", backgroundColor: c.border, marginVertical: 2 }} />
          <View style={{ alignItems: "center", gap: 4, minWidth: 46 }}>
            <Ionicons name={metaIcon} size={17} color={c.foreground} />
            <T variant="caption" muted style={{ fontSize: 11 }}>
              {meta}
            </T>
          </View>
        </View>
      </Press>
    </View>
  );
}

// ---------------------------------------------------------------- FAB
export function FAB({ onPress, icon = "add", label }: { onPress: () => void; icon?: React.ComponentProps<typeof Ionicons>["name"]; label: string }) {
  const c = useColors();
  return (
    <Press
      haptic
      onPress={onPress}
      accessibilityLabel={label}
      style={[
        { position: "absolute", right: 22, bottom: 22, width: 58, height: 58, borderRadius: 29, backgroundColor: c.brown, alignItems: "center", justifyContent: "center" },
        cardShadow(c, 3),
      ]}
    >
      <Ionicons name={icon} size={28} color={c.brownForeground} />
    </Press>
  );
}

// ---------------------------------------------------------------- StatTile
export function StatTile({ icon, tile, value, label }: { icon: React.ComponentProps<typeof Ionicons>["name"]; tile: string; value: string; label: string }) {
  const c = useColors();
  return (
    <View style={[{ flex: 1, backgroundColor: c.card, borderRadius: Radius.lg, padding: 14, gap: 10, minWidth: 140 }, cardShadow(c, 0.8)]}>
      <IconTile icon={icon} tile={tile} size={36} radius={12} />
      <View>
        <T variant="number" style={{ fontSize: 24 }}>
          {value}
        </T>
        <T variant="caption" muted>
          {label}
        </T>
      </View>
    </View>
  );
}
