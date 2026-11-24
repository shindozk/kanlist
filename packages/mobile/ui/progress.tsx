import { View } from "react-native";
import Svg, { Circle } from "react-native-svg";
import { Radius } from "../constants/theme";
import { useColors } from "../hooks/use-colors";
import { T } from "./primitives";

export function ProgressBar({ value, height = 6, color }: { value: number; height?: number; color?: string }) {
  const c = useColors();
  const pct = Math.max(0, Math.min(1, value || 0));
  return (
    <View style={{ height, borderRadius: Radius.pill, backgroundColor: c.track, overflow: "hidden" }}>
      <View style={{ width: `${pct * 100}%`, height, borderRadius: Radius.pill, backgroundColor: color ?? c.primary }} />
    </View>
  );
}

export function ProgressRing({
  value,
  size = 84,
  stroke = 8,
  label,
  sublabel,
  color,
}: {
  value: number;
  size?: number;
  stroke?: number;
  label?: string;
  sublabel?: string;
  color?: string;
}) {
  const c = useColors();
  const r = (size - stroke) / 2;
  const circ = 2 * Math.PI * r;
  const pct = Math.max(0, Math.min(1, value || 0));
  return (
    <View style={{ width: size, height: size, alignItems: "center", justifyContent: "center" }}>
      <Svg width={size} height={size} style={{ position: "absolute" }}>
        <Circle cx={size / 2} cy={size / 2} r={r} stroke={c.track} strokeWidth={stroke} fill="none" />
        <Circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          stroke={color ?? c.primary}
          strokeWidth={stroke}
          fill="none"
          strokeDasharray={`${circ} ${circ}`}
          strokeDashoffset={circ * (1 - pct)}
          strokeLinecap="round"
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
      </Svg>
      {label ? (
        <T variant="title" style={{ fontSize: size > 70 ? 18 : 13 }}>
          {label}
        </T>
      ) : null}
      {sublabel ? (
        <T variant="caption" muted style={{ fontSize: 10, marginTop: -2 }}>
          {sublabel}
        </T>
      ) : null}
    </View>
  );
}
