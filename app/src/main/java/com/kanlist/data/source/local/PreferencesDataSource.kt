package com.kanlist.data.source.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rx.preferenceDataStoreFactory
import androidx.datastore.rxjava2.RxDataStore
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.LibrarySortOrder
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.ThemeMode
import com.kanlist.domain.model.TitleLanguage
import com.kanlist.domain.model.UserPreferences
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

object PreferencesDataSource {

    private const val PREF_NAME = "kanlist_prefs"
    private var instance: PreferencesDataSource? = null

    private val NAME = stringPreferencesKey("name")
    private val THEME = stringPreferencesKey("theme")
    private val TITLE_LANGUAGE = stringPreferencesKey("title_language")
    private val DAILY_GOAL_TARGET = intPreferencesKey("daily_goal_target")
    private val CHAPTER_LANGUAGES = stringPreferencesKey("chapter_languages")
    private val AUTO_TRANSLATE_SYNOPSIS = booleanPreferencesKey("auto_translate_synopsis")
    private val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    private val SHOW_ADULT_CONTENT = booleanPreferencesKey("show_adult_content")
    private val ENABLED_SOURCES = stringPreferencesKey("enabled_sources")
    private val SORT_ORDER = stringPreferencesKey("sort_order")
    private val LIBRARY_FILTER_STATUS = stringPreferencesKey("library_filter_status")
    private val LIBRARY_FILTER_TYPE = stringPreferencesKey("library_filter_type")
    private val LIBRARY_SEARCH_QUERY = stringPreferencesKey("library_search_query")
    private val COMPACT_MODE = booleanPreferencesKey("compact_mode")
    private val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    private val DATA_SAVER_MODE = booleanPreferencesKey("data_saver_mode")

    @Volatile
    private var dataStore: RxDataStore<Preferences>? = null

    fun getInstance(context: Context): PreferencesDataSource {
        return instance ?: synchronized(this) {
            val ds = RxDataStoreBuilder(context, PREF_NAME).build()
            dataStore = ds
            PreferencesDataSource().also { instance = it }
        }
    }

    private suspend fun getDataStore(): RxDataStore<Preferences> = dataStore!!
        .also { if (it == null) throw IllegalStateException("DataStore not initialized") }

    suspend fun getPreferences(): UserPreferences = withContext(io.ktor.util.Kotlinx.coroutines.Dispatchers.IO) {
        val prefs = dataStore!!.data.firstOrNull()?.await() ?: Preferences.Builder().build()
        buildPreferences(prefs)
    }

    private fun buildPreferences(prefs: Preferences): UserPreferences {
        val gson = com.google.gson.Gson()
        return UserPreferences(
            name = prefs[NAME] ?: "",
            theme = ThemeMode.valueOf(prefs[THEME] ?: ThemeMode.SYSTEM.name),
            titleLanguage = TitleLanguage.valueOf(prefs[TITLE_LANGUAGE] ?: TitleLanguage.ROMAJI.name),
            dailyGoalTarget = prefs[DAILY_GOAL_TARGET] ?: 3,
            chapterLanguages = gson.fromJson(
                prefs[CHAPTER_LANGUAGES] ?: "[]",
                com.google.gson.reflect.TypeToken.getParameterized(List::class.java, ChapterLanguage::class.java).type
            ),
            autoTranslateSynopsis = prefs[AUTO_TRANSLATE_SYNOPSIS] ?: true,
            hapticFeedback = prefs[HAPTIC_FEEDBACK] ?: true,
            showAdultContent = prefs[SHOW_ADULT_CONTENT] ?: false,
            enabledSources = gson.fromJson(
                prefs[ENABLED_SOURCES] ?: "[]",
                com.google.gson.reflect.TypeToken.getParameterized(List::class.java, MediaSource::class.java).type
            ).ifEmpty { MediaSource.allEnabled },
            sortOrder = LibrarySortOrder.valueOf(prefs[SORT_ORDER] ?: LibrarySortOrder.RECENT_ACTIVITY.name),
            libraryFilterStatus = prefs[LIBRARY_FILTER_STATUS]?.let { com.kanlist.domain.model.MediaStatus.valueOf(it) },
            libraryFilterType = prefs[LIBRARY_FILTER_TYPE]?.let { com.kanlist.domain.model.MediaType.valueOf(it) },
            librarySearchQuery = prefs[LIBRARY_SEARCH_QUERY] ?: "",
            compactMode = prefs[COMPACT_MODE] ?: false,
            reduceMotion = prefs[REDUCE_MOTION] ?: false,
            dataSaverMode = prefs[DATA_SAVER_MODE] ?: false
        )
    }

    private suspend fun updatePreferences(block: Preferences.Builder.() -> Unit): Preferences {
        val prefs = dataStore!!.data.firstOrNull()?.await() ?: Preferences.Builder().build()
        val builder = prefs.toBuilder()
        builder.block()
        val newPrefs = builder.build()
        dataStore!!.updateData { _ -> newPrefs }.await()
        return newPrefs
    }

    suspend fun updateName(name: String): UserPreferences {
        val prefs = updatePreferences { putString(NAME, name) }
        return buildPreferences(prefs)
    }

    suspend fun updateTheme(theme: ThemeMode): UserPreferences {
        val prefs = updatePreferences { putString(THEME, theme.name) }
        return buildPreferences(prefs)
    }

    suspend fun updateTitleLanguage(language: TitleLanguage): UserPreferences {
        val prefs = updatePreferences { putString(TITLE_LANGUAGE, language.name) }
        return buildPreferences(prefs)
    }

    suspend fun updateDailyGoalTarget(target: Int): UserPreferences {
        val prefs = updatePreferences { putInt(DAILY_GOAL_TARGET, target) }
        return buildPreferences(prefs)
    }

    suspend fun updateChapterLanguages(languages: List<ChapterLanguage>): UserPreferences {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(languages.map { it.code })
        val prefs = updatePreferences { putString(CHAPTER_LANGUAGES, json) }
        return buildPreferences(prefs)
    }

    suspend fun updateAutoTranslateSynopsis(enabled: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(AUTO_TRANSLATE_SYNOPSIS, enabled) }
        return buildPreferences(prefs)
    }

    suspend fun updateHapticFeedback(enabled: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(HAPTIC_FEEDBACK, enabled) }
        return buildPreferences(prefs)
    }

    suspend fun updateShowAdultContent(show: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(SHOW_ADULT_CONTENT, show) }
        return buildPreferences(prefs)
    }

    suspend fun updateEnabledSources(sources: List<MediaSource>): UserPreferences {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(sources.map { it.name })
        val prefs = updatePreferences { putString(ENABLED_SOURCES, json) }
        return buildPreferences(prefs)
    }

    suspend fun updateSortOrder(order: LibrarySortOrder): UserPreferences {
        val prefs = updatePreferences { putString(SORT_ORDER, order.name) }
        return buildPreferences(prefs)
    }

    suspend fun updateLibraryFilterStatus(status: com.kanlist.domain.model.MediaStatus?): UserPreferences {
        val prefs = updatePreferences { 
            status?.let { putString(LIBRARY_FILTER_STATUS, it.name) } ?: remove(LIBRARY_FILTER_STATUS) 
        }
        return buildPreferences(prefs)
    }

    suspend fun updateLibraryFilterType(type: com.kanlist.domain.model.MediaType?): UserPreferences {
        val prefs = updatePreferences { 
            type?.let { putString(LIBRARY_FILTER_TYPE, it.name) } ?: remove(LIBRARY_FILTER_TYPE) 
        }
        return buildPreferences(prefs)
    }

    suspend fun updateLibrarySearchQuery(query: String): UserPreferences {
        val prefs = updatePreferences { putString(LIBRARY_SEARCH_QUERY, query) }
        return buildPreferences(prefs)
    }

    suspend fun updateCompactMode(enabled: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(COMPACT_MODE, enabled) }
        return buildPreferences(prefs)
    }

    suspend fun updateReduceMotion(enabled: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(REDUCE_MOTION, enabled) }
        return buildPreferences(prefs)
    }

    suspend fun updateDataSaverMode(enabled: Boolean): UserPreferences {
        val prefs = updatePreferences { putBoolean(DATA_SAVER_MODE, enabled) }
        return buildPreferences(prefs)
    }

    fun observePreferences(): Flowable<UserPreferences> {
        return dataStore!!.data
            .map { prefs -> buildPreferences(prefs) }
    }
}