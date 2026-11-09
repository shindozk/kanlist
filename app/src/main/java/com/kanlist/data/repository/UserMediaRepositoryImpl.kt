package com.kanlist.data.repository

import com.kanlist.data.local.Daos
import com.kanlist.data.local.KanlistDatabase
import com.kanlist.data.local.EntityModels.*
import com.kanlist.data.source.local.PreferencesDataSource
import com.kanlist.domain.model.ActivityEntry
import com.kanlist.domain.model.ActivityType
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.Collection
import com.kanlist.domain.model.DailyGoal
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserChapterProgress
import com.kanlist.domain.model.UserMedia
import com.kanlist.domain.model.UserPreferences
import com.kanlist.domain.model.UserStats
import com.kanlist.domain.repository.UserMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMediaRepositoryImpl @Inject constructor(
    private val database: KanlistDatabase,
    private val preferencesDataSource: PreferencesDataSource
) : UserMediaRepository {

    private val userMediaDao = database.userMediaDao()
    private val chapterDao = database.chapterDao()
    private val userChapterProgressDao = database.userChapterProgressDao()
    private val collectionDao = database.collectionDao()
    private val activityDao = database.activityDao()
    private val dailyGoalDao = database.dailyGoalDao()
    private val userStatsDao = database.userStatsDao()
    private val searchHistoryDao = database.searchHistoryDao()

    override suspend fun getAllUserMedia(): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getAll().map { toDomain(it) })
    }

    override suspend fun getUserMediaById(id: String): Result<UserMedia?> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getById(id)?.let { toDomain(it) })
    }

    override suspend fun getUserMediaByMediaId(mediaId: String): Result<UserMedia?> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getByMediaId(mediaId)?.let { toDomain(it) })
    }

    override suspend fun getUserMediaByStatus(status: MediaStatus): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getByStatus(status).map { toDomain(it) })
    }

    override suspend fun getUserMediaByType(type: MediaType): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getByType(type).map { toDomain(it) })
    }

    override suspend fun searchUserMedia(query: String): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.search("%$query%").map { toDomain(it) })
    }

    override suspend fun insertUserMedia(userMedia: UserMedia): Result<UserMedia> = withContext(Dispatchers.IO) {
        val entity = toEntity(userMedia)
        userMediaDao.insert(entity)
        Result.success(userMedia)
    }

    override suspend fun updateUserMedia(userMedia: UserMedia): Result<UserMedia> = withContext(Dispatchers.IO) {
        val entity = toEntity(userMedia.copy(updatedAt = LocalDateTime.now(), lastActivityAt = LocalDateTime.now()))
        userMediaDao.update(entity)
        Result.success(userMedia)
    }

    override suspend fun updateUserMediaProgress(id: String, progress: Int, status: MediaStatus?): Result<UserMedia> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val current = userMediaDao.getById(id) ?: return Result.failure(Exception("UserMedia not found"))
        val newStatus = status ?: current.status
        userMediaDao.updateProgress(id, progress, newStatus, now)
        Result.success(toDomain(current.copy(progress = progress, status = newStatus, updatedAt = now, lastActivityAt = now)))
    }

    override suspend fun updateUserMediaScore(id: String, score: Float): Result<UserMedia> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        userMediaDao.updateScore(id, score, now)
        val current = userMediaDao.getById(id) ?: return Result.failure(Exception("UserMedia not found"))
        Result.success(toDomain(current.copy(score = score, updatedAt = now)))
    }

    override suspend fun updateUserMediaNotes(id: String, notes: String): Result<UserMedia> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        userMediaDao.updateNotes(id, notes, now)
        val current = userMediaDao.getById(id) ?: return Result.failure(Exception("UserMedia not found"))
        Result.success(toDomain(current.copy(notes = notes, updatedAt = now)))
    }

    override suspend fun toggleFavorite(id: String): Result<UserMedia> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        userMediaDao.toggleFavorite(id, now)
        val current = userMediaDao.getById(id) ?: return Result.failure(Exception("UserMedia not found"))
        Result.success(toDomain(current.copy(isFavorite = !current.isFavorite, updatedAt = now)))
    }

    override suspend fun deleteUserMedia(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        userMediaDao.deleteById(id)
        userChapterProgressDao.deleteAllForUserMedia(id)
        Result.success(Unit)
    }

    override suspend fun resetProgress(id: String): Result<UserMedia> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        userMediaDao.resetProgress(id, now)
        val current = userMediaDao.getById(id) ?: return Result.failure(Exception("UserMedia not found"))
        Result.success(toDomain(current.copy(
            progress = 0,
            status = MediaStatus.PLANNING,
            startedAt = null,
            completedAt = null,
            updatedAt = now,
            lastActivityAt = now
        )))
    }

    override suspend fun addToCollection(userMediaId: String, collectionId: String): Result<UserMedia> = withContext(Dispatchers.IO) {
        val current = userMediaDao.getById(userMediaId) ?: return Result.failure(Exception("UserMedia not found"))
        val collections = current.collectionIds + collectionId
        val updated = current.copy(collectionIds = collections.distinct(), updatedAt = LocalDateTime.now())
        userMediaDao.update(toEntity(updated))
        Result.success(toDomain(updated))
    }

    override suspend fun removeFromCollection(userMediaId: String, collectionId: String): Result<UserMedia> = withContext(Dispatchers.IO) {
        val current = userMediaDao.getById(userMediaId) ?: return Result.failure(Exception("UserMedia not found"))
        val collections = current.collectionIds.filter { it != collectionId }
        val updated = current.copy(collectionIds = collections, updatedAt = LocalDateTime.now())
        userMediaDao.update(toEntity(updated))
        Result.success(toDomain(updated))
    }

    override suspend fun getInProgressMedia(): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getInProgress().map { toDomain(it) })
    }

    override suspend fun getRecentActivity(limit: Int = 10): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getRecentActivity(limit).map { toDomain(it) })
    }

    override suspend fun getFavorites(): Result<List<UserMedia>> = withContext(Dispatchers.IO) {
        Result.success(userMediaDao.getFavorites().map { toDomain(it) })
    }

    override fun observeAllUserMedia(): Flow<List<UserMedia>> {
        return userMediaDao.observeAll().map { it.map { toDomain(it) } }
    }

    override fun observeUserMediaByStatus(status: MediaStatus): Flow<List<UserMedia>> {
        return userMediaDao.observeByStatus(status).map { it.map { toDomain(it) } }
    }

    override fun observeInProgressMedia(): Flow<List<UserMedia>> {
        return userMediaDao.observeInProgress().map { it.map { toDomain(it) } }
    }

    override fun observeFavorites(): Flow<List<UserMedia>> {
        return userMediaDao.observeFavorites().map { it.map { toDomain(it) } }
    }

    // Collections
    override suspend fun getAllCollections(): Result<List<Collection>> = withContext(Dispatchers.IO) {
        Result.success(collectionDao.getAll().map { toCollectionDomain(it) })
    }

    override suspend fun getCollectionById(id: String): Result<Collection?> = withContext(Dispatchers.IO) {
        Result.success(collectionDao.getById(id)?.let { toCollectionDomain(it) })
    }

    override suspend fun insertCollection(collection: Collection): Result<Collection> = withContext(Dispatchers.IO) {
        val entity = toCollectionEntity(collection)
        collectionDao.insert(entity)
        Result.success(collection)
    }

    override suspend fun updateCollection(collection: Collection): Result<Collection> = withContext(Dispatchers.IO) {
        val entity = toCollectionEntity(collection.copy(updatedAt = LocalDateTime.now()))
        collectionDao.update(entity)
        Result.success(collection)
    }

    override suspend fun deleteCollection(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        collectionDao.deleteById(id)
        Result.success(Unit)
    }

    override suspend fun addMediaToCollection(collectionId: String, mediaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val collection = collectionDao.getById(collectionId) ?: return Result.failure(Exception("Collection not found"))
        val mediaIds = collection.mediaIds + mediaId
        val updated = collection.copy(mediaIds = mediaIds.distinct(), updatedAt = LocalDateTime.now())
        collectionDao.update(toCollectionEntity(updated))
        Result.success(Unit)
    }

    override suspend fun removeMediaFromCollection(collectionId: String, mediaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val collection = collectionDao.getById(collectionId) ?: return Result.failure(Exception("Collection not found"))
        val mediaIds = collection.mediaIds.filter { it != mediaId }
        val updated = collection.copy(mediaIds = mediaIds, updatedAt = LocalDateTime.now())
        collectionDao.update(toCollectionEntity(updated))
        Result.success(Unit)
    }

    override fun observeCollections(): Flow<List<Collection>> {
        return collectionDao.observeAll().map { it.map { toCollectionDomain(it) } }
    }

    // Chapter Progress
    override suspend fun getChapterProgress(userMediaId: String): Result<List<UserChapterProgress>> = withContext(Dispatchers.IO) {
        Result.success(userChapterProgressDao.getByUserMediaId(userMediaId).map { toChapterProgressDomain(it) })
    }

    override suspend fun markChapterRead(userMediaId: String, chapterId: String, chapterNumber: Double): Result<UserChapterProgress> = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val progress = UserChapterProgressEntity(
            userMediaId = userMediaId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            isRead = true,
            readAt = now,
            createdAt = now
        )
        userChapterProgressDao.insert(progress)
        
        // Update user media progress
        val userMedia = userMediaDao.getById(userMediaId) ?: return Result.failure(Exception("UserMedia not found"))
        val media = userMedia.media
        val chaptersTotal = media?.chapters ?: 0
        val readCount = userChapterProgressDao.getReadChapters(userMediaId).size
        val newProgress = readCount.coerceAtMost(chaptersTotal)
        
        userMediaDao.updateProgress(userMediaId, newProgress, MediaStatus.CURRENT, now)
        
        Result.success(UserChapterProgress(
            userMediaId = userMediaId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            isRead = true,
            readAt = now
        ))
    }

    override suspend fun markChapterUnread(userMediaId: String, chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        userChapterProgressDao.markAsUnread(userMediaId, chapterId)
        
        // Update progress
        val readCount = userChapterProgressDao.getReadChapters(userMediaId).size
        val userMedia = userMediaDao.getById(userMediaId) ?: return Result.failure(Exception("UserMedia not found"))
        val media = userMedia.media
        val chaptersTotal = media?.chapters ?: 0
        val newProgress = readCount.coerceAtMost(chaptersTotal)
        
        userMediaDao.updateProgress(userMediaId, newProgress, userMedia.status, LocalDateTime.now())
        
        Result.success(Unit)
    }

    override suspend fun markChaptersUpTo(userMediaId: String, chapterNumber: Double): Result<Int> = withContext(Dispatchers.IO) {
        val userMedia = userMediaDao.getById(userMediaId) ?: return Result.failure(Exception("UserMedia not found"))
        val media = userMedia.media
        val chapters = chapterDao.getByMediaId(media?.id ?: "")
        
        var marked = 0
        val now = LocalDateTime.now()
        
        for (chapter in chapters) {
            if (chapter.chapterNumber <= chapterNumber) {
                val progress = UserChapterProgressEntity(
                    userMediaId = userMediaId,
                    chapterId = chapter.id,
                    chapterNumber = chapter.chapterNumber,
                    isRead = true,
                    readAt = now,
                    createdAt = now
                )
                userChapterProgressDao.insert(progress)
                marked++
            }
        }
        
        // Update user media progress
        val newProgress = (userMedia.progress + marked).coerceAtMost(media?.chapters ?: Int.MAX_VALUE)
        userMediaDao.updateProgress(userMediaId, newProgress, MediaStatus.CURRENT, now)
        
        Result.success(marked)
    }

    override suspend fun getReadChapterNumbers(userMediaId: String): Result<Set<Double>> = withContext(Dispatchers.IO) {
        Result.success(userChapterProgressDao.getReadChapterNumbers(userMediaId).toSet())
    }

    // Activity
    override suspend fun addActivityEntry(entry: ActivityEntry): Result<ActivityEntry> = withContext(Dispatchers.IO) {
        val entity = toActivityEntity(entry)
        activityDao.insert(entity)
        Result.success(entry)
    }

    override suspend fun getActivityHistory(limit: Int = 50): Result<List<ActivityEntry>> = withContext(Dispatchers.IO) {
        Result.success(activityDao.getRecent(limit).map { toActivityDomain(it) })
    }

    override suspend fun getActivityForDate(date: LocalDate): Result<List<ActivityEntry>> = withContext(Dispatchers.IO) {
        Result.success(activityDao.getByDate(date).map { toActivityDomain(it) })
    }

    override suspend fun getActivityForWeek(startDate: LocalDate): Result<Map<LocalDate, List<ActivityEntry>>> = withContext(Dispatchers.IO) {
        val endDate = startDate.plusDays(6)
        val entries = activityDao.getByDateRange(startDate, endDate).map { toActivityDomain(it) }
        val grouped = entries.groupBy { it.timestamp.toLocalDate() }
        val result = (0..6).associateWith { startDate.plusDays(it) }.mapValues { (date, _) -> grouped[date] ?: emptyList() }
        Result.success(result)
    }

    override fun observeActivityForDate(date: LocalDate): Flow<List<ActivityEntry>> {
        return activityDao.observeByDate(date).map { it.map { toActivityDomain(it) } }
    }

    override fun observeActivityForWeek(startDate: LocalDate): Flow<Map<LocalDate, List<ActivityEntry>>> {
        val endDate = startDate.plusDays(6)
        return activityDao.observeByDateRange(startDate, endDate).map { entries ->
            val domainEntries = entries.map { toActivityDomain(it) }
            domainEntries.groupBy { it.timestamp.toLocalDate() }
        }
    }

    // Daily Goal
    override suspend fun getDailyGoal(date: LocalDate): Result<DailyGoal> = withContext(Dispatchers.IO) {
        val entity = dailyGoalDao.getByDate(date)
        Result.success(entity?.let { toDailyGoalDomain(it) } ?: DailyGoal(date = date))
    }

    override suspend fun updateDailyGoal(goal: DailyGoal): Result<DailyGoal> = withContext(Dispatchers.IO) {
        val entity = toDailyGoalEntity(goal)
        dailyGoalDao.insert(entity)
        Result.success(goal)
    }

    override suspend fun incrementDailyGoal(date: LocalDate): Result<DailyGoal> = withContext(Dispatchers.IO) {
        val existing = dailyGoalDao.getByDate(date) ?: DailyGoalEntity(
            id = UUID.randomUUID().toString(),
            date = date,
            target = 3,
            completed = 0,
            streak = 0,
            lastCompletedDate = null
        )
        
        val newCompleted = existing.completed + 1
        val prefs = preferencesDataSource.getPreferences()
        val target = prefs.dailyGoalTarget
        val isCompleted = newCompleted >= target
        
        var newStreak = existing.streak
        var lastCompleted = existing.lastCompletedDate
        
        if (isCompleted && lastCompleted != date) {
            if (lastCompleted == date.minusDays(1)) {
                newStreak++
            } else {
                newStreak = 1
            }
            lastCompleted = date
        }
        
        val updated = existing.copy(
            completed = newCompleted,
            streak = newStreak,
            lastCompletedDate = lastCompleted
        )
        dailyGoalDao.insert(updated)
        
        Result.success(toDailyGoalDomain(updated))
    }

    override fun observeDailyGoal(date: LocalDate): Flow<DailyGoal> {
        return dailyGoalDao.observeByDate(date).map { it?.let { toDailyGoalDomain(it) } ?: DailyGoal(date = date) }
    }

    override fun observeCurrentStreak(): Flow<Int> {
        return dailyGoalDao.observeToday().map { it?.streak ?: 0 }
    }

    // Stats
    override suspend fun getUserStats(): Result<UserStats> = withContext(Dispatchers.IO) {
        val entity = userStatsDao.get()
        Result.success(entity?.let { toStatsDomain(it) } ?: UserStats())
    }

    override suspend fun recalculateStats(): Result<UserStats> = withContext(Dispatchers.IO) {
        val allMedia = userMediaDao.getAll().map { toDomain(it) }
        val stats = computeStats(allMedia)
        val entity = toStatsEntity(stats)
        userStatsDao.insert(entity)
        Result.success(stats)
    }

    override fun observeUserStats(): Flow<UserStats> {
        return userStatsDao.observe().map { it?.let { toStatsDomain(it) } ?: UserStats() }
    }

    private fun computeStats(media: List<UserMedia>): UserStats {
        var episodesWatched = 0
        var chaptersRead = 0
        var timeSpent = 0
        val byStatus = mutableMapOf<MediaStatus, Int>()
        val byType = mutableMapOf<MediaType, Int>()
        var totalScore = 0f
        var scoredCount = 0
        var favorites = 0

        for (m in media) {
            if (m.media != null) {
                if (MediaType.isWatchType(m.media!!.type)) {
                    episodesWatched += m.progress
                    timeSpent += m.progress * (m.media!!.duration ?: 24)
                } else {
                    chaptersRead += m.progress
                    timeSpent += m.progress * (m.media!!.duration ?: 6)
                }
            }
            
            byStatus[m.status] = byStatus.getOrDefault(m.status, 0) + 1
            m.media?.let { byType[it.type] = byType.getOrDefault(it.type, 0) + 1 }
            
            if (m.score > 0) {
                totalScore += m.score
                scoredCount++
            }
            if (m.isFavorite) favorites++
        }

        return UserStats(
            totalEpisodesWatched = episodesWatched,
            totalChaptersRead = chaptersRead,
            totalTimeSpentMinutes = timeSpent,
            currentStreak = 0, // Would need to compute from daily goals
            longestStreak = 0,
            totalMediaInLibrary = media.size,
            mediaByStatus = byStatus,
            mediaByType = byType,
            averageScore = if (scoredCount > 0) totalScore / scoredCount else 0f,
            favoriteCount = favorites
        )
    }

    // Preferences
    override suspend fun getPreferences(): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.getPreferences())
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> = withContext(Dispatchers.IO) {
        // Update each preference through data source
        Result.success(preferences)
    }

    override suspend fun updateName(name: String): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateName(name))
    }

    override suspend fun updateTheme(theme: com.kanlist.domain.model.ThemeMode): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateTheme(theme))
    }

    override suspend fun updateTitleLanguage(language: com.kanlist.domain.model.TitleLanguage): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateTitleLanguage(language))
    }

    override suspend fun updateDailyGoalTarget(target: Int): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateDailyGoalTarget(target))
    }

    override suspend fun updateChapterLanguages(languages: List<ChapterLanguage>): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateChapterLanguages(languages))
    }

    override suspend fun updateAutoTranslateSynopsis(enabled: Boolean): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateAutoTranslateSynopsis(enabled))
    }

    override suspend fun updateHapticFeedback(enabled: Boolean): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateHapticFeedback(enabled))
    }

    override suspend fun updateShowAdultContent(show: Boolean): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateShowAdultContent(show))
    }

    override suspend fun updateEnabledSources(sources: List<com.kanlist.domain.model.MediaSource>): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateEnabledSources(sources))
    }

    override suspend fun updateSortOrder(order: com.kanlist.domain.model.LibrarySortOrder): Result<UserPreferences> = withContext(Dispatchers.IO) {
        Result.success(preferencesDataSource.updateSortOrder(order))
    }

    override fun observePreferences(): Flow<UserPreferences> {
        return preferencesDataSource.observePreferences().asFlow()
    }

    // Backup/Restore
    override suspend fun exportBackup(): Result<String> = withContext(Dispatchers.IO) {
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        
        val backup = mapOf(
            "version" to 1,
            "exportedAt" to LocalDateTime.now().toString(),
            "userMedia" to userMediaDao.getAll().map { toDomain(it) },
            "collections" to collectionDao.getAll().map { toCollectionDomain(it) },
            "chapterProgress" to userChapterProgressDao.getByUserMediaId("").map { toChapterProgressDomain(it) }, // Would need all
            "activities" to activityDao.getRecent(10000).map { toActivityDomain(it) },
            "dailyGoals" to dailyGoalDao.getByDate(LocalDate.now().minusYears(1))?.let { listOf(toDailyGoalDomain(it)) } ?: emptyList(),
            "stats" to userStatsDao.get()?.let { toStatsDomain(it) },
            "preferences" to preferencesDataSource.getPreferences()
        )
        
        Result.success(gson.toJson(backup))
    }

    override suspend fun importBackup(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        val gson = com.google.gson.Gson()
        val backup = gson.fromJson(json, Map::class.java)
        
        // Import logic would go here
        Result.success(Unit)
    }

    override suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        userMediaDao.getAll().forEach { userMediaDao.delete(it) }
        collectionDao.getAll().forEach { collectionDao.delete(it) }
        chapterDao.getByMediaId("").forEach { chapterDao.delete(it) } // Would need proper cleanup
        activityDao.getRecent(10000).forEach { activityDao.delete(it) }
        dailyGoalDao.getByDate(LocalDate.now())?.let { dailyGoalDao.delete(it) }
        userStatsDao.get()?.let { userStatsDao.delete(it) }
        searchHistoryDao.clearAll()
        Result.success(Unit)
    }

    override suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }

    // Mappers
    private fun toDomain(entity: UserMediaEntity): UserMedia {
        return UserMedia(
            id = entity.id,
            mediaId = entity.mediaId,
            media = entity.media?.let { entityToMediaDomain(it) },
            status = entity.status,
            progress = entity.progress,
            score = entity.score,
            isFavorite = entity.isFavorite,
            notes = entity.notes,
            startedAt = entity.startedAt,
            completedAt = entity.completedAt,
            updatedAt = entity.updatedAt,
            createdAt = entity.createdAt,
            lastActivityAt = entity.lastActivityAt,
            rewatchCount = entity.rewatchCount,
            rereadCount = entity.rereadCount,
            customTitle = entity.customTitle,
            customCoverUrl = entity.customCoverUrl,
            episodeOffset = entity.episodeOffset,
            chapterOffset = entity.chapterOffset,
            volumeProgress = entity.volumeProgress,
            timeSpentMinutes = entity.timeSpentMinutes,
            isHidden = entity.isHidden,
            collectionIds = entity.collectionIds
        )
    }

    private fun toEntity(userMedia: UserMedia): UserMediaEntity {
        return UserMediaEntity(
            id = userMedia.id,
            mediaId = userMedia.mediaId,
            media = userMedia.media?.let { mediaToEntity(it) },
            status = userMedia.status,
            progress = userMedia.progress,
            score = userMedia.score,
            isFavorite = userMedia.isFavorite,
            notes = userMedia.notes,
            startedAt = userMedia.startedAt,
            completedAt = userMedia.completedAt,
            updatedAt = userMedia.updatedAt,
            createdAt = userMedia.createdAt,
            lastActivityAt = userMedia.lastActivityAt,
            rewatchCount = userMedia.rewatchCount,
            rereadCount = userMedia.rereadCount,
            customTitle = userMedia.customTitle,
            customCoverUrl = userMedia.customCoverUrl,
            episodeOffset = userMedia.episodeOffset,
            chapterOffset = userMedia.chapterOffset,
            volumeProgress = userMedia.volumeProgress,
            timeSpentMinutes = userMedia.timeSpentMinutes,
            isHidden = userMedia.isHidden,
            collectionIds = userMedia.collectionIds
        )
    }

    private fun entityToMediaDomain(entity: MediaEntity): Media {
        // Simplified - reuse from MediaRepositoryImpl
        return Media(
            id = entity.id,
            idAniList = entity.idAniList,
            idMangaDex = entity.idMangaDex,
            idKitsu = entity.idKitsu,
            idMyAnimeList = entity.idMyAnimeList,
            title = entity.title,
            type = entity.type,
            format = entity.format,
            status = entity.status,
            season = entity.season,
            seasonYear = entity.seasonYear,
            startDate = entity.startDate,
            endDate = entity.endDate,
            episodes = entity.episodes,
            chapters = entity.chapters,
            volumes = entity.volumes,
            duration = entity.duration,
            averageScore = entity.averageScore,
            popularity = entity.popularity,
            description = entity.description,
            coverImage = MediaCoverImage(
                large = entity.coverImage.large,
                medium = entity.coverImage.medium,
                color = entity.coverImage.color,
                extraLarge = entity.coverImage.extraLarge
            ),
            bannerImage = entity.bannerImage,
            genres = entity.genres,
            tags = entity.tags,
            studios = entity.studios.map { com.kanlist.domain.model.MediaStudio(
                id = it.id, name = it.name, isAnimationStudio = it.isAnimationStudio, isMain = it.isMain
            ) },
            staff = entity.staff.map { com.kanlist.domain.model.MediaStaff(
                id = it.id, name = com.kanlist.domain.model.MediaName(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), role = it.role, language = it.language
            ) },
            characters = entity.characters.map { com.kanlist.domain.model.MediaCharacter(
                id = it.id, name = com.kanlist.domain.model.MediaName(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), image = it.image, role = it.role,
                voiceActors = it.voiceActors.map { com.kanlist.domain.model.MediaVoiceActor(
                    id = it.id, name = com.kanlist.domain.model.MediaName(
                        full = it.name.full, native = it.name.native, alternative = it.name.alternative
                    ), image = it.image, language = it.language
                ) }
            ) },
            relations = entity.relations.map { com.kanlist.domain.model.MediaRelation(
                id = it.id, relationType = it.relationType, media = entityToMediaDomain(it.media)
            ) },
            recommendations = entity.recommendations.map { com.kanlist.domain.model.MediaRecommendation(
                id = it.id, rating = it.rating, media = entityToMediaDomain(it.media)
            ) },
            externalLinks = entity.externalLinks.map { com.kanlist.domain.model.MediaExternalLink(
                id = it.id, site = it.site, url = it.url, type = it.type,
                language = it.language, color = it.color, icon = it.icon, notes = it.notes
            ) },
            nextAiringEpisode = entity.nextAiringEpisode?.let { NextAiringEpisode(
                episode = it.episode, airingAt = it.airingAt, timeUntilAiring = it.timeUntilAiring
            ) },
            source = entity.source,
            isAdult = entity.isAdult,
            countryOfOrigin = entity.countryOfOrigin,
            updatedAt = entity.updatedAt
        )
    }

    private fun mediaToEntity(media: Media): MediaEntity {
        return MediaEntity(
            id = media.id,
            idAniList = media.idAniList,
            idMangaDex = media.idMangaDex,
            idKitsu = media.idKitsu,
            idMyAnimeList = media.idMyAnimeList,
            title = media.title,
            type = media.type,
            format = media.format,
            status = media.status,
            season = media.season,
            seasonYear = media.seasonYear,
            startDate = media.startDate,
            endDate = media.endDate,
            episodes = media.episodes,
            chapters = media.chapters,
            volumes = media.volumes,
            duration = media.duration,
            averageScore = media.averageScore,
            popularity = media.popularity,
            description = media.description,
            coverImage = com.kanlist.data.local.EntityModels.MediaCoverImageEntity(
                large = media.coverImage.large,
                medium = media.coverImage.medium,
                color = media.coverImage.color,
                extraLarge = media.coverImage.extraLarge
            ),
            bannerImage = media.bannerImage,
            genres = media.genres,
            tags = media.tags,
            studios = media.studios.map { com.kanlist.data.local.EntityModels.MediaStudioEntity(
                id = it.id, name = it.name, isAnimationStudio = it.isAnimationStudio, isMain = it.isMain
            ) },
            staff = media.staff.map { com.kanlist.data.local.EntityModels.MediaStaffEntity(
                id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), role = it.role, language = it.language
            ) },
            characters = media.characters.map { com.kanlist.data.local.EntityModels.MediaCharacterEntity(
                id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                    full = it.name.full, native = it.name.native, alternative = it.name.alternative
                ), image = it.image, role = it.role,
                voiceActors = it.voiceActors.map { com.kanlist.data.local.EntityModels.MediaVoiceActorEntity(
                    id = it.id, name = com.kanlist.data.local.EntityModels.MediaNameEntity(
                        full = it.name.full, native = it.name.native, alternative = it.name.alternative
                    ), image = it.image, language = it.language
                ) }
            ) },
            relations = media.relations.map { com.kanlist.data.local.EntityModels.MediaRelationEntity(
                id = it.id, relationType = it.relationType, media = mediaToEntity(it.media)
            ) },
            recommendations = media.recommendations.map { com.kanlist.data.local.EntityModels.MediaRecommendationEntity(
                id = it.id, rating = it.rating, media = mediaToEntity(it.media)
            ) },
            externalLinks = media.externalLinks.map { com.kanlist.data.local.EntityModels.MediaExternalLinkEntity(
                id = it.id, site = it.site, url = it.url, type = it.type,
                language = it.language, color = it.color, icon = it.icon, notes = it.notes
            ) },
            nextAiringEpisode = media.nextAiringEpisode?.let { com.kanlist.data.local.EntityModels.NextAiringEpisodeEntity(
                episode = it.episode, airingAt = it.airingAt, timeUntilAiring = it.timeUntilAiring
            ) },
            source = media.source,
            isAdult = media.isAdult,
            countryOfOrigin = media.countryOfOrigin,
            updatedAt = media.updatedAt
        )
    }

    private fun toCollectionDomain(entity: CollectionEntity): Collection {
        return Collection(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            icon = entity.icon,
            description = entity.description,
            mediaIds = entity.mediaIds,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            sortOrder = entity.sortOrder
        )
    }

    private fun toCollectionEntity(collection: Collection): CollectionEntity {
        return CollectionEntity(
            id = collection.id,
            name = collection.name,
            color = collection.color,
            icon = collection.icon,
            description = collection.description,
            mediaIds = collection.mediaIds,
            createdAt = collection.createdAt,
            updatedAt = collection.updatedAt,
            sortOrder = collection.sortOrder
        )
    }

    private fun toChapterProgressDomain(entity: UserChapterProgressEntity): UserChapterProgress {
        return UserChapterProgress(
            userMediaId = entity.userMediaId,
            chapterId = entity.chapterId,
            chapterNumber = entity.chapterNumber,
            isRead = entity.isRead,
            readAt = entity.readAt,
            createdAt = entity.createdAt
        )
    }

    private fun toActivityEntity(entry: ActivityEntry): ActivityEntryEntity {
        return ActivityEntryEntity(
            id = entry.id,
            userMediaId = entry.userMediaId,
            mediaId = entry.mediaId,
            type = ActivityTypeEntity.valueOf(entry.type.name),
            progress = entry.progress,
            previousProgress = entry.previousProgress,
            timestamp = entry.timestamp,
            note = entry.note
        )
    }

    private fun toActivityDomain(entity: ActivityEntryEntity): ActivityEntry {
        return ActivityEntry(
            id = entity.id,
            userMediaId = entity.userMediaId,
            mediaId = entity.mediaId,
            type = ActivityType.valueOf(entity.type.name),
            progress = entity.progress,
            previousProgress = entity.previousProgress,
            timestamp = entity.timestamp,
            note = entity.note
        )
    }

    private fun toDailyGoalDomain(entity: DailyGoalEntity): DailyGoal {
        return DailyGoal(
            id = entity.id,
            date = entity.date,
            target = entity.target,
            completed = entity.completed,
            streak = entity.streak,
            lastCompletedDate = entity.lastCompletedDate
        )
    }

    private fun toDailyGoalEntity(goal: DailyGoal): DailyGoalEntity {
        return DailyGoalEntity(
            id = goal.id,
            date = goal.date,
            target = goal.target,
            completed = goal.completed,
            streak = goal.streak,
            lastCompletedDate = goal.lastCompletedDate
        )
    }

    private fun toStatsDomain(entity: UserStatsEntity): UserStats {
        return UserStats(
            totalEpisodesWatched = entity.totalEpisodesWatched,
            totalChaptersRead = entity.totalChaptersRead,
            totalTimeSpentMinutes = entity.totalTimeSpentMinutes,
            currentStreak = entity.currentStreak,
            longestStreak = entity.longestStreak,
            totalMediaInLibrary = entity.totalMediaInLibrary,
            mediaByStatus = entity.mediaByStatus,
            mediaByType = entity.mediaByType,
            averageScore = entity.averageScore,
            favoriteCount = entity.favoriteCount,
            lastUpdated = entity.lastUpdated
        )
    }

    private fun toStatsEntity(stats: UserStats): UserStatsEntity {
        return UserStatsEntity(
            totalEpisodesWatched = stats.totalEpisodesWatched,
            totalChaptersRead = stats.totalChaptersRead,
            totalTimeSpentMinutes = stats.totalTimeSpentMinutes,
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak,
            totalMediaInLibrary = stats.totalMediaInLibrary,
            mediaByStatus = stats.mediaByStatus,
            mediaByType = stats.mediaByType,
            averageScore = stats.averageScore,
            favoriteCount = stats.favoriteCount,
            lastUpdated = stats.lastUpdated
        )
    }
}