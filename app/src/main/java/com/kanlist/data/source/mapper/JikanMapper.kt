package com.kanlist.data.source.mapper

import com.kanlist.data.source.api.jikan.JikanApiModels
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

object JikanMapper {

    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toDomain(anime: JikanApiModels.JikanAnime): Media {
        val title = toTitle(anime)
        val mediaType = toMediaType(anime.type, anime.source)

        return Media(
            id = "mal-${anime.mal_id}",
            idAniList = null,
            idMangaDex = null,
            idKitsu = null,
            idMyAnimeList = anime.mal_id,
            title = title,
            type = mediaType,
            format = toFormat(anime.type, anime.source),
            status = toAiringStatus(anime.status),
            season = toSeason(anime.season),
            seasonYear = anime.year,
            startDate = parseAiredDate(anime.aired?.prop?.from),
            endDate = parseAiredDate(anime.aired?.prop?.to),
            episodes = anime.episodes,
            chapters = null,
            volumes = null,
            duration = parseDuration(anime.duration),
            averageScore = anime.score,
            popularity = anime.popularity,
            description = anime.synopsis,
            coverImage = toCoverImage(anime.images),
            bannerImage = anime.trailer?.images?.maximum_image_url,
            genres = anime.genres?.map { it.name } ?: emptyList(),
            tags = (anime.themes ?: emptyList()).map { toTag(it) } + (anime.demographics ?: emptyList()).map { toTag(it) },
            studios = anime.studios?.map { toStudio(it) } ?: emptyList(),
            staff = emptyList(),
            characters = emptyList(),
            relations = anime.relations?.flatMap { rel -> rel.entry.map { toRelation(rel.relation, it) } } ?: emptyList(),
            recommendations = emptyList(),
            externalLinks = anime.external?.map { toExternalLink(it) } ?: emptyList() + anime.streaming?.map { toStreamingLink(it) } ?: emptyList(),
            nextAiringEpisode = anime.broadcast?.let { parseNextAiring(it, anime.episodes) },
            source = MediaSource.JIKAN,
            isAdult = anime.rating?.lowercase()?.contains("rx") == true || anime.rating?.lowercase()?.contains("hentai") == true,
            countryOfOrigin = "JP",
            updatedAt = System.currentTimeMillis()
        )
    }

    fun toDomain(manga: JikanApiModels.JikanManga): Media {
        val title = toTitle(manga)
        val mediaType = toMangaMediaType(manga.type)

        return Media(
            id = "mal-${manga.mal_id}",
            idAniList = null,
            idMangaDex = null,
            idKitsu = null,
            idMyAnimeList = manga.mal_id,
            title = title,
            type = mediaType,
            format = toMangaFormat(manga.type),
            status = toAiringStatus(manga.status),
            season = null,
            seasonYear = null,
            startDate = parsePublishedDate(manga.published?.prop?.from),
            endDate = parsePublishedDate(manga.published?.prop?.to),
            episodes = null,
            chapters = manga.chapters,
            volumes = manga.volumes,
            duration = when (mediaType) {
                MediaType.NOVEL -> 30
                else -> 6
            },
            averageScore = manga.score,
            popularity = manga.popularity,
            description = manga.synopsis,
            coverImage = toCoverImage(manga.images),
            bannerImage = null,
            genres = manga.genres?.map { it.name } ?: emptyList(),
            tags = (manga.themes ?: emptyList()).map { toTag(it) } + (manga.demographics ?: emptyList()).map { toTag(it) },
            studios = manga.authors?.map { toStudio(it) } ?: emptyList(),
            staff = emptyList(),
            characters = emptyList(),
            relations = manga.relations?.flatMap { rel -> rel.entry.map { toRelation(rel.relation, it) } } ?: emptyList(),
            recommendations = emptyList(),
            externalLinks = manga.external?.map { toExternalLink(it) } ?: emptyList(),
            nextAiringEpisode = null,
            source = MediaSource.JIKAN,
            isAdult = manga.genres?.any { it.name.lowercase().contains("hentai") || it.name.lowercase().contains("erotica") } == true,
            countryOfOrigin = "JP",
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun toTitle(anime: JikanApiModels.JikanAnime): MediaTitle {
        val romaji = anime.titles?.firstOrNull { it.type == "Default" }?.title ?: anime.title
        val english = anime.title_english
        val nativeTitle = anime.title_japanese
        return MediaTitle(romaji = romaji, english = english, native = nativeTitle, userPreferred = anime.title)
    }

    private fun toTitle(manga: JikanApiModels.JikanManga): MediaTitle {
        val romaji = manga.titles?.firstOrNull { it.type == "Default" }?.title ?: manga.title
        val english = manga.title_english
        val nativeTitle = manga.title_japanese
        return MediaTitle(romaji = romaji, english = english, native = nativeTitle, userPreferred = manga.title)
    }

    private fun toMediaType(type: String?, source: String?): MediaType {
        val t = type?.uppercase() ?: ""
        val s = source?.uppercase() ?: ""
        return when {
            t == "MOVIE" -> MediaType.MOVIE
            t == "OVA" -> MediaType.OVA
            t == "ONA" -> MediaType.DONGHUA
            t == "SPECIAL" -> MediaType.OVA
            t == "MUSIC" -> MediaType.ANIME
            t == "TV" -> MediaType.ANIME
            s == "MANGA" -> MediaType.MANGA
            s == "NOVEL" -> MediaType.NOVEL
            s == "LIGHT_NOVEL" -> MediaType.NOVEL
            s == "ONE_SHOT" -> MediaType.ONE_SHOT
            s == "DOUJINSHI" -> MediaType.DOUJIN
            else -> MediaType.ANIME
        }
    }

    private fun toMangaMediaType(type: String?): MediaType {
        val t = type?.uppercase() ?: ""
        return when (t) {
            "MANGA" -> MediaType.MANGA
            "NOVEL", "LIGHT_NOVEL" -> MediaType.NOVEL
            "ONE_SHOT" -> MediaType.ONE_SHOT
            "DOUJINSHI" -> MediaType.DOUJIN
            "MANHWA" -> MediaType.MANHWA
            "MANHUA" -> MediaType.MANHUA
            else -> MediaType.MANGA
        }
    }

    private fun toFormat(type: String?, source: String?): MediaFormat {
        val t = type?.uppercase() ?: ""
        return when (t) {
            "TV" -> MediaFormat.TV
            "MOVIE" -> MediaFormat.MOVIE
            "OVA" -> MediaFormat.OVA
            "ONA" -> MediaFormat.ONA
            "SPECIAL" -> MediaFormat.SPECIAL
            "MUSIC" -> MediaFormat.MUSIC
            else -> MediaFormat.TV
        }
    }

    private fun toMangaFormat(type: String?): MediaFormat {
        val t = type?.uppercase() ?: ""
        return when (t) {
            "MANGA" -> MediaFormat.MANGA
            "NOVEL", "LIGHT_NOVEL" -> MediaFormat.NOVEL
            "ONE_SHOT" -> MediaFormat.ONE_SHOT
            "DOUJINSHI" -> MediaFormat.DOUJIN
            else -> MediaFormat.MANGA
        }
    }

    private fun toAiringStatus(status: String?): MediaStatusAiring {
        val s = status?.uppercase() ?: ""
        return when (s) {
            "FINISHED", "COMPLETE" -> MediaStatusAiring.FINISHED
            "CURRENTLY AIRING", "PUBLISHING" -> MediaStatusAiring.RELEASING
            "NOT YET AIRED", "NOT YET PUBLISHED" -> MediaStatusAiring.NOT_YET_RELEASED
            "ON HIATUS", "HIATUS" -> MediaStatusAiring.HIATUS
            "DISCONTINUED", "CANCELLED" -> MediaStatusAiring.CANCELLED
            else -> MediaStatusAiring.UNKNOWN
        }
    }

    private fun toSeason(season: String?): MediaSeason? {
        return season?.uppercase()?.let { MediaSeason.valueOf(it) }
    }

    private fun parseAiredDate(prop: JikanApiModels.JikanDateProp?): LocalDate? {
        return prop?.let {
            try {
                LocalDate.of(
                    it.year ?: 1970,
                    it.month ?: 1,
                    it.day ?: 1
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parsePublishedDate(prop: JikanApiModels.JikanDateProp?): LocalDate? {
        return parseAiredDate(prop)
    }

    private fun parseDuration(duration: String?): Int? {
        return duration?.let {
            val parts = it.split(" ")
            if (parts.size >= 2 && parts[1] == "min") {
                parts[0].toIntOrNull()
            } else if (parts.size >= 3 && parts[2] == "hr") {
                (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
            } else null
        }
    }

    private fun toCoverImage(images: JikanApiModels.JikanImages?): MediaCoverImage {
        val jpg = images?.jpg
        return MediaCoverImage(
            large = jpg?.large_image_url,
            medium = jpg?.image_url,
            color = null,
            extraLarge = jpg?.large_image_url
        )
    }

    private fun toTag(genre: JikanApiModels.JikanGenre): MediaTag {
        return MediaTag(
            id = genre.mal_id,
            name = genre.name,
            description = null,
            category = genre.type.lowercase(),
            rank = null,
            isGeneralSpoiler = false,
            isMediaSpoiler = false,
            isAdult = false
        )
    }

    private fun toStudio(producer: JikanApiModels.JikanProducer): com.kanlist.domain.model.MediaStudio {
        return com.kanlist.domain.model.MediaStudio(
            id = producer.mal_id,
            name = producer.name,
            isAnimationStudio = producer.type == "anime",
            isMain = true
        )
    }

    private fun toRelation(relationType: String, entry: JikanApiModels.JikanRelationEntry): MediaRelation {
        return MediaRelation(
            id = "mal-${entry.mal_id}",
            relationType = toRelationType(relationType),
            media = Media(
                id = "mal-${entry.mal_id}",
                idMyAnimeList = entry.mal_id,
                title = MediaTitle(romaji = entry.name, english = null, native = null, userPreferred = entry.name),
                type = MediaType.ANIME,
                format = MediaFormat.TV,
                status = MediaStatusAiring.UNKNOWN,
                source = MediaSource.JIKAN,
                coverImage = MediaCoverImage(large = null, medium = null, color = null, extraLarge = null),
                updatedAt = 0
            )
        )
    }

    private fun toRelationType(type: String): MediaRelationType {
        return when (type.uppercase()) {
            "SEQUEL" -> MediaRelationType.SEQUEL
            "PREQUEL" -> MediaRelationType.PREQUEL
            "PARENT STORY", "PARENT" -> MediaRelationType.PARENT
            "SIDE STORY" -> MediaRelationType.SIDE_STORY
            "SPIN-OFF", "SPIN_OFF" -> MediaRelationType.SPIN_OFF
            "SUMMARY" -> MediaRelationType.SUMMARY
            "ALTERNATIVE VERSION", "ALTERNATIVE" -> MediaRelationType.ALTERNATIVE
            "ADAPTATION" -> MediaRelationType.ADAPTATION
            "CHARACTER" -> MediaRelationType.CHARACTER
            else -> MediaRelationType.OTHER
        }
    }

    private fun toExternalLink(ext: JikanApiModels.JikanExternal): MediaExternalLink {
        return MediaExternalLink(
            id = ext.name.hashCode(),
            site = ext.name,
            url = ext.url,
            type = "external",
            language = null,
            color = null,
            icon = null,
            notes = null
        )
    }

    private fun toStreamingLink(streaming: JikanApiModels.JikanStreaming): MediaExternalLink {
        return MediaExternalLink(
            id = streaming.name.hashCode(),
            site = streaming.name,
            url = streaming.url,
            type = "streaming",
            language = null,
            color = null,
            icon = null,
            notes = null
        )
    }

    private fun parseNextAiring(broadcast: JikanApiModels.JikanBroadcast, totalEpisodes: Int?): NextAiringEpisode? {
        // Jikan broadcast info doesn't give exact next episode air time
        return null
    }

    fun toEpisodes(response: JikanApiModels.JikanResponse<List<JikanApiModels.JikanEpisode>>): List<Episode> {
        return response.data.map { toEpisode(it) }
    }

    private fun toEpisode(ep: JikanApiModels.JikanEpisode): Episode {
        return Episode(
            id = "mal-${ep.mal_id}",
            mediaId = "",
            number = ep.mal_id, // This is episode ID, not number
            title = ep.title,
            thumbnail = null,
            airDate = ep.aired?.let { LocalDate.parse(it.substring(0, 10), dateOnlyFormatter) },
            duration = ep.duration,
            isFiller = ep.filler == true,
            isRecap = ep.recap == true,
            source = MediaSource.JIKAN
        )
    }
}