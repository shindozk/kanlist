package com.kanlist.presentation.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.usecase.GetTrendingMediaUseCase
import com.kanlist.domain.usecase.GetSeasonalMediaUseCase
import com.kanlist.domain.usecase.SearchMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getTrendingUseCase: GetTrendingMediaUseCase,
    private val getSeasonalUseCase: GetSeasonalMediaUseCase,
    private val searchUseCase: SearchMediaUseCase
) : ViewModel() {

    // Tabs
    enum class Tab { WATCH, READ }

    // Watch tab state
    private val _trendingWatch = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val trendingWatch = _trendingWatch.distinctUntilChanged()

    private val _seasonalWatch = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val seasonalWatch = _seasonalWatch.distinctUntilChanged()

    private val _popularManhwa = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularManhwa = _popularManhwa.distinctUntilChanged()

    private val _popularDonghua = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularDonghua = _popularDonghua.distinctUntilChanged()

    // Read tab state
    private val _trendingRead = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val trendingRead = _trendingRead.distinctUntilChanged()

    private val _popularManga = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularManga = _popularManga.distinctUntilChanged()

    private val _popularManhwaRead = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularManhwaRead = _popularManhwaRead.distinctUntilChanged()

    private val _popularManhua = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularManhua = _popularManhua.distinctUntilChanged()

    private val _popularNovels = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val popularNovels = _popularNovels.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Watch tab
            _trendingWatch.value = getTrendingUseCase(type = MediaType.ANIME)
            _popularManhwa.value = getTrendingUseCase(type = MediaType.MANHWA)
            _popularDonghua.value = getTrendingUseCase(type = MediaType.DONGHUA)
            
            // Seasonal
            val now = LocalDate.now()
            val year = Year.from(now).value
            val season = when (now.month) {
                MonthDay.of(1, 1)..MonthDay.of(3, 31) -> com.kanlist.domain.model.MediaSeason.WINTER
                MonthDay.of(4, 1)..MonthDay.of(6, 30) -> com.kanlist.domain.model.MediaSeason.SPRING
                MonthDay.of(7, 1)..MonthDay.of(9, 30) -> com.kanlist.domain.model.MediaSeason.SUMMER
                else -> com.kanlist.domain.model.MediaSeason.FALL
            }
            _seasonalWatch.value = getSeasonalUseCase(year = year, season = season, type = MediaType.ANIME)
            
            // Read tab
            _trendingRead.value = getTrendingUseCase(type = MediaType.MANGA)
            _popularManga.value = getTrendingUseCase(type = MediaType.MANGA)
            _popularManhwaRead.value = getTrendingUseCase(type = MediaType.MANHWA)
            _popularManhua.value = getTrendingUseCase(type = MediaType.MANHUA)
            _popularNovels.value = getTrendingUseCase(type = MediaType.NOVEL)
            
            _isLoading.value = false
        }
    }

    fun search(query: String, type: MediaType? = null): Result<MediaSearchResult> {
        return searchUseCase(query = query, types = type?.let { listOf(it) })
    }

    fun refresh() {
        loadAll()
    }
}