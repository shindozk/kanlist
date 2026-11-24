import AsyncStorage from "@react-native-async-storage/async-storage";
import { z } from "zod";
import { MEDIA_KINDS, TRACKING_STATUSES, type ActivityEvent, type Collection, type LibraryEntry, type Settings } from "../../domain/types";

/**
 * Local persistence for the user's library. A single versioned document in AsyncStorage,
 * migrated forward on load, with a rolling daily backup and JSON export / import.
 */
export const DB_VERSION = 2;
const KEY = "kl:db";
const BACKUP_KEY = "kl:db:backup";
const BACKUP_AT_KEY = "kl:db:backupAt";

export interface LibraryDoc {
  version: number;
  entries: Record<string, LibraryEntry>;
  events: ActivityEvent[];
  collections: Collection[];
  settings: Settings;
  searches: string[];
}

export const DEFAULT_SETTINGS: Settings = {
  name: "",
  onboarded: false,
  theme: "system",
  dailyGoal: 3,
  titleLanguage: "romaji",
  chapterLangs: ["pt-br", "en"],
  showAdult: false,
  autoTranslate: true,
  haptics: true,
  interests: ["anime", "manga", "manhwa"],
  sources: { kitsu: true, jikan: true, mangadex: true, translate: true },
};

export function emptyDoc(): LibraryDoc {
  return { version: DB_VERSION, entries: {}, events: [], collections: [], settings: { ...DEFAULT_SETTINGS }, searches: [] };
}

// ---- migrations --------------------------------------------------------------
type AnyDoc = Record<string, unknown> & { version?: number };

const MIGRATIONS: Record<number, (d: AnyDoc) => AnyDoc> = {
  // v1 → v2: events gained `count` and `kind`; settings gained `sources`.
  1: (d) => {
    const entries = (d.entries ?? {}) as Record<string, LibraryEntry>;
    const events = ((d.events ?? []) as Partial<ActivityEvent>[]).map((e) => ({
      ...e,
      count: e.count ?? 1,
      kind: e.kind ?? entries[e.key ?? ""]?.title.kind ?? "anime",
    }));
    return { ...d, events, version: 2 };
  },
};

function migrate(raw: AnyDoc): LibraryDoc {
  let d = raw;
  let v = d.version ?? 1;
  while (v < DB_VERSION) {
    const step = MIGRATIONS[v];
    d = step ? step(d) : { ...d, version: v + 1 };
    v = d.version ?? v + 1;
  }
  const base = emptyDoc();
  const settings = { ...base.settings, ...(d.settings as Partial<Settings> | undefined) };
  settings.sources = { ...base.settings.sources, ...settings.sources };
  return {
    version: DB_VERSION,
    entries: (d.entries as LibraryDoc["entries"]) ?? {},
    events: (d.events as LibraryDoc["events"]) ?? [],
    collections: (d.collections as LibraryDoc["collections"]) ?? [],
    settings,
    searches: (d.searches as string[]) ?? [],
  };
}

// ---- validation (import) -----------------------------------------------------
const titleSchema = z
  .object({
    key: z.string(),
    source: z.string(),
    sourceId: z.string(),
    kind: z.enum(MEDIA_KINDS),
    medium: z.enum(["watch", "read"]),
    title: z.string(),
  })
  .passthrough();

const entrySchema = z
  .object({
    key: z.string(),
    title: titleSchema,
    status: z.enum(TRACKING_STATUSES as [string, ...string[]]),
    done: z.record(z.string(), z.number()),
  })
  .passthrough();

const importSchema = z.object({
  app: z.literal("kanlist").optional(),
  version: z.number().int().positive().optional(),
  entries: z.record(z.string(), entrySchema),
  events: z.array(z.object({ key: z.string(), at: z.number() }).passthrough()).optional(),
  collections: z.array(z.object({ id: z.string(), name: z.string(), keys: z.array(z.string()) }).passthrough()).optional(),
  settings: z.record(z.string(), z.unknown()).optional(),
  searches: z.array(z.string()).optional(),
});

// ---- io --------------------------------------------------------------------------
export async function load(): Promise<LibraryDoc> {
  const raw = await AsyncStorage.getItem(KEY);
  if (!raw) {
    const backup = await AsyncStorage.getItem(BACKUP_KEY);
    if (backup) {
      try {
        return migrate(JSON.parse(backup));
      } catch {
        // ignore broken backup
      }
    }
    return emptyDoc();
  }
  try {
    return migrate(JSON.parse(raw));
  } catch {
    // Corrupted main document: recover from the backup if possible.
    const backup = await AsyncStorage.getItem(BACKUP_KEY);
    if (backup) return migrate(JSON.parse(backup));
    return emptyDoc();
  }
}

export async function save(doc: LibraryDoc) {
  const json = JSON.stringify(doc);
  await AsyncStorage.setItem(KEY, json);
  const last = Number(await AsyncStorage.getItem(BACKUP_AT_KEY)) || 0;
  if (Date.now() - last > 86_400_000) {
    await AsyncStorage.multiSet([
      [BACKUP_KEY, json],
      [BACKUP_AT_KEY, String(Date.now())],
    ]);
  }
}

export function exportJson(doc: LibraryDoc) {
  return JSON.stringify({ app: "kanlist", exportedAt: new Date().toISOString(), ...doc }, null, 2);
}

/** Parses and validates an exported file. Throws a PT-BR message on failure. */
export function parseImport(json: string): LibraryDoc {
  let data: unknown;
  try {
    data = JSON.parse(json);
  } catch {
    throw new Error("O texto colado não é um JSON válido.");
  }
  const res = importSchema.safeParse(data);
  if (!res.success) throw new Error("Esse arquivo não parece um backup do Kanlist.");
  return migrate(data as AnyDoc);
}

/** Merges an imported doc into the current one (newer entries win). */
export function mergeDocs(cur: LibraryDoc, inc: LibraryDoc): LibraryDoc {
  const entries = { ...cur.entries };
  for (const [k, e] of Object.entries(inc.entries)) {
    const mine = entries[k];
    if (!mine || (e.updatedAt ?? 0) >= (mine.updatedAt ?? 0)) entries[k] = { ...mine, ...e, done: { ...mine?.done, ...e.done } };
  }
  const ids = new Set(cur.events.map((e) => e.id));
  const events = [...cur.events, ...inc.events.filter((e) => !ids.has(e.id))].sort((a, b) => a.at - b.at);
  const colIds = new Set(cur.collections.map((c) => c.id));
  return {
    ...cur,
    entries,
    events,
    collections: [...cur.collections, ...inc.collections.filter((c) => !colIds.has(c.id))],
    searches: [...new Set([...cur.searches, ...inc.searches])].slice(0, 12),
  };
}

export async function wipe() {
  await AsyncStorage.multiRemove([KEY, BACKUP_KEY, BACKUP_AT_KEY]);
}
