import { KIND_META } from "./kinds";
import type { LibraryEntry, Title, TrackingStatus } from "./types";

/** Known total units for an entry (user override wins). */
export function totalFor(e: Pick<LibraryEntry, "customTotal" | "title">): number | undefined {
  return e.customTotal ?? e.title.totalUnits ?? undefined;
}

export function doneCount(e: Pick<LibraryEntry, "done">) {
  return Object.keys(e.done).length;
}

/** 0..1, or undefined when the total is unknown. */
export function progressOf(e: LibraryEntry): number | undefined {
  const total = totalFor(e);
  if (!total) return undefined;
  return Math.min(1, doneCount(e) / total);
}

/** Highest unit number marked as done. */
export function lastDone(e: Pick<LibraryEntry, "done">): number {
  let max = 0;
  for (const k of Object.keys(e.done)) {
    const n = Number(k);
    if (Number.isFinite(n) && n > max) max = n;
  }
  return max;
}

/** Next unit to consume: first integer gap after 0, else lastDone + 1. */
export function nextUnit(e: LibraryEntry): number | undefined {
  const total = totalFor(e);
  let n = 1;
  while (e.done[String(n)] !== undefined) n++;
  if (total && n > total) return undefined;
  return n;
}

export function minutesPerUnit(t: Title) {
  return t.unitDuration ?? KIND_META[t.kind].minutes;
}

/** Remaining minutes to finish, if the total is known. */
export function remainingMinutes(e: LibraryEntry) {
  const total = totalFor(e);
  if (!total) return undefined;
  return Math.max(0, total - doneCount(e)) * minutesPerUnit(e.title);
}

/** Status after a progress change (planning → current, all done → completed). */
export function autoStatus(e: LibraryEntry): TrackingStatus {
  const total = totalFor(e);
  const done = doneCount(e);
  if (total && done >= total && e.title.status === "finished") return "completed";
  if (done > 0 && (e.status === "planning" || e.status === "completed")) return "current";
  return e.status;
}
