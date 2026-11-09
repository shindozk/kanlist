package com.kanlist.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.usecase.GetTrendingMediaUseCase
import com.kanlist.domain.usecase.GetSeasonalMediaUseCase
import com.kanlist.domain.usecase.GetLibraryUseCase
import com.kanlist.domain.usecase.DailyGoalUseCases
import com.kanlist.domain.usecase.ActivityUseCases
import com.kanlist.domain.usecase.PreferencesUseCases
import com.kanlist.domain.usecase.StatsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrendingUseCase: GetTrendingMediaUseCase,
    private val getSeasonalUseCase: GetSeasonalMediaUseCase,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val dailyGoalUseCases: DailyGoalUseCases,
    private val activityUseCases: ActivityUseCases,
    private val preferencesUseCases: PreferencesUseCases,
    private val statsUseCases: StatsUseCases
) : ViewModel() {

    // UI State
    private val _trendingAnime = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val trendingAnime = _trendingAnime.distinctUntilChanged()

    private val _trendingManga = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val trendingManga = _trendingManga.distinctUntilChanged()

    private val _seasonalAnime = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val seasonalAnime = _seasonalAnime.distinctUntilChanged()

    private val _continueWatching = MutableStateFlow<Media?>(null)
    val continueWatching = _continueWatching.distinctUntilChanged()

    private val _dailyGoal = MutableStateFlow<com.kanlist.domain.model.DailyGoal>(com.kanlist.domain.model.DailyGoal(date = LocalDate.now()))
    val dailyGoal = _dailyGoal.distinctUntilChanged()

    private val _todayActivity = MutableStateFlow<List<com.kanlist.domain.model.ActivityEntry>>(emptyList())
    val todayActivity = _todayActivity.distinctUntilChanged()

    private val _weekActivity = MutableStateFlow<Map<LocalDate, List<com.kanlist.domain.model.ActivityEntry>>>(emptyMap())
    val weekActivity = _weekActivity.distinctUntilChanged()

    private val _preferences = MutableStateFlow<com.kanlist.domain.model.UserPreferences>(com.kanlist.domain.model.UserPreferences())
    val preferences = _preferences.distinctUntilChanged()

    private val _stats = MutableStateFlow<com.kanlist.domain.model.UserStats>(com.kanlist.domain.model.UserStats())
    val stats = _stats.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    init {
        loadInitialData()
        observePreferences()
        observeDailyGoal()
        observeWeekActivity()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            loadTrending()
            loadSeasonal()
            loadContinueWatching()
            loadStats()
            _isLoading.value = false
        }
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _trendingAnime.value = getTrendingUseCase(type = MediaType.ANIME)
            _trendingManga.value = getTrendingUseCase(type = MediaType.MANGA)
        }
    }

    private fun loadSeasonal() {
        val now = LocalDate.now()
        val year = Year.from(now).value
        val season = when (now.month) {
            MonthDay.of(1, 1)..MonthDay.of(3, 31) -> com.kanlist.domain.model.MediaSeason.WINTER
            MonthDay.of(4, 1)..MonthDay.of(6, 30) -> com.kanlist.domain.model.MediaSeason.SPRING
            MonthDay.of(7, 1)..MonthDay.of(9, 30) -> com.kanlist.domain.model.MediaSeason.SUMMER
            else -> com.kanlist.domain.model.MediaSeason.FALL
        }
        
        viewModelScope.launch {
            _seasonalAnime.value = getSeasonalUseCase(year = year, season = season, type = MediaType.ANIME)
        }
    }

    private fun loadContinueWatching() {
        viewModelScope.launch {
            val library = getLibraryUseCase.getInProgress()
            library.onSuccess { media ->
                val inProgress = media.filter { it.status == com.kanlist.domain.model.MediaStatus.CURRENT }
                    .maxByOrNull { it.lastActivityAt }
                _continueWatching.value = inProgress?.media
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            _stats.value = statsUseCases.get().getOrDefault(com.kanlist.domain.model.UserStats())
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesUseCases.observe().collect { prefs ->
                _preferences.value = prefs
            }
        }
    }

    private fun observeDailyGoal() {
        viewModelScope.launch {
            dailyGoalUseCases.observeForDate(LocalDate.now()).collect { goal ->
                _dailyGoal.value = goal
            }
        }
    }

    private fun observeWeekActivity() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value - 1)
            activityUseCases.observeForWeek(startOfWeek).collect { activityMap ->
                _weekActivity.value = activityMap
                // Today's activity
                _todayActivity.value = activityMap[today] ?: emptyList()
            }
        }
    }

    fun refresh() {
        loadInitialData()
    }

    fun incrementDailyGoal() {
        viewModelScope.launch {
            _dailyGoal.value = dailyGoalUseCases.increment(LocalDate.now()).getOrDefault(_dailyGoal.value)
        }
    }

    fun onActivityEntry(entry: com.kanlist.domain.model.ActivityEntry) {
        viewModelScope.launch {
            activityUseCases.addEntry(entry)
            incrementDailyGoal()
        }
    }

    // Greeting based on time
    val greeting: String
        get() {
            val hour = java.time.LocalTime.now().hour
            val name = _preferences.value.name
            val prefix = when {
                hour < 6 -> "Boa madrugada"
                hour < 12 -> "Bom dia"
                hour < 18 -> "Boa tarde"
                else -> "Boa noite"
            }
            return if (name.isNotBlank()) "$prefix, $name" else prefix
        }
}