package com.kanlist.presentation.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaSearchResult
import com.kanlist.domain.model.MediaSource
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.usecase.SearchMediaUseCase
import com.kanlist.domain.usecase.GetLibraryUseCase
import com.kanlist.domain.usecase.PreferencesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchMediaUseCase,
    private val getLibraryUseCase: GetLibraryUseCase,
    private val preferencesUseCases: PreferencesUseCases
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Result<MediaSearchResult>>(Result.success(MediaSearchResult(emptyList(), com.kanlist.domain.model.PageInfo(0,20,1,1,false), emptyList(), emptyMap())))
    val searchResults = _searchResults.distinctUntilChanged()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches = _recentSearches.distinctUntilChanged()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.distinctUntilChanged()

    private val _selectedSources = MutableStateFlow<Set<MediaSource>>(MediaSource.allEnabled.toSet())
    val selectedSources = _selectedSources.distinctUntilChanged()

    private val _includeAdult = MutableStateFlow(false)
    val includeAdult = _includeAdult.distinctUntilChanged()

    private var currentQuery = ""
    private var currentPage = 1
    private var hasMore = false

    init {
        loadRecentSearches()
    }

    fun search(query: String, page: Int = 1) {
        if (query.trim().isEmpty()) return
        
        currentQuery = query
        currentPage = page
        _isSearching.value = true
        
        viewModelScope.launch {
            val prefs = preferencesUseCases.get().getOrDefault(com.kanlist.domain.model.UserPreferences())
            val types = prefs.libraryFilterType?.let { listOf(it) }
            
            val result = searchUseCase(
                query = query,
                page = page,
                types = types,
                sources = _selectedSources.value.toList(),
                includeAdult = _includeAdult.value
            )
            
            _searchResults.value = result
            result.onSuccess { res ->
                hasMore = res.pageInfo.hasNextPage
                if (page == 1) {
                    addToRecentSearches(query)
                }
            }
            _isSearching.value = false
        }
    }

    fun loadMore() {
        if (hasMore && !_isSearching.value) {
            search(currentQuery, currentPage + 1)
        }
    }

    fun addToLibrary(media: Media, status: com.kanlist.domain.model.MediaStatus = com.kanlist.domain.model.MediaStatus.PLANNING) {
        viewModelScope.launch {
            getLibraryUseCase.addFromMedia(media, status)
        }
    }

    fun toggleSource(source: MediaSource) {
        val newSources = _selectedSources.value.toMutableSet()
        if (newSources.contains(source)) {
            newSources.remove(source)
        } else {
            newSources.add(source)
        }
        _selectedSources.value = newSources
    }

    fun toggleAdultContent() {
        _includeAdult.value = !_includeAdult.value
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            // Load from DataStore/preferences
            _recentSearches.value = emptyList() // TODO: Implement
        }
    }

    private fun addToRecentSearches(query: String) {
        val updated = (_recentSearches.value - query + query).take(10)
        _recentSearches.value = updated
        // Save to preferences
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
}