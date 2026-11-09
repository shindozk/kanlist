package com.kanlist.data.source.api.mangadex

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

@Serializable
data class MangaDexResponse<T>(
    val result: String,
    val response: String?,
    val data: T?,
    val errors: List<MangaDexError>?,
    val limit: Int?,
    val offset: Int?,
    val total: Int?
)

@Serializable
data class MangaDexError(
    val id: String,
    val status: Int,
    val title: String,
    val detail: String
)

@Serializable
data class MangaDexManga(
    val id: String,
    val type: String = "manga",
    val attributes: MangaDexMangaAttributes,
    val relationships: List<MangaDexRelationship>?
)

@Serializable
data class MangaDexMangaAttributes(
    val title: Map<String, String>,
    val altTitles: List<Map<String, String>>,
    val description: Map<String, String>,
    val status: String, // ongoing, completed, hiatus, cancelled
    val year: Int?,
    val originalLanguage: String,
    val lastVolume: String?,
    val lastChapter: String?,
    val publicationDemographic: String?,
    val tags: List<MangaDexTag>,
    val state: String, // published, draft
    val chapterNumbersResetOnNewVolume: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val version: Int,
    val availableTranslatedLanguages: List<String>,
    @Json(name = "latestUploadedChapter") val latestUploadedChapter: String?,
    @Json(name = "contentRating") val contentRating: String, // safe, suggestive, erotica, pornographic
    @Json(name = "publicationDemographic") val demographic: String?
)

@Serializable
data class MangaDexTag(
    val id: String,
    val type: String = "tag",
    val attributes: MangaDexTagAttributes
)

@Serializable
data class MangaDexTagAttributes(
    val name: Map<String, String>,
    val description: Map<String, String>?,
    val group: String, // content, format, theme, genre
    val version: Int
)

@Serializable
data class MangaDexRelationship(
    val id: String,
    val type: String,
    val attributes: MangaDexRelationshipAttributes? = null
)

@Serializable
data class MangaDexRelationshipAttributes(
    val name: String?,
    val fileName: String?
)

@Serializable
data class MangaDexCover(
    val id: String,
    val type: String = "cover_art",
    val attributes: MangaDexCoverAttributes
)

@Serializable
data class MangaDexCoverAttributes(
    val description: String?,
    val volume: String?,
    val fileName: String,
    val locale: String?,
    val createdAt: String,
    val updatedAt: String,
    val version: Int
)

@Serializable
data class MangaDexChapter(
    val id: String,
    val type: String = "chapter",
    val attributes: MangaDexChapterAttributes,
    val relationships: List<MangaDexRelationship>?
)

@Serializable
data class MangaDexChapterAttributes(
    val volume: String?,
    val chapter: String,
    val title: String?,
    val translatedLanguage: String,
    @Json(name = "externalUrl") val externalUrl: String?,
    val publishAt: String,
    val readableAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val pages: Int?,
    val version: Int,
    @Json(name = "subGroup") val subGroup: String? = null,
    @Json(name = "scanlationGroup") val scanlationGroup: String? = null
)

@Serializable
data class MangaDexChapterListResponse(
    val result: String,
    val response: String,
    val data: List<MangaDexChapter>,
    val limit: Int,
    val offset: Int,
    val total: Int
)

@Serializable
data class MangaDexAtHomeResponse(
    val result: String,
    val baseUrl: String,
    val chapter: MangaDexAtHomeChapter
)

@Serializable
data class MangaDexAtHomeChapter(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>
)

@Serializable
data class MangaDexAuthor(
    val id: String,
    val type: String = "author",
    val attributes: MangaDexAuthorAttributes
)

@Serializable
data class MangaDexAuthorAttributes(
    val name: String,
    val imageUrl: String?,
    val version: Int
)

@Serializable
data class MangaDexScanlationGroup(
    val id: String,
    val type: String = "scanlation_group",
    val attributes: MangaDexScanlationGroupAttributes
)

@Serializable
data class MangaDexScanlationGroupAttributes(
    val name: String,
    val altNames: List<String>?,
    val website: String?,
    val ircChannel: String?,
    val discord: String?,
    val contactEmail: String?,
    val description: String?,
    val twitter: String?,
    val mangaUpdates: String?,
    val version: Int
)

object MangaDexEndpoints {
    const val BASE_URL = "https://api.mangadex.org/"
    const val MANGA = "manga"
    const val CHAPTER = "chapter"
    const val COVER = "cover"
    const val AUTHOR = "author"
    const val SCANLATION_GROUP = "group"
    const val AT_HOME_SERVER = "at-home/server"
    
    const val SEARCH_MANGA = "manga"
    const val GET_MANGA = "manga/{id}"
    const val GET_MANGA_CHAPTERS = "manga/{id}/chapters"
    const val GET_CHAPTER = "chapter/{id}"
    const val GET_CHAPTER_PAGES = "at-home/server/{chapterId}"
    const val GET_COVER = "cover/{id}"
    const val GET_AUTHOR = "author/{id}"
    const val GET_GROUP = "group/{id}"
}

object MangaDexQueryParams {
    const val LIMIT = "limit"
    const val OFFSET = "offset"
    const val TITLE = "title"
    const val INCLUDES = "includes[]"
    const val TRANSLATED_LANGUAGE = "translatedLanguage[]"
    const val CONTENT_RATING = "contentRating[]"
    const val STATUS = "status[]"
    const val TAGS = "tags[]"
    const val TAG_MODE = "tagsMode" // AND, OR
    const val ORDER = "order"
    const val CREATED_AT = "createdAt"
    const val UPDATED_AT = "updatedAt"
    const val FOLLOWS_COUNT = "followsCount"
    const val RELEVANCE = "relevance"
    const val RATING = "rating"
    const val LATEST_UPLOADED_CHAPTER = "latestUploadedChapter"
    
    // Includes
    const val INCLUDE_COVER_ART = "cover_art"
    const val INCLUDE_AUTHOR = "author"
    const val INCLUDE_ARTIST = "artist"
    const val INCLUDE_TAGS = "tags"
    const val INCLUDE_CHAPTERS = "chapter"
    
    // Order
    const val ORDER_FOLLOWS_DESC = "followsCount:desc"
    const val ORDER_RATING_DESC = "rating:desc"
    const val ORDER_LATEST_CHAPTER_DESC = "latestUploadedChapter:desc"
    const val ORDER_CREATED_AT_DESC = "createdAt:desc"
    const val ORDER_UPDATED_AT_DESC = "updatedAt:desc"
    const val ORDER_RELEVANCE_DESC = "relevance:desc"
    const val ORDER_CHAPTER_ASC = "chapter:asc"
    const val ORDER_CHAPTER_DESC = "chapter:desc"
    const val ORDER_VOLUME_ASC = "volume:asc"
    const val ORDER_VOLUME_DESC = "volume:desc"
}

// Status mapping
object MangaDexStatus {
    const val ONGOING = "ongoing"
    const val COMPLETED = "completed"
    const val HIATUS = "hiatus"
    const val CANCELLED = "cancelled"
    
    fun toAiringStatus(status: String): com.kanlist.domain.model.MediaStatusAiring {
        return when (status) {
            ONGOING -> com.kanlist.domain.model.MediaStatusAiring.RELEASING
            COMPLETED -> com.kanlist.domain.model.MediaStatusAiring.FINISHED
            HIATUS -> com.kanlist.domain.model.MediaStatusAiring.HIATUS
            CANCELLED -> com.kanlist.domain.model.MediaStatusAiring.CANCELLED
            else -> com.kanlist.domain.model.MediaStatusAiring.UNKNOWN
        }
    }
}

// Content Rating mapping
object MangaDexContentRating {
    const val SAFE = "safe"
    const val SUGGESTIVE = "suggestive"
    const val EROTICA = "erotica"
    const val PORNOGRAPHIC = "pornographic"
    
    fun isAdult(rating: String): Boolean {
        return rating == EROTICA || rating == PORNOGRAPHIC
    }
}