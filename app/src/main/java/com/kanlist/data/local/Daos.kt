package com.kanlist.data.local

import androidx.room.*
import com.kanlist.data.local.EntityModels.*
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<MediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity)

    @Update
    suspend fun update(media: MediaEntity)

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("DELETE FROM media WHERE cachedAt < :threshold")
    suspend fun deleteOldCache(threshold: Long)

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getById(id: String): MediaEntity?

    @Query("SELECT * FROM media WHERE idAniList = :id")
    suspend fun getByAniListId(id: Int): MediaEntity?

    @Query("SELECT * FROM media WHERE idMangaDex = :id")
    suspend fun getByMangaDexId(id: String): MediaEntity?

    @Query("SELECT * FROM media WHERE idKitsu = :id")
    suspend fun getByKitsuId(id: Int): MediaEntity?

    @Query("SELECT * FROM media WHERE idMyAnimeList = :id")
    suspend fun getByMyAnimeListId(id: Int): MediaEntity?

    @Query("SELECT * FROM media WHERE type = :type ORDER BY popularity DESC NULLS LAST LIMIT :limit OFFSET :offset")
    suspend fun getByType(type: MediaType, limit: Int, offset: Int): List<MediaEntity>

    @Query("SELECT * FROM media WHERE source = :source ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getBySource(source: String, limit: Int, offset: Int): List<MediaEntity>

    @Query("SELECT * FROM media WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<MediaEntity>

    @Query("SELECT * FROM media WHERE (title->>'$.romaji' LIKE :query OR title->>'$.english' LIKE :query OR title->>'$.native' LIKE :query) AND isAdult = 0 LIMIT :limit")
    suspend fun search(query: String, limit: Int): List<MediaEntity>

    @Query("SELECT COUNT(*) FROM media")
    suspend fun getCount(): Int
}

@Dao
interface UserMediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(userMedia: List<UserMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userMedia: UserMediaEntity)

    @Update
    suspend fun update(userMedia: UserMediaEntity)

    @Update
    suspend fun updateAll(userMedia: List<UserMediaEntity>)

    @Delete
    suspend fun delete(userMedia: UserMediaEntity)

    @Query("DELETE FROM user_media WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM user_media WHERE id = :id")
    suspend fun getById(id: String): UserMediaEntity?

    @Query("SELECT * FROM user_media WHERE mediaId = :mediaId")
    suspend fun getByMediaId(mediaId: String): UserMediaEntity?

    @Query("SELECT * FROM user_media WHERE status = :status ORDER BY lastActivityAt DESC")
    suspend fun getByStatus(status: MediaStatus): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE (media->>'$.type') = :type ORDER BY lastActivityAt DESC")
    suspend fun getByType(type: MediaType): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE isFavorite = 1 ORDER BY lastActivityAt DESC")
    suspend fun getFavorites(): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE status = 'CURRENT' ORDER BY lastActivityAt DESC")
    suspend fun getInProgress(): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE isHidden = 0 ORDER BY lastActivityAt DESC LIMIT :limit")
    suspend fun getRecentActivity(limit: Int): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE isHidden = 0 ORDER BY lastActivityAt DESC")
    suspend fun getAll(): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE (media->>'$.title'->>'$.romaji' LIKE :query OR media->>'$.title'->>'$.english' LIKE :query OR media->>'$.title'->>'$.native' LIKE :query) AND isHidden = 0")
    suspend fun search(query: String): List<UserMediaEntity>

    @Query("SELECT * FROM user_media WHERE :collectionId IN (SELECT value FROM json_each(collectionIds)) ORDER BY lastActivityAt DESC")
    suspend fun getByCollectionId(collectionId: String): List<UserMediaEntity>

    @RawQuery(observedEntities = [UserMediaEntity::class])
    fun observeAll(): Flow<List<UserMediaEntity>>

    @RawQuery(observedEntities = [UserMediaEntity::class])
    fun observeByStatus(status: MediaStatus): Flow<List<UserMediaEntity>>

    @RawQuery(observedEntities = [UserMediaEntity::class])
    fun observeInProgress(): Flow<List<UserMediaEntity>>

    @RawQuery(observedEntities = [UserMediaEntity::class])
    fun observeFavorites(): Flow<List<UserMediaEntity>>

    @Query("UPDATE user_media SET progress = :progress, status = :status, lastActivityAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, status: MediaStatus, now: LocalDateTime): Int

    @Query("UPDATE user_media SET score = :score, updatedAt = :now WHERE id = :id")
    suspend fun updateScore(id: String, score: Float, now: LocalDateTime): Int

    @Query("UPDATE user_media SET notes = :notes, updatedAt = :now WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String, now: LocalDateTime): Int

    @Query("UPDATE user_media SET isFavorite = NOT isFavorite, updatedAt = :now WHERE id = :id")
    suspend fun toggleFavorite(id: String, now: LocalDateTime): Int

    @Query("UPDATE user_media SET progress = 0, status = 'PLANNING', startedAt = null, completedAt = null, updatedAt = :now, lastActivityAt = :now WHERE id = :id")
    suspend fun resetProgress(id: String, now: LocalDateTime): Int

    @Query("UPDATE user_media SET collectionIds = json_set(collectionIds, '$', json_array(:collectionId)) WHERE id = :id AND json_type(collectionIds) = 'array' AND json_array_length(collectionIds) = 0")
    suspend fun addToCollectionEmpty(id: String, collectionId: String): Int

    @Query("UPDATE user_media SET collectionIds = json_insert(collectionIds, '$[#]', :collectionId) WHERE id = :id AND :collectionId NOT IN (SELECT value FROM json_each(collectionIds))")
    suspend fun addToCollection(id: String, collectionId: String): Int

    @Query("UPDATE user_media SET collectionIds = json_remove(collectionIds, '$[#]') WHERE id = :id AND :collectionId IN (SELECT value FROM json_each(collectionIds))")
    suspend fun removeFromCollection(id: String, collectionId: String): Int
}

@Dao
interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: EpisodeEntity)

    @Query("SELECT * FROM episode WHERE mediaId = :mediaId ORDER BY number ASC")
    suspend fun getByMediaId(mediaId: String): List<EpisodeEntity>

    @Query("SELECT * FROM episode WHERE mediaId = :mediaId AND number = :number")
    suspend fun getByMediaIdAndNumber(mediaId: String, number: Int): EpisodeEntity?

    @Query("DELETE FROM episode WHERE mediaId = :mediaId")
    suspend fun deleteByMediaId(mediaId: String): Int
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: ChapterEntity)

    @Query("SELECT * FROM chapter WHERE mediaId = :mediaId AND language IN (:languages) ORDER BY volumeNumber ASC, chapterNumber ASC")
    suspend fun getByMediaIdAndLanguages(mediaId: String, languages: List<String>): List<ChapterEntity>

    @Query("SELECT * FROM chapter WHERE mediaId = :mediaId ORDER BY volumeNumber ASC, chapterNumber ASC")
    suspend fun getByMediaId(mediaId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapter WHERE mediaId = :mediaId AND chapter = :chapter AND language = :language")
    suspend fun getByMediaIdChapterLanguage(mediaId: String, chapter: String, language: String): ChapterEntity?

    @Query("DELETE FROM chapter WHERE mediaId = :mediaId")
    suspend fun deleteByMediaId(mediaId: String): Int

    @Query("SELECT DISTINCT language FROM chapter WHERE mediaId = :mediaId")
    suspend fun getAvailableLanguages(mediaId: String): List<String>

    @Query("SELECT DISTINCT groupName FROM chapter WHERE mediaId = :mediaId AND groupName IS NOT NULL")
    suspend fun getScanlationGroups(mediaId: String): List<String>
}

@Dao
interface UserChapterProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progress: List<UserChapterProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserChapterProgressEntity)

    @Update
    suspend fun update(progress: UserChapterProgressEntity)

    @Delete
    suspend fun delete(progress: UserChapterProgressEntity)

    @Query("DELETE FROM user_chapter_progress WHERE userMediaId = :userMediaId AND chapterId = :chapterId")
    suspend fun deleteByUserMediaAndChapter(userMediaId: String, chapterId: String): Int

    @Query("SELECT * FROM user_chapter_progress WHERE userMediaId = :userMediaId")
    suspend fun getByUserMediaId(userMediaId: String): List<UserChapterProgressEntity>

    @Query("SELECT * FROM user_chapter_progress WHERE userMediaId = :userMediaId AND isRead = 1")
    suspend fun getReadChapters(userMediaId: String): List<UserChapterProgressEntity>

    @Query("SELECT chapterNumber FROM user_chapter_progress WHERE userMediaId = :userMediaId AND isRead = 1")
    suspend fun getReadChapterNumbers(userMediaId: String): List<Double>

    @Query("SELECT * FROM user_chapter_progress WHERE userMediaId = :userMediaId AND chapterId = :chapterId")
    suspend fun getByUserMediaAndChapter(userMediaId: String, chapterId: String): UserChapterProgressEntity?

    @Query("UPDATE user_chapter_progress SET isRead = 1, readAt = :now WHERE userMediaId = :userMediaId AND chapterId = :chapterId")
    suspend fun markAsRead(userMediaId: String, chapterId: String, now: LocalDateTime): Int

    @Query("UPDATE user_chapter_progress SET isRead = 0, readAt = null WHERE userMediaId = :userMediaId AND chapterId = :chapterId")
    suspend fun markAsUnread(userMediaId: String, chapterId: String): Int

    @Query("DELETE FROM user_chapter_progress WHERE userMediaId = :userMediaId")
    suspend fun deleteAllForUserMedia(userMediaId: String): Int
}

@Dao
interface CollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(collections: List<CollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: CollectionEntity)

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("DELETE FROM collection WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM collection ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<CollectionEntity>

    @Query("SELECT * FROM collection WHERE id = :id")
    suspend fun getById(id: String): CollectionEntity?

    @RawQuery(observedEntities = [CollectionEntity::class])
    fun observeAll(): Flow<List<CollectionEntity>>
}

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ActivityEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityEntryEntity)

    @Query("SELECT * FROM activity_entry WHERE userMediaId = :userMediaId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByUserMediaId(userMediaId: String, limit: Int): List<ActivityEntryEntity>

    @Query("SELECT * FROM activity_entry WHERE date(timestamp) = date(:date) ORDER BY timestamp DESC")
    suspend fun getByDate(date: LocalDate): List<ActivityEntryEntity>

    @Query("SELECT * FROM activity_entry WHERE date(timestamp) BETWEEN date(:startDate) AND date(:endDate) ORDER BY timestamp DESC")
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<ActivityEntryEntity>

    @Query("SELECT * FROM activity_entry ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ActivityEntryEntity>

    @RawQuery(observedEntities = [ActivityEntryEntity::class])
    fun observeByDate(date: LocalDate): Flow<List<ActivityEntryEntity>>

    @RawQuery(observedEntities = [ActivityEntryEntity::class])
    fun observeByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<ActivityEntryEntity>>
}

@Dao
interface DailyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: DailyGoalEntity)

    @Update
    suspend fun update(goal: DailyGoalEntity)

    @Query("SELECT * FROM daily_goal WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailyGoalEntity?

    @Query("SELECT * FROM daily_goal WHERE date = date('now')")
    suspend fun getToday(): DailyGoalEntity?

    @Query("SELECT streak FROM daily_goal WHERE date = (SELECT MAX(date) FROM daily_goal WHERE completed > 0)")
    suspend fun getCurrentStreak(): Int?

    @RawQuery(observedEntities = [DailyGoalEntity::class])
    fun observeByDate(date: LocalDate): Flow<DailyGoalEntity?>

    @RawQuery(observedEntities = [DailyGoalEntity::class])
    fun observeToday(): Flow<DailyGoalEntity?>
}

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: UserStatsEntity)

    @Update
    suspend fun update(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun get(): UserStatsEntity?

    @RawQuery(observedEntities = [UserStatsEntity::class])
    fun observe(): Flow<UserStatsEntity?>
}

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SearchHistoryEntity>

    @Query("DELETE FROM search_history WHERE timestamp < :threshold")
    suspend fun deleteOld(threshold: LocalDateTime): Int

    @Query("DELETE FROM search_history")
    suspend fun clearAll(): Int
}

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntryEntity)

    @Query("SELECT value FROM cache_entry WHERE key = :key AND expiresAt > :now")
    suspend fun get(key: String, now: LocalDateTime): String?

    @Query("DELETE FROM cache_entry WHERE key = :key")
    suspend fun delete(key: String): Int

    @Query("DELETE FROM cache_entry WHERE expiresAt <= :now")
    suspend fun deleteExpired(now: LocalDateTime): Int

    @Query("DELETE FROM cache_entry")
    suspend fun clearAll(): Int
}