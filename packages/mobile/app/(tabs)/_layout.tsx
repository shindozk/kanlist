import { Redirect, Tabs } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Font } from "../../constants/theme";
import { useColors } from "../../hooks/use-colors";
import { useLibrary } from "../../state/library";

type IconName = React.ComponentProps<typeof Ionicons>["name"];

const TABS: { name: string; title: string; icon: IconName; active: IconName }[] = [
  { name: "index", title: "Início", icon: "home-outline", active: "home" },
  { name: "explore", title: "Explorar", icon: "compass-outline", active: "compass" },
  { name: "library", title: "Biblioteca", icon: "albums-outline", active: "albums" },
  { name: "profile", title: "Perfil", icon: "person-outline", active: "person" },
];

export default function TabLayout() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const bottom = Math.max(insets.bottom, 10);
  const onboarded = useLibrary((s) => s.settings.onboarded);
  if (!onboarded) return <Redirect href="/onboarding" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.brown,
        tabBarInactiveTintColor: colors.mutedForeground,
        tabBarLabelStyle: { fontFamily: Font.semibold, fontSize: 11 },
        tabBarStyle: {
          backgroundColor: colors.card,
          borderTopWidth: 0,
          elevation: 0,
          paddingTop: 8,
          paddingBottom: bottom,
          height: 58 + bottom,
        },
        sceneStyle: { backgroundColor: colors.background },
      }}
    >
      {TABS.map((t) => (
        <Tabs.Screen
          key={t.name}
          name={t.name}
          options={{
            title: t.title,
            tabBarIcon: ({ color, size, focused }) => <Ionicons name={focused ? t.active : t.icon} size={size - 2} color={color as string} />,
          }}
        />
      ))}
    </Tabs>
  );
}
