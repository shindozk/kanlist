package com.kanlist.data.source.api.kitsu

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

@Serializable
data class KitsuResponse<T>(
    val data: T,
    val links: KitsuLinks?,
    val meta: KitsuMeta?,
    val included: List<KitsuResource>? = null
)

@Serializable
data class KitsuListResponse<T>(
    val data: List<T>,
    val links: KitsuLinks?,
    val meta: KitsuMeta?,
    val included: List<KitsuResource>? = null
)

@Serializable
data class KitsuLinks(
    val first: String?,
    val last: String?,
    val prev: String?,
    val next: String?
)

@Serializable
data class KitsuMeta(
    val count: Int?
)

@Serializable
data class KitsuResource(
    val id: String,
    val type: String,
    val attributes: KitsuAttributes?,
    val relationships: KitsuRelationships?
)

@Serializable
data class KitsuAttributes(
    val slug: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val titles: Map<String, String>?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,
    val averageRating: String?,
    val ratingFrequencies: Map<String, Int>?,
    val popularityRank: Int?,
    val ratingRank: Int?,
    val ageRating: String?,
    val ageRatingGuide: String?,
    val subtype: String?,
    val status: String?,
    val tba: String?,
    val posterImage: KitsuImage?,
    val coverImage: KitsuImage?,
    val episodeCount: Int?,
    val episodeLength: Int?,
    val totalLength: Int?,
    val youtubeVideoId: String?,
    val showType: String?,
    val nsfw: Boolean?,
    val description: String?,
    val startDate: String?,
    val endDate: String?,
    val nextRelease: String?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val serialization: String?,
    val synopsis: String?
)

@Serializable
data class KitsuImage(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val meta: KitsuImageMeta?
)

@Serializable
data class KitsuImageMeta(
    val dimensions: KitsuDimensions?
)

@Serializable
data class KitsuDimensions(
    val tiny: KitsuDimension?,
    val small: KitsuDimension?,
    val medium: KitsuDimension?,
    val large: KitsuDimension?,
    val original: KitsuDimension?
)

@Serializable
data class KitsuDimension(
    val width: Int?,
    val height: Int?
)

@Serializable
data class KitsuRelationships(
    val episodes: KitsuRelationshipData?,
    val chapters: KitsuRelationshipData?,
    val characters: KitsuRelationshipData?,
    val staff: KitsuRelationshipData?,
    val mappings: KitsuRelationshipData?,
    val genres: KitsuRelationshipData?,
    val categories: KitsuRelationshipData?,
    val recommendations: KitsuRelationshipData?
)

@Serializable
data class KitsuRelationshipData(
    val links: KitsuRelationshipLinks?,
    val data: List<KitsuResourceIdentifier>?
)

@Serializable
data class KitsuRelationshipLinks(
    val self: String?,
    val related: String?
)

@Serializable
data class KitsuResourceIdentifier(
    val id: String,
    val type: String
)

@Serializable
data class KitsuEpisode(
    val id: String,
    val type: String = "episodes",
    val attributes: KitsuEpisodeAttributes,
    val relationships: KitsuEpisodeRelationships?
)

@Serializable
data class KitsuEpisodeAttributes(
    val number: Int?,
    val title: String?,
    val synopsis: String?,
    val airdate: String?,
    val length: Int?,
    val thumbnail: String?,
    val seasonNumber: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

@Serializable
data class KitsuEpisodeRelationships(
    val media: KitsuRelationshipData?
)

@Serializable
data class KitsuChapter(
    val id: String,
    val type: String = "chapters",
    val attributes: KitsuChapterAttributes,
    val relationships: KitsuChapterRelationships?
)

@Serializable
data class KitsuChapterAttributes(
    val number: String?,
    val volume: String?,
    val title: String?,
    val synopsis: String?,
    val published: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val length: Int?,
    val language: String?
)

@Serializable
data class KitsuChapterRelationships(
    val manga: KitsuRelationshipData?
)

@Serializable
data class KitsuMapping(
    val id: String,
    val type: String = "mappings",
    val attributes: KitsuMappingAttributes
)

@Serializable
data class KitsuMappingAttributes(
    val externalSite: String,
    val externalId: String
)

@Serializable
data class KitsuGenre(
    val id: String,
    val type: String = "genres",
    val attributes: KitsuGenreAttributes
)

@Serializable
data class KitsuGenreAttributes(
    val name: String,
    val slug: String,
    val description: String?,
    val hentai: Boolean?
)

@Serializable
data class KitsuCategory(
    val id: String,
    val type: String = "categories",
    val attributes: KitsuCategoryAttributes
)

@Serializable
data class KitsuCategoryAttributes(
    val title: String,
    val slug: String,
    val description: String?,
    val nsfw: Boolean?
)

object KitsuEndpoints {
    const val BASE_URL = "https://kitsu.io/api/edge/"
    const val ANIME = "anime"
    const val MANGA = "manga"
    const val EPISODES = "episodes"
    const val CHAPTERS = "chapters"
    const val CHARACTERS = "characters"
    const val STAFF = "staff"
    const val MAPPINGS = "mappings"
    const val GENRES = "genres"
    const val CATEGORIES = "categories"
    
    const val FILTER = "filter"
    const val PAGE_LIMIT = "page[limit]"
    const val PAGE_OFFSET = "page[offset]"
    const val INCLUDE = "include"
    const val FIELDS = "fields"
    const val SORT = "sort"
}

object KitsuStatus {
    const val CURRENT = "current"
    const val FINISHED = "finished"
    const val TBA = "tba"
    const val UNRELEASED = "unreleased"
    const val HIATUS = "hiatus"
    const val CANCELLED = "cancelled"
    
    fun toAiringStatus(status: String): com.kanlist.domain.model.MediaStatusAiring {
        return when (status) {
            CURRENT -> com.kanlist.domain.model.MediaStatusAiring.RELEASING
            FINISHED -> com.kanlist.domain.model.MediaStatusAiring.FINISHED
            TBA, UNRELEASED -> com.kanlist.domain.model.MediaStatusAiring.NOT_YET_RELEASED
            HIATUS -> com.kanlist.domain.model.MediaStatusAiring.HIATUS
            CANCELLED -> com.kanlist.domain.model.MediaStatusAiring.CANCELLED
            else -> com.kanlist.domain.model.MediaStatusAiring.UNKNOWN
        }
    }
}

object KitsuSubtype {
    const val TV = "TV"
    const val MOVIE = "movie"
    const val OVA = "OVA"
    const val ONA = "ONA"
    const val SPECIAL = "special"
    const val MUSIC = "music"
    const val MANGA = "manga"
    const val NOVEL = "novel"
    const val ONE_SHOT = "one_shot"
}