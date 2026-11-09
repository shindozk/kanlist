package com.kanlist.data.source.mapper

import com.kanlist.data.source.api.anilist.AniListApiModels
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
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaStatusAiring
import com.kanlist.domain.model.MediaTag
import com.kanlist.domain.model.MediaTitle
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.NextAiringEpisode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

object AniListMapper {

    fun toDomain(media: AniListApiModels.AniListMedia): Media {
        return Media(
            id = media.id.toString(),
            idAniList = media.id,
            idMangaDex = null,
            idKitsu = null,
            idMyAnimeList = media.idMal,
            title = toTitle(media.title),
            type = toMediaType(media.type, media.format),
            format = toFormat(media.format),
            status = toAiringStatus(media.status),
            season = toSeason(media.season),
            seasonYear = media.seasonYear,
            startDate = toLocalDate(media.startDate),
            endDate = toLocalDate(media.endDate),
            episodes = media.episodes,
            chapters = media.chapters,
            volumes = media.volumes,
            duration = media.duration,
            averageScore = media.averageScore?.div(10.0),
            popularity = media.popularity,
            description = media.description,
            coverImage = toCoverImage(media.coverImage),
            bannerImage = media.bannerImage,
            genres = media.genres,
            tags = media.tags.map { toTag(it) },
            studios = media.studios?.nodes?.map { toStudio(it) } ?: emptyList(),
            staff = media.staff?.nodes?.map { toStaff(it) } ?: emptyList(),
            characters = media.characters?.nodes?.map { toCharacter(it) } ?: emptyList(),
            relations = media.relations?.nodes?.map { toRelation(it) } ?: emptyList(),
            recommendations = media.recommendations?.nodes?.map { toRecommendation(it) } ?: emptyList(),
            externalLinks = media.externalLinks?.map { toExternalLink(it) } ?: emptyList(),
            nextAiringEpisode = media.nextAiringEpisode?.let { toNextAiringEpisode(it) },
            source = MediaSource.ANILIST,
            isAdult = media.isAdult,
            countryOfOrigin = media.countryOfOrigin,
            updatedAt = media.updatedAt * 1000L // AniList returns seconds
        )
    }

    private fun toTitle(title: AniListApiModels.AniListTitle): MediaTitle {
        return MediaTitle(
            romaji = title.romaji,
            english = title.english,
            native = title.native,
            userPreferred = title.userPreferred
        )
    }

    private fun toMediaType(type: String, format: String?): MediaType {
        return when (type) {
            "ANIME" -> when (format) {
                "MOVIE" -> MediaType.MOVIE
                "OVA", "SPECIAL" -> MediaType.OVA
                "ONA" -> MediaType.DONGHUA
                "MUSIC" -> MediaType.ANIME
                else -> MediaType.ANIME
            }
            "MANGA" -> when (format) {
                "MANGA" -> MediaType.MANGA
                "NOVEL" -> MediaType.NOVEL
                "ONE_SHOT" -> MediaType.ONE_SHOT
                "DOUJIN" -> MediaType.DOUJIN
                "MANHWA" -> MediaType.MANHWA
                "MANHUA" -> MediaType.MANHUA
                else -> MediaType.MANGA
            }
            else -> MediaType.ANIME
        }
    }

    private fun toFormat(format: String?): MediaFormat {
        return when (format?.uppercase()) {
            "TV" -> MediaFormat.TV
            "MOVIE" -> MediaFormat.MOVIE
            "OVA" -> MediaFormat.OVA
            "ONA" -> MediaFormat.ONA
            "SPECIAL" -> MediaFormat.SPECIAL
            "MUSIC" -> MediaFormat.MUSIC
            "MANGA" -> MediaFormat.MANGA
            "NOVEL" -> MediaFormat.NOVEL
            "ONE_SHOT" -> MediaFormat.ONE_SHOT
            "DOUJIN" -> MediaFormat.DOUJIN
            "MANHWA" -> MediaFormat.MANHWA
            "MANHUA" -> MediaFormat.MANHUA
            else -> MediaFormat.UNKNOWN
        }
    }

    private fun toAiringStatus(status: String): MediaStatusAiring {
        return when (status.uppercase()) {
            "FINISHED" -> MediaStatusAiring.FINISHED
            "RELEASING" -> MediaStatusAiring.RELEASING
            "NOT_YET_RELEASED" -> MediaStatusAiring.NOT_YET_RELEASED
            "HIATUS" -> MediaStatusAiring.HIATUS
            "CANCELLED" -> MediaStatusAiring.CANCELLED
            else -> MediaStatusAiring.UNKNOWN
        }
    }

    private fun toSeason(season: String?): MediaSeason? {
        return season?.uppercase()?.let { MediaSeason.valueOf(it) }
    }

    private fun toLocalDate(date: AniListApiModels.AniListDate?): LocalDate? {
        return date?.let {
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

    private fun toCoverImage(cover: AniListApiModels.AniListCoverImage): MediaCoverImage {
        return MediaCoverImage(
            large = cover.large,
            medium = cover.medium,
            color = cover.color,
            extraLarge = cover.extraLarge
        )
    }

    private fun toTag(tag: AniListApiModels.AniListTag): MediaTag {
        return MediaTag(
            id = tag.id,
            name = tag.name,
            description = tag.description,
            category = tag.category,
            rank = tag.rank,
            isGeneralSpoiler = tag.isGeneralSpoiler,
            isMediaSpoiler = tag.isMediaSpoiler,
            isAdult = tag.isAdult
        )
    }

    private fun toStudio(studio: AniListApiModels.AniListStudio): com.kanlist.domain.model.MediaStudio {
        return com.kanlist.domain.model.MediaStudio(
            id = studio.id,
            name = studio.name,
            isAnimationStudio = studio.isAnimationStudio,
            isMain = true
        )
    }

    private fun toStaff(staff: AniListApiModels.AniListStaffNode): MediaStaff {
        return MediaStaff(
            id = staff.id,
            name = toName(staff.name),
            role = staff.role,
            language = staff.language
        )
    }

    private fun toName(name: AniListApiModels.AniListName): MediaName {
        return MediaName(
            full = name.full,
            native = name.native,
            alternative = name.alternative
        )
    }

    private fun toCharacter(character: AniListApiModels.AniListCharacterNode): MediaCharacter {
        return MediaCharacter(
            id = character.id,
            name = toName(character.name),
            image = character.image,
            role = character.role,
            voiceActors = character.voiceActors.map { toVoiceActor(it) }
        )
    }

    private fun toVoiceActor(va: AniListApiModels.AniListVoiceActor): com.kanlist.domain.model.MediaVoiceActor {
        return com.kanlist.domain.model.MediaVoiceActor(
            id = va.id,
            name = toName(va.name),
            image = va.image,
            language = va.language
        )
    }

    private fun toRelation(relation: AniListApiModels.AniListRelationNode): MediaRelation {
        return MediaRelation(
            id = relation.id.toString(),
            relationType = toRelationType(relation.relationType),
            media = toDomain(relation.media)
        )
    }

    private fun toRelationType(type: String): MediaRelationType {
        return when (type.uppercase()) {
            "SEQUEL" -> MediaRelationType.SEQUEL
            "PREQUEL" -> MediaRelationType.PREQUEL
            "PARENT" -> MediaRelationType.PARENT
            "SIDE_STORY" -> MediaRelationType.SIDE_STORY
            "SPIN_OFF" -> MediaRelationType.SPIN_OFF
            "SUMMARY" -> MediaRelationType.SUMMARY
            "ALTERNATIVE" -> MediaRelationType.ALTERNATIVE
            "ADAPTATION" -> MediaRelationType.ADAPTATION
            "CHARACTER" -> MediaRelationType.CHARACTER
            else -> MediaRelationType.OTHER
        }
    }

    private fun toRecommendation(rec: AniListApiModels.AniListRecommendationNode): MediaRecommendation {
        return MediaRecommendation(
            id = rec.id.toString(),
            rating = rec.rating,
            media = toDomain(rec.media)
        )
    }

    private fun toExternalLink(link: AniListApiModels.AniListExternalLink): MediaExternalLink {
        return MediaExternalLink(
            id = link.id,
            site = link.site,
            url = link.url,
            type = link.type,
            language = link.language,
            color = link.color,
            icon = link.icon,
            notes = link.notes
        )
    }

    private fun toNextAiringEpisode(episode: AniListApiModels.AniListNextAiringEpisode): NextAiringEpisode {
        return NextAiringEpisode(
            episode = episode.episode,
            airingAt = episode.airingAt * 1000L,
            timeUntilAiring = episode.timeUntilAiring * 1000L
        )
    }

    fun toSearchResult(
        response: AniListApiModels.AniListSearchResponse,
        source: MediaSource = MediaSource.ANILIST
    ): com.kanlist.domain.model.MediaSearchResult {
        return com.kanlist.domain.model.MediaSearchResult(
            media = response.Page.media.map { toDomain(it) },
            pageInfo = toPageInfo(response.Page.pageInfo),
            sources = listOf(source),
            errors = emptyMap()
        )
    }

    private fun toPageInfo(pageInfo: AniListApiModels.AniListPageInfo?): com.kanlist.domain.model.PageInfo {
        return com.kanlist.domain.model.PageInfo(
            total = pageInfo?.total ?: 0,
            perPage = pageInfo?.perPage ?: 20,
            currentPage = pageInfo?.currentPage ?: 1,
            lastPage = pageInfo?.lastPage ?: 1,
            hasNextPage = pageInfo?.hasNextPage ?: false
        )
    }
}