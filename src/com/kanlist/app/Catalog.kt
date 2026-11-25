package com.kanlist.app

import java.util.concurrent.Executors

/** Catalog repository: search + browse presets with fallbacks (mirror of data/repositories/catalog.ts). */
object Catalog {
    private val pool = Executors.newFixedThreadPool(4)

    data class SearchResult(val items: List<Title>, val used: List<String>, val failed: List<String>)

    fun search(query: String, filter: String = "all", adult: Boolean = false): SearchResult {
        val q = query.trim()
        if (q.length < 2) return SearchResult(emptyList(), emptyList(), emptyList())
        val used = mutableListOf<String>()
        val failed = mutableListOf<String>()
        val primary = mutableListOf<Title>()

        // AniList
        try {
            val types = when (filter) {
                "all" -> listOf("ANIME", "MANGA")
                else -> listOf(if (Kind.fromId(filter).medium == Medium.WATCH) "ANIME" else "MANGA")
            }
            for (t in types) {
                primary.addAll(Sources.alBrowse(mapOf("type" to t, "search" to q)))
            }
            used.add("AniList")
        } catch (e: Exception) { failed.add("AniList") }

        // Kitsu fallback if AniList empty
        if (primary.isEmpty()) {
            try {
                val type = if (Kind.fromId(filter).medium == Medium.READ) "manga" else "anime"
                primary.addAll(Sources.kitsuSearch(type, q))
                used.add("Kitsu")
            } catch (e: Exception) { failed.add("Kitsu") }
        }

        // MangaDex for comics
        if (Kind.fromId(filter).medium != Medium.WATCH) {
            try {
                primary.addAll(Sources.mdSearch(q))
                used.add("MangaDex")
            } catch (e: Exception) { failed.add("MangaDex") }
        }

        val merged = dedupe(primary)
            .filter { filter == "all" || it.kind.id == filter }
            .filter { adult || !it.isAdult }
        return SearchResult(rank(merged, q), used, failed)
    }

    private fun rank(items: List<Title>, query: String): List<Title> {
        val q = Sources.normalizeTitle(query)
        return items.sortedByDescending { t ->
            val names = listOfNotNull(t.title, t.titleAlt, t.titleNative).map { Sources.normalizeTitle(it) }
            var s = 0.0
            when {
                names.any { it == q } -> s += 1000
                names.any { it.startsWith(q) } -> s += 500
                names.any { it.contains(q) } -> s += 200
            }
            s += Math.log10((t.popularity ?: 0) + 1.0) * 40
            if (t.source == "anilist") s += 30
            s
        }
    }

    private fun dedupe(items: List<Title>): List<Title> {
        val seenAl = mutableSetOf<String>()
        val seenName = mutableSetOf<String>()
        val out = mutableListOf<Title>()
        for (t in items) {
            val al = if (t.source == "anilist") t.sourceId else null
            val name = "${t.medium}:${Sources.normalizeTitle(t.title)}"
            if ((al != null && seenAl.contains(al)) || seenName.contains(name)) continue
            if (al != null) seenAl.add(al)
            seenName.add(name)
            out.add(t)
        }
        return out
    }

    // ---- browse presets (mirror of domain/browse.ts) ----
    data class Preset(val id: String, val label: String, val subtitle: String, val icon: String, val tile: String, val source: String, val params: Map<String, String>)

    fun presets(): List<Preset> = listOf(
        Preset("trending-anime", "Animes em alta", "O que todo mundo está assistindo", "e53d", "peach", "anilist", mapOf("type" to "ANIME", "sort" to "TRENDING_DESC", "formatNotIn" to "MUSIC")),
        Preset("season", "Temporada", "Lançamentos da temporada", "e8ce", "sage", "anilist", seasonParams()),
        Preset("trending-manga", "Mangás em alta", "Os mais lidos agora", "e865", "honey", "anilist", mapOf("type" to "MANGA", "country" to "JP", "sort" to "TRENDING_DESC", "formatNotIn" to "NOVEL")),
        Preset("manhwa", "Manhwas populares", "Webtoons coreanos", "e324", "sky", "anilist", mapOf("type" to "MANGA", "country" to "KR", "sort" to "POPULARITY_DESC")),
        Preset("donghua", "Donghuas", "Animações chinesas", "e8d1", "lilac", "anilist", mapOf("type" to "ANIME", "country" to "CN", "sort" to "POPULARITY_DESC")),
        Preset("movies", "Filmes", "Longas-metragens", "e3a0", "coral", "anilist", mapOf("type" to "ANIME", "formatIn" to "MOVIE", "sort" to "POPULARITY_DESC")),
        Preset("manhua", "Manhuas", "Quadrinhos chineses", "e8d5", "sand", "anilist", mapOf("type" to "MANGA", "country" to "CN", "sort" to "POPULARITY_DESC")),
        Preset("doujin", "Doujinshis", "Obras independentes (MangaDex)", "e3ae", "blush", "mangadex", mapOf("doujin" to "true")),
    )

    private fun seasonParams(): Map<String, String> {
        val c = java.util.Calendar.getInstance()
        val m = c.get(java.util.Calendar.MONTH)
        val (season, label) = when {
            m <= 2 -> "WINTER" to "Inverno"
            m <= 5 -> "SPRING" to "Primavera"
            m <= 8 -> "SUMMER" to "Verão"
            else -> "FALL" to "Outono"
        }
        return mapOf("type" to "ANIME", "season" to season, "seasonYear" to c.get(java.util.Calendar.YEAR).toString(), "sort" to "POPULARITY_DESC")
    }

    fun presetById(id: String): Preset? = presets().firstOrNull { it.id == id }

    fun browse(presetId: String, page: Int = 1): List<Title> {
        val p = presetById(presetId) ?: return emptyList()
        return if (p.source == "mangadex") {
            Sources.mdPopular(limit = 20, offset = (page - 1) * 20, doujin = p.params["doujin"] == "true")
        } else {
            Sources.alBrowse(p.params, page)
        }
    }

    fun detail(key: String): Title? {
        val idx = key.indexOf(':')
        val source = key.substring(0, idx)
        val id = key.substring(idx + 1)
        return when (source) {
            "anilist" -> Sources.alDetail(id.toIntOrNull() ?: return null)
            "mangadex" -> Sources.mdSearch(id).firstOrNull { it.sourceId == id } ?: run {
                // fetch by id via search of the id is not reliable; fall back to popular lookup
                null
            }
            "kitsu" -> {
                val parts = id.split("-")
                if (parts.size == 2) Sources.kitsuSearch(parts[0], parts[1]).firstOrNull { it.sourceId == id } else null
            }
            "jikan" -> Sources.jikanTitle(id.split("-").firstOrNull() ?: "anime", org.json.JSONObject().put("mal_id", id.substringAfter("-").toIntOrNull() ?: 0))
            else -> null
        }
    }
}
