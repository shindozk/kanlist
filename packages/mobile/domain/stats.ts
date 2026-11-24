import { addDays, dayKey, startOfDay } from "../core/dates";
import { KIND_META } from "./kinds";
import type { ActivityEvent, LibraryEntry, MediaKind, TrackingStatus } from "./types";
import { doneCount, minutesPerUnit } from "./progress";

export function activeDays(events: ActivityEvent[]) {
  const set = new Set<string>();
  for (const e of events) set.add(dayKey(e.at));
  return set;
}

/** Consecutive days with activity ending today (or yesterday, if today is still empty). */
export function streak(events: ActivityEvent[], now = new Date()) {
  const days = activeDays(events);
  let cursor = startOfDay(now);
  if (!days.has(dayKey(cursor))) cursor = addDays(cursor, -1);
  let n = 0;
  while (days.has(dayKey(cursor))) {
    n++;
    cursor = addDays(cursor, -1);
  }
  return n;
}

export function bestStreak(events: ActivityEvent[]) {
  const keys = [...activeDays(events)].sort();
  let best = 0;
  let cur = 0;
  let prev: Date | null = null;
  for (const k of keys) {
    const d = new Date(`${k}T12:00:00`);
    cur = prev && dayKey(addDays(prev, 1)) === k ? cur + 1 : 1;
    best = Math.max(best, cur);
    prev = d;
  }
  return best;
}

/** Per-entry streak: consecutive days with activity on that entry. */
export function entryStreak(events: ActivityEvent[], key: string) {
  return streak(events.filter((e) => e.key === key));
}

export interface DayTotal {
  day: string;
  units: number;
  minutes: number;
}

export function dayTotal(events: ActivityEvent[], d: Date): DayTotal {
  const k = dayKey(d);
  let units = 0;
  let minutes = 0;
  for (const e of events) {
    if (dayKey(e.at) !== k) continue;
    units += e.count;
    minutes += e.minutes;
  }
  return { day: k, units, minutes };
}

export function lastDays(events: ActivityEvent[], n: number, now = new Date()): DayTotal[] {
  const map = new Map<string, DayTotal>();
  for (let i = n - 1; i >= 0; i--) {
    const k = dayKey(addDays(now, -i));
    map.set(k, { day: k, units: 0, minutes: 0 });
  }
  for (const e of events) {
    const t = map.get(dayKey(e.at));
    if (!t) continue;
    t.units += e.count;
    t.minutes += e.minutes;
  }
  return [...map.values()];
}

export interface LibraryStats {
  entries: number;
  episodes: number;
  chapters: number;
  minutes: number;
  completed: number;
  favorites: number;
  byStatus: Record<TrackingStatus, number>;
  byKind: { kind: MediaKind; count: number; units: number }[];
  meanRating?: number;
}

export function libraryStats(entries: LibraryEntry[]): LibraryStats {
  const byStatus: Record<TrackingStatus, number> = {
    current: 0,
    planning: 0,
    completed: 0,
    paused: 0,
    dropped: 0,
    repeating: 0,
  };
  const kinds = new Map<MediaKind, { kind: MediaKind; count: number; units: number }>();
  let episodes = 0;
  let chapters = 0;
  let minutes = 0;
  let favorites = 0;
  let ratingSum = 0;
  let rated = 0;
  for (const e of entries) {
    const done = doneCount(e) * (1 + e.repeatCount);
    byStatus[e.status]++;
    if (KIND_META[e.title.kind].medium === "watch") episodes += done;
    else chapters += done;
    minutes += done * minutesPerUnit(e.title);
    if (e.favorite) favorites++;
    if (e.rating) {
      ratingSum += e.rating;
      rated++;
    }
    const k = kinds.get(e.title.kind) ?? { kind: e.title.kind, count: 0, units: 0 };
    k.count++;
    k.units += done;
    kinds.set(e.title.kind, k);
  }
  return {
    entries: entries.length,
    episodes,
    chapters,
    minutes,
    completed: byStatus.completed,
    favorites,
    byStatus,
    byKind: [...kinds.values()].sort((a, b) => b.count - a.count),
    meanRating: rated ? ratingSum / rated : undefined,
  };
}
