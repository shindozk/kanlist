import { useEffect, useRef, type ReactNode } from "react";
import {
  Animated,
  Platform,
  Pressable,
  Text,
  View,
  type PressableProps,
  type StyleProp,
  type TextProps,
  type TextStyle,
  type ViewStyle,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import * as Haptics from "expo-haptics";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { Font, Radius, Space, Tiles, cardShadow, type TileName } from "../constants/theme";
import { useColors, useScheme } from "../hooks/use-colors";
import { useLibrary } from "../state/library";

// ---------------------------------------------------------------- haptics
export function tap(kind: "light" | "success" | "select" = "light") {
  if (Platform.OS === "web" || !useLibrary.getState().settings.haptics) return;
  if (kind === "success") void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
  else if (kind === "select") void Haptics.selectionAsync();
  else void Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
}

// ---------------------------------------------------------------- text
type Variant = "display" | "h1" | "h2" | "title" | "body" | "small" | "caption" | "label" | "number";

const VARIANTS: Record<Variant, TextStyle> = {
  display: { fontFamily: Font.bold, fontSize: 30, letterSpacing: -0.6, lineHeight: 36 },
  h1: { fontFamily: Font.bold, fontSize: 24, letterSpacing: -0.4, lineHeight: 30 },
  h2: { fontFamily: Font.bold, fontSize: 19, letterSpacing: -0.2, lineHeight: 24 },
  title: { fontFamily: Font.semibold, fontSize: 16, lineHeight: 21 },
  body: { fontFamily: Font.medium, fontSize: 14, lineHeight: 21 },
  small: { fontFamily: Font.medium, fontSize: 13, lineHeight: 18 },
  caption: { fontFamily: Font.medium, fontSize: 12, lineHeight: 16 },
  label: { fontFamily: Font.semibold, fontSize: 13, lineHeight: 17 },
  number: { fontFamily: Font.extrabold, fontSize: 28, letterSpacing: -0.5, lineHeight: 32 },
};

export function T({
  variant = "body",
  muted,
  color,
  style,
  ...rest
}: TextProps & { variant?: Variant; muted?: boolean; color?: string }) {
  const c = useColors();
  return (
    <Text
      {...rest}
      style={[VARIANTS[variant], { color: color ?? (muted ? c.mutedForeground : c.foreground) }, style]}
    />
  );
}

// ---------------------------------------------------------------- press
export function Press({
  style,
  children,
  scale = 0.97,
  haptic,
  onPress,
  ...rest
}: Omit<PressableProps, "style" | "children"> & {
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
  scale?: number;
  haptic?: boolean;
}) {
  return (
    <Pressable
      {...rest}
      onPress={(e) => {
        if (haptic) tap();
        onPress?.(e);
      }}
      style={({ pressed }) => [style, pressed && { opacity: 0.85, transform: [{ scale }] }]}
    >
      {children}
    </Pressable>
  );
}

// ---------------------------------------------------------------- layout
export function Screen({
  children,
  style,
  edges = ["top", "left", "right"],
}: {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
  edges?: ("top" | "left" | "right" | "bottom")[];
}) {
  const c = useColors();
  return <SafeAreaView edges={edges} style={[{ flex: 1, backgroundColor: c.background }, style]}>{children}</SafeAreaView>;
}

export function Card({ children, style, strong }: { children: ReactNode; style?: StyleProp<ViewStyle>; strong?: boolean }) {
  const c = useColors();
  return (
    <View style={[{ backgroundColor: c.card, borderRadius: Radius.lg }, cardShadow(c, strong ? 1.6 : 1), style]}>{children}</View>
  );
}

export function Row({ children, style, gap = 8 }: { children: ReactNode; style?: StyleProp<ViewStyle>; gap?: number }) {
  return <View style={[{ flexDirection: "row", alignItems: "center", gap }, style]}>{children}</View>;
}

/** Stack screen header: circular back button, centered title, optional right slot. */
export function TopBar({ title, right, onBack }: { title?: string; right?: ReactNode; onBack?: () => void }) {
  return (
    <Row style={{ paddingHorizontal: Space.screen, paddingVertical: 10, justifyContent: "space-between" }}>
      <IconButton
        icon="chevron-back"
        onPress={() => (onBack ? onBack() : router.canGoBack() ? router.back() : router.replace("/"))}
        label="Voltar"
      />
      <T variant="title" numberOfLines={1} style={{ flex: 1, textAlign: "center", marginHorizontal: 8 }}>
        {title ?? ""}
      </T>
      <View style={{ minWidth: 42, alignItems: "flex-end" }}>{right ?? <View style={{ width: 42 }} />}</View>
    </Row>
  );
}

export function IconButton({
  icon,
  onPress,
  label,
  color,
  bg,
  size = 42,
}: {
  icon: React.ComponentProps<typeof Ionicons>["name"];
  onPress?: () => void;
  label: string;
  color?: string;
  bg?: string;
  size?: number;
}) {
  const c = useColors();
  return (
    <Press
      onPress={onPress}
      accessibilityLabel={label}
      hitSlop={6}
      style={[
        { width: size, height: size, borderRadius: size / 2, alignItems: "center", justifyContent: "center", backgroundColor: bg ?? c.card },
        cardShadow(c, 0.6),
      ]}
    >
      <Ionicons name={icon} size={size * 0.48} color={color ?? c.foreground} />
    </Press>
  );
}

// ---------------------------------------------------------------- tiles & chips
export function useTile(name: TileName | string) {
  const scheme = useScheme();
  const t = Tiles[(name in Tiles ? name : "peach") as TileName][scheme];
  return { bg: t[0], fg: t[1] };
}

export function IconTile({
  icon,
  tile,
  size = 44,
  radius = Radius.md,
}: {
  icon: React.ComponentProps<typeof Ionicons>["name"];
  tile: TileName | string;
  size?: number;
  radius?: number;
}) {
  const { bg, fg } = useTile(tile);
  return (
    <View style={{ width: size, height: size, borderRadius: radius, backgroundColor: bg, alignItems: "center", justifyContent: "center" }}>
      <Ionicons name={icon} size={size * 0.5} color={fg} />
    </View>
  );
}

export function Chip({
  label,
  active,
  onPress,
  icon,
  count,
}: {
  label: string;
  active?: boolean;
  onPress?: () => void;
  icon?: React.ComponentProps<typeof Ionicons>["name"];
  count?: number;
}) {
  const c = useColors();
  return (
    <Press
      onPress={() => {
        tap("select");
        onPress?.();
      }}
      style={{
        height: 36,
        paddingHorizontal: 15,
        borderRadius: Radius.pill,
        flexDirection: "row",
        alignItems: "center",
        gap: 6,
        backgroundColor: active ? c.ink : c.card,
        borderWidth: 1,
        borderColor: active ? c.ink : c.border,
      }}
    >
      {icon ? <Ionicons name={icon} size={15} color={active ? c.inkForeground : c.mutedForeground} /> : null}
      <T variant="label" color={active ? c.inkForeground : c.foreground}>
        {label}
      </T>
      {count !== undefined ? (
        <T variant="caption" color={active ? c.inkForeground : c.mutedForeground} style={{ opacity: 0.8 }}>
          {count}
        </T>
      ) : null}
    </Press>
  );
}

export function Pill({
  label,
  onPress,
  icon,
  variant = "brown",
  disabled,
  full,
}: {
  label: string;
  onPress?: () => void;
  icon?: React.ComponentProps<typeof Ionicons>["name"];
  variant?: "brown" | "ink" | "soft" | "outline" | "danger";
  disabled?: boolean;
  full?: boolean;
}) {
  const c = useColors();
  const styles = {
    brown: { bg: c.brown, fg: c.brownForeground, border: c.brown },
    ink: { bg: c.ink, fg: c.inkForeground, border: c.ink },
    soft: { bg: c.accent, fg: c.accentForeground, border: c.accent },
    outline: { bg: "transparent", fg: c.foreground, border: c.border },
    danger: { bg: "transparent", fg: c.destructive, border: c.destructive },
  }[variant];
  return (
    <Press
      disabled={disabled}
      haptic
      onPress={onPress}
      style={{
        height: 44,
        paddingHorizontal: 20,
        borderRadius: Radius.pill,
        backgroundColor: styles.bg,
        borderWidth: 1,
        borderColor: styles.border,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        opacity: disabled ? 0.5 : 1,
        alignSelf: full ? "stretch" : "flex-start",
      }}
    >
      {icon ? <Ionicons name={icon} size={17} color={styles.fg} /> : null}
      <T variant="label" color={styles.fg} style={{ fontSize: 14 }}>
        {label}
      </T>
    </Press>
  );
}

export function Badge({ label, tile, icon }: { label: string; tile?: TileName | string; icon?: React.ComponentProps<typeof Ionicons>["name"] }) {
  const c = useColors();
  const t = useTile(tile ?? "peach");
  const bg = tile ? t.bg : c.muted;
  const fg = tile ? t.fg : c.mutedForeground;
  return (
    <View style={{ flexDirection: "row", alignItems: "center", gap: 4, backgroundColor: bg, paddingHorizontal: 9, height: 24, borderRadius: Radius.pill }}>
      {icon ? <Ionicons name={icon} size={12} color={fg} /> : null}
      <T variant="caption" color={fg} style={{ fontFamily: Font.semibold }}>
        {label}
      </T>
    </View>
  );
}

export function SectionHeader({ title, action, onAction }: { title: string; action?: string; onAction?: () => void }) {
  return (
    <Row style={{ justifyContent: "space-between", marginBottom: 14 }}>
      <T variant="h2">{title}</T>
      {action ? (
        <Press onPress={onAction} hitSlop={10}>
          <T variant="small" style={{ fontFamily: Font.semibold }}>
            {action}
          </T>
        </Press>
      ) : null}
    </Row>
  );
}

// ---------------------------------------------------------------- feedback
export function FadeIn({ children, delay = 0, style }: { children: ReactNode; delay?: number; style?: StyleProp<ViewStyle> }) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.timing(v, { toValue: 1, duration: 380, delay, useNativeDriver: false }).start();
  }, [v, delay]);
  return (
    <Animated.View
      style={[style, { opacity: v, transform: [{ translateY: v.interpolate({ inputRange: [0, 1], outputRange: [12, 0] }) }] }]}
    >
      {children}
    </Animated.View>
  );
}

export function Skeleton({ width, height, radius = Radius.md, style }: { width?: number | `${number}%`; height: number; radius?: number; style?: StyleProp<ViewStyle> }) {
  const c = useColors();
  const v = useRef(new Animated.Value(0.5)).current;
  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(v, { toValue: 1, duration: 700, useNativeDriver: false }),
        Animated.timing(v, { toValue: 0.5, duration: 700, useNativeDriver: false }),
      ]),
    );
    loop.start();
    return () => loop.stop();
  }, [v]);
  return <Animated.View style={[{ width: width ?? "100%", height, borderRadius: radius, backgroundColor: c.muted, opacity: v }, style]} />;
}

export function EmptyState({
  icon,
  title,
  text,
  action,
  onAction,
  tile = "peach",
}: {
  icon: React.ComponentProps<typeof Ionicons>["name"];
  title: string;
  text?: string;
  action?: string;
  onAction?: () => void;
  tile?: TileName;
}) {
  return (
    <View style={{ alignItems: "center", paddingVertical: 36, paddingHorizontal: 24, gap: 10 }}>
      <IconTile icon={icon} tile={tile} size={64} radius={22} />
      <T variant="title" style={{ textAlign: "center", marginTop: 6 }}>
        {title}
      </T>
      {text ? (
        <T variant="small" muted style={{ textAlign: "center", maxWidth: 280 }}>
          {text}
        </T>
      ) : null}
      {action ? (
        <View style={{ marginTop: 8 }}>
          <Pill label={action} onPress={onAction} />
        </View>
      ) : null}
    </View>
  );
}

export function ErrorNote({ message, onRetry }: { message: string; onRetry?: () => void }) {
  const c = useColors();
  return (
    <Card style={{ padding: 16, flexDirection: "row", alignItems: "center", gap: 12 }}>
      <IconTile icon="cloud-offline-outline" tile="coral" size={40} />
      <T variant="small" style={{ flex: 1 }}>
        {message}
      </T>
      {onRetry ? (
        <Press onPress={onRetry} hitSlop={8}>
          <T variant="label" color={c.primary}>
            Tentar
          </T>
        </Press>
      ) : null}
    </Card>
  );
}
