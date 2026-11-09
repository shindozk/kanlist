package com.kanlist.data.source.mapper

import com.kanlist.data.source.api.mangadex.MangaDexApiModels
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.ChapterList
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaCoverImage
import com.kanlist.domain.model.MediaFormat
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaStatusAiring
import com.kanlist.domain.model.MediaTitle
import com.kanlist.domain.model.MediaType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MangaDexMapper {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(manga: MangaDexApiModels.MangaDexManga): Media {
        val attributes = manga.attributes
        val coverArt = manga.relationships?.firstOrNull { it.type == "cover_art" }?.attributes?.fileName
        val author = manga.relationships?.firstOrNull { it.type == "author" }?.attributes?.name
        
        val title = MediaTitle(
            romaji = attributes.title["en"] ?: attributes.title.values.firstOrNull(),
            english = attributes.title["en"],
            native = attributes.title[attributes.originalLanguage],
            userPreferred = attributes.title["en"] ?: attributes.title.values.firstOrNull()
        )

        val mediaType = determineMediaType(attributes)

        return Media(
            id = manga.id,
            idAniList = null,
            idMangaDex = manga.id,
            idKitsu = null,
            idMyAnimeList = null,
            title = title,
            type = mediaType,
            format = toFormat(mediaType),
            status = MangaDexApiModels.MangaDexStatus.toAiringStatus(attributes.status),
            season = null,
            seasonYear = attributes.year,
            startDate = parseDate(attributes.createdAt),
            endDate = parseDate(attributes.updatedAt),
            episodes = null,
            chapters = attributes.lastChapter?.toIntOrNull(),
            volumes = attributes.lastVolume?.toIntOrNull(),
            duration = when (mediaType) {
                MediaType.MANGA, MediaType.MANHWA, MediaType.MANHUA -> 6
                MediaType.NOVEL -> 30
                else -> 5
            },
            averageScore = null, // MangaDex doesn't provide ratings directly
            popularity = null,
            description = attributes.description["en"] ?: attributes.description.values.firstOrNull(),
            coverImage = toCoverImage(coverArt),
            bannerImage = null,
            genres = attributes.tags
                .filter { it.attributes.group == "genre" }
                .map { it.attributes.name["en"] ?: it.attributes.name.values.firstOrNull() }
                .filterNotNull(),
            tags = attributes.tags.map { toTag(it) },
            studios = emptyList(),
            staff = author?.let { listOf(toStaff(it)) } ?: emptyList(),
            characters = emptyList(),
            relations = emptyList(),
            recommendations = emptyList(),
            externalLinks = emptyList(),
            nextAiringEpisode = null,
            source = MediaSource.MANGDEX,
            isAdult = MangaDexApiModels.MangaDexContentRating.isAdult(attributes.contentRating),
            countryOfOrigin = when (attributes.originalLanguage) {
                "ja" -> "JP"
                "ko" -> "KR"
                "zh" -> "CN"
                "zh-hk" -> "HK"
                "zh-tw" -> "TW"
                else -> "JP"
            },
            updatedAt = parseDateTime(attributes.updatedAt)?.toEpochSecond(ZoneOffset.UTC)?.times(1000) ?: System.currentTimeMillis()
        )
    }

    private fun determineMediaType(attributes: MangaDexApiModels.MangaDexMangaAttributes): MediaType {
        val tags = attributes.tags.map { it.attributes.name["en"]?.lowercase() ?: "" }.toSet()
        val demographic = attributes.demographic?.lowercase() ?: ""
        
        // Check for manhwa/manhua tags
        if (tags.contains("manhwa") || tags.contains("webtoon") || demographic == "manhwa" || attributes.originalLanguage == "ko") {
            return MediaType.MANHWA
        }
        if (tags.contains("manhua") || demographic == "manhua" || attributes.originalLanguage.startsWith("zh")) {
            return MediaType.MANHUA
        }
        if (tags.contains("novel") || tags.contains("light novel")) {
            return MediaType.NOVEL
        }
        if (tags.contains("one shot") || tags.contains("one-shot")) {
            return MediaType.ONE_SHOT
        }
        if (tags.contains("doujin") || tags.contains("doujinshi")) {
            return MediaType.DOUJIN
        }
        
        return MediaType.MANGA
    }

    private fun toFormat(type: MediaType): MediaFormat {
        return when (type) {
            MediaType.MANGA -> MediaFormat.MANGA
            MediaType.MANHWA -> MediaFormat.MANHWA
            MediaType.MANHUA -> MediaFormat.MANHUA
            MediaType.NOVEL -> MediaFormat.NOVEL
            MediaType.ONE_SHOT -> MediaFormat.ONE_SHOT
            MediaType.DOUJIN -> MediaFormat.DOUJIN
            else -> MediaFormat.MANGA
        }
    }

    private fun toCoverImage(fileName: String?): MediaCoverImage {
        val url = fileName?.let { "https://uploads.mangadex.org/covers/$it" }
        return MediaCoverImage(
            large = url,
            medium = url?.let { it.replace("covers/", "covers/256x256/") },
            color = null,
            extraLarge = url?.let { it.replace("covers/", "covers/512x512/") }
        )
    }

    private fun toTag(tag: MangaDexApiModels.MangaDexTag): com.kanlist.domain.model.MediaTag {
        return com.kanlist.domain.model.MediaTag(
            id = tag.id.hashCode(),
            name = tag.attributes.name["en"] ?: tag.attributes.name.values.firstOrNull() ?: "",
            description = tag.attributes.description?["en"] ?: tag.attributes.description?.values?.firstOrNull(),
            category = tag.attributes.group,
            rank = null,
            isGeneralSpoiler = false,
            isMediaSpoiler = false,
            isAdult = false
        )
    }

    private fun toStaff(name: String): com.kanlist.domain.model.MediaStaff {
        return com.kanlist.domain.model.MediaStaff(
            id = name.hashCode(),
            name = com.kanlist.domain.model.MediaName(full = name, native = null, alternative = null),
            role = "Author",
            language = null
        )
    }

    private fun parseDate(dateStr: String?): LocalDate? {
        return dateStr?.let {
            try {
                LocalDate.parse(it.substring(0, 10), dateOnlyFormatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseDateTime(dateStr: String?): LocalDateTime? {
        return dateStr?.let {
            try {
                LocalDateTime.parse(it, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toChapterList(
        response: MangaDexApiModels.MangaDexChapterListResponse,
        mediaId: String,
        languages: List<ChapterLanguage>
    ): ChapterList {
        val allowedLanguages = languages.map { it.code }.toSet()
        
        val chapters = response.data
            .filter { it.attributes.translatedLanguage in allowedLanguages }
            .map { toChapter(it, mediaId) }
            .sortedBy { it.chapterNumber }

        return ChapterList(
            mediaId = mediaId,
            chapters = chapters,
            languages = languages,
            lastUpdated = LocalDateTime.now(),
            source = MediaSource.MANGDEX
        )
    }

    private fun toChapter(chapter: MangaDexApiModels.MangaDexChapter, mediaId: String): Chapter {
        val attrs = chapter.attributes
        val groupName = chapter.relationships?.firstOrNull { it.type == "scanlation_group" }?.attributes?.name
        val groupId = chapter.relationships?.firstOrNull { it.type == "scanlation_group" }?.id

        return Chapter(
            id = chapter.id,
            mediaId = mediaId,
            chapter = attrs.chapter,
            volume = attrs.volume,
            title = attrs.title,
            language = attrs.translatedLanguage,
            groupName = groupName,
            groupId = groupId,
            pages = attrs.pages,
            publishAt = parseDate(attrs.publishAt),
            createdAt = parseDateTime(attrs.createdAt) ?: LocalDateTime.now(),
            updatedAt = parseDateTime(attrs.updatedAt) ?: LocalDateTime.now(),
            source = MediaSource.MANGDEX
        )
    }

    fun toChapterLanguage(langCode: String): ChapterLanguage {
        return ChapterLanguage.values().firstOrNull { it.code == langCode } ?: ChapterLanguage.EN
    }
}