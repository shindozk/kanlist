package com.kanlist.app

import java.io.Serializable

/** Kanlist domain model — pure Kotlin mirror of packages/mobile/domain/types.ts. */

enum class Medium { WATCH, READ }

enum class Kind(
    val id: String,
    val label: String,
    val plural: String,
    val medium: Medium,
    val icon: String,
    val tile: String,
    val minutes: Int,
) {
    ANIME("anime", "Anime", "Animes", Medium.WATCH, "ic_tv", "peach", 24),
    MOVIE("movie", "Filme", "Filmes", Medium.WATCH, "ic_film", "honey", 105),
    OVA("ova", "OVA/Especial", "OVAs", Medium.WATCH, "ic_sparkle", "coral", 25),
    DONGHUA("donghua", "Donghua", "Donghuas", Medium.WATCH, "ic_flower", "rose", 20),
    MANGA("manga", "Mangá", "Mangás", Medium.READ, "ic_book", "sage", 6),
    MANHWA("manhwa", "Manhwa", "Manhwas", Medium.READ, "ic_phone", "sky", 5),
    MANHUA("manhua", "Manhua", "Manhuas", Medium.READ, "ic_menu_book", "lilac", 5),
    NOVEL("novel", "Novel", "Novels", Medium.READ, "ic_description", "sand", 20),
    ONESHOT("oneshot", "One-shot", "One-shots", Medium.READ, "ic_bolt", "mint", 15),
    DOUJIN("doujin", "Doujin", "Doujins", Medium.READ, "ic_brush", "blush", 8);

    companion object {
        fun fromId(id: String): Kind = entries.firstOrNull { it.id == id } ?: MANGA
    }
}

enum class ReleaseStatus { RELEASING, FINISHED, UPCOMING, HIATUS, CANCELLED, UNKNOWN }

enum class TrackingStatus { CURRENT, PLANNING, COMPLETED, PAUSED, DROPPED, REPEATING }

data class Title(
    val key: String,
    val source: String,
    val sourceId: String,
    val kind: Kind,
    val title: String,
    val titleAlt: String? = null,
    val titleNative: String? = null,
    val cover: String? = null,
    val banner: String? = null,
    val synopsis: String? = null,
    val synopsisLang: String? = null,
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val season: String? = null,
    val status: ReleaseStatus = ReleaseStatus.UNKNOWN,
    val totalUnits: Int? = null,
    val totalVolumes: Int? = null,
    val releasedUnits: Int? = null,
    val unitDuration: Int? = null,
    val score: Int? = null,
    val popularity: Int? = null,
    val studios: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val isAdult: Boolean = false,
    val nextAiring: Pair<Int, Long>? = null,
    val externalMal: String? = null,
    val externalKitsu: String? = null,
    val externalMangadex: String? = null,
) : java.io.Serializable {
    val medium: Medium get() = kind.medium
}

data class LibraryEntry(
    val key: String,
    val title: Title,
    var status: TrackingStatus,
    val done: MutableMap<String, Long> = mutableMapOf(),
    val volumes: Int = 0,
    val rating: Int? = null,
    val notes: String = "",
    val favorite: Boolean = false,
    var repeatCount: Int = 0,
    val customTotal: Int? = null,
    val addedAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var startedAt: Long? = null,
    var finishedAt: Long? = null,
)

data class ActivityEvent(
    val id: String,
    val key: String,
    val kind: Kind,
    val unit: String,
    val count: Int,
    val at: Long,
    val minutes: Int,
)

data class Collection(
    val id: String,
    val name: String,
    val description: String = "",
    val tile: String = "peach",
    val keys: MutableList<String> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
)

data class Settings(
    var name: String = "",
    var onboarded: Boolean = false,
    var theme: String = "system",
    var dailyGoal: Int = 3,
    var titleLanguage: String = "romaji",
    var chapterLangs: List<String> = listOf("pt-br", "en"),
    var showAdult: Boolean = false,
    var autoTranslate: Boolean = true,
    var haptics: Boolean = true,
    var interests: List<Kind> = listOf(Kind.ANIME, Kind.MANGA, Kind.MANHWA),
    var kitsu: Boolean = true,
    var jikan: Boolean = true,
    var mangadex: Boolean = true,
    var translate: Boolean = true,
)
