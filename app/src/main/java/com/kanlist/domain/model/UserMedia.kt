package com.kanlist.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class UserMedia(
    val id: String = java.util.UUID.randomUUID().toString(),
    val mediaId: String,
    val media: Media?,
    val status: MediaStatus = MediaStatus.PLANNING,
    val progress: Int = 0, // episodes watched or chapters read
    val score: Float = 0f, // 0.0 to 5.0 (user rating)
    val isFavorite: Boolean = false,
    val notes: String = "",
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastActivityAt: LocalDateTime = LocalDateTime.now(),
    val rewatchCount: Int = 0,
    val rereadCount: Int = 0,
    val customTitle: String? = null,
    val customCoverUrl: String? = null,
    val episodeOffset: Int = 0, // for offset like starting from episode 5
    val chapterOffset: Int = 0,
    val volumeProgress: Int = 0,
    val timeSpentMinutes: Int = 0,
    val isHidden: Boolean = false,
    val collectionIds: List<String> = emptyList()
) {
    val progressPercentage: Float
        get() {
            val total = media?.totalEpisodesOrChapters ?: 0
            return if (total > 0) (progress.toFloat() / total * 100).coerceAtMost(100f) else 0f
        }

    val remainingEpisodesOrChapters: Int
        get() {
            val total = media?.totalEpisodesOrChapters ?: 0
            return (total - progress).coerceAtLeast(0)
        }

    val estimatedTimeRemaining: String
        get() {
            val remaining = remainingEpisodesOrChapters
            val duration = media?.duration ?: (if (MediaType.isWatchType(media?.type ?: MediaType.ANIME)) 24 else 6)
            val totalMinutes = remaining * duration
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
        }

    val nextEpisodeOrChapterNumber: Int
        get() = progress + 1

    val isCompleted: Boolean
        get() = status == MediaStatus.COMPLETED

    val isInProgress: Boolean
        get() = status == MediaStatus.CURRENT

    val displayStatus: String
        get() = status.getDisplayName(media?.type ?: MediaType.ANIME)

    copy(
        progress = progress,
        status = status,
        score = score,
        isFavorite = isFavorite,
        notes = notes,
        lastActivityAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    ): UserMedia
}

@Serializable
data class Episode(
    val id: String,
    val mediaId: String,
    val number: Int,
    val title: String?,
    val thumbnail: String?,
    val airDate: LocalDate?,
    val duration: Int?,
    val isFiller: Boolean = false,
    val isRecap: Boolean = false,
    val source: MediaSource = MediaSource.ANILIST
)

@Serializable
data class Chapter(
    val id: String,
    val mediaId: String,
    val chapter: String, // can be "1", "1.5", "100"
    val volume: String?,
    val title: String?,
    val language: String,
    val groupName: String?,
    val groupId: String?,
    val pages: Int?,
    val publishAt: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val source: MediaSource = MediaSource.MANGDEX,
    val isExternalUrl: Boolean = false,
    val externalUrl: String? = null
) {
    val chapterNumber: Double
        get() = chapter.toDoubleOrNull() ?: 0.0

    val volumeNumber: Int?
        get() = volume?.toIntOrNull()
}

@Serializable
data class ChapterList(
    val mediaId: String,
    val chapters: List<Chapter>,
    val languages: List<ChapterLanguage>,
    val lastUpdated: LocalDateTime,
    val source: MediaSource
)

@Serializable
data class UserChapterProgress(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userMediaId: String,
    val chapterId: String,
    val chapterNumber: Double,
    val isRead: Boolean = false,
    val readAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class Collection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val color: Int, // Material color
    val icon: String? = null,
    val description: String = "",
    val mediaIds: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val sortOrder: Int = 0
) {
    val mediaCount: Int = mediaIds.size
}

@Serializable
data class ActivityEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userMediaId: String,
    val mediaId: String,
    val type: ActivityType,
    val progress: Int,
    val previousProgress: Int,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val note: String? = null
)

enum class ActivityType {
    EPISODE_WATCHED,
    CHAPTER_READ,
    STATUS_CHANGED,
    SCORE_CHANGED,
    ADDED_TO_LIBRARY,
    REMOVED_FROM_LIBRARY,
    FAVORITED,
    UNFAVORITED,
    NOTE_ADDED,
    PROGRESS_RESET
}

@Serializable
data class DailyGoal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: LocalDate,
    val target: Int = 3, // episodes/chapters per day
    val completed: Int = 0,
    val streak: Int = 0,
    val lastCompletedDate: LocalDate? = null
) {
    val isCompleted: Boolean = completed >= target
    val progressPercentage: Float = if (target > 0) (completed.toFloat() / target * 100).coerceAtMost(100f) else 0f
}

@Serializable
data class UserStats(
    val totalEpisodesWatched: Int = 0,
    val totalChaptersRead: Int = 0,
    val totalTimeSpentMinutes: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalMediaInLibrary: Int = 0,
    val mediaByStatus: Map<MediaStatus, Int> = emptyMap(),
    val mediaByType: Map<MediaType, Int> = emptyMap(),
    val averageScore: Float = 0f,
    val favoriteCount: Int = 0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class UserPreferences(
    val name: String = "",
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val titleLanguage: TitleLanguage = TitleLanguage.ROMAJI,
    val dailyGoalTarget: Int = 3,
    val chapterLanguages: List<ChapterLanguage> = listOf(ChapterLanguage.PT_BR, ChapterLanguage.EN),
    val autoTranslateSynopsis: Boolean = true,
    val hapticFeedback: Boolean = true,
    val showAdultContent: Boolean = false,
    val enabledSources: List<MediaSource> = MediaSource.allEnabled,
    val sortOrder: LibrarySortOrder = LibrarySortOrder.RECENT_ACTIVITY,
    val libraryFilterStatus: MediaStatus? = null,
    val libraryFilterType: MediaType? = null,
    val librarySearchQuery: String = "",
    val compactMode: Boolean = false,
    val reduceMotion: Boolean = false,
    val dataSaverMode: Boolean = false
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class LibrarySortOrder(
    val displayName: String
) {
    RECENT_ACTIVITY("Atividade recente"),
    NAME_A_Z("Nome (A-Z)"),
    NAME_Z_A("Nome (Z-A)"),
    SCORE_HIGH_LOW("Nota (maior)"),
    SCORE_LOW_HIGH("Nota (menor)"),
    PROGRESS_HIGH_LOW("Progresso (maior)"),
    PROGRESS_LOW_HIGH("Progresso (menor)"),
    DATE_ADDED("Data de adição"),
    DATE_STARTED("Iniciado em"),
    DATE_COMPLETED("Finalizado em")
}