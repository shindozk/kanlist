import { create } from "zustand";
import { uid } from "../core/text";
import * as repo from "../data/repositories/library";
import type { LibraryDoc } from "../data/repositories/library";
import { autoStatus, minutesPerUnit, totalFor } from "../domain/progress";
import type { Collection, LibraryEntry, Settings, Title, TrackingStatus } from "../domain/types";

/**
 * Library state: the single source of truth for everything the user tracks.
 * Mutations are synchronous (instant UI) and persisted with a debounced write.
 */
interface LibraryState extends LibraryDoc {
  hydrated: boolean;
  hydrate: () => Promise<void>;

  add: (title: Title, status?: TrackingStatus) => void;
  remove: (key: string) => void;
  update: (key: string, patch: Partial<Omit<LibraryEntry, "key" | "done">>) => void;
  setStatus: (key: string, status: TrackingStatus) => void;
  refreshTitle: (title: Title) => void;

  toggleUnit: (key: string, unit: string) => void;
  /** Marks every unit in `keys` as done (e.g. "mark up to here"). */
  markMany: (key: string, keys: string[]) => void;
  unmarkMany: (key: string, keys: string[]) => void;
  markNext: (key: string) => string | undefined;
  resetProgress: (key: string) => void;

  createCollection: (name: string, tile: string, description?: string) => string;
  updateCollection: (id: string, patch: Partial<Pick<Collection, "name" | "tile" | "description">>) => void;
  deleteCollection: (id: string) => void;
  toggleInCollection: (id: string, key: string) => void;

  updateSettings: (patch: Partial<Settings>) => void;
  addSearch: (q: string) => void;
  clearSearches: () => void;

  exportJson: () => string;
  importJson: (json: string, mode: "merge" | "replace") => number;
  resetAll: () => Promise<void>;
}

const now = () => Date.now();

function docOf(s: LibraryState): LibraryDoc {
  return {
    version: s.version,
    entries: s.entries,
    events: s.events,
    collections: s.collections,
    settings: s.settings,
    searches: s.searches,
  };
}

function newEntry(title: Title, status: TrackingStatus): LibraryEntry {
  const t = now();
  return {
    key: title.key,
    title,
    status,
    done: {},
    volumes: 0,
    notes: "",
    favorite: false,
    repeatCount: 0,
    addedAt: t,
    updatedAt: t,
    startedAt: status === "current" ? t : undefined,
  };
}

/** Applies a done-map change and derives status / dates. */
function withProgress(e: LibraryEntry, done: Record<string, number>): LibraryEntry {
  const next: LibraryEntry = { ...e, done, updatedAt: now() };
  const status = autoStatus(next);
  if (status !== e.status) {
    next.status = status;
    if (status === "current" && !next.startedAt) next.startedAt = now();
    if (status === "completed") next.finishedAt = now();
  }
  return next;
}

export const useLibrary = create<LibraryState>((set, get) => {
  const event = (e: LibraryEntry, unit: string, count: number) => ({
    id: uid(),
    key: e.key,
    kind: e.title.kind,
    unit,
    count,
    at: now(),
    minutes: count * minutesPerUnit(e.title),
  });

  const patchEntry = (key: string, fn: (e: LibraryEntry) => LibraryEntry) => {
    const e = get().entries[key];
    if (!e) return;
    set({ entries: { ...get().entries, [key]: fn(e) } });
  };

  return {
    ...repo.emptyDoc(),
    hydrated: false,

    hydrate: async () => {
      if (get().hydrated) return;
      try {
        const doc = await repo.load();
        set({ ...doc, hydrated: true });
      } catch {
        set({ hydrated: true });
      }
    },

    add: (title, status = "planning") => {
      const cur = get().entries[title.key];
      if (cur) {
        patchEntry(title.key, (e) => ({ ...e, status, updatedAt: now() }));
        return;
      }
      set({ entries: { ...get().entries, [title.key]: newEntry(title, status) } });
    },

    remove: (key) => {
      const { [key]: _gone, ...rest } = get().entries;
      set({
        entries: rest,
        events: get().events.filter((e) => e.key !== key),
        collections: get().collections.map((c) => ({ ...c, keys: c.keys.filter((k) => k !== key) })),
      });
    },

    update: (key, patch) => patchEntry(key, (e) => ({ ...e, ...patch, updatedAt: now() })),

    setStatus: (key, status) =>
      patchEntry(key, (e) => {
        const t = now();
        const next: LibraryEntry = { ...e, status, updatedAt: t };
        if (status === "current" && !e.startedAt) next.startedAt = t;
        if (status === "completed") {
          next.finishedAt = t;
          const total = totalFor(e);
          // Completing marks everything known as done.
          if (total && Object.keys(e.done).length < total) {
            const done = { ...e.done };
            for (let i = 1; i <= total; i++) done[String(i)] ??= t;
            next.done = done;
          }
        }
        if (status === "repeating" && e.status === "completed") {
          next.repeatCount = e.repeatCount + 1;
          next.done = {};
        }
        return next;
      }),

    refreshTitle: (title) => {
      const e = get().entries[title.key];
      if (!e) return;
      set({ entries: { ...get().entries, [title.key]: { ...e, title: { ...e.title, ...title } } } });
    },

    toggleUnit: (key, unit) => {
      const e = get().entries[key];
      if (!e) return;
      const done = { ...e.done };
      if (done[unit] !== undefined) {
        delete done[unit];
        // Remove the most recent single-unit event for it (keeps stats honest).
        const events = [...get().events];
        const idx = events.findLastIndex((ev) => ev.key === key && ev.unit === unit && ev.count === 1);
        if (idx >= 0) events.splice(idx, 1);
        set({ entries: { ...get().entries, [key]: withProgress(e, done) }, events });
      } else {
        done[unit] = now();
        set({
          entries: { ...get().entries, [key]: withProgress(e, done) },
          events: [...get().events, event(e, unit, 1)],
        });
      }
    },

    markMany: (key, keys) => {
      const e = get().entries[key];
      if (!e) return;
      const done = { ...e.done };
      const t = now();
      const added = keys.filter((k) => done[k] === undefined);
      if (!added.length) return;
      for (const k of added) done[k] = t;
      set({
        entries: { ...get().entries, [key]: withProgress(e, done) },
        events: [...get().events, event(e, added[0], added.length)],
      });
    },

    unmarkMany: (key, keys) => {
      const e = get().entries[key];
      if (!e) return;
      const done = { ...e.done };
      for (const k of keys) delete done[k];
      set({ entries: { ...get().entries, [key]: withProgress(e, done) } });
    },

    markNext: (key) => {
      const e = get().entries[key];
      if (!e) return undefined;
      let n = 1;
      while (e.done[String(n)] !== undefined) n++;
      const total = totalFor(e);
      if (total && n > total) return undefined;
      get().toggleUnit(key, String(n));
      return String(n);
    },

    resetProgress: (key) => patchEntry(key, (e) => ({ ...e, done: {}, updatedAt: now() })),

    createCollection: (name, tile, description = "") => {
      const id = uid();
      const t = now();
      set({ collections: [...get().collections, { id, name, tile, description, keys: [], createdAt: t, updatedAt: t }] });
      return id;
    },
    updateCollection: (id, patch) =>
      set({ collections: get().collections.map((c) => (c.id === id ? { ...c, ...patch, updatedAt: now() } : c)) }),
    deleteCollection: (id) => set({ collections: get().collections.filter((c) => c.id !== id) }),
    toggleInCollection: (id, key) =>
      set({
        collections: get().collections.map((c) =>
          c.id !== id
            ? c
            : { ...c, keys: c.keys.includes(key) ? c.keys.filter((k) => k !== key) : [...c.keys, key], updatedAt: now() },
        ),
      }),

    updateSettings: (patch) => set({ settings: { ...get().settings, ...patch } }),
    addSearch: (q) => {
      const v = q.trim();
      if (v.length < 2) return;
      set({ searches: [v, ...get().searches.filter((s) => s.toLowerCase() !== v.toLowerCase())].slice(0, 10) });
    },
    clearSearches: () => set({ searches: [] }),

    exportJson: () => repo.exportJson(docOf(get())),
    importJson: (json, mode) => {
      const inc = repo.parseImport(json);
      const next = mode === "replace" ? inc : repo.mergeDocs(docOf(get()), inc);
      set({ ...next });
      return Object.keys(inc.entries).length;
    },
    resetAll: async () => {
      await repo.wipe();
      set({ ...repo.emptyDoc(), hydrated: true });
    },
  };
});

// ---- persistence: debounced write after hydration -----------------------------------
let timer: ReturnType<typeof setTimeout> | undefined;
useLibrary.subscribe((s, prev) => {
  if (!s.hydrated || !prev.hydrated) return;
  if (s.entries === prev.entries && s.events === prev.events && s.collections === prev.collections && s.settings === prev.settings && s.searches === prev.searches) return;
  clearTimeout(timer);
  timer = setTimeout(() => {
    void repo.save(docOf(useLibrary.getState())).catch(() => {});
  }, 350);
});

// ---- selectors -----------------------------------------------------------------------
export const useEntry = (key: string | undefined) => useLibrary((s) => (key ? s.entries[key] : undefined));
export const useSettings = () => useLibrary((s) => s.settings);
