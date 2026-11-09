package com.kanlist.data.source.api.jikan

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

@Serializable
data class JikanResponse<T>(
    val data: T,
    val pagination: JikanPagination?,
    val meta: Any?
)

@Serializable
data class JikanListResponse<T>(
    val data: List<T>,
    val pagination: JikanPagination?
)

@Serializable
data class JikanPagination(
    val last_visible_page: Int?,
    val has_next_page: Boolean?,
    val current_page: Int?,
    val items: JikanPaginationItems?
)

@Serializable
data class JikanPaginationItems(
    val count: Int?,
    val total: Int?,
    val per_page: Int?
)

@Serializable
data class JikanAnime(
    val mal_id: Int,
    val url: String?,
    val images: JikanImages?,
    val trailer: JikanTrailer?,
    val approved: Boolean?,
    val titles: List<JikanTitle>?,
    val title: String?,
    val title_english: String?,
    val title_japanese: String?,
    val title_synonyms: List<String>?,
    val type: String?,
    val source: String?,
    val episodes: Int?,
    val status: String?,
    val airing: Boolean?,
    val aired: JikanAired?,
    val duration: String?,
    val rating: String?,
    val score: Double?,
    val scored_by: Int?,
    val rank: Int?,
    val popularity: Int?,
    val members: Int?,
    val favorites: Int?,
    val synopsis: String?,
    val background: String?,
    val season: String?,
    val year: Int?,
    val broadcast: JikanBroadcast?,
    val producers: List<JikanProducer>?,
    val licensors: List<JikanProducer>?,
    val studios: List<JikanProducer>?,
    val genres: List<JikanGenre>?,
    val explicit_genres: List<JikanGenre>?,
    val themes: List<JikanGenre>?,
    val demographics: List<JikanGenre>?,
    val relations: List<JikanRelation>?,
    val theme: JikanTheme?,
    val external: List<JikanExternal>?,
    val streaming: List<JikanStreaming>?
)

@Serializable
data class JikanManga(
    val mal_id: Int,
    val url: String?,
    val images: JikanImages?,
    val approved: Boolean?,
    val titles: List<JikanTitle>?,
    val title: String?,
    val title_english: String?,
    val title_japanese: String?,
    val title_synonyms: List<String>?,
    val type: String?,
    val volumes: Int?,
    val chapters: Int?,
    val status: String?,
    val publishing: Boolean?,
    val published: JikanPublished?,
    val score: Double?,
    val scored_by: Int?,
    val rank: Int?,
    val popularity: Int?,
    val members: Int?,
    val favorites: Int?,
    val synopsis: String?,
    val background: String?,
    val authors: List<JikanProducer>?,
    val serializations: List<JikanProducer>?,
    val genres: List<JikanGenre>?,
    val explicit_genres: List<JikanGenre>?,
    val themes: List<JikanGenre>?,
    val demographics: List<JikanGenre>?,
    val relations: List<JikanRelation>?,
    val external: List<JikanExternal>?
)

@Serializable
data class JikanImages(
    val jpg: JikanImageUrls?,
    val webp: JikanImageUrls?
)

@Serializable
data class JikanImageUrls(
    val image_url: String?,
    val small_image_url: String?,
    val large_image_url: String?
)

@Serializable
data class JikanTrailer(
    val youtube_id: String?,
    val url: String?,
    val embed_url: String?,
    val images: JikanTrailerImages?
)

@Serializable
data class JikanTrailerImages(
    val image_url: String?,
    val small_image_url: String?,
    val medium_image_url: String?,
    val large_image_url: String?,
    val maximum_image_url: String?
)

@Serializable
data class JikanTitle(
    val type: String?,
    val title: String?
)

@Serializable
data class JikanAired(
    val from: String?,
    val to: String?,
    val prop: JikanAiredProp?
)

@Serializable
data class JikanAiredProp(
    val from: JikanDateProp?,
    val to: JikanDateProp?
)

@Serializable
data class JikanDateProp(
    val day: Int?,
    val month: Int?,
    val year: Int?
)

@Serializable
data class JikanPublished(
    val from: String?,
    val to: String?,
    val prop: JikanPublishedProp?
)

@Serializable
data class JikanPublishedProp(
    val from: JikanDateProp?,
    val to: JikanDateProp?
)

@Serializable
data class JikanBroadcast(
    val day: String?,
    val time: String?,
    val timezone: String?,
    val string: String?
)

@Serializable
data class JikanProducer(
    val mal_id: Int,
    val type: String,
    val name: String,
    val url: String?
)

@Serializable
data class JikanGenre(
    val mal_id: Int,
    val type: String,
    val name: String,
    val url: String?
)

@Serializable
data class JikanRelation(
    val relation: String,
    val entry: List<JikanRelationEntry>
)

@Serializable
data class JikanRelationEntry(
    val mal_id: Int,
    val type: String,
    val name: String,
    val url: String?
)

@Serializable
data class JikanTheme(
    val openings: List<String>?,
    val endings: List<String>?
)

@Serializable
data class JikanExternal(
    val name: String,
    val url: String
)

@Serializable
data class JikanStreaming(
    val name: String,
    val url: String
)

@Serializable
data class JikanCharacter(
    val mal_id: Int,
    val url: String?,
    val images: JikanImages?,
    val name: String,
    val name_kanji: String?,
    val nicknames: List<String>?,
    val favorites: Int?,
    val about: String?,
    val role: String?,
    val voice_actors: List<JikanVoiceActor>?
)

@Serializable
data class JikanVoiceActor(
    val mal_id: Int,
    val url: String?,
    val images: JikanImages?,
    val name: String,
    val language: String?
)

@Serializable
data class JikanStaff(
    val mal_id: Int,
    val url: String?,
    val images: JikanImages?,
    val name: String,
    val given_name: String?,
    val family_name: String?,
    val favorites: Int?,
    val about: String?,
    val positions: List<String>?
)

@Serializable
data class JikanEpisode(
    val mal_id: Int,
    val url: String?,
    val title: String?,
    val title_japanese: String?,
    val title_romanji: String?,
    val duration: Int?,
    val aired: String?,
    val filler: Boolean?,
    val recap: Boolean?,
    val forum_url: String?
)

@Serializable
data class JikanSearchResponse<T>(
    val data: List<T>,
    val pagination: JikanPagination?
)

object JikanEndpoints {
    const val BASE_URL = "https://api.jikan.moe/v4/"
    const val ANIME = "anime"
    const val MANGA = "manga"
    const val CHARACTERS = "characters"
    val STAFF = "staff"
    val EPISODES = "episodes"
    val SEARCH = "search"
    val RECOMMENDATIONS = "recommendations"
    val RELATIONS = "relations"
    val EXTERNAL = "external"
    val STREAMING = "streaming"
    val PICTURES = "pictures"
    val VIDEOS = "videos"
    val STATS = "statistics"
    val MORE_INFO = "moreinfo"
    val REVIEWS = "reviews"
    val USER_UPDATES = "userupdates"
    val FORUM = "forum"
    val NEWS = "news"
    val ARTICLES = "articles"
    val TOPICS = "topics"
    val PRODUCERS = "producers"
    val MAGAZINES = "magazines"
    val GENRES = "genres"
    val THEMES = "themes"
    val DEMOGRAPHICS = "demographics"
    val SCHEDULES = "schedules"
    val TOP = "top"
    val SEASON = "seasons"
    val SEASON_NOW = "seasons/now"
    val SEASON_UPCOMING = "seasons/upcoming"
}

object JikanQueryParams {
    const val PAGE = "page"
    const val LIMIT = "limit"
    const val Q = "q"
    const val TYPE = "type"
    const val SCORE = "score"
    const val MIN_SCORE = "min_score"
    const val MAX_SCORE = "max_score"
    const val STATUS = "status"
    const val RATING = "rating"
    const val SFW = "sfw"
    const val GENRE = "genre"
    const val GENRE_EXCLUDE = "genre_exclude"
    const val ORDER_BY = "order_by"
    const val SORT = "sort"
    const val LETTER = "letter"
    
    // Order by
    const val ORDER_BY_TITLE = "title"
    const val ORDER_BY_SCORE = "score"
    const val ORDER_BY_POPULARITY = "popularity"
    const val ORDER_BY_MEMBERS = "members"
    const val ORDER_BY_FAVORITES = "favorites"
    const val ORDER_BY_RANK = "rank"
    const val ORDER_BY_START_DATE = "start_date"
    const val ORDER_BY_END_DATE = "end_date"
    const val ORDER_BY_EPISODES = "episodes"
    const val ORDER_BY_TYPE = "type"
    
    // Sort
    const val SORT_ASC = "asc"
    const val SORT_DESC = "desc"
}