package com.kanlist.data.source.api.anilist

import com.kanlist.data.source.api.ApiService
import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

// AniList GraphQL Types
@Serializable
data class AniListResponse<T>(
    val data: T?,
    val errors: List<AniListError>?
)

@Serializable
data class AniListError(
    val message: String,
    val status: Int?,
    val locations: List<Location>?
)

@Serializable
data class Location(
    val line: Int,
    val column: Int
)

// Search
@Serializable
data class AniListSearchResponse(
    val Page: AniListPage,
    @Json(name = "pageInfo") val pageInfo: AniListPageInfo? = null
)

@Serializable
data class AniListPage(
    val media: List<AniListMedia>
)

@Serializable
data class AniListPageInfo(
    val total: Int,
    val perPage: Int,
    val currentPage: Int,
    val lastPage: Int,
    val hasNextPage: Boolean
)

@Serializable
data class AniListMedia(
    val id: Int,
    val idMal: Int?,
    val title: AniListTitle,
    val type: String, // ANIME, MANGA
    val format: String?,
    val status: String,
    val season: String?,
    val seasonYear: Int?,
    val startDate: AniListDate?,
    val endDate: AniListDate?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val duration: Int?,
    val averageScore: Int?,
    val popularity: Int?,
    val description: String?,
    val coverImage: AniListCoverImage,
    val bannerImage: String?,
    val genres: List<String>,
    val tags: List<AniListTag>,
    val studios: AniListStudios?,
    val staff: AniListStaff?,
    val characters: AniListCharacters?,
    val relations: AniListRelations?,
    val recommendations: AniListRecommendations?,
    val externalLinks: List<AniListExternalLink>?,
    val nextAiringEpisode: AniListNextAiringEpisode?,
    val isAdult: Boolean,
    val countryOfOrigin: String?,
    val updatedAt: Long
)

@Serializable
data class AniListTitle(
    val romaji: String?,
    val english: String?,
    val native: String?,
    val userPreferred: String?
)

@Serializable
data class AniListCoverImage(
    val large: String?,
    val medium: String?,
    val color: String?,
    val extraLarge: String?
)

@Serializable
data class AniListDate(
    val year: Int?,
    val month: Int?,
    val day: Int?
)

@Serializable
data class AniListTag(
    val id: Int,
    val name: String,
    val description: String?,
    val category: String?,
    val rank: Int?,
    val isGeneralSpoiler: Boolean,
    val isMediaSpoiler: Boolean,
    val isAdult: Boolean
)

@Serializable
data class AniListStudios(
    val nodes: List<AniListStudio>
)

@Serializable
data class AniListStudio(
    val id: Int,
    val name: String,
    val isAnimationStudio: Boolean
)

@Serializable
data class AniListStaff(
    val nodes: List<AniListStaffNode>
)

@Serializable
data class AniListStaffNode(
    val id: Int,
    val name: AniListName,
    val role: String,
    val language: String?
)

@Serializable
data class AniListCharacters(
    val nodes: List<AniListCharacterNode>
)

@Serializable
data class AniListCharacterNode(
    val id: Int,
    val name: AniListName,
    val image: String?,
    val role: String?,
    val voiceActors: List<AniListVoiceActor>
)

@Serializable
data class AniListName(
    val full: String?,
    val native: String?,
    val alternative: List<String>?
)

@Serializable
data class AniListVoiceActor(
    val id: Int,
    val name: AniListName,
    val image: String?,
    val language: String
)

@Serializable
data class AniListRelations(
    val nodes: List<AniListRelationNode>
)

@Serializable
data class AniListRelationNode(
    val id: Int,
    val relationType: String,
    val media: AniListMedia
)

@Serializable
data class AniListRecommendations(
    val nodes: List<AniListRecommendationNode>
)

@Serializable
data class AniListRecommendationNode(
    val id: Int,
    val rating: Int?,
    val media: AniListMedia
)

@Serializable
data class AniListExternalLink(
    val id: Int,
    val site: String,
    val url: String,
    val type: String,
    val language: String?,
    val color: String?,
    val icon: String?,
    val notes: String?
)

@Serializable
data class AniListNextAiringEpisode(
    val episode: Int,
    val airingAt: Long,
    val timeUntilAiring: Long
)

// Trending/Seasonal
@Serializable
data class AniListTrendingResponse(
    val Page: AniListTrendingPage
)

@Serializable
data class AniListTrendingPage(
    val media: List<AniListMedia>
)

// Queries
object AniListQueries {
    const val SEARCH_MEDIA = """
        query SearchMedia(\$search: String, \$page: Int, \$perPage: Int, \$type: MediaType, \$genre_not_in: [String], \$tag_not_in: [String], \$isAdult: Boolean) {
            Page(page: \$page, perPage: \$perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(search: \$search, type: \$type, genre_not_in: \$genre_not_in, tag_not_in: \$tag_not_in, isAdult: \$isAdult, sort: [POPULARITY_DESC, SCORE_DESC]) {
                    ...MediaFields
                }
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_MEDIA_DETAILS = """
        query GetMediaDetails(\$id: Int, \$idMal: Int, \$idMalList: [Int]) {
            Media(id: \$id, idMal: \$idMal) {
                ...MediaFields
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_TRENDING = """
        query GetTrending(\$page: Int, \$perPage: Int, \$type: MediaType) {
            Page(page: \$page, perPage: \$perPage) {
                media(sort: TRENDING_DESC, type: \$type) {
                    ...MediaFields
                }
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_SEASONAL = """
        query GetSeasonal(\$season: MediaSeason, \$seasonYear: Int, \$page: Int, \$perPage: Int, \$type: MediaType) {
            Page(page: \$page, perPage: \$perPage) {
                media(season: \$season, seasonYear: \$seasonYear, type: \$type, sort: [POPULARITY_DESC, SCORE_DESC]) {
                    ...MediaFields
                }
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_EPISODES = """
        query GetEpisodes(\$id: Int) {
            Media(id: \$id) {
                id
                episodes
                airingSchedule(notYetAired: true, page: 1, perPage: 100) {
                    nodes {
                        episode
                        airingAt
                        timeUntilAiring
                    }
                }
            }
        }
    """

    const val GET_CHARACTERS = """
        query GetCharacters(\$id: Int) {
            Media(id: \$id) {
                characters(sort: [ROLE, RELEVANCY]) {
                    nodes {
                        ...CharacterFields
                    }
                }
            }
        }
        ${CHARACTER_FIELDS}
    """

    const val GET_STAFF = """
        query GetStaff(\$id: Int) {
            Media(id: \$id) {
                staff(sort: [RELEVANCY]) {
                    nodes {
                        ...StaffFields
                    }
                }
            }
        }
        ${STAFF_FIELDS}
    """

    const val GET_RELATIONS = """
        query GetRelations(\$id: Int) {
            Media(id: \$id) {
                relations {
                    nodes {
                        id
                        relationType
                        ...MediaFields
                    }
                }
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_RECOMMENDATIONS = """
        query GetRecommendations(\$id: Int) {
            Media(id: \$id) {
                recommendations(sort: [RATING_DESC], perPage: 20) {
                    nodes {
                        id
                        rating
                        media {
                            ...MediaFields
                        }
                    }
                }
            }
        }
        ${MEDIA_FIELDS}
    """

    const val GET_EXTERNAL_LINKS = """
        query GetExternalLinks(\$id: Int) {
            Media(id: \$id) {
                externalLinks {
                    ...ExternalLinkFields
                }
            }
        }
        ${EXTERNAL_LINK_FIELDS}
    """

    private const val MEDIA_FIELDS = """
        fragment MediaFields on Media {
            id
            idMal
            title { romaji english native userPreferred }
            type
            format
            status
            season
            seasonYear
            startDate { year month day }
            endDate { year month day }
            episodes
            chapters
            volumes
            duration
            averageScore
            popularity
            description
            coverImage { large medium color extraLarge }
            bannerImage
            genres
            tags { id name description category rank isGeneralSpoiler isMediaSpoiler isAdult }
            studios { nodes { id name isAnimationStudio } }
            staff { nodes { id name { full native alternative } role language } }
            characters { nodes { id name { full native alternative } image role voiceActors { id name { full native alternative } image language } } }
            relations { nodes { id relationType media { ...MediaFields } } }
            recommendations { nodes { id rating media { ...MediaFields } } }
            externalLinks { ...ExternalLinkFields }
            nextAiringEpisode { episode airingAt timeUntilAiring }
            isAdult
            countryOfOrigin
            updatedAt
        }
    """

    private const val CHARACTER_FIELDS = """
        fragment CharacterFields on Character {
            id
            name { full native alternative }
            image
            role
            voiceActors { id name { full native alternative } image language }
        }
    """

    private const val STAFF_FIELDS = """
        fragment StaffFields on Staff {
            id
            name { full native alternative }
            role
            language
        }
    """

    private const val EXTERNAL_LINK_FIELDS = """
        fragment ExternalLinkFields on MediaExternalLink {
            id
            site
            url
            type
            language
            color
            icon
            notes
        }
    """
}