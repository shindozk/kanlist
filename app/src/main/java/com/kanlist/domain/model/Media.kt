package com.kanlist.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Media(
    val id: String,
    val idAniList: Int?,
    val idMangaDex: String?,
    val idKitsu: Int?,
    val idMyAnimeList: Int?,
    val title: MediaTitle,
    val type: MediaType,
    val format: MediaFormat,
    val status: MediaStatusAiring,
    val season: MediaSeason?,
    val seasonYear: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val duration: Int?, // minutes per episode
    val averageScore: Double?,
    val popularity: Int?,
    val description: String?, // HTML format
    val coverImage: MediaCoverImage,
    val bannerImage: String?,
    val genres: List<String>,
    val tags: List<MediaTag>,
    val studios: List<MediaStudio>,
    val staff: List<MediaStaff>,
    val characters: List<MediaCharacter>,
    val relations: List<MediaRelation>,
    val recommendations: List<MediaRecommendation>,
    val externalLinks: List<MediaExternalLink>,
    val nextAiringEpisode: NextAiringEpisode?,
    val source: MediaSource,
    val isAdult: Boolean,
    val countryOfOrigin: String?,
    val updatedAt: Long
) {
    val totalEpisodesOrChapters: Int
        get() = when {
            MediaType.isWatchType(type) -> episodes ?: 0
            else -> chapters ?: 0
        }

    val hasEpisodes: Boolean
        get() = MediaType.isWatchType(type) && (episodes ?: 0) > 0

    val hasChapters: Boolean
        get() = MediaType.isReadType(type) && (chapters ?: 0) > 0

    val displayTitle: String
        get() = title.romaji ?: title.english ?: title.native ?: "Sem título"
}

@Serializable
data class MediaTitle(
    val romaji: String?,
    val english: String?,
    val native: String?,
    val userPreferred: String?
)

@Serializable
data class MediaCoverImage(
    val large: String?,
    val medium: String?,
    val color: String?, // dominant color hex
    val extraLarge: String?
)

@Serializable
data class MediaTag(
    val id: Int,
    val name: String,
    val description: String?,
    val category: String?,
    val rank: Int?,
    val isGeneralSpoiler: Boolean,
    val isMediaSpoiler: Boolean,
    val isAdult: Boolean
)

@Serializable
data class MediaStudio(
    val id: Int,
    val name: String,
    val isAnimationStudio: Boolean,
    val isMain: Boolean
)

@Serializable
data class MediaStaff(
    val id: Int,
    val name: MediaName,
    val role: String,
    val language: String?
)

@Serializable
data class MediaCharacter(
    val id: Int,
    val name: MediaName,
    val image: String?,
    val role: String?,
    val voiceActors: List<MediaVoiceActor>
)

@Serializable
data class MediaVoiceActor(
    val id: Int,
    val name: MediaName,
    val image: String?,
    val language: String
)

@Serializable
data class MediaName(
    val full: String?,
    val native: String?,
    val alternative: List<String>?
)

@Serializable
data class MediaRelation(
    val id: String,
    val relationType: MediaRelationType,
    val media: Media
)

@Serializable
data class MediaRecommendation(
    val id: String,
    val rating: Int?, // 0-100
    val media: Media
)

@Serializable
data class MediaExternalLink(
    val id: Int,
    val site: String,
    val url: String,
    val type: String,
    val language: String?,
    val color: String?,
    val icon: String?,
    val notes: String?
)

@Serializable
data class NextAiringEpisode(
    val episode: Int,
    val airingAt: Long, // timestamp
    val timeUntilAiring: Long // milliseconds
)

@Serializable
data class MediaSearchResult(
    val media: List<Media>,
    val pageInfo: PageInfo,
    val sources: List<MediaSource>,
    val errors: Map<MediaSource, String>
)

@Serializable
data class PageInfo(
    val total: Int,
    val perPage: Int,
    val currentPage: Int,
    val lastPage: Int,
    val hasNextPage: Boolean
)