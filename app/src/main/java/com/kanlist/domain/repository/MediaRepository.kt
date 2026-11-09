package com.kanlist.domain.repository

import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.PageInfo
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    // Search
    suspend fun searchMedia(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        types: List<MediaType>? = null,
        sources: List<MediaSource>? = null,
        includeAdult: Boolean = false
    ): Result<MediaSearchResult>

    // Trending / Popular
    suspend fun getTrending(
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ): Result<MediaSearchResult>

    // Seasonal
    suspend fun getSeasonal(
        year: Int,
        season: com.kanlist.domain.model.MediaSeason,
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ): Result<MediaSearchResult>

    // Media Details
    suspend fun getMediaDetails(
        id: String,
        source: MediaSource? = null
    ): Result<Media>

    suspend fun getMediaById(id: String): Result<Media?>

    // Episodes/Chapters
    suspend fun getEpisodes(
        mediaId: String,
        source: MediaSource? = null
    ): Result<List<com.kanlist.domain.model.Episode>>

    suspend fun getChapters(
        mediaId: String,
        languages: List<com.kanlist.domain.model.ChapterLanguage>? = null,
        source: MediaSource? = null
    ): Result<com.kanlist.domain.model.ChapterList>

    // Relations & Recommendations
    suspend fun getRelations(mediaId: String): Result<List<com.kanlist.domain.model.MediaRelation>>

    suspend fun getRecommendations(mediaId: String): Result<List<com.kanlist.domain.model.MediaRecommendation>>

    suspend fun getCharacters(mediaId: String): Result<List<com.kanlist.domain.model.MediaCharacter>>

    suspend fun getStaff(mediaId: String): Result<List<com.kanlist.domain.model.MediaStaff>>

    // External links
    suspend fun getExternalLinks(mediaId: String): Result<List<com.kanlist.domain.model.MediaExternalLink>>

    // Cache management
    suspend fun clearCache()

    suspend fun getCachedMedia(id: String): Result<Media?>

    // Observables
    fun observeTrending(type: MediaType?): Flow<Result<MediaSearchResult>>
    fun observeSeasonal(year: Int, season: com.kanlist.domain.model.MediaSeason, type: MediaType?): Flow<Result<MediaSearchResult>>
}