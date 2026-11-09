package com.kanlist.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kanlist.data.local.converters.Converters
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.MediaFormat
import com.kanlist.domain.model.MediaRelationType
import com.kanlist.domain.model.MediaSeason
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaStatusAiring
import com.kanlist.domain.model.MediaTag
import com.kanlist.domain.model.MediaTitle
import com.kanlist.domain.model.MediaType
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "media",
    indices = [Index("idAniList"), Index("idMangaDex"), Index("idKitsu"), Index("idMyAnimeList"), Index("type"), Index("source")]
)
@Serializable
data class MediaEntity(
    @PrimaryKey val id: String,
    val idAniList: Int?,
    val idMangaDex: String?,
    val idKitsu: Int?,
    val idMyAnimeList: Int?,
    @TypeConverters(Converters::class) val title: MediaTitle,
    @TypeConverters(Converters::class) val type: MediaType,
    @TypeConverters(Converters::class) val format: MediaFormat,
    @TypeConverters(Converters::class) val status: MediaStatusAiring,
    @TypeConverters(Converters::class) val season: MediaSeason?,
    val seasonYear: Int?,
    @TypeConverters(Converters::class) val startDate: LocalDate?,
    @TypeConverters(Converters::class) val endDate: LocalDate?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val duration: Int?,
    val averageScore: Double?,
    val popularity: Int?,
    val description: String?,
    @TypeConverters(Converters::class) val coverImage: MediaCoverImageEntity,
    val bannerImage: String?,
    @TypeConverters(Converters::class) val genres: List<String>,
    @TypeConverters(Converters::class) val tags: List<MediaTag>,
    @TypeConverters(Converters::class) val studios: List<MediaStudioEntity>,
    @TypeConverters(Converters::class) val staff: List<MediaStaffEntity>,
    @TypeConverters(Converters::class) val characters: List<MediaCharacterEntity>,
    @TypeConverters(Converters::class) val relations: List<MediaRelationEntity>,
    @TypeConverters(Converters::class) val recommendations: List<MediaRecommendationEntity>,
    @TypeConverters(Converters::class) val externalLinks: List<MediaExternalLinkEntity>,
    @TypeConverters(Converters::class) val nextAiringEpisode: NextAiringEpisodeEntity?,
    @TypeConverters(Converters::class) val source: MediaSource,
    val isAdult: Boolean,
    val countryOfOrigin: String?,
    val updatedAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

@Serializable
data class MediaCoverImageEntity(
    val large: String?,
    val medium: String?,
    val color: String?,
    val extraLarge: String?
)

@Serializable
data class MediaStudioEntity(
    val id: Int,
    val name: String,
    val isAnimationStudio: Boolean,
    val isMain: Boolean
)

@Serializable
data class MediaStaffEntity(
    val id: Int,
    @TypeConverters(Converters::class) val name: MediaNameEntity,
    val role: String,
    val language: String?
)

@Serializable
data class MediaNameEntity(
    val full: String?,
    val native: String?,
    val alternative: List<String>?
)

@Serializable
data class MediaCharacterEntity(
    val id: Int,
    @TypeConverters(Converters::class) val name: MediaNameEntity,
    val image: String?,
    val role: String?,
    @TypeConverters(Converters::class) val voiceActors: List<MediaVoiceActorEntity>
)

@Serializable
data class MediaVoiceActorEntity(
    val id: Int,
    @TypeConverters(Converters::class) val name: MediaNameEntity,
    val image: String?,
    val language: String
)

@Serializable
data class MediaRelationEntity(
    val id: String,
    @TypeConverters(Converters::class) val relationType: MediaRelationType,
    @TypeConverters(Converters::class) val media: MediaEntity
)

@Serializable
data class MediaRecommendationEntity(
    val id: String,
    val rating: Int?,
    @TypeConverters(Converters::class) val media: MediaEntity
)

@Serializable
data class MediaExternalLinkEntity(
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
data class NextAiringEpisodeEntity(
    val episode: Int,
    val airingAt: Long,
    val timeUntilAiring: Long
)

@Entity(
    tableName = "user_media",
    indices = [Index("mediaId"), Index("status"), Index("type"), Index("lastActivityAt"), Index("isFavorite")]
)
@Serializable
data class UserMediaEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    @TypeConverters(Converters::class) val media: MediaEntity?,
    @TypeConverters(Converters::class) val status: MediaStatus,
    val progress: Int,
    val score: Float,
    val isFavorite: Boolean,
    val notes: String,
    @TypeConverters(Converters::class) val startedAt: LocalDateTime?,
    @TypeConverters(Converters::class) val completedAt: LocalDateTime?,
    @TypeConverters(Converters::class) val updatedAt: LocalDateTime,
    @TypeConverters(Converters::class) val createdAt: LocalDateTime,
    @TypeConverters(Converters::class) val lastActivityAt: LocalDateTime,
    val rewatchCount: Int,
    val rereadCount: Int,
    val customTitle: String?,
    val customCoverUrl: String?,
    val episodeOffset: Int,
    val chapterOffset: Int,
    val volumeProgress: Int,
    val timeSpentMinutes: Int,
    val isHidden: Boolean,
    @TypeConverters(Converters::class) val collectionIds: List<String>
)

@Entity(
    tableName = "episode",
    indices = [Index("mediaId"), Index("number")],
    foreignKeys = [ForeignKey(
        entity = MediaEntity::class,
        parentColumns = ["id"],
        childColumns = ["mediaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Serializable
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val number: Int,
    val title: String?,
    val thumbnail: String?,
    @TypeConverters(Converters::class) val airDate: LocalDate?,
    val duration: Int?,
    val isFiller: Boolean,
    val isRecap: Boolean,
    @TypeConverters(Converters::class) val source: MediaSource
)

@Entity(
    tableName = "chapter",
    indices = [Index("mediaId"), Index("chapterNumber"), Index("volumeNumber"), Index("language"), Index("groupId")],
    foreignKeys = [ForeignKey(
        entity = MediaEntity::class,
        parentColumns = ["id"],
        childColumns = ["mediaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Serializable
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val chapter: String,
    @TypeConverters(Converters::class) val chapterNumber: Double,
    val volume: String?,
    @TypeConverters(Converters::class) val volumeNumber: Int?,
    val title: String?,
    val language: String,
    val groupName: String?,
    val groupId: String?,
    val pages: Int?,
    @TypeConverters(Converters::class) val publishAt: LocalDate?,
    @TypeConverters(Converters::class) val createdAt: LocalDateTime,
    @TypeConverters(Converters::class) val updatedAt: LocalDateTime,
    @TypeConverters(Converters::class) val source: MediaSource,
    val isExternalUrl: Boolean,
    val externalUrl: String?
)

@Entity(
    tableName = "user_chapter_progress",
    indices = [Index("userMediaId"), Index("chapterId"), Index("chapterNumber")],
    primaryKeys = ["userMediaId", "chapterId"]
)
@Serializable
data class UserChapterProgressEntity(
    val userMediaId: String,
    val chapterId: String,
    val chapterNumber: Double,
    val isRead: Boolean,
    @TypeConverters(Converters::class) val readAt: LocalDateTime?,
    @TypeConverters(Converters::class) val createdAt: LocalDateTime
)

@Entity(
    tableName = "collection",
    indices = [Index("sortOrder")]
)
@Serializable
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Int,
    val icon: String?,
    val description: String,
    @TypeConverters(Converters::class) val mediaIds: List<String>,
    @TypeConverters(Converters::class) val createdAt: LocalDateTime,
    @TypeConverters(Converters::class) val updatedAt: LocalDateTime,
    val sortOrder: Int
)

@Entity(
    tableName = "activity_entry",
    indices = [Index("userMediaId"), Index("mediaId"), Index("timestamp"), Index("type")]
)
@Serializable
data class ActivityEntryEntity(
    @PrimaryKey val id: String,
    val userMediaId: String,
    val mediaId: String,
    @TypeConverters(Converters::class) val type: ActivityTypeEntity,
    val progress: Int,
    val previousProgress: Int,
    @TypeConverters(Converters::class) val timestamp: LocalDateTime,
    val note: String?
)

enum class ActivityTypeEntity {
    EPISODE_WATCHED, CHAPTER_READ, STATUS_CHANGED, SCORE_CHANGED,
    ADDED_TO_LIBRARY, REMOVED_FROM_LIBRARY, FAVORITED, UNFAVORITED,
    NOTE_ADDED, PROGRESS_RESET
}

@Entity(tableName = "daily_goal")
@Serializable
data class DailyGoalEntity(
    @PrimaryKey val id: String,
    @TypeConverters(Converters::class) val date: LocalDate,
    val target: Int,
    val completed: Int,
    val streak: Int,
    @TypeConverters(Converters::class) val lastCompletedDate: LocalDate?
)

@Entity(tableName = "user_stats")
@Serializable
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1, // Singleton
    val totalEpisodesWatched: Int,
    val totalChaptersRead: Int,
    val totalTimeSpentMinutes: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalMediaInLibrary: Int,
    @TypeConverters(Converters::class) val mediaByStatus: Map<MediaStatus, Int>,
    @TypeConverters(Converters::class) val mediaByType: Map<MediaType, Int>,
    val averageScore: Float,
    val favoriteCount: Int,
    @TypeConverters(Converters::class) val lastUpdated: LocalDateTime
)

@Entity(tableName = "search_history")
@Serializable
data class SearchHistoryEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val query: String,
    @TypeConverters(Converters::class) val timestamp: LocalDateTime = LocalDateTime.now(),
    val resultCount: Int,
    val sources: List<String>
)

@Entity(tableName = "cache_entry")
@Serializable
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val value: String, // JSON
    @TypeConverters(Converters::class) val expiresAt: LocalDateTime,
    val source: String
)