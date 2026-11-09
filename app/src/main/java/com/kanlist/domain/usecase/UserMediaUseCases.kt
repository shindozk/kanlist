package com.kanlist.domain.usecase

import com.kanlist.domain.model.ActivityEntry
import com.kanlist.domain.model.Collection
import com.kanlist.domain.model.DailyGoal
import com.kanlist.domain.model.UserChapterProgress
import com.kanlist.domain.model.UserMedia
import com.kanlist.domain.model.UserPreferences
import com.kanlist.domain.model.UserStats
import com.kanlist.domain.repository.UserMediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLibraryUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(): Result<List<UserMedia>> = repository.getAllUserMedia()

    suspend fun getByStatus(status: com.kanlist.domain.model.MediaStatus): Result<List<UserMedia>> =
        repository.getUserMediaByStatus(status)

    suspend fun getByType(type: com.kanlist.domain.model.MediaType): Result<List<UserMedia>> =
        repository.getUserMediaByType(type)

    suspend fun search(query: String): Result<List<UserMedia>> = repository.searchUserMedia(query)

    suspend fun getInProgress(): Result<List<UserMedia>> = repository.getInProgressMedia()

    suspend fun getRecentActivity(limit: Int = 10): Result<List<UserMedia>> = repository.getRecentActivity(limit)

    suspend fun getFavorites(): Result<List<UserMedia>> = repository.getFavorites()

    fun observeAll(): Flow<List<UserMedia>> = repository.observeAllUserMedia()

    fun observeByStatus(status: com.kanlist.domain.model.MediaStatus): Flow<List<UserMedia>> =
        repository.observeUserMediaByStatus(status)

    fun observeInProgress(): Flow<List<UserMedia>> = repository.observeInProgressMedia()

    fun observeFavorites(): Flow<List<UserMedia>> = repository.observeFavorites()
}

class GetUserMediaUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(id: String): Result<UserMedia?> = repository.getUserMediaById(id)

    suspend fun getByMediaId(mediaId: String): Result<UserMedia?> = repository.getUserMediaByMediaId(mediaId)
}

class AddToLibraryUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(userMedia: UserMedia): Result<UserMedia> = repository.insertUserMedia(userMedia)

    suspend fun addFromMedia(
        media: com.kanlist.domain.model.Media,
        status: com.kanlist.domain.model.MediaStatus = com.kanlist.domain.model.MediaStatus.PLANNING
    ): Result<UserMedia> {
        val userMedia = UserMedia(
            mediaId = media.id,
            media = media,
            status = status,
            progress = 0
        )
        return repository.insertUserMedia(userMedia)
    }
}

class UpdateLibraryEntryUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(userMedia: UserMedia): Result<UserMedia> = repository.updateUserMedia(userMedia)

    suspend fun updateProgress(
        id: String,
        progress: Int,
        status: com.kanlist.domain.model.MediaStatus? = null
    ): Result<UserMedia> = repository.updateUserMediaProgress(id, progress, status)

    suspend fun updateScore(id: String, score: Float): Result<UserMedia> = repository.updateUserMediaScore(id, score)

    suspend fun updateNotes(id: String, notes: String): Result<UserMedia> = repository.updateUserMediaNotes(id, notes)

    suspend fun toggleFavorite(id: String): Result<UserMedia> = repository.toggleFavorite(id)

    suspend fun resetProgress(id: String): Result<UserMedia> = repository.resetProgress(id)

    suspend fun addToCollection(userMediaId: String, collectionId: String): Result<UserMedia> =
        repository.addToCollection(userMediaId, collectionId)

    suspend fun removeFromCollection(userMediaId: String, collectionId: String): Result<UserMedia> =
        repository.removeFromCollection(userMediaId, collectionId)
}

class RemoveFromLibraryUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(id: String): Result<Unit> = repository.deleteUserMedia(id)
}

class IncrementProgressUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun invoke(userMedia: UserMedia): Result<UserMedia> {
        val newProgress = userMedia.progress + 1
        val total = userMedia.media?.totalEpisodesOrChapters ?: 0
        val newStatus = if (newProgress >= total && total > 0)
            com.kanlist.domain.model.MediaStatus.COMPLETED
        else
            userMedia.status

        return repository.updateUserMediaProgress(userMedia.id, newProgress, newStatus)
    }
}

class MarkChapterReadUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(userMediaId: String, chapterId: String, chapterNumber: Double): Result<UserChapterProgress> =
        repository.markChapterRead(userMediaId, chapterId, chapterNumber)

    suspend fun markUnread(userMediaId: String, chapterId: String): Result<Unit> =
        repository.markChapterUnread(userMediaId, chapterId)

    suspend fun markUpTo(userMediaId: String, chapterNumber: Double): Result<Int> =
        repository.markChaptersUpTo(userMediaId, chapterNumber)

    suspend fun getReadChapters(userMediaId: String): Result<Set<Double>> =
        repository.getReadChapterNumbers(userMediaId)
}

class GetChapterProgressUseCase @Inject constructor(
    private val repository: UserMediaRepository
) {
    operator fun invoke(userMediaId: String): Result<List<UserChapterProgress>> =
        repository.getChapterProgress(userMediaId)
}

class CollectionUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun getAll(): Result<List<Collection>> = repository.getAllCollections()

    suspend fun getById(id: String): Result<Collection?> = repository.getCollectionById(id)

    suspend fun create(collection: Collection): Result<Collection> = repository.insertCollection(collection)

    suspend fun update(collection: Collection): Result<Collection> = repository.updateCollection(collection)

    suspend fun delete(id: String): Result<Unit> = repository.deleteCollection(id)

    suspend fun addMedia(collectionId: String, mediaId: String): Result<Unit> =
        repository.addMediaToCollection(collectionId, mediaId)

    suspend fun removeMedia(collectionId: String, mediaId: String): Result<Unit> =
        repository.removeMediaFromCollection(collectionId, mediaId)

    fun observe(): Flow<List<Collection>> = repository.observeCollections()
}

class ActivityUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun addEntry(entry: ActivityEntry): Result<ActivityEntry> = repository.addActivityEntry(entry)

    suspend fun getHistory(limit: Int = 50): Result<List<ActivityEntry>> = repository.getActivityHistory(limit)

    suspend fun getForDate(date: java.time.LocalDate): Result<List<ActivityEntry>> = repository.getActivityForDate(date)

    suspend fun getForWeek(startDate: java.time.LocalDate): Result<Map<java.time.LocalDate, List<ActivityEntry>>> =
        repository.getActivityForWeek(startDate)

    fun observeForDate(date: java.time.LocalDate): Flow<List<ActivityEntry>> = repository.observeActivityForDate(date)

    fun observeForWeek(startDate: java.time.LocalDate): Flow<Map<java.time.LocalDate, List<ActivityEntry>>> =
        repository.observeActivityForWeek(startDate)
}

class DailyGoalUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun getForDate(date: java.time.LocalDate): Result<DailyGoal> = repository.getDailyGoal(date)

    suspend fun update(goal: DailyGoal): Result<DailyGoal> = repository.updateDailyGoal(goal)

    suspend fun increment(date: java.time.LocalDate): Result<DailyGoal> = repository.incrementDailyGoal(date)

    fun observeForDate(date: java.time.LocalDate): Flow<DailyGoal> = repository.observeDailyGoal(date)

    fun observeCurrentStreak(): Flow<Int> = repository.observeCurrentStreak()
}

class StatsUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun get(): Result<UserStats> = repository.getUserStats()

    suspend fun recalculate(): Result<UserStats> = repository.recalculateStats()

    fun observe(): Flow<UserStats> = repository.observeUserStats()
}

class PreferencesUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun get(): Result<UserPreferences> = repository.getPreferences()

    suspend fun update(prefs: UserPreferences): Result<UserPreferences> = repository.updatePreferences(prefs)

    suspend fun updateName(name: String): Result<UserPreferences> = repository.updateName(name)

    suspend fun updateTheme(theme: com.kanlist.domain.model.ThemeMode): Result<UserPreferences> = repository.updateTheme(theme)

    suspend fun updateTitleLanguage(lang: com.kanlist.domain.model.TitleLanguage): Result<UserPreferences> = repository.updateTitleLanguage(lang)

    suspend fun updateDailyGoalTarget(target: Int): Result<UserPreferences> = repository.updateDailyGoalTarget(target)

    suspend fun updateChapterLanguages(langs: List<com.kanlist.domain.model.ChapterLanguage>): Result<UserPreferences> =
        repository.updateChapterLanguages(langs)

    suspend fun updateAutoTranslateSynopsis(enabled: Boolean): Result<UserPreferences> = repository.updateAutoTranslateSynopsis(enabled)

    suspend fun updateHapticFeedback(enabled: Boolean): Result<UserPreferences> = repository.updateHapticFeedback(enabled)

    suspend fun updateShowAdultContent(show: Boolean): Result<UserPreferences> = repository.updateShowAdultContent(show)

    suspend fun updateEnabledSources(sources: List<com.kanlist.domain.model.MediaSource>): Result<UserPreferences> =
        repository.updateEnabledSources(sources)

    suspend fun updateSortOrder(order: com.kanlist.domain.model.LibrarySortOrder): Result<UserPreferences> = repository.updateSortOrder(order)

    fun observe(): Flow<UserPreferences> = repository.observePreferences()
}

class BackupUseCases @Inject constructor(
    private val repository: UserMediaRepository
) {
    suspend fun export(): Result<String> = repository.exportBackup()

    suspend fun import(json: String): Result<Unit> = repository.importBackup(json)

    suspend fun clearAllData(): Result<Unit> = repository.clearAllData()

    suspend fun clearCache(): Result<Unit> = repository.clearCache()
}