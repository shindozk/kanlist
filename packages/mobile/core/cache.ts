import AsyncStorage from "@react-native-async-storage/async-storage";

/**
 * Two-tier TTL cache (memory + AsyncStorage) with stale-if-error:
 * when the loader fails (offline, API down), a stale value is returned instead of failing.
 */
const PREFIX = "kl:cache:";
const MEM_LIMIT = 250;

interface Entry<T> {
  v: T;
  exp: number;
}

const mem = new Map<string, Entry<unknown>>();

function memSet(key: string, entry: Entry<unknown>) {
  mem.delete(key);
  mem.set(key, entry);
  if (mem.size > MEM_LIMIT) {
    const oldest = mem.keys().next().value;
    if (oldest) mem.delete(oldest);
  }
}

async function readStored<T>(key: string): Promise<Entry<T> | null> {
  try {
    const raw = await AsyncStorage.getItem(PREFIX + key);
    return raw ? (JSON.parse(raw) as Entry<T>) : null;
  } catch {
    return null;
  }
}

async function writeStored(key: string, entry: Entry<unknown>) {
  try {
    await AsyncStorage.setItem(PREFIX + key, JSON.stringify(entry));
  } catch {
    // Quota exceeded (web localStorage) — drop the persistent cache and keep going.
    await clearCache().catch(() => {});
  }
}

export interface CacheOptions {
  ttlMs: number;
  persist?: boolean;
}

export async function cached<T>(key: string, opts: CacheOptions, loader: () => Promise<T>): Promise<T> {
  const now = Date.now();
  const m = mem.get(key) as Entry<T> | undefined;
  if (m && m.exp > now) return m.v;

  const stored = opts.persist ? await readStored<T>(key) : null;
  if (stored && stored.exp > now) {
    memSet(key, stored);
    return stored.v;
  }

  try {
    const v = await loader();
    const entry = { v, exp: now + opts.ttlMs };
    memSet(key, entry);
    if (opts.persist) void writeStored(key, entry);
    return v;
  } catch (e) {
    const stale = stored ?? m;
    if (stale) return stale.v;
    throw e;
  }
}

export async function clearCache() {
  mem.clear();
  const keys = await AsyncStorage.getAllKeys();
  const ours = keys.filter((k) => k.startsWith(PREFIX));
  if (ours.length) await AsyncStorage.multiRemove(ours);
  return ours.length;
}

export async function cacheCount() {
  const keys = await AsyncStorage.getAllKeys();
  return keys.filter((k) => k.startsWith(PREFIX)).length;
}

export const TTL = {
  search: 15 * 60_000,
  discover: 45 * 60_000,
  detail: 12 * 3600_000,
  units: 6 * 3600_000,
  mapping: 7 * 86400_000,
  translate: 60 * 86400_000,
  airing: 30 * 60_000,
} as const;
