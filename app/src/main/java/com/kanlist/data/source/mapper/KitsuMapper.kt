package com.kanlist.data.source.mapper

import com.kanlist.data.source.api.kitsu.KitsuApiModels
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.Episode
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaCharacter
import com.kanlist.domain.model.MediaCoverImage
import com.kanlist.domain.model.MediaExternalLink
import com.kanlist.domain.model.MediaFormat
import com.kanlist.domain.model.MediaName
import com.kanlist.domain.model.MediaRelation
import com.kanlist.domain.model.MediaRelationType
import com.kanlist.domain.model.MediaRecommendation
import com.kanlist.domain.model.MediaSeason
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaStaff
import com.kanlist.domain.model.MediaStatusAiring
import com.kanlist.domain.model.MediaTag
import com.kanlist.domain.model.MediaTitle
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.NextAiringEpisode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object KitsuMapper {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(resource: KitsuApiModels.KitsuResource): Media? {
        val attrs = resource.attributes ?: return null
        val isAnime = resource.type == "anime"
        
        val mediaType = determineMediaType(attrs, isAnime)
        val title = toTitle(attrs.titles, attrs.canonicalTitle)

        return Media(
            id = resource.id,
            idAniList = getMappingId(resource, "anilist"),
            idMangaDex = getMappingId(resource, "mangadex"),
            idKitsu = resource.id.toIntOrNull(),
            idMyAnimeList = getMappingId(resource, "myanimelist")?.toIntOrNull(),
            title = title,
            type = mediaType,
            format = toFormat(attrs.subtype, mediaType),
            status = KitsuApiModels.KitsuStatus.toAiringStatus(attrs.status ?: ""),
            season = toSeason(attrs.startDate),
            seasonYear = attrs.startDate?.substring(0, 4).toIntOrNull(),
            startDate = parseDate(attrs.startDate),
            endDate = parseDate(attrs.endDate),
            episodes = attrs.episodeCount,
            chapters = attrs.chapterCount,
            volumes = attrs.volumeCount,
            duration = attrs.episodeLength,
            averageScore = attrs.averageRating?.toDouble()?.div(10),
            popularity = attrs.popularityRank,
            description = attrs.description ?: attrs.synopsis,
            coverImage = toCoverImage(attrs.coverImage),
            bannerImage = attrs.posterImage?.original,
            genres = (attrs as? Map<String, Any>)?.get("genres") as? List<String> ?: emptyList(),
            tags = emptyList(),
            studios = emptyList(),
            staff = emptyList(),
            characters = emptyList(),
            relations = emptyList(),
            recommendations = emptyList(),
            externalLinks = emptyList(),
            nextAiringEpisode = attrs.nextRelease?.let { parseNextAiring(it) },
            source = MediaSource.KITSU,
            isAdult = attrs.nsfw == true,
            countryOfOrigin = "JP",
            updatedAt = parseDateTime(attrs.updatedAt)?.toEpochSecond(java.time.ZoneOffset.UTC)?.times(1000) ?: System.currentTimeMillis()
        )
    }

    private fun determineMediaType(attrs: KitsuApiModels.KitsuAttributes, isAnime: Boolean): MediaType {
        val subtype = attrs.subtype?.uppercase() ?: ""
        return when {
            isAnime -> when (subtype) {
                "MOVIE" -> MediaType.MOVIE
                "OVA" -> MediaType.OVA
                "ONA" -> MediaType.DONGHUA
                "SPECIAL" -> MediaType.OVA
                "MUSIC" -> MediaType.ANIME
                "TV" -> MediaType.ANIME
                else -> MediaType.ANIME
            }
            else -> when (subtype) {
                "MANGA" -> MediaType.MANGA
                "NOVEL" -> MediaType.NOVEL
                "ONE_SHOT" -> MediaType.ONE_SHOT
                else -> MediaType.MANGA
            }
        }
    }

    private fun toFormat(subtype: String?, mediaType: MediaType): MediaFormat {
        val sub = subtype?.uppercase() ?: ""
        return when {
            mediaType in MediaType.watchTypes -> when (sub) {
                "TV" -> MediaFormat.TV
                "MOVIE" -> MediaFormat.MOVIE
                "OVA" -> MediaFormat.OVA
                "ONA" -> MediaFormat.ONA
                "SPECIAL" -> MediaFormat.SPECIAL
                "MUSIC" -> MediaFormat.MUSIC
                else -> MediaFormat.TV
            }
            else -> when (sub) {
                "MANGA" -> MediaFormat.MANGA
                "NOVEL" -> MediaFormat.NOVEL
                "ONE_SHOT" -> MediaFormat.ONE_SHOT
                else -> MediaFormat.MANGA
            }
        }
    }

    private fun toTitle(titles: Map<String, String>?, canonical: String?): MediaTitle {
        return MediaTitle(
            romaji = titles?.getOrDefault("en_jp", titles?.getOrDefault("ja_jp", canonical)),
            english = titles?.getOrDefault("en", titles?.getOrDefault("en_us", canonical)),
            native = titles?.getOrDefault("ja_jp", canonical),
            userPreferred = canonical
        )
    }

    private fun toCoverImage(image: KitsuApiModels.KitsuImage?): MediaCoverImage {
        return MediaCoverImage(
            large = image?.large,
            medium = image?.medium,
            color = null,
            extraLarge = image?.original
        )
    }

    private fun getMappingId(resource: KitsuApiModels.KitsuResource, site: String): String? {
        return resource.relationships?.mappings?.data?.firstOrNull { 
            (resource.relationships?.mappings?.data?.firstOrNull { it.type == "mappings" }?.id).let { 
                // This would need included resources to resolve
                false 
            }
        }?.id
    }

    private fun toSeason(startDate: String?): MediaSeason? {
        startDate?.substring(5, 7)?.toIntOrNull()?.let { month ->
            return when {
                month in 1..3 -> MediaSeason.WINTER
                month in 4..6 -> MediaSeason.SPRING
                month in 7..9 -> MediaSeason.SUMMER
                else -> MediaSeason.FALL
            }
        }
        return null
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

    private fun parseNextAiring(dateStr: String): NextAiringEpisode? {
        return try {
            val date = LocalDateTime.parse(dateStr, dateFormatter)
            val now = LocalDateTime.now()
            if (date.isAfter(now)) {
                NextAiringEpisode(
                    episode = 0, // Would need episode count
                    airingAt = date.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    timeUntilAiring = java.time.Duration.between(now, date).toMillis()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun toEpisodes(response: KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuEpisode>): List<Episode> {
        return response.data.map { toEpisode(it) }
    }

    private fun toEpisode(ep: KitsuApiModels.KitsuEpisode): Episode {
        val attrs = ep.attributes
        return Episode(
            id = ep.id,
            mediaId = ep.relationships?.media?.data?.firstOrNull()?.id ?: "",
            number = attrs.number ?: 0,
            title = attrs.title,
            thumbnail = attrs.thumbnail,
            airDate = attrs.airdate?.let { parseDate(it) },
            duration = attrs.length,
            isFiller = false,
            isRecap = false,
            source = MediaSource.KITSU
        )
    }

    fun toChapters(response: KitsuApiModels.KitsuListResponse<KitsuApiModels.KitsuChapter>): List<Chapter> {
        return response.data.map { toChapter(it) }
    }

    private fun toChapter(ch: KitsuApiModels.KitsuChapter): Chapter {
        val attrs = ch.attributes
        return Chapter(
            id = ch.id,
            mediaId = ch.relationships?.manga?.data?.firstOrNull()?.id ?: "",
            chapter = attrs.number ?: "0",
            volume = attrs.volume,
            title = attrs.title,
            language = attrs.language ?: "en",
            groupName = null,
            groupId = null,
            pages = attrs.length,
            publishAt = attrs.published?.let { parseDate(it) },
            createdAt = parseDateTime(attrs.createdAt) ?: LocalDateTime.now(),
            updatedAt = parseDateTime(attrs.updatedAt) ?: LocalDateTime.now(),
            source = MediaSource.KITSU
        )
    }
}