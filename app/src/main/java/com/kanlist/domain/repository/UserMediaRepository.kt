package com.kanlist.domain.repository

import com.kanlist.domain.model.ActivityEntry
import com.kanlist.domain.model.Collection
import com.kanlist.domain.model.DailyGoal
import com.kanlist.domain.model.UserChapterProgress
import com.kanlist.domain.model.UserMedia
import com.kanlist.domain.model.UserPreferences
import com.kanlist.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface UserMediaRepository {
    // User Media (Library)
    suspend fun getAllUserMedia(): Result<List<UserMedia>>

    suspend fun getUserMediaById(id: String): Result<UserMedia?>

    suspend fun getUserMediaByMediaId(mediaId: String): Result<UserMedia?>

    suspend fun getUserMediaByStatus(status: com.kanlist.domain.model.MediaStatus): Result<List<UserMedia>>

    suspend fun getUserMediaByType(type: com.kanlist.domain.model.MediaType): Result<List<UserMedia>>

    suspend fun searchUserMedia(query: String): Result<List<UserMedia>>

    suspend fun insertUserMedia(userMedia: UserMedia): Result<UserMedia>

    suspend fun updateUserMedia(userMedia: UserMedia): Result<UserMedia>

    suspend fun updateUserMediaProgress(id: String, progress: Int, status: com.kanlist.domain.model.MediaStatus? = null): Result<UserMedia>

    suspend fun updateUserMediaScore(id: String, score: Float): Result<UserMedia>

    suspend fun updateUserMediaNotes(id: String, notes: String): Result<UserMedia>

    suspend fun toggleFavorite(id: String): Result<UserMedia>

    suspend fun deleteUserMedia(id: String): Result<Unit>

    suspend fun resetProgress(id: String): Result<UserMedia>

    suspend fun addToCollection(userMediaId: String, collectionId: String): Result<UserMedia>

    suspend fun removeFromCollection(userMediaId: String, collectionId: String): Result<UserMedia>

    suspend fun getInProgressMedia(): Result<List<UserMedia>>

    suspend fun getRecentActivity(limit: Int = 10): Result<List<UserMedia>>

    suspend fun getFavorites(): Result<List<UserMedia>>

    // Observables
    fun observeAllUserMedia(): Flow<List<UserMedia>>

    fun observeUserMediaByStatus(status: com.kanlist.domain.model.MediaStatus): Flow<List<UserMedia>>

    fun observeInProgressMedia(): Flow<List<UserMedia>>

    fun observeFavorites(): Flow<List<UserMedia>>

    // Collections
    suspend fun getAllCollections(): Result<List<Collection>>

    suspend fun getCollectionById(id: String): Result<Collection?>

    suspend fun insertCollection(collection: Collection): Result<Collection>

    suspend fun updateCollection(collection: Collection): Result<Collection>

    suspend fun deleteCollection(id: String): Result<Unit>

    suspend fun addMediaToCollection(collectionId: String, mediaId: String): Result<Unit>

    suspend fun removeMediaFromCollection(collectionId: String, mediaId: String): Result<Unit>

    fun observeCollections(): Flow<List<Collection>>

    // Chapter Progress
    suspend fun getChapterProgress(userMediaId: String): Result<List<UserChapterProgress>>

    suspend fun markChapterRead(userMediaId: String, chapterId: String, chapterNumber: Double): Result<UserChapterProgress>

    suspend fun markChapterUnread(userMediaId: String, chapterId: String): Result<Unit>

    suspend fun markChaptersUpTo(userMediaId: String, chapterNumber: Double): Result<Int>

    suspend fun getReadChapterNumbers(userMediaId: String): Result<Set<Double>>

    // Activity
    suspend fun addActivityEntry(entry: ActivityEntry): Result<ActivityEntry>

    suspend fun getActivityHistory(limit: Int = 50): Result<List<ActivityEntry>>

    suspend fun getActivityForDate(date: java.time.LocalDate): Result<List<ActivityEntry>>

    suspend fun getActivityForWeek(startDate: java.time.LocalDate): Result<Map<java.time.LocalDate, List<ActivityEntry>>>

    fun observeActivityForDate(date: java.time.LocalDate): Flow<List<ActivityEntry>>

    fun observeActivityForWeek(startDate: java.time.LocalDate): Flow<Map<java.time.LocalDate, List<ActivityEntry>>>

    // Daily Goal
    suspend fun getDailyGoal(date: java.time.LocalDate): Result<DailyGoal>

    suspend fun updateDailyGoal(goal: DailyGoal): Result<DailyGoal>

    suspend fun incrementDailyGoal(date: java.time.LocalDate): Result<DailyGoal>

    fun observeDailyGoal(date: java.time.LocalDate): Flow<DailyGoal>

    fun observeCurrentStreak(): Flow<Int>

    // Stats
    suspend fun getUserStats(): Result<UserStats>

    suspend fun recalculateStats(): Result<UserStats>

    fun observeUserStats(): Flow<UserStats>

    // Preferences
    suspend fun getPreferences(): Result<UserPreferences>

    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences>

    suspend fun updateName(name: String): Result<UserPreferences>

    suspend fun updateTheme(theme: com.kanlist.domain.model.ThemeMode): Result<UserPreferences>

    suspend fun updateTitleLanguage(language: com.kanlist.domain.model.TitleLanguage): Result<UserPreferences>

    suspend fun updateDailyGoalTarget(target: Int): Result<UserPreferences>

    suspend fun updateChapterLanguages(languages: List<com.kanlist.domain.model.ChapterLanguage>): Result<UserPreferences>

    suspend fun updateAutoTranslateSynopsis(enabled: Boolean): Result<UserPreferences>

    suspend fun updateHapticFeedback(enabled: Boolean): Result<UserPreferences>

    suspend fun updateShowAdultContent(show: Boolean): Result<UserPreferences>

    suspend fun updateEnabledSources(sources: List<com.kanlist.domain.model.MediaSource>): Result<UserPreferences>

    suspend fun updateSortOrder(order: com.kanlist.domain.model.LibrarySortOrder): Result<UserPreferences>

    fun observePreferences(): Flow<UserPreferences>

    // Backup/Restore
    suspend fun exportBackup(): Result<String> // JSON string

    suspend fun importBackup(json: String): Result<Unit>

    suspend fun clearAllData(): Result<Unit>

    suspend fun clearCache(): Result<Unit>
}