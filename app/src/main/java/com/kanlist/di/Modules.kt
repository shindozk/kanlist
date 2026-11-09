package com.kanlist.di

import android.content.Context
import com.kanlist.data.local.KanlistDatabase
import com.kanlist.data.local.Daos
import com.kanlist.data.repository.MediaRepositoryImpl
import com.kanlist.data.repository.TranslationRepositoryImpl
import com.kanlist.data.repository.UserMediaRepositoryImpl
import com.kanlist.data.source.api.ApiService
import com.kanlist.data.source.local.PreferencesDataSource
import com.kanlist.domain.repository.MediaRepository
import com.kanlist.domain.repository.TranslationRepository
import com.kanlist.domain.repository.UserMediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Singleton
import okhttp3.Cache
import java.io.File
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): KanlistDatabase {
        return KanlistDatabase.getInstance(context)
    }

    @Provides
    fun provideMediaDao(database: KanlistDatabase): Daos.MediaDao {
        return database.mediaDao()
    }

    @Provides
    fun provideUserMediaDao(database: KanlistDatabase): Daos.UserMediaDao {
        return database.userMediaDao()
    }

    @Provides
    fun provideEpisodeDao(database: KanlistDatabase): Daos.EpisodeDao {
        return database.episodeDao()
    }

    @Provides
    fun provideChapterDao(database: KanlistDatabase): Daos.ChapterDao {
        return database.chapterDao()
    }

    @Provides
    fun provideUserChapterProgressDao(database: KanlistDatabase): Daos.UserChapterProgressDao {
        return database.userChapterProgressDao()
    }

    @Provides
    fun provideCollectionDao(database: KanlistDatabase): Daos.CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideActivityDao(database: KanlistDatabase): Daos.ActivityDao {
        return database.activityDao()
    }

    @Provides
    fun provideDailyGoalDao(database: KanlistDatabase): Daos.DailyGoalDao {
        return database.dailyGoalDao()
    }

    @Provides
    fun provideUserStatsDao(database: KanlistDatabase): Daos.UserStatsDao {
        return database.userStatsDao()
    }

    @Provides
    fun provideSearchHistoryDao(database: KanlistDatabase): Daos.SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    fun provideCacheDao(database: KanlistDatabase): Daos.CacheDao {
        return database.cacheDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@dagger.hilt.android.qualifiers.ApplicationContext context: Context, moshi: Moshi): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, 50 * 1024 * 1024) // 50 MB

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideAniListApi(okHttpClient: OkHttpClient, moshi: Moshi): ApiService.AniListApiService {
        return Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService.AniListApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMangaDexApi(okHttpClient: OkHttpClient, moshi: Moshi): ApiService.MangaDexApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.mangadex.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService.MangaDexApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideKitsuApi(okHttpClient: OkHttpClient, moshi: Moshi): ApiService.KitsuApiService {
        return Retrofit.Builder()
            .baseUrl("https://kitsu.io/api/edge/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService.KitsuApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideJikanApi(okHttpClient: OkHttpClient, moshi: Moshi): ApiService.JikanApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.jikan.moe/v4/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService.JikanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTranslationApi(okHttpClient: OkHttpClient, moshi: Moshi): ApiService.TranslationApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService.TranslationApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMediaRepository(impl: MediaRepositoryImpl): MediaRepository = impl

    @Provides
    @Singleton
    fun provideUserMediaRepository(impl: UserMediaRepositoryImpl): UserMediaRepository = impl

    @Provides
    @Singleton
    fun provideTranslationRepository(impl: TranslationRepositoryImpl): TranslationRepository = impl
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferencesDataSource(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): PreferencesDataSource {
        return PreferencesDataSource.getInstance(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Media Use Cases
    @Provides
    fun provideSearchMediaUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.SearchMediaUseCase(repo)

    @Provides
    fun provideGetTrendingMediaUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetTrendingMediaUseCase(repo)

    @Provides
    fun provideGetSeasonalMediaUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetSeasonalMediaUseCase(repo)

    @Provides
    fun provideGetMediaDetailsUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetMediaDetailsUseCase(repo)

    @Provides
    fun provideGetEpisodesUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetEpisodesUseCase(repo)

    @Provides
    fun provideGetChaptersUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetChaptersUseCase(repo)

    @Provides
    fun provideGetRelationsUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetRelationsUseCase(repo)

    @Provides
    fun provideGetRecommendationsUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetRecommendationsUseCase(repo)

    @Provides
    fun provideGetCharactersUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetCharactersUseCase(repo)

    @Provides
    fun provideGetStaffUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetStaffUseCase(repo)

    @Provides
    fun provideGetExternalLinksUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.GetExternalLinksUseCase(repo)

    @Provides
    fun provideTranslateSynopsisUseCase(repo: TranslationRepository) = com.kanlist.domain.usecase.TranslateSynopsisUseCase(repo)

    @Provides
    fun provideClearMediaCacheUseCase(repo: MediaRepository) = com.kanlist.domain.usecase.ClearMediaCacheUseCase(repo)

    // User Media Use Cases
    @Provides
    fun provideGetLibraryUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.GetLibraryUseCase(repo)

    @Provides
    fun provideGetUserMediaUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.GetUserMediaUseCase(repo)

    @Provides
    fun provideAddToLibraryUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.AddToLibraryUseCase(repo)

    @Provides
    fun provideUpdateLibraryEntryUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.UpdateLibraryEntryUseCase(repo)

    @Provides
    fun provideRemoveFromLibraryUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.RemoveFromLibraryUseCase(repo)

    @Provides
    fun provideIncrementProgressUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.IncrementProgressUseCase(repo)

    @Provides
    fun provideMarkChapterReadUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.MarkChapterReadUseCase(repo)

    @Provides
    fun provideGetChapterProgressUseCase(repo: UserMediaRepository) = com.kanlist.domain.usecase.GetChapterProgressUseCase(repo)

    @Provides
    fun provideCollectionUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.CollectionUseCases(repo)

    @Provides
    fun provideActivityUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.ActivityUseCases(repo)

    @Provides
    fun provideDailyGoalUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.DailyGoalUseCases(repo)

    @Provides
    fun provideStatsUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.StatsUseCases(repo)

    @Provides
    fun providePreferencesUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.PreferencesUseCases(repo)

    @Provides
    fun provideBackupUseCases(repo: UserMediaRepository) = com.kanlist.domain.usecase.BackupUseCases(repo)
}