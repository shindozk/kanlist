package com.kanlist.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local persistence (mirror of data/repositories/library.ts) using SharedPreferences. */
object Store {
    private const val KEY = "kl_db"
    private const val VERSION = 2

    data class Doc(
        val entries: MutableMap<String, LibraryEntry> = mutableMapOf(),
        val events: MutableList<ActivityEvent> = mutableListOf(),
        val collections: MutableList<Collection> = mutableListOf(),
        var settings: Settings = Settings(),
        val searches: MutableList<String> = mutableListOf(),
    )

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("kanlist", Context.MODE_PRIVATE)

    fun load(ctx: Context): Doc {
        val raw = prefs(ctx).getString(KEY, null) ?: return Doc()
        return try { parse(raw) } catch (e: Exception) { Doc() }
    }

    fun save(ctx: Context, doc: Doc) {
        val o = JSONObject()
        o.put("version", VERSION)
        val entries = JSONObject()
        for ((k, e) in doc.entries) entries.put(k, entryJson(e))
        o.put("entries", entries)
        val events = JSONArray()
        for (ev in doc.events) events.put(JSONObject().apply {
            put("id", ev.id); put("key", ev.key); put("kind", ev.kind.id)
            put("unit", ev.unit); put("count", ev.count); put("at", ev.at); put("minutes", ev.minutes)
        })
        o.put("events", events)
        val cols = JSONArray()
        for (c in doc.collections) cols.put(JSONObject().apply {
            put("id", c.id); put("name", c.name); put("description", c.description)
            put("tile", c.tile); put("keys", JSONArray(c.keys)); put("createdAt", c.createdAt); put("updatedAt", c.updatedAt)
        })
        o.put("collections", cols)
        val s = doc.settings
        o.put("settings", JSONObject().apply {
            put("name", s.name); put("onboarded", s.onboarded); put("theme", s.theme)
            put("dailyGoal", s.dailyGoal); put("titleLanguage", s.titleLanguage)
            put("chapterLangs", JSONArray(s.chapterLangs)); put("showAdult", s.showAdult)
            put("autoTranslate", s.autoTranslate); put("haptics", s.haptics)
            put("interests", JSONArray(s.interests.map { it.id }))
            put("sources", JSONObject().apply { put("kitsu", s.kitsu); put("jikan", s.jikan); put("mangadex", s.mangadex); put("translate", s.translate) })
        })
        o.put("searches", JSONArray(doc.searches))
        prefs(ctx).edit().putString(KEY, o.toString()).apply()
    }

    private fun entryJson(e: LibraryEntry): JSONObject = JSONObject().apply {
        put("key", e.key)
        put("title", titleJson(e.title))
        put("status", e.status.name)
        val done = JSONObject(); for ((k, v) in e.done) done.put(k, v); put("done", done)
        put("volumes", e.volumes); e.rating?.let { put("rating", it) }
        put("notes", e.notes); put("favorite", e.favorite); put("repeatCount", e.repeatCount)
        e.customTotal?.let { put("customTotal", it) }
        put("addedAt", e.addedAt); put("updatedAt", e.updatedAt)
        e.startedAt?.let { put("startedAt", it) }; e.finishedAt?.let { put("finishedAt", it) }
    }

    private fun titleJson(t: Title): JSONObject = JSONObject().apply {
        put("key", t.key); put("source", t.source); put("sourceId", t.sourceId); put("kind", t.kind.id)
        put("title", t.title); t.titleAlt?.let { put("titleAlt", it) }; t.titleNative?.let { put("titleNative", it) }
        t.cover?.let { put("cover", it) }; t.banner?.let { put("banner", it) }
        t.synopsis?.let { put("synopsis", it) }; t.synopsisLang?.let { put("synopsisLang", it) }
        put("genres", JSONArray(t.genres)); t.year?.let { put("year", it) }; t.season?.let { put("season", it) }
        put("status", t.status.name); t.totalUnits?.let { put("totalUnits", it) }; t.totalVolumes?.let { put("totalVolumes", it) }
        t.releasedUnits?.let { put("releasedUnits", it) }; t.unitDuration?.let { put("unitDuration", it) }
        t.score?.let { put("score", it) }; t.popularity?.let { put("popularity", it) }
        put("studios", JSONArray(t.studios)); put("authors", JSONArray(t.authors)); put("isAdult", t.isAdult)
        t.externalMal?.let { put("externalMal", it) }; t.externalKitsu?.let { put("externalKitsu", it) }; t.externalMangadex?.let { put("externalMangadex", it) }
    }

    private fun parse(raw: String): Doc {
        val o = JSONObject(raw)
        val entries = mutableMapOf<String, LibraryEntry>()
        val eo = o.optJSONObject("entries")
        if (eo != null) for (k in eo.keys()) entries[k] = entryFrom(eo.getJSONObject(k))
        val events = mutableListOf<ActivityEvent>()
        o.optJSONArray("events")?.let { a -> for (i in 0 until a.length()) { val e = a.getJSONObject(i); events.add(ActivityEvent(e.getString("id"), e.getString("key"), Kind.fromId(e.optString("kind")), e.getString("unit"), e.optInt("count", 1), e.optLong("at"), e.optInt("minutes"))) } }
        val cols = mutableListOf<Collection>()
        o.optJSONArray("collections")?.let { a -> for (i in 0 until a.length()) { val c = a.getJSONObject(i); cols.add(Collection(c.getString("id"), c.getString("name"), c.optString("description"), c.optString("tile", "peach"), mutableListOf<String>().apply { c.optJSONArray("keys")?.let { k -> for (j in 0 until k.length()) add(k.getString(j)) } }, c.optLong("createdAt"), c.optLong("updatedAt"))) } }
        val settings = Settings()
        o.optJSONObject("settings")?.let { s ->
            settings.name = s.optString("name"); settings.onboarded = s.optBoolean("onboarded"); settings.theme = s.optString("theme", "system")
            settings.dailyGoal = s.optInt("dailyGoal", 3); settings.titleLanguage = s.optString("titleLanguage", "romaji")
            s.optJSONArray("chapterLangs")?.let { a -> settings.chapterLangs = (0 until a.length()).map { a.getString(it) } }
            settings.showAdult = s.optBoolean("showAdult"); settings.autoTranslate = s.optBoolean("autoTranslate"); settings.haptics = s.optBoolean("haptics")
            s.optJSONArray("interests")?.let { a -> settings.interests = (0 until a.length()).map { Kind.fromId(a.getString(it)) } }
            s.optJSONObject("sources")?.let { src -> settings.kitsu = src.optBoolean("kitsu", true); settings.jikan = src.optBoolean("jikan", true); settings.mangadex = src.optBoolean("mangadex", true); settings.translate = src.optBoolean("translate", true) }
        }
        val searches = mutableListOf<String>()
        o.optJSONArray("searches")?.let { a -> for (i in 0 until a.length()) searches.add(a.getString(i)) }
        return Doc(entries, events, cols, settings, searches)
    }

    private fun entryFrom(o: JSONObject): LibraryEntry {
        val t = titleFrom(o.getJSONObject("title"))
        val done = mutableMapOf<String, Long>()
        o.optJSONObject("done")?.let { d -> for (k in d.keys()) done[k] = d.getLong(k) }
        return LibraryEntry(o.getString("key"), t, TrackingStatus.valueOf(o.optString("status", "PLANNING")), done,
            o.optInt("volumes"), if (o.has("rating")) o.optInt("rating") else null, o.optString("notes"),
            o.optBoolean("favorite"), o.optInt("repeatCount"), if (o.has("customTotal")) o.optInt("customTotal") else null,
            o.optLong("addedAt"), o.optLong("updatedAt"), if (o.has("startedAt")) o.optLong("startedAt") else null, if (o.has("finishedAt")) o.optLong("finishedAt") else null)
    }

    private fun titleFrom(o: JSONObject): Title = Title(
        key = o.getString("key"), source = o.optString("source"), sourceId = o.optString("sourceId"),
        kind = Kind.fromId(o.optString("kind")), title = o.optString("title"),
        titleAlt = if (o.has("titleAlt")) o.optString("titleAlt") else null,
        titleNative = if (o.has("titleNative")) o.optString("titleNative") else null,
        cover = if (o.has("cover")) o.optString("cover") else null,
        banner = if (o.has("banner")) o.optString("banner") else null,
        synopsis = if (o.has("synopsis")) o.optString("synopsis") else null,
        synopsisLang = if (o.has("synopsisLang")) o.optString("synopsisLang") else null,
        genres = o.optJSONArray("genres")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        year = if (o.has("year")) o.optInt("year") else null,
        season = if (o.has("season")) o.optString("season") else null,
        status = runCatching { ReleaseStatus.valueOf(o.optString("status", "UNKNOWN")) }.getOrDefault(ReleaseStatus.UNKNOWN),
        totalUnits = if (o.has("totalUnits")) o.optInt("totalUnits") else null,
        totalVolumes = if (o.has("totalVolumes")) o.optInt("totalVolumes") else null,
        releasedUnits = if (o.has("releasedUnits")) o.optInt("releasedUnits") else null,
        unitDuration = if (o.has("unitDuration")) o.optInt("unitDuration") else null,
        score = if (o.has("score")) o.optInt("score") else null,
        popularity = if (o.has("popularity")) o.optInt("popularity") else null,
        studios = o.optJSONArray("studios")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        authors = o.optJSONArray("authors")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
        isAdult = o.optBoolean("isAdult"),
        externalMal = if (o.has("externalMal")) o.optString("externalMal") else null,
        externalKitsu = if (o.has("externalKitsu")) o.optString("externalKitsu") else null,
        externalMangadex = if (o.has("externalMangadex")) o.optString("externalMangadex") else null,
    )
}
