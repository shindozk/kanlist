package com.kanlist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MediaEntity::class,
        UserMediaEntity::class,
        EpisodeEntity::class,
        ChapterEntity::class,
        UserChapterProgressEntity::class,
        CollectionEntity::class,
        ActivityEntryEntity::class,
        DailyGoalEntity::class,
        UserStatsEntity::class,
        SearchHistoryEntity::class,
        CacheEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KanlistDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao
    abstract fun userMediaDao(): UserMediaDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun chapterDao(): ChapterDao
    abstract fun userChapterProgressDao(): UserChapterProgressDao
    abstract fun collectionDao(): CollectionDao
    abstract fun activityDao(): ActivityDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var INSTANCE: KanlistDatabase? = null

        fun getInstance(context: Context): KanlistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KanlistDatabase::class.java,
                    "kanlist-db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}