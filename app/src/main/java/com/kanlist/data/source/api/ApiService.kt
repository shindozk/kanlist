package com.kanlist.data.source.api

import com.kanlist.data.source.api.anilist.AniListApiModels
import com.kanlist.data.source.api.anilist.AniListQueries
import com.kanlist.data.source.api.jikan.JikanApiModels
import com.kanlist.data.source.api.kitsu.KitsuApiModels
import com.kanlist.data.source.api.mangadex.MangaDexApiModels
import com.kanlist.data.source.api.mangadex.MangaDexEndpoints
import com.kanlist.data.source.api.mangadex.MangaDexQueryParams
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Response
import retrofit2.http.*

interface AniListApiService {
    @POST("/")
    suspend fun searchMedia(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListSearchResponse>>

    @POST("/")
    suspend fun getMediaDetails(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getTrending(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListTrendingResponse>>

    @POST("/")
    suspend fun getSeasonal(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListSearchResponse>>

    @POST("/")
    suspend fun getEpisodes(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getCharacters(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getStaff(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getRelations(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getRecommendations(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>

    @POST("/")
    suspend fun getExternalLinks(
        @Body request: AniListGraphQLRequest
    ): Response<AniListApiModels.AniListResponse<AniListApiModels.AniListMedia>>
}

data class AniListGraphQLRequest(
    val query: String,
    val variables: Map<String, Any?>
)

interface MangaDexApiService {
    @GET(MangaDexEndpoints.SEARCH_MANGA)
    suspend fun searchManga(
        @Query(MangaDexQueryParams.TITLE) title: String?,
        @Query(MangaDexQueryParams.LIMIT) limit: Int = 20,
        @Query(MangaDexQueryParams.OFFSET) offset: Int = 0,
        @Query(MangaDexQueryParams.INCLUDES) includes: List<String> = listOf("cover_art", "author", "artist", "tags"),
        @Query(MangaDexQueryParams.CONTENT_RATING) contentRating: List<String> = listOf("safe", "suggestive", "erotica"),
        @Query(MangaDexQueryParams.ORDER) order: Map<String, String> = mapOf("followsCount" to "desc")
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexManga>>

    @GET(MangaDexEndpoints.GET_MANGA)
    suspend fun getManga(
        @Path("id") id: String,
        @Query(MangaDexQueryParams.INCLUDES) includes: List<String> = listOf("cover_art", "author", "artist", "tags")
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexManga>>

    @GET(MangaDexEndpoints.GET_MANGA_CHAPTERS)
    suspend fun getChapters(
        @Path("id") id: String,
        @Query(MangaDexQueryParams.TRANSLATED_LANGUAGE) translatedLanguage: List<String> = listOf("en", "pt-br"),
        @Query(MangaDexQueryParams.LIMIT) limit: Int = 100,
        @Query(MangaDexQueryParams.OFFSET) offset: Int = 0,
        @Query(MangaDexQueryParams.ORDER) order: Map<String, String> = mapOf("chapter" to "asc", "volume" to "asc"),
        @Query(MangaDexQueryParams.INCLUDES) includes: List<String> = listOf("scanlation_group", "manga")
    ): Response<MangaDexApiModels.MangaDexChapterListResponse>

    @GET(MangaDexEndpoints.GET_CHAPTER)
    suspend fun getChapter(
        @Path("id") id: String,
        @Query(MangaDexQueryParams.INCLUDES) includes: List<String> = listOf("scanlation_group", "manga")
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexChapter>>

    @GET(MangaDexEndpoints.GET_CHAPTER_PAGES)
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): Response<MangaDexApiModels.MangaDexAtHomeResponse>

    @GET(MangaDexEndpoints.GET_COVER)
    suspend fun getCover(
        @Path("id") id: String
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexCover>>

    @GET(MangaDexEndpoints.GET_AUTHOR)
    suspend fun getAuthor(
        @Path("id") id: String
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexAuthor>>

    @GET(MangaDexEndpoints.GET_GROUP)
    suspend fun getGroup(
        @Path("id") id: String
    ): Response<MangaDexApiModels.MangaDexResponse<MangaDexApiModels.MangaDexScanlationGroup>>
}

interface KitsuApiService {
    @GET(KitsuEndpoints.ANIME)
    suspend fun searchAnime(
        @Query(KitsuEndpoints.FILTER + "[text]") text: String?,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 20,
        @Query(KitsuEndpoints.PAGE_OFFSET) offset: Int = 0,
        @Query(KitsuEndpoints.INCLUDE) include: String = "genres,categories",
        @Query(KitsuEndpoints.SORT) sort: String = "-popularityRank"
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuResource>>

    @GET(KitsuEndpoints.MANGA)
    suspend fun searchManga(
        @Query(KitsuEndpoints.FILTER + "[text]") text: String?,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 20,
        @Query(KitsuEndpoints.PAGE_OFFSET) offset: Int = 0,
        @Query(KitsuEndpoints.INCLUDE) include: String = "genres,categories",
        @Query(KitsuEndpoints.SORT) sort: String = "-popularityRank"
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuResource>>

    @GET(KitsuEndpoints.ANIME + "/{id}")
    suspend fun getAnime(
        @Path("id") id: String,
        @Query(KitsuEndpoints.INCLUDE) include: String = "genres,categories,mappings,episodes,characters,staff,recommendations"
    ): Response<KitsuApiModels.KitsuResponse<KitsuApiModels.KitsuResource>>

    @GET(KitsuEndpoints.MANGA + "/{id}")
    suspend fun getManga(
        @Path("id") id: String,
        @Query(KitsuEndpoints.INCLUDE) include: String = "genres,categories,mappings,chapters,characters,staff,recommendations"
    ): Response<KitsuApiModels.KitsuResponse<KitsuApiModels.KitsuResource>>

    @GET(KitsuEndpoints.EPISODES)
    suspend fun getEpisodes(
        @Query("filter[anime]") animeId: String,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 100,
        @Query(KitsuEndpoints.SORT) sort: String = "number"
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuEpisode>>

    @GET(KitsuEndpoints.CHAPTERS)
    suspend fun getChapters(
        @Query("filter[manga]") mangaId: String,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 500,
        @Query(KitsuEndpoints.SORT) sort: String = "number,volume"
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuChapter>>

    @GET(KitsuEndpoints.MAPPINGS)
    suspend fun getMappings(
        @Query("filter[externalSite]") externalSite: String,
        @Query("filter[externalId]") externalId: String
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuMapping>>

    @GET(KitsuEndpoints.CHARACTERS)
    suspend fun getCharacters(
        @Query("filter[anime]") animeId: String?,
        @Query("filter[manga]") mangaId: String?,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 50
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuResource>>

    @GET(KitsuEndpoints.STAFF)
    suspend fun getStaff(
        @Query("filter[anime]") animeId: String?,
        @Query("filter[manga]") mangaId: String?,
        @Query(KitsuEndpoints.PAGE_LIMIT) limit: Int = 50
    ): Response<KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuResource>>
}

interface JikanApiService {
    @GET(JikanEndpoints.SEARCH + "/" + JikanEndpoints.ANIME)
    suspend fun searchAnime(
        @Query(JikanQueryParams.Q) q: String?,
        @Query(JikanQueryParams.PAGE) page: Int = 1,
        @Query(JikanQueryParams.LIMIT) limit: Int = 20,
        @Query(JikanQueryParams.SFW) sfw: Boolean = true,
        @Query(JikanQueryParams.ORDER_BY) orderBy: String = "popularity",
        @Query(JikanQueryParams.SORT) sort: String = "desc"
    ): Response<JikanApiModels.JikanSearchResponse<JikanApiModels.JikanAnime>>

    @GET(JikanEndpoints.SEARCH + "/" + JikanEndpoints.MANGA)
    suspend fun searchManga(
        @Query(JikanQueryParams.Q) q: String?,
        @Query(JikanQueryParams.PAGE) page: Int = 1,
        @Query(JikanQueryParams.LIMIT) limit: Int = 20,
        @Query(JikanQueryParams.SFW) sfw: Boolean = true,
        @Query(JikanQueryParams.ORDER_BY) orderBy: String = "popularity",
        @Query(JikanQueryParams.SORT) sort: String = "desc"
    ): Response<JikanApiModels.JikanSearchResponse<JikanApiModels.JikanManga>>

    @GET(JikanEndpoints.ANIME + "/{id}")
    suspend fun getAnime(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<JikanApiModels.JikanAnime>>

    @GET(JikanEndpoints.MANGA + "/{id}")
    suspend fun getManga(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<JikanApiModels.JikanManga>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.CHARACTERS)
    suspend fun getAnimeCharacters(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanCharacter>>>

    @GET(JikanEndpoints.MANGA + "/{id}/" + JikanEndpoints.CHARACTERS)
    suspend fun getMangaCharacters(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanCharacter>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.STAFF)
    suspend fun getAnimeStaff(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanStaff>>>

    @GET(JikanEndpoints.MANGA + "/{id}/" + JikanEndpoints.STAFF)
    suspend fun getMangaStaff(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanStaff>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.EPISODES)
    suspend fun getAnimeEpisodes(
        @Path("id") id: Int,
        @Query(JikanQueryParams.PAGE) page: Int = 1
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanEpisode>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.RELATIONS)
    suspend fun getAnimeRelations(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanRelation>>>

    @GET(JikanEndpoints.MANGA + "/{id}/" + JikanEndpoints.RELATIONS)
    suspend fun getMangaRelations(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanRelation>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.RECOMMENDATIONS)
    suspend fun getAnimeRecommendations(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanAnime>>>

    @GET(JikanEndpoints.MANGA + "/{id}/" + JikanEndpoints.RECOMMENDATIONS)
    suspend fun getMangaRecommendations(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanManga>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.EXTERNAL)
    suspend fun getAnimeExternal(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanExternal>>>

    @GET(JikanEndpoints.MANGA + "/{id}/" + JikanEndpoints.EXTERNAL)
    suspend fun getMangaExternal(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanExternal>>>

    @GET(JikanEndpoints.ANIME + "/{id}/" + JikanEndpoints.STREAMING)
    suspend fun getAnimeStreaming(
        @Path("id") id: Int
    ): Response<JikanApiModels.JikanResponse<List<JikanApiModels.JikanStreaming>>>

    @GET(JikanEndpoints.SEASON_NOW)
    suspend fun getCurrentSeason(
        @Query(JikanQueryParams.PAGE) page: Int = 1,
        @Query(JikanQueryParams.LIMIT) limit: Int = 20
    ): Response<JikanApiModels.JikanSearchResponse<JikanApiModels.JikanAnime>>

    @GET(JikanEndpoints.TOP + "/" + JikanEndpoints.ANIME)
    suspend fun getTopAnime(
        @Query(JikanQueryParams.PAGE) page: Int = 1,
        @Query(JikanQueryParams.LIMIT) limit: Int = 20
    ): Response<JikanApiModels.JikanSearchResponse<JikanApiModels.JikanAnime>>

    @GET(JikanEndpoints.TOP + "/" + JikanEndpoints.MANGA)
    suspend fun getTopManga(
        @Query(JikanQueryParams.PAGE) page: Int = 1,
        @Query(JikanQueryParams.LIMIT) limit: Int = 20
    ): Response<JikanApiModels.JikanSearchResponse<JikanApiModels.JikanManga>>
}

interface TranslationApiService {
    @GET("https://api.mymemory.translated.net/get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String // "en|pt"
    ): Response<TranslationResponse>
}

@Serializable
data class TranslationResponse(
    val responseData: TranslationResponseData,
    val responseDetails: String,
    val responseStatus: Int,
    val responderId: String?,
    val matches: List<TranslationMatch>?
)

@Serializable
data class TranslationResponseData(
    val translatedText: String,
    val match: Double
)

@Serializable
data class TranslationMatch(
    val id: String,
    val segment: String,
    val translation: String,
    val source: String,
    val target: String,
    val quality: Int,
    val reference: String?,
    @Json(name = "usage-count") val usageCount: Int,
    val subject: String?,
    val created: String?,
    val updated: String?,
    val match: Double
)