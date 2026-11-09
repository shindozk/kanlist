package com.kanlist.data.repository

import com.kanlist.data.local.Daos
import com.kanlist.data.local.KanlistDatabase
import com.kanlist.data.local.converters.Converters
import com.kanlist.data.mapper.AniListMapper
import com.kanlist.data.mapper.JikanMapper
import com.kanlist.data.mapper.KitsuMapper
import com.kanlist.data.mapper.MangaDexMapper
import com.kanlist.data.source.api.AniListApiModels
import com.kanlist.data.source.api.ApiService
import com.kanlist.data.source.api.JikanApiModels
import com.kanlist.data.source.api.KitsuApiModels
import com.kanlist.data.source.api.MangaDexApiModels
import com.kanlist.data.source.api.anilist.AniListQueries
import com.kanlist.data.source.local.EntityModels.MediaEntity
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.ChapterList
import com.kanlist.domain.model.Episode
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.NextAiringEpisode
import com.kanlist.domain.model.PageInfo
import com.kanlist.domain.repository.MediaRepository
import com.kanlist.data.source.local.CacheDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val database: KanlistDatabase,
    private val anilistApi: ApiService.AniListApiService,
    private val mangadexApi: ApiService.MangaDexApiService,
    private val kitsuApi: ApiService.KitsuApiService,
    private val jikanApi: ApiService.JikanApiService,
    private val cacheDao: CacheDao
) : MediaRepository {

    private val mediaDao = database.mediaDao()
    private val episodeDao = database.episodeDao()
    private val chapterDao = database.chapterDao()

    private val searchCache = ConcurrentHashMap<String, MediaSearchResult>()
    private val detailsCache = ConcurrentHashMap<String, Media>()
    private val cacheExpiryHours = 24L

    override suspend fun searchMedia(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        types: List<MediaType>? = null,
        sources: List<MediaSource>? = null,
        includeAdult: Boolean = false
    ): Result<MediaSearchResult> = withContext(Dispatchers.IO) {
        val enabledSources = sources ?: MediaSource.allEnabled
        val allResults = mutableListOf<Media>()
        val errors = mutableMapOf<MediaSource, String>()
        val cacheKey = "search_${query}_${page}_${perPage}_${types?.joinToString(",")}_${enabledSources.joinToString(",")}_${includeAdult}"

        // Check cache first
        val cached = searchCache[cacheKey]
        if (cached != null) {
            return@withContext Result.success(cached)
        }

        val tasks = mutableListOf<kotlinx.coroutines.Deferred<Result<MediaSearchResult>>>()

        if (MediaSource.ANILIST in enabledSources) {
            tasks.add(kotlinx.coroutines.async {
                searchAniList(query, page, perPage, types, includeAdult)
            })
        }
        if (MediaSource.MANGDEX in enabledSources) {
            tasks.add(kotlinx.coroutines.async {
                searchMangaDex(query, page, perPage, types, includeAdult)
            })
        }
        if (MediaSource.KITSU in enabledSources) {
            tasks.add(kotlinx.coroutines.async {
                searchKitsu(query, page, perPage, types)
            })
        }
        if (MediaSource.JIKAN in enabledSources) {
            tasks.add(kotlinx.coroutines.async {
                searchJikan(query, page, perPage, types, includeAdult)
            })
        }

        val results = tasks.awaitAll()

        for (result in results) {
            result.onSuccess { allResults.addAll(it.media) }
                .onFailure { errors[it.source] = it.exception.message ?: "Unknown error" }
        }

        // Deduplicate by ID
        val uniqueResults = allResults.distinctBy { it.id }
        val pageInfo = PageInfo(
            total = uniqueResults.size,
            perPage = perPage,
            currentPage = page,
            lastPage = (uniqueResults.size + perPage - 1) / perPage,
            hasNextPage = uniqueResults.size > page * perPage
        )

        val finalResult = MediaSearchResult(
            media = uniqueResults.take(perPage),
            pageInfo = pageInfo,
            sources = enabledSources,
            errors = errors
        )

        searchCache[cacheKey] = finalResult
        Result.success(finalResult)
    }

    private suspend fun searchAniList(
        query: String,
        page: Int,
        perPage: Int,
        types: List<MediaType>?,
        includeAdult: Boolean
    ): Result<MediaSearchResult> {
        val variables = mutableMapOf<String, Any>(
            "search" to query,
            "page" to page,
            "perPage" to perPage,
            "isAdult" to includeAdult
        )
        types?.let { variables["type"] = it.first().name } // Simplified
        
        val request = ApiService.AniListGraphQLRequest(
            query = AniListQueries.SEARCH_MEDIA,
            variables = variables
        )

        try {
            val response = anilistApi.searchMedia(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val result = AniListMapper.toSearchResult(response.body()!!.data!!)
                return Result.success(result)
            }
            return Result.failure(Exception("AniList search failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun searchMangaDex(
        query: String,
        page: Int,
        perPage: Int,
        types: List<MediaType>?,
        includeAdult: Boolean
    ): Result<MediaSearchResult> {
        val offset = (page - 1) * perPage
        val contentRating = if (includeAdult) 
            listOf("safe", "suggestive", "erotica", "pornographic") 
        else 
            listOf("safe", "suggestive")

        try {
            val response = mangadexApi.searchManga(
                title = query,
                limit = perPage,
                offset = offset,
                contentRating = contentRating
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val mangaList = response.body()!!.data!!
                val media = mangaList.map { MangaDexMapper.toDomain(it) }
                val pageInfo = PageInfo(
                    total = response.body()?.total ?: media.size,
                    perPage = perPage,
                    currentPage = page,
                    lastPage = ((response.body()?.total ?: media.size) + perPage - 1) / perPage,
                    hasNextPage = (offset + perPage) < (response.body()?.total ?: 0)
                )
                return Result.success(MediaSearchResult(
                    media = media,
                    pageInfo = pageInfo,
                    sources = listOf(MediaSource.MANGDEX),
                    errors = emptyMap()
                ))
            }
            return Result.failure(Exception("MangaDex search failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun searchKitsu(
        query: String,
        page: Int,
        perPage: Int,
        types: List<MediaType>?
    ): Result<MediaSearchResult> {
        val offset = (page - 1) * perPage
        val media = mutableListOf<Media>()

        // Search anime
        if (types == null || types.any { it in MediaType.watchTypes }) {
            try {
                val response = kitsuApi.searchAnime(text = query, limit = perPage, offset = offset)
                if (response.isSuccessful && response.body()?.data != null) {
                    media.addAll(response.body()!!.data.mapNotNull { KitsuMapper.toDomain(it) })
                }
            } catch (e: Exception) {
                // Continue with other sources
            }
        }

        // Search manga
        if (types == null || types.any { it in MediaType.readTypes }) {
            try {
                val response = kitsuApi.searchManga(text = query, limit = perPage, offset = offset)
                if (response.isSuccessful && response.body()?.data != null) {
                    media.addAll(response.body()!!.data.mapNotNull { KitsuMapper.toDomain(it) })
                }
            } catch (e: Exception) {
                // Continue with other sources
            }
        }

        val pageInfo = PageInfo(
            total = media.size,
            perPage = perPage,
            currentPage = page,
            lastPage = 1,
            hasNextPage = false
        )

        return Result.success(MediaSearchResult(
            media = media,
            pageInfo = pageInfo,
            sources = listOf(MediaSource.KITSU),
            errors = emptyMap()
        ))
    }

    private suspend fun searchJikan(
        query: String,
        page: Int,
        perPage: Int,
        types: List<MediaType>?,
        includeAdult: Boolean
    ): Result<MediaSearchResult> {
        val media = mutableListOf<Media>()

        if (types == null || types.any { it in MediaType.watchTypes }) {
            try {
                val response = jikanApi.searchAnime(q = query, page = page, limit = perPage, sfw = !includeAdult)
                if (response.isSuccessful && response.body()?.data != null) {
                    media.addAll(response.body()!!.data.map { JikanMapper.toDomain(it) })
                }
            } catch (e: Exception) {
                // Continue
            }
        }

        if (types == null || types.any { it in MediaType.readTypes }) {
            try {
                val response = jikanApi.searchManga(q = query, page = page, limit = perPage, sfw = !includeAdult)
                if (response.isSuccessful && response.body()?.data != null) {
                    media.addAll(response.body()!!.data.map { JikanMapper.toDomain(it) })
                }
            } catch (e: Exception) {
                // Continue
            }
        }

        val pageInfo = PageInfo(
            total = media.size,
            perPage = perPage,
            currentPage = page,
            lastPage = 1,
            hasNextPage = false
        )

        return Result.success(MediaSearchResult(
            media = media,
            pageInfo = pageInfo,
            sources = listOf(MediaSource.JIKAN),
            errors = emptyMap()
        ))
    }

    override suspend fun getTrending(
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ): Result<MediaSearchResult> = withContext(Dispatchers.IO) {
        // Try AniList first
        try {
            val mediaType = type?.name ?: "ANIME"
            val request = ApiService.AniListGraphQLRequest(
                query = AniListQueries.GET_TRENDING,
                variables = mapOf("page" to page, "perPage" to perPage, "type" to mediaType)
            )
            val response = anilistApi.getTrending(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val result = AniListMapper.toSearchResult(
                    AniListApiModels.AniListSearchResponse(
                        Page = AniListApiModels.AniListPage(media = response.body()!!.data!!.Page.media),
                        pageInfo = AniListApiModels.AniListPageInfo(
                            total = response.body()!!.data!!.Page.media.size,
                            perPage = perPage,
                            currentPage = page,
                            lastPage = 1,
                            hasNextPage = false
                        )
                    )
                )
                return@withContext Result.success(result)
            }
        } catch (e: Exception) {
            // Fallback to Jikan
        }

        // Fallback to Jikan top
        try {
            val response = if (type == null || type in MediaType.watchTypes) {
                jikanApi.getTopAnime(page = page, limit = perPage)
            } else {
                jikanApi.getTopManga(page = page, limit = perPage)
            }
            if (response.isSuccessful && response.body()?.data != null) {
                val media = response.body()!!.data.map { 
                    if (type == null || type in MediaType.watchTypes) JikanMapper.toDomain(it) 
                    else JikanMapper.toDomain(it as JikanApiModels.JikanManga)
                }
                return@withContext Result.success(MediaSearchResult(
                    media = media,
                    pageInfo = PageInfo(total = media.size, perPage = perPage, currentPage = page, lastPage = 1, hasNextPage = false),
                    sources = listOf(MediaSource.JIKAN),
                    errors = emptyMap()
                ))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        Result.failure(Exception("All sources failed"))
    }

    override suspend fun getSeasonal(
        year: Int,
        season: com.kanlist.domain.model.MediaSeason,
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ): Result<MediaSearchResult> = withContext(Dispatchers.IO) {
        try {
            val request = ApiService.AniListGraphQLRequest(
                query = AniListQueries.GET_SEASONAL,
                variables = mapOf(
                    "season" to season.name,
                    "seasonYear" to year,
                    "page" to page,
                    "perPage" to perPage,
                    "type" to (type?.name ?: "ANIME")
                )
            )
            val response = anilistApi.getSeasonal(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val result = AniListMapper.toSearchResult(response.body()!!.data!!)
                return@withContext Result.success(result)
            }
        } catch (e: Exception) {
            // Fallback
        }

        // Fallback to Jikan current season
        try {
            val response = jikanApi.getCurrentSeason(page = page, limit = perPage)
            if (response.isSuccessful && response.body()?.data != null) {
                val media = response.body()!!.data.map { JikanMapper.toDomain(it) }
                return@withContext Result.success(MediaSearchResult(
                    media = media,
                    pageInfo = PageInfo(total = media.size, perPage = perPage, currentPage = page, lastPage = 1, hasNextPage = false),
                    sources = listOf(MediaSource.JIKAN),
                    errors = emptyMap()
                ))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        Result.failure(Exception("All sources failed"))
    }

    override suspend fun getMediaDetails(id: String, source: MediaSource? = null): Result<Media> = withContext(Dispatchers.IO) {
        // Check cache first
        detailsCache[id]?.let { return@withContext Result.success(it) }

        // Try to get from local database
        val cached = mediaDao.getById(id)
        if (cached != null) {
            val media = entityToDomain(cached)
            detailsCache[id] = media
            return@withContext Result.success(media)
        }

        // Determine source from ID prefix
        val src = source ?: when {
            id.startsWith("mal-") -> MediaSource.JIKAN
            id.all { it.isDigit() } -> MediaSource.ANILIST
            id.length == 36 && id.count { it == '-' } == 4 -> MediaSource.MANGDEX
            else -> MediaSource.ANILIST
        }

        return@withContext when (src) {
            MediaSource.ANILIST -> getFromAniList(id)
            MediaSource.MANGDEX -> getFromMangaDex(id)
            MediaSource.KITSU -> getFromKitsu(id)
            MediaSource.JIKAN -> getFromJikan(id.replace("mal-", "").toIntOrNull() ?: 0)
        }
    }

    private suspend fun getFromAniList(id: String): Result<Media> {
        try {
            val request = ApiService.AniListGraphQLRequest(
                query = AniListQueries.GET_MEDIA_DETAILS,
                variables = mapOf("id" to id.toIntOrNull() ?: 0)
            )
            val response = anilistApi.getMediaDetails(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val media = AniListMapper.toDomain(response.body()!!.data!!)
                cacheMedia(media)
                detailsCache[id] = media
                return Result.success(media)
            }
            return Result.failure(Exception("AniList details failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun getFromMangaDex(id: String): Result<Media> {
        try {
            val response = mangadexApi.getManga(id)
            if (response.isSuccessful && response.body()?.data != null) {
                val media = MangaDexMapper.toDomain(response.body()!!.data!!)
                cacheMedia(media)
                detailsCache[id] = media
                return Result.success(media)
            }
            return Result.failure(Exception("MangaDex details failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun getFromKitsu(id: String): Result<Media> {
        try {
            val response = kitsuApi.getAnime(id) // or getManga
            if (response.isSuccessful && response.body()?.data != null) {
                val media = KitsuMapper.toDomain(response.body()!!.data!!)
                if (media != null) {
                    cacheMedia(media)
                    detailsCache[id] = media
                    return Result.success(media)
                }
            }
            return Result.failure(Exception("Kitsu details failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun getFromJikan(id: Int): Result<Media> {
        try {
            val response = jikanApi.getAnime(id)
            if (response.isSuccessful && response.body()?.data != null) {
                val media = JikanMapper.toDomain(response.body()!!.data!!)
                cacheMedia(media)
                detailsCache["mal-$id"] = media
                return Result.success(media)
            }
            return Result.failure(Exception("Jikan details failed: ${response.message()}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getMediaById(id: String): Result<Media?> = withContext(Dispatchers.IO) {
        val cached = mediaDao.getById(id)
        Result.success(cached?.let { entityToDomain(it) })
    }

    private fun cacheMedia(media: Media) {
        val entity = domainToEntity(media)
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            mediaDao.insert(entity)
        }
    }

    private fun entityToDomain(entity: MediaEntity): Media {
        // Convert entity back to domain model
        // This is a simplified version - in practice you'd want a proper mapper
        return Media(
            id = entity.id,
            idAniList = entity.idAniList,
            idMangaDex = entity.idMangaDex,
            idKitsu = entity.idKitsu,
            idMyAnimeList = entity.idMyAnimeList,
            title = entity.title,
            type = entity.type,
            format = entity.format,
            status = entity.status,
            season = entity.season,
            seasonYear = entity.seasonYear,
            startDate = entity.startDate,
            endDate = entity.endDate,
            episodes = entity.episodes,
            chapters = entity.chapters,
            volumes = entity.volumes,
            duration = entity.duration,
            averageScore = entity.averageScore,
            popularity = entity.popularity,
            description = entity.description,
            coverImage = MediaCoverImage(
                large = entity.coverImage.large,
                medium = entity.coverImage.medium,
                color = entity.coverImage.color,
                extraLarge = entity.coverImage.extraLarge
            ),
            bannerImage = entity.bannerImage,
            genres = entity.genres,
            tags = entity.tags,
            studios = entity.studios.map { com.kanlist.domain.model.MediaStudio(
                id = it.id, name = it.name, isAnimationStudio = it.isAnimationStudio, isMain = it.isMain
            ) },
            staff = entity.staff.map { com.kanlist.domain.model.MediaStaff(
                id = it.id, name = com.kanlist.domain.model.MediaName(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), role = it.role, language = it.language
            ) },
            characters = entity.characters.map { com.kanlist.domain.model.MediaCharacter(
                id = it.id, name = com.kanlist.domain.model.MediaName(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), image = it.image, role = it.role,
                voiceActors = it.voiceActors.map { com.kanlist.domain.model.MediaVoiceActor(
                    id = it.id, name = com.kanlist.domain.model.MediaName(
                        full = it.name.full, native = it.name.native, alternative = it.name.alternative
                    ), image = it.image, language = it.language
                ) }
            ) },
            relations = entity.relations.map { com.kanlist.domain.model.MediaRelation(
                id = it.id, relationType = it.relationType, media = entityToDomain(it.media)
            ) },
            recommendations = entity.recommendations.map { com.kanlist.domain.model.MediaRecommendation(
                id = it.id, rating = it.rating, media = entityToDomain(it.media)
            ) },
            externalLinks = entity.externalLinks.map { com.kanlist.domain.model.MediaExternalLink(
                id = it.id, site = it.site, url = it.url, type = it.type,
                language = it.language, color = it.color, icon = it.icon, notes = it.notes
            ) },
            nextAiringEpisode = entity.nextAiringEpisode?.let { NextAiringEpisode(
                episode = it.episode, airingAt = it.airingAt, timeUntilAiring = it.timeUntilAiring
            ) },
            source = entity.source,
            isAdult = entity.isAdult,
            countryOfOrigin = entity.countryOfOrigin,
            updatedAt = entity.updatedAt
        )
    }

    private fun domainToEntity(media: Media): MediaEntity {
        return MediaEntity(
            id = media.id,
            idAniList = media.idAniList,
            idMangaDex = media.idMangaDex,
            idKitsu = media.idKitsu,
            idMyAnimeList = media.idMyAnimeList,
            title = media.title,
            type = media.type,
            format = media.format,
            status = media.status,
            season = media.season,
            seasonYear = media.seasonYear,
            startDate = media.startDate,
            endDate = media.endDate,
            episodes = media.episodes,
            chapters = media.chapters,
            volumes = media.volumes,
            duration = media.duration,
            averageScore = media.averageScore,
            popularity = media.popularity,
            description = media.description,
            coverImage = com.kanlist.data.local.EntityModels.MediaCoverImageEntity(
                large = media.coverImage.large,
                medium = media.coverImage.medium,
                color = media.coverImage.color,
                extraLarge = media.coverImage.extraLarge
            ),
            bannerImage = media.bannerImage,
            genres = media.genres,
            tags = media.tags,
            studios = media.studios.map { com.kanlist.data.local.EntityModels.MediaStudioEntity(
                id = it.id, name = it.name, isAnimationStudio = it.isAnimationStudio, isMain = it.isMain
            ) },
            staff = media.staff.map { com.kanlist.data.local.EntityModels.MediaStaffEntity(
                id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), role = it.role, language = it.language
            ) },
            characters = media.characters.map { com.kanlist.data.local.EntityModels.MediaCharacterEntity(
                id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), image = it.image, role = it.role,
                voiceActors = it.voiceActors.map { com.kanlist.data.local.EntityModels.MediaVoiceActorEntity(
                    id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                        full = it.name.full, native = it.name.native, alternative = it.name.alternative
                    ), image = it.image, language = it.language
                ) }
            ) },
            relations = media.relations.map { com.kanlist.data.local.EntityModels.MediaRelationEntity(
                id = it.id, relationType = it.relationType, media = domainToEntity(it.media)
            ) },
            recommendations = media.recommendations.map { com.kanlist.data.local.EntityModels.MediaRecommendationEntity(
                id = it.id, rating = it.rating, media = domainToEntity(it.media)
            ) },
            externalLinks = media.externalLinks.map { com.kanlist.data.local.EntityModels.MediaExternalLinkEntity(
                id = it.id, site = it.site, url = it.url, type = it.type,
                language = it.language, color = it.color, icon = it.icon, notes = it.notes
            ) },
            nextAiringEpisode = media.nextAiringEpisode?.let { com.kanlist.data.local.EntityModels.NextAiringEpisodeEntity(
                episode = it.episode, airingAt = it.airingAt, timeUntilAiring = it.timeUntilAiring
            ) },
            source = media.source,
            isAdult = media.isAdult,
            countryOfOrigin = media.countryOfOrigin,
            updatedAt = media.updatedAt
        )
    }

    override suspend fun getEpisodes(mediaId: String, source: MediaSource? = null): Result<List<Episode>> = withContext(Dispatchers.IO) {
        // Try local cache first
        val cached = episodeDao.getByMediaId(mediaId)
        if (cached.isNotEmpty()) {
            return@withContext Result.success(cached.map { toEpisodeDomain(it) })
        }

        // Fetch from API based on source
        val src = source ?: MediaSource.ANILIST
        return@withContext when (src) {
            MediaSource.ANILIST -> getEpisodesFromAniList(mediaId)
            MediaSource.KITSU -> getEpisodesFromKitsu(mediaId)
            MediaSource.JIKAN -> getEpisodesFromJikan(mediaId.replace("mal-", "").toIntOrNull() ?: 0)
            else -> Result.success(emptyList())
        }
    }

    private suspend fun getEpisodesFromAniList(mediaId: String): Result<List<Episode>> {
        try {
            val request = ApiService.AniListGraphQLRequest(
                query = AniListQueries.GET_EPISODES,
                variables = mapOf("id" to mediaId.toIntOrNull() ?: 0)
            )
            val response = anilistApi.getEpisodes(request)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                val episodes = data.airingSchedule?.nodes?.map { airing ->
                    Episode(
                        id = "anilist-${airing.episode}",
                        mediaId = mediaId,
                        number = airing.episode,
                        title = null,
                        thumbnail = null,
                        airDate = LocalDateTime.ofEpochSecond(airing.airingAt / 1000, 0, ZoneOffset.UTC).toLocalDate(),
                        duration = null,
                        isFiller = false,
                        isRecap = false,
                        source = MediaSource.ANILIST
                    )
                } ?: emptyList()
                
                // Cache episodes
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    episodeDao.insertAll(episodes.map { toEpisodeEntity(it) })
                }
                
                return Result.success(episodes)
            }
            return Result.failure(Exception("Failed to get episodes"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun getEpisodesFromKitsu(mediaId: String): Result<List<Episode>> {
        try {
            val response = kitsuApi.getEpisodes(animeId = mediaId)
            if (response.isSuccessful && response.body()?.data != null) {
                val episodes = KitsuMapper.toEpisodes(response.body()!!.also { it.data = it.data })
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    episodeDao.insertAll(episodes.map { toEpisodeEntity(it) })
                }
                return Result.success(episodes)
            }
            return Result.failure(Exception("Failed to get episodes"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private suspend fun getEpisodesFromJikan(id: Int): Result<List<Episode>> {
        try {
            val response = jikanApi.getAnimeEpisodes(id)
            if (response.isSuccessful && response.body()?.data != null) {
                val episodes = JikanMapper.toEpisodes(response.body()!!)
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    episodeDao.insertAll(episodes.map { toEpisodeEntity(it) })
                }
                return Result.success(episodes)
            }
            return Result.failure(Exception("Failed to get episodes"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun toEpisodeEntity(ep: Episode): EpisodeEntity {
        return EpisodeEntity(
            id = ep.id,
            mediaId = ep.mediaId,
            number = ep.number,
            title = ep.title,
            thumbnail = ep.thumbnail,
            airDate = ep.airDate,
            duration = ep.duration,
            isFiller = ep.isFiller,
            isRecap = ep.isRecap,
            source = ep.source
        )
    }

    private fun toEpisodeDomain(entity: EpisodeEntity): Episode {
        return Episode(
            id = entity.id,
            mediaId = entity.mediaId,
            number = entity.number,
            title = entity.title,
            thumbnail = entity.thumbnail,
            airDate = entity.airDate,
            duration = entity.duration,
            isFiller = entity.isFiller,
            isRecap = entity.isRecap,
            source = entity.source
        )
    }

    override suspend fun getChapters(
        mediaId: String,
        languages: List<ChapterLanguage>? = null,
        source: MediaSource? = null
    ): Result<ChapterList> = withContext(Dispatchers.IO) {
        val langs = languages ?: listOf(ChapterLanguage.PT_BR, ChapterLanguage.EN)
        val src = source ?: MediaSource.MANGDEX

        return@withContext when (src) {
            MediaSource.MANGDEX -> getChaptersFromMangaDex(mediaId, langs)
            MediaSource.KITSU -> getChaptersFromKitsu(mediaId, langs)
            else -> Result.success(ChapterList(mediaId, emptyList(), langs, LocalDateTime.now(), src))
        }
    }

    private suspend fun getChaptersFromMangaDex(mediaId: String, languages: List<ChapterLanguage>): Result<ChapterList> {
        try {
            val allChapters = mutableListOf<ChapterEntity>()
            var offset = 0
            val limit = 100
            var hasMore = true

            while (hasMore) {
                val response = mangadexApi.getChapters(
                    id = mediaId,
                    translatedLanguage = languages.map { it.code },
                    limit = limit,
                    offset = offset
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    val chapters = response.body()!!.data
                    val mapped = chapters.map { MangaDexMapper.toChapter(it, mediaId) }
                    allChapters.addAll(mapped.map { toChapterEntity(it) })
                    
                    hasMore = chapters.size == limit
                    offset += limit
                } else {
                    hasMore = false
                }
            }

            val chapterList = MangaDexMapper.toChapterList(
                MangaDexApiModels.MangaDexChapterListResponse(
                    result = "ok", response = "collection",
                    data = allChapters.map { toMangaDexChapter(it) },
                    limit = allChapters.size, offset = 0, total = allChapters.size
                ),
                mediaId, languages
            )

            // Cache chapters
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                chapterDao.insertAll(allChapters)
            }

            Result.success(chapterList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getChaptersFromKitsu(mediaId: String, languages: List<ChapterLanguage>): Result<ChapterList> {
        try {
            val response = kitsuApi.getChapters(mangaId = mediaId)
            if (response.isSuccessful && response.body()?.data != null) {
                val chapters = KitsuMapper.toChapters(response.body()!!.also { it.data = it.data })
                    .filter { it.language in languages.map { it.code } }
                val chapterList = ChapterList(
                    mediaId = mediaId,
                    chapters = chapters,
                    languages = languages,
                    lastUpdated = LocalDateTime.now(),
                    source = MediaSource.KITSU
                )
                Result.success(chapterList)
            } else {
                Result.failure(Exception("Failed to get chapters"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun toChapterEntity(ch: Chapter): ChapterEntity {
        return ChapterEntity(
            id = ch.id,
            mediaId = ch.mediaId,
            chapter = ch.chapter,
            chapterNumber = ch.chapterNumber,
            volume = ch.volume,
            volumeNumber = ch.volumeNumber,
            title = ch.title,
            language = ch.language,
            groupName = ch.groupName,
            groupId = ch.groupId,
            pages = ch.pages,
            publishAt = ch.publishAt,
            createdAt = ch.createdAt,
            updatedAt = ch.updatedAt,
            source = ch.source,
            isExternalUrl = ch.isExternalUrl,
            externalUrl = ch.externalUrl
        )
    }

    private fun toMangaDexChapter(entity: ChapterEntity): MangaDexApiModels.MangaDexChapter {
        // Simplified conversion for caching
        return MangaDexApiModels.MangaDexChapter(
            id = entity.id,
            type = "chapter",
            attributes = MangaDexApiModels.MangaDexChapterAttributes(
                volume = entity.volume,
                chapter = entity.chapter,
                title = entity.title,
                translatedLanguage = entity.language,
                externalUrl = entity.externalUrl,
                publishAt = entity.publishAt?.toString() ?: "",
                readableAt = null,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString(),
                pages = entity.pages,
                version = 1
            ),
            relationships = null
        )
    }

    override suspend fun getRelations(mediaId: String): Result<List<com.kanlist.domain.model.MediaRelation>> {
        // Implementation would fetch from APIs
        return Result.success(emptyList())
    }

    override suspend fun getRecommendations(mediaId: String): Result<List<com.kanlist.domain.model.MediaRecommendation>> {
        return Result.success(emptyList())
    }

    override suspend fun getCharacters(mediaId: String): Result<List<com.kanlist.domain.model.MediaCharacter>> {
        return Result.success(emptyList())
    }

    override suspend fun getStaff(mediaId: String): Result<List<com.kanlist.domain.model.MediaStaff>> {
        return Result.success(emptyList())
    }

    override suspend fun getExternalLinks(mediaId: String): Result<List<com.kanlist.domain.model.MediaExternalLink>> {
        return Result.success(emptyList())
    }

    override suspend fun clearCache() {
        searchCache.clear()
        detailsCache.clear()
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            mediaDao.deleteOldCache(System.currentTimeMillis() - cacheExpiryHours * 60 * 60 * 1000)
        }
    }

    override suspend fun getCachedMedia(id: String): Result<Media?> {
        val entity = mediaDao.getById(id)
        Result.success(entity?.let { entityToDomain(it) })
    }

    override fun observeTrending(type: MediaType?): Flow<Result<MediaSearchResult>> {
        // Return cached or trigger fetch
        return kotlinx.coroutines.flow.flow {
            emit(Result.success(MediaSearchResult(emptyList(), PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
        }
    }

    override fun observeSeasonal(year: Int, season: com.kanlist.domain.model.MediaSeason, type: MediaType?): Flow<Result<MediaSearchResult>> {
        return kotlinx.coroutines.flow.flow {
            emit(Result.success(MediaSearchResult(emptyList(), PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
        }
    }
}