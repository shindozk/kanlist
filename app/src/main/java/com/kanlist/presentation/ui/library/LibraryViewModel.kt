package com.kanlist.presentation.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.Collection
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserMedia
import com.kanlist.domain.usecase.GetLibraryUseCase
import com.kanlist.domain.usecase.UpdateLibraryEntryUseCase
import com.kanlist.domain.usecase.RemoveFromLibraryUseCase
import com.kanlist.domain.usecase.IncrementProgressUseCase
import com.kanlist.domain.usecase.CollectionUseCases
import com.kanlist.domain.usecase.PreferencesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibraryUseCase: GetLibraryUseCase,
    private val updateEntryUseCase: UpdateLibraryEntryUseCase,
    private val removeFromLibraryUseCase: RemoveFromLibraryUseCase,
    private val incrementProgressUseCase: IncrementProgressUseCase,
    private val collectionUseCases: CollectionUseCases,
    private val preferencesUseCases: PreferencesUseCases
) : ViewModel() {

    // Tabs
    enum class Tab { LIST, COLLECTIONS }

    // State
    private val _library = MutableStateFlow<List<UserMedia>>(emptyList())
    val library = _library.distinctUntilChanged()

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections = _collections.distinctUntilChanged()

    private val _statusCounts = MutableStateFlow<Map<MediaStatus, Int>>(emptyMap())
    val statusCounts = _statusCounts.distinctUntilChanged()

    private val _filterStatus = MutableStateFlow<MediaStatus?>(null)
    val filterStatus = _filterStatus.distinctUntilChanged()

    private val _filterType = MutableStateFlow<MediaType?>(null)
    val filterType = _filterType.distinctUntilChanged()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.distinctUntilChanged()

    private val _sortOrder = MutableStateFlow<com.kanlist.domain.model.LibrarySortOrder>(com.kanlist.domain.model.LibrarySortOrder.RECENT_ACTIVITY)
    val sortOrder = _sortOrder.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    init {
        loadLibrary()
        loadCollections()
        observePreferences()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val prefs = preferencesUseCases.get().getOrDefault(com.kanlist.domain.model.UserPreferences())
            _filterStatus.value = prefs.libraryFilterStatus
            _filterType.value = prefs.libraryFilterType
            _searchQuery.value = prefs.librarySearchQuery
            _sortOrder.value = prefs.sortOrder
            
            getLibraryUseCase.observeAll().collect { media ->
                _library.value = applyFiltersAndSort(media)
                updateStatusCounts(media)
            }
            _isLoading.value = false
        }
    }

    private fun applyFiltersAndSort(media: List<UserMedia>): List<UserMedia> {
        var filtered = media.filter { !it.isHidden }

        // Status filter
        _filterStatus.value?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // Type filter
        _filterType.value?.let { type ->
            filtered = filtered.filter { it.media?.type == type }
        }

        // Search query
        val query = _searchQuery.value.lowercase()
        if (query.isNotBlank()) {
            filtered = filtered.filter { 
                it.media?.displayTitle.lowercase().contains(query) == true ||
                it.media?.title.english?.lowercase().contains(query) == true ||
                it.media?.title.native?.lowercase().contains(query) == true
            }
        }

        // Sort
        return when (_sortOrder.value) {
            com.kanlist.domain.model.LibrarySortOrder.RECENT_ACTIVITY -> filtered.sortedByDescending { it.lastActivityAt }
            com.kanlist.domain.model.LibrarySortOrder.NAME_A_Z -> filtered.sortedBy { it.media?.displayTitle ?: "" }
            com.kanlist.domain.model.LibrarySortOrder.NAME_Z_A -> filtered.sortedByDescending { it.media?.displayTitle ?: "" }
            com.kanlist.domain.model.LibrarySortOrder.SCORE_HIGH_LOW -> filtered.sortedByDescending { it.score }
            com.kanlist.domain.model.LibrarySortOrder.SCORE_LOW_HIGH -> filtered.sortedBy { it.score }
            com.kanlist.domain.model.LibrarySortOrder.PROGRESS_HIGH_LOW -> filtered.sortedByDescending { it.progressPercentage }
            com.kanlist.domain.model.LibrarySortOrder.PROGRESS_LOW_HIGH -> filtered.sortedBy { it.progressPercentage }
            com.kanlist.domain.model.LibrarySortOrder.DATE_ADDED -> filtered.sortedByDescending { it.createdAt }
            com.kanlist.domain.model.LibrarySortOrder.DATE_STARTED -> filtered.sortedByDescending { it.startedAt?.toEpochSecond(java.time.ZoneOffset.UTC) ?: 0L }
            com.kanlist.domain.model.LibrarySortOrder.DATE_COMPLETED -> filtered.sortedByDescending { it.completedAt?.toEpochSecond(java.time.ZoneOffset.UTC) ?: 0L }
        }
    }

    private fun updateStatusCounts(media: List<UserMedia>) {
        val counts = mutableMapOf<MediaStatus, Int>()
        for (status in MediaStatus.values()) {
            counts[status] = media.count { it.status == status }
        }
        _statusCounts.value = counts
    }

    private fun loadCollections() {
        viewModelScope.launch {
            collectionUseCases.observe().collect { collections ->
                _collections.value = collections
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesUseCases.observe().collect { prefs ->
                _filterStatus.value = prefs.libraryFilterStatus
                _filterType.value = prefs.libraryFilterType
                _searchQuery.value = prefs.librarySearchQuery
                _sortOrder.value = prefs.sortOrder
            }
        }
    }

    fun setFilterStatus(status: MediaStatus?) {
        _filterStatus.value = status
        preferencesUseCases.updateLibraryFilterStatus(status)
    }

    fun setFilterType(type: MediaType?) {
        _filterType.value = type
        preferencesUseCases.updateLibraryFilterType(type)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        preferencesUseCases.updateLibrarySearchQuery(query)
    }

    fun setSortOrder(order: com.kanlist.domain.model.LibrarySortOrder) {
        _sortOrder.value = order
        preferencesUseCases.updateSortOrder(order)
    }

    fun updateProgress(userMedia: UserMedia) {
        viewModelScope.launch {
            incrementProgressUseCase(userMedia)
        }
    }

    fun updateStatus(userMedia: UserMedia, status: MediaStatus) {
        viewModelScope.launch {
            updateEntryUseCase.updateProgress(userMedia.id, userMedia.progress, status)
        }
    }

    fun updateScore(userMedia: UserMedia, score: Float) {
        viewModelScope.launch {
            updateEntryUseCase.updateScore(userMedia.id, score)
        }
    }

    fun toggleFavorite(userMedia: UserMedia) {
        viewModelScope.launch {
            updateEntryUseCase.toggleFavorite(userMedia.id)
        }
    }

    fun removeFromLibrary(userMedia: UserMedia) {
        viewModelScope.launch {
            removeFromLibraryUseCase(userMedia.id)
        }
    }

    fun resetProgress(userMedia: UserMedia) {
        viewModelScope.launch {
            updateEntryUseCase.resetProgress(userMedia.id)
        }
    }

    // Collections
    fun createCollection(name: String, color: Int, icon: String? = null) {
        viewModelScope.launch {
            val collection = Collection(
                name = name,
                color = color,
                icon = icon
            )
            collectionUseCases.create(collection)
        }
    }

    fun updateCollection(collection: Collection) {
        viewModelScope.launch {
            collectionUseCases.update(collection)
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch {
            collectionUseCases.delete(collection.id)
        }
    }

    fun addToCollection(userMedia: UserMedia, collectionId: String) {
        viewModelScope.launch {
            updateEntryUseCase.addToCollection(userMedia.id, collectionId)
        }
    }

    fun removeFromCollection(userMedia: UserMedia, collectionId: String) {
        viewModelScope.launch {
            updateEntryUseCase.removeFromCollection(userMedia.id, collectionId)
        }
    }

    fun refresh() {
        loadLibrary()
        loadCollections()
    }
}