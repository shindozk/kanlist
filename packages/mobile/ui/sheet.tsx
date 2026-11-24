import { useEffect, useRef, useState, type ReactNode } from "react";
import { Animated, KeyboardAvoidingView, Modal, Platform, Pressable, ScrollView, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { Radius, Space } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { Press, T } from "./primitives";

/** Bottom sheet (Modal-based so it works on native and web preview). */
export function Sheet({ open, onClose, title, children }: { open: boolean; onClose: () => void; title?: string; children: ReactNode }) {
  const c = useColors();
  const insets = useSafeAreaInsets();
  const y = useRef(new Animated.Value(0)).current;
  const [visible, setVisible] = useState(open);

  useEffect(() => {
    if (open) {
      setVisible(true);
      Animated.timing(y, { toValue: 1, duration: 240, useNativeDriver: false }).start();
    } else if (visible) {
      Animated.timing(y, { toValue: 0, duration: 180, useNativeDriver: false }).start(() => setVisible(false));
    }
  }, [open, visible, y]);

  return (
    <Modal visible={visible} transparent animationType="none" onRequestClose={onClose} statusBarTranslucent>
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={{ flex: 1 }}>
        <Animated.View style={{ flex: 1, backgroundColor: c.overlay, opacity: y }}>
          <Pressable style={{ flex: 1 }} onPress={onClose} accessibilityLabel="Fechar" />
        </Animated.View>
        <Animated.View
          style={{
            position: "absolute",
            left: 0,
            right: 0,
            bottom: 0,
            maxHeight: "88%",
            backgroundColor: c.background,
            borderTopLeftRadius: Radius.xxl,
            borderTopRightRadius: Radius.xxl,
            paddingBottom: Math.max(insets.bottom, 16),
            transform: [{ translateY: y.interpolate({ inputRange: [0, 1], outputRange: [500, 0] }) }],
          }}
        >
          <View style={{ alignItems: "center", paddingTop: 10 }}>
            <View style={{ width: 40, height: 5, borderRadius: 3, backgroundColor: c.border }} />
          </View>
          {title ? (
            <View style={{ flexDirection: "row", alignItems: "center", justifyContent: "space-between", paddingHorizontal: Space.screen, paddingTop: 14, paddingBottom: 6 }}>
              <T variant="h2">{title}</T>
              <Press onPress={onClose} hitSlop={10} accessibilityLabel="Fechar">
                <Ionicons name="close" size={22} color={c.mutedForeground} />
              </Press>
            </View>
          ) : null}
          <ScrollView contentContainerStyle={{ paddingHorizontal: Space.screen, paddingTop: 8, paddingBottom: 8 }} keyboardShouldPersistTaps="handled">
            {children}
          </ScrollView>
        </Animated.View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

/** Option row used inside sheets. */
export function SheetOption({
  icon,
  label,
  hint,
  selected,
  onPress,
  danger,
}: {
  icon?: React.ComponentProps<typeof Ionicons>["name"];
  label: string;
  hint?: string;
  selected?: boolean;
  onPress: () => void;
  danger?: boolean;
}) {
  const c = useColors();
  const color = danger ? c.destructive : c.foreground;
  return (
    <Press
      onPress={onPress}
      style={{ flexDirection: "row", alignItems: "center", gap: 14, paddingVertical: 13, paddingHorizontal: 14, borderRadius: Radius.md, backgroundColor: selected ? c.accent : "transparent" }}
    >
      {icon ? <Ionicons name={icon} size={20} color={selected ? c.primary : danger ? c.destructive : c.mutedForeground} /> : null}
      <View style={{ flex: 1 }}>
        <T variant="title" color={color} style={{ fontSize: 15 }}>
          {label}
        </T>
        {hint ? (
          <T variant="caption" muted>
            {hint}
          </T>
        ) : null}
      </View>
      {selected ? <Ionicons name="checkmark-circle" size={20} color={c.primary} /> : null}
    </Press>
  );
}
