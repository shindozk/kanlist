package com.kanlist.domain.usecase

import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.repository.MediaRepository
import com.kanlist.domain.repository.TranslationRepository
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.ChapterList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        types: List<MediaType>? = null,
        sources: List<MediaSource>? = null,
        includeAdult: Boolean = false
    ) = mediaRepository.searchMedia(query, page, perPage, types, sources, includeAdult)
}

class GetTrendingMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ) = mediaRepository.getTrending(page, perPage, type)

    fun observe(type: MediaType?): Flow<Result<MediaSearchResult>> = mediaRepository.observeTrending(type)
}

class GetSeasonalMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(
        year: Int,
        season: com.kanlist.domain.model.MediaSeason,
        page: Int = 1,
        perPage: Int = 20,
        type: MediaType? = null
    ) = mediaRepository.getSeasonal(year, season, page, perPage, type)

    fun observe(year: Int, season: com.kanlist.domain.model.MediaSeason, type: MediaType?): Flow<Result<MediaSearchResult>> =
        mediaRepository.observeSeasonal(year, season, type)
}

class GetMediaDetailsUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(id: String, source: MediaSource? = null): Result<Media> =
        mediaRepository.getMediaDetails(id, source)

    suspend fun getById(id: String): Result<Media?> = mediaRepository.getMediaById(id)
}

class GetEpisodesUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String, source: MediaSource? = null) =
        mediaRepository.getEpisodes(mediaId, source)
}

class GetChaptersUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(
        mediaId: String,
        languages: List<ChapterLanguage>? = null,
        source: MediaSource? = null
    ): Result<ChapterList> = mediaRepository.getChapters(mediaId, languages, source)
}

class GetRelationsUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String) = mediaRepository.getRelations(mediaId)
}

class GetRecommendationsUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String) = mediaRepository.getRecommendations(mediaId)
}

class GetCharactersUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String) = mediaRepository.getCharacters(mediaId)
}

class GetStaffUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String) = mediaRepository.getStaff(mediaId)
}

class GetExternalLinksUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke(mediaId: String) = mediaRepository.getExternalLinks(mediaId)
}

class TranslateSynopsisUseCase @Inject constructor(
    private val translationRepository: TranslationRepository
) {
    operator fun invoke(media: Media): Result<String> = translationRepository.translateSynopsis(media)

    suspend fun translate(text: String, sourceLang: String = "en", targetLang: String = "pt"): Result<String> =
        translationRepository.translateText(text, sourceLang, targetLang)
}

class ClearMediaCacheUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    operator fun invoke() = mediaRepository.clearCache()
}