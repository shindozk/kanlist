package com.kanlist.app

import org.json.JSONObject
import org.json.JSONArray

/** Data sources: AniList (GraphQL), Kitsu, Jikan, MangaDex + mappers. */
object Sources {

    // ---------------- AniList ----------------
    private const val AL = "https://graphql.anilist.co"
    private const val AL_MEDIA = """
      id idMal type format status episodes chapters volumes duration countryOfOrigin isAdult
      title { romaji english native } synonyms
      coverImage { large extraLarge color } bannerImage
      description(asHtml: false) genres season seasonYear
      startDate { year month day } endDate { year month day }
      averageScore meanScore popularity
      studios(isMain: true) { nodes { name } }
      nextAiringEpisode { episode airingAt }
    """

    private fun alGql(query: String, vars: String): JSONObject? {
        val body = JSONObject().put("query", query).put("variables", JSONObject(vars))
        val r = Http.postJson(AL, body.toString())
        if (!r.ok) return null
        val root = J.obj(r.body) ?: return null
        if (root.optJSONArray("errors") != null && !root.has("data")) return null
        return root.optJSONObject("data")
    }

    fun alKind(type: String, format: String?, country: String?): Kind {
        val c = country ?: "JP"
        if (type == "ANIME") {
            if (format == "MOVIE") return Kind.MOVIE
            if (format == "OVA" || format == "SPECIAL" || format == "MUSIC") return Kind.OVA
            if (c == "CN" || c == "TW") return Kind.DONGHUA
            return Kind.ANIME
        }
        if (format == "NOVEL") return Kind.NOVEL
        if (format == "ONE_SHOT") return Kind.ONESHOT
        if (c == "KR") return Kind.MANHWA
        if (c == "CN" || c == "TW") return Kind.MANHUA
        return Kind.MANGA
    }

    fun alTitle(m: JSONObject): Title {
        val type = J.str(m, "type") ?: "ANIME"
        val format = J.str(m, "format")
        val country = J.str(m, "countryOfOrigin")
        val kind = alKind(type, format, country)
        val t = J.objAt(m, "title")
        val romaji = J.str(t, "romaji")
        val english = J.str(t, "english")
        val native = J.str(t, "native")
        val total = if (kind.medium == Medium.WATCH) J.int(m, "episodes") else J.int(m, "chapters")
        val nextEp = J.int(m, "nextAiringEpisode", "episode")
        val released = if (kind.medium == Medium.WATCH && nextEp != null) (nextEp - 1).coerceAtLeast(0) else total
        val seasonMap = mapOf("WINTER" to "Inverno", "SPRING" to "Primavera", "SUMMER" to "Verão", "FALL" to "Outono")
        val status = when (J.str(m, "status")) {
            "RELEASING" -> ReleaseStatus.RELEASING
            "FINISHED" -> ReleaseStatus.FINISHED
            "NOT_YET_RELEASED" -> ReleaseStatus.UPCOMING
            "HIATUS" -> ReleaseStatus.HIATUS
            "CANCELLED" -> ReleaseStatus.CANCELLED
            else -> ReleaseStatus.UNKNOWN
        }
        return Title(
            key = "anilist:" + J.str(m, "id"),
            source = "anilist", sourceId = J.str(m, "id") ?: "",
            kind = kind,
            title = romaji ?: english ?: native ?: "Sem título",
            titleAlt = if (english != null && english != romaji) english else null,
            titleNative = native,
            cover = J.str(m, "coverImage", "extraLarge") ?: J.str(m, "coverImage", "large"),
            banner = J.str(m, "bannerImage"),
            synopsis = stripHtml(J.str(m, "description")),
            synopsisLang = "en",
            genres = J.strArray(J.arrAt(m, "genres")),
            year = J.int(m, "seasonYear") ?: J.int(m, "startDate", "year"),
            season = J.str(m, "season")?.let { seasonMap[it] },
            status = status,
            totalUnits = total,
            totalVolumes = J.int(m, "volumes"),
            releasedUnits = released,
            unitDuration = J.int(m, "duration"),
            score = J.int(m, "averageScore") ?: J.int(m, "meanScore"),
            popularity = J.int(m, "popularity"),
            studios = studios(m),
            isAdult = J.bool(m, "isAdult") ?: false,
            nextAiring = if (nextEp != null && J.int(m, "nextAiringEpisode", "airingAt") != null) nextEp to (J.int(m, "nextAiringEpisode", "airingAt")!! * 1000L) else null,
            externalMal = J.str(m, "idMal"),
        )
    }

    private fun studios(m: JSONObject): List<String> {
        val nodes = J.arrAt(m, "studios", "nodes") ?: return emptyList()
        return (0 until nodes.length()).mapNotNull { J.str(nodes.getJSONObject(it), "name") }
    }

    fun alBrowse(params: Map<String, String>, page: Int = 1): List<Title> {
        val query = """
          query (%s) {
            Page(page: $page, perPage: 20) {
              pageInfo { hasNextPage total }
              media(%s) { %s }
            }
          }
        """.trimIndent()
        val varDefs = buildString {
            append("\u0024\u0024type: MediaType")
            if (params.containsKey("search")) append(", \u0024\u0024search: String")
            if (params.containsKey("sort")) append(", \u0024\u0024sort: [MediaSort]")
            if (params.containsKey("season")) append(", \u0024\u0024season: MediaSeason")
            if (params.containsKey("seasonYear")) append(", \u0024\u0024seasonYear: Int")
            if (params.containsKey("genre")) append(", \u0024\u0024genre: String")
            if (params.containsKey("country")) append(", \u0024\u0024country: CountryCode")
            if (params.containsKey("formatIn")) append(", \u0024\u0024formatIn: [MediaFormat]")
            if (params.containsKey("formatNotIn")) append(", \u0024\u0024formatNotIn: [MediaFormat]")
        }
        val args = buildString {
            append("type: \u0024\u0024type")
            params["search"]?.let { append(", search: \u0024\u0024search") }
            params["sort"]?.let { append(", sort: \u0024\u0024sort") }
            params["season"]?.let { append(", season: \u0024\u0024season") }
            params["seasonYear"]?.let { append(", seasonYear: \u0024\u0024seasonYear") }
            params["genre"]?.let { append(", genre: \u0024\u0024genre") }
            params["country"]?.let { append(", countryOfOrigin: \u0024\u0024country") }
            params["formatIn"]?.let { append(", format_in: \u0024\u0024formatIn") }
            params["formatNotIn"]?.let { append(", format_not_in: \u0024\u0024formatNotIn") }
        }
        val vars = JSONObject().apply {
            put("type", params["type"] ?: "ANIME")
            params["search"]?.let { put("search", it) }
            params["sort"]?.let { put("sort", JSONArray(listOf(it))) }
            params["season"]?.let { put("season", it) }
            params["seasonYear"]?.let { put("seasonYear", it.toInt()) }
            params["genre"]?.let { put("genre", it) }
            params["country"]?.let { put("country", it) }
            params["formatIn"]?.let { put("formatIn", JSONArray(it.split(","))) }
            params["formatNotIn"]?.let { put("formatNotIn", JSONArray(it.split(","))) }
        }
        val q = String.format(query, varDefs, args, AL_MEDIA)
        val data = alGql(q, vars.toString()) ?: return emptyList()
        val pageObj = data.optJSONObject("Page") ?: return emptyList()
        val media = pageObj.optJSONArray("media") ?: return emptyList()
        return (0 until media.length()).map { alTitle(media.getJSONObject(it)) }
    }

    fun alDetail(id: Int): Title? {
        val query = """
          query (\$id:Int) {
            Media(id:\$id) {
              %s
              relations { edges { relationType node { %s } } }
              recommendations(perPage: 12, sort: RATING_DESC) { nodes { mediaRecommendation { %s } } }
              characters(perPage: 12, sort: [ROLE, RELEVANCE]) { edges { role node { name { full } image { medium } } } }
              staff(perPage: 8, sort: RELEVANCE) { edges { role node { name { full } } } }
              externalLinks { site url }
              tags { name rank isMediaSpoiler }
            }
          }
        """.trimIndent().format(AL_MEDIA, AL_MEDIA, AL_MEDIA)
        val data = alGql(query, "{\"id\":$id}") ?: return null
        val media = data.optJSONObject("Media") ?: return null
        return alTitle(media)
    }

    // ---------------- Kitsu ----------------
    private const val KITSU = "https://kitsu.io/api/edge"
    private val KITSU_HEADERS = mapOf("Accept" to "application/vnd.api+json")

    fun kitsuKind(type: String, subtype: String?): Kind {
        val st = subtype ?: ""
        if (type == "anime") {
            if (st == "movie") return Kind.MOVIE
            if (st == "OVA" || st == "special" || st == "music") return Kind.OVA
            return Kind.ANIME
        }
        return when (st) {
            "manhwa" -> Kind.MANHWA
            "manhua" -> Kind.MANHUA
            "novel" -> Kind.NOVEL
            "oneshot" -> Kind.ONESHOT
            "doujin" -> Kind.DOUJIN
            else -> Kind.MANGA
        }
    }

    fun kitsuTitle(m: JSONObject): Title {
        val type = J.str(m, "type") ?: "anime"
        val a = J.objAt(m, "attributes") ?: JSONObject()
        val titles = J.objAt(a, "titles")
        val canonical = J.str(a, "canonicalTitle") ?: "Sem título"
        val en = J.str(titles, "en")
        val ja = J.str(titles, "ja_jp")
        val poster = J.objAt(a, "posterImage")
        val rating = J.str(a, "averageRating")?.toDoubleOrNull()
        val status = when (J.str(a, "status")) {
            "current" -> ReleaseStatus.RELEASING
            "finished" -> ReleaseStatus.FINISHED
            "upcoming", "unreleased", "tba" -> ReleaseStatus.UPCOMING
            else -> ReleaseStatus.UNKNOWN
        }
        return Title(
            key = "kitsu:$type-" + J.str(m, "id"),
            source = "kitsu", sourceId = "$type-" + J.str(m, "id"),
            kind = kitsuKind(type, J.str(a, "subtype")),
            title = J.str(titles, "en_jp") ?: canonical,
            titleAlt = if (en != null && en != canonical) en else null,
            titleNative = ja,
            cover = J.str(poster, "large") ?: J.str(poster, "medium") ?: J.str(poster, "original"),
            synopsis = stripHtml(J.str(a, "synopsis")), synopsisLang = "en",
            year = J.str(a, "startDate")?.take(4)?.toIntOrNull(),
            status = status,
            totalUnits = if (type == "anime") J.int(a, "episodeCount") else J.int(a, "chapterCount"),
            totalVolumes = J.int(a, "volumeCount"),
            unitDuration = J.int(a, "episodeLength"),
            score = rating?.toInt(),
            popularity = J.int(a, "userCount"),
            isAdult = J.bool(a, "nsfw") ?: false,
            externalKitsu = J.str(m, "id"),
        )
    }

    fun kitsuSearch(type: String, text: String): List<Title> {
        val r = Http.get("$KITSU/$type?filter[text]=${enc(text)}&page[limit]=12", KITSU_HEADERS)
        if (!r.ok) return emptyList()
        val data = J.arrAt(J.obj(r.body), "data") ?: return emptyList()
        return (0 until data.length()).map { kitsuTitle(data.getJSONObject(it)) }
    }

    // ---------------- Jikan ----------------
    private const val JIKAN = "https://api.jikan.moe/v4"

    fun jikanTitle(type: String, m: JSONObject): Title {
        val s = (J.str(m, "type") ?: "").lowercase()
        val kind = when {
            type == "anime" && s == "movie" -> Kind.MOVIE
            type == "anime" && (s == "ova" || s == "special" || s == "music") -> Kind.OVA
            type == "anime" -> Kind.ANIME
            s == "manhwa" -> Kind.MANHWA
            s == "manhua" -> Kind.MANHUA
            s.contains("novel") -> Kind.NOVEL
            s == "one-shot" -> Kind.ONESHOT
            s == "doujinshi" -> Kind.DOUJIN
            else -> Kind.MANGA
        }
        val status = when (J.str(m, "status")) {
            "Currently Airing", "Publishing" -> ReleaseStatus.RELEASING
            "Finished Airing", "Finished" -> ReleaseStatus.FINISHED
            "Not yet aired" -> ReleaseStatus.UPCOMING
            "On Hiatus" -> ReleaseStatus.HIATUS
            "Discontinued" -> ReleaseStatus.CANCELLED
            else -> ReleaseStatus.UNKNOWN
        }
        val from = J.str(J.objAt(m, "aired"), "from") ?: J.str(J.objAt(m, "published"), "from")
        val img = J.objAt(J.objAt(m, "images"), "jpg")
        val score = J.str(m, "score")?.toDoubleOrNull()
        return Title(
            key = "jikan:$type-" + J.str(m, "mal_id"),
            source = "jikan", sourceId = "$type-" + J.str(m, "mal_id"),
            kind = kind,
            title = J.str(m, "title") ?: "Sem título",
            titleAlt = J.str(m, "title_english"),
            titleNative = J.str(m, "title_japanese"),
            cover = J.str(img, "large_image_url") ?: J.str(img, "image_url"),
            synopsis = stripHtml(J.str(m, "synopsis")), synopsisLang = "en",
            genres = J.strArray(J.arrAt(m, "genres")).mapNotNull { g -> J.str(J.obj(g), "name") },
            year = J.int(m, "year") ?: from?.take(4)?.toIntOrNull(),
            status = status,
            totalUnits = if (kind.medium == Medium.WATCH) J.int(m, "episodes") else J.int(m, "chapters"),
            totalVolumes = J.int(m, "volumes"),
            score = score?.times(10)?.toInt(),
            popularity = J.int(m, "members"),
            externalMal = J.str(m, "mal_id"),
        )
    }

    fun jikanSearch(type: String, q: String): List<Title> {
        val r = Http.get("$JIKAN/$type?q=${enc(q)}&limit=12&sfw=true")
        if (!r.ok) return emptyList()
        val data = J.arrAt(J.obj(r.body), "data") ?: return emptyList()
        return (0 until data.length()).map { jikanTitle(type, data.getJSONObject(it)) }
    }

    // ---------------- MangaDex ----------------
    private const val MD = "https://api.mangadex.org"
    private const val MD_COVERS = "https://uploads.mangadex.org/covers"
    private const val TAG_DOUJINSHI = "b13b2a48-c720-44a9-9c77-39c9979373fb"
    private const val TAG_ONESHOT = "0234a31e-a729-4e28-9d6a-3f87c4966b9e"
    private val MD_SAFE = listOf("safe", "suggestive")

    fun mdKind(a: JSONObject): Kind {
        val tags = J.arrAt(a, "tags") ?: J.arr("")!!
        val ids = (0 until tags.length()).map { J.str(tags.getJSONObject(it), "id") }
        if (ids.contains(TAG_DOUJINSHI)) return Kind.DOUJIN
        if (ids.contains(TAG_ONESHOT)) return Kind.ONESHOT
        return when (J.str(a, "originalLanguage")) {
            "ko" -> Kind.MANHWA
            "zh", "zh-hk" -> Kind.MANHUA
            else -> Kind.MANGA
        }
    }

    fun mdTitle(m: JSONObject): Title {
        val a = J.objAt(m, "attributes") ?: JSONObject()
        val titleMap = J.objAt(a, "title")
        val main = pick(titleMap, "en", "ja-ro", "ko-ro", "zh-ro") ?: "Sem título"
        val alt = J.arrAt(a, "altTitles")
        var altEn: String? = null
        var nativeT: String? = null
        if (alt != null) for (i in 0 until alt.length()) {
            val o = alt.getJSONObject(i)
            if (altEn == null && o.has("en")) altEn = o.optString("en")
            if (nativeT == null && (o.has("ja") || o.has("ko") || o.has("zh"))) nativeT = o.optString("ja").ifEmpty { o.optString("ko").ifEmpty { o.optString("zh") } }
        }
        val rels = J.arrAt(m, "relationships") ?: J.arr("")!!
        val coverFile = (0 until rels.length()).firstOrNull { J.str(rels.getJSONObject(it), "type") == "cover_art" }
            ?.let { J.str(J.objAt(rels.getJSONObject(it), "attributes"), "fileName") }
        val authors = (0 until rels.length()).map { rels.getJSONObject(it) }
            .filter { J.str(it, "type") == "author" || J.str(it, "type") == "artist" }
            .mapNotNull { J.str(J.objAt(it, "attributes"), "name") }
        val status = when (J.str(a, "status")) {
            "ongoing" -> ReleaseStatus.RELEASING
            "completed" -> ReleaseStatus.FINISHED
            "hiatus" -> ReleaseStatus.HIATUS
            "cancelled" -> ReleaseStatus.CANCELLED
            else -> ReleaseStatus.UNKNOWN
        }
        val lastCh = J.str(a, "lastChapter")?.toIntOrNull()
        val links = J.objAt(a, "links")
        val desc = J.objAt(a, "description")
        val pt = J.str(desc, "pt-br")
        return Title(
            key = "mangadex:" + J.str(m, "id"),
            source = "mangadex", sourceId = J.str(m, "id") ?: "",
            kind = mdKind(a),
            title = main,
            titleAlt = if (altEn != null && altEn != main) altEn else null,
            titleNative = nativeT,
            cover = coverFile?.let { "$MD_COVERS/${J.str(m, "id")}/$it.512.jpg" },
            synopsis = stripHtml(pt ?: pick(desc, "en")), synopsisLang = if (pt != null) "pt" else "en",
            year = J.int(a, "year"),
            status = status,
            totalUnits = if (J.str(a, "status") == "completed") lastCh else null,
            totalVolumes = J.str(a, "lastVolume")?.toIntOrNull(),
            authors = authors,
            isAdult = J.str(a, "contentRating") == "erotica" || J.str(a, "contentRating") == "pornographic",
            externalMangadex = J.str(m, "id"),
            externalMal = J.str(links, "mal"),
        )
    }

    fun mdSearch(title: String, limit: Int = 12, doujin: Boolean = false): List<Title> {
        val r = Http.get("$MD/manga?title=${enc(title)}&limit=$limit&includes[]=cover_art&includes[]=author&contentRating[]=${MD_SAFE.joinToString("&contentRating[]=")}&order[relevance]=desc" + if (doujin) "&includedTags[]=$TAG_DOUJINSHI" else "")
        if (!r.ok) return emptyList()
        val data = J.arrAt(J.obj(r.body), "data") ?: return emptyList()
        return (0 until data.length()).map { mdTitle(data.getJSONObject(it)) }
    }

    fun mdPopular(limit: Int = 20, offset: Int = 0, doujin: Boolean = false): List<Title> {
        val r = Http.get("$MD/manga?limit=$limit&offset=$offset&includes[]=cover_art&includes[]=author&contentRating[]=${MD_SAFE.joinToString("&contentRating[]=")}&order[followedCount]=desc&hasAvailableChapters=true" + if (doujin) "&includedTags[]=$TAG_DOUJINSHI" else "")
        if (!r.ok) return emptyList()
        val data = J.arrAt(J.obj(r.body), "data") ?: return emptyList()
        return (0 until data.length()).map { mdTitle(data.getJSONObject(it)) }
    }

    // ---------------- helpers ----------------
    private fun pick(map: JSONObject?, vararg langs: String): String? {
        if (map == null) return null
        for (l in langs) if (map.has(l)) return map.optString(l)
        return if (map.length() > 0) map.optString(map.keys().next()) else null
    }

    private fun enc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")

    fun stripHtml(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val out = input.replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("</p>"), "\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&quot;", "\"").replace("&#039;", "'").replace("&apos;", "'")
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&mdash;", "—")
            .replace(Regex("\\(Source:[^)]*\\)", setOf(RegexOption.IGNORE_CASE)), "")
            .replace("[Written by MAL Rewrite]", "")
            .replace(Regex("\\n{3,}"), "\n\n").trim()
        return out.ifEmpty { null }
    }

    fun normalizeTitle(s: String): String = s.lowercase()
        .replace(Regex("[\\u0300-\\u036f]"), "")
        .replace(Regex("[^a-z0-9]+"), " ").trim()
}
