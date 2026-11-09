package com.kanlist.presentation.ui.workdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.Chapter
import com.kanlist.domain.model.ChapterLanguage
import com.kanlist.domain.model.ChapterList
import com.kanlist.domain.model.Episode
import com.kanlist.domain.model.Media
import com.kanlist.domain.model.MediaStatus
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserChapterProgress
import com.kanlist.domain.model.UserMedia
import com.kanlist.domain.usecase.GetMediaDetailsUseCase
import com.kanlist.domain.usecase.GetEpisodesUseCase
import com.kanlist.domain.usecase.GetChaptersUseCase
import com.kanlist.domain.usecase.GetRelationsUseCase
import com.kanlist.domain.usecase.GetRecommendationsUseCase
import com.kanlist.domain.usecase.GetCharactersUseCase
import com.kanlist.domain.usecase.GetStaffUseCase
import com.kanlist.domain.usecase.GetExternalLinksUseCase
import com.kanlist.domain.usecase.TranslateSynopsisUseCase
import com.kanlist.domain.usecase.GetUserMediaUseCase
import com.kanlist.domain.usecase.UpdateLibraryEntryUseCase
import com.kanlist.domain.usecase.RemoveFromLibraryUseCase
import com.kanlist.domain.usecase.IncrementProgressUseCase
import com.kanlist.domain.usecase.MarkChapterReadUseCase
import com.kanlist.domain.usecase.GetChapterProgressUseCase
import com.kanlist.domain.usecase.CollectionUseCases
import com.kanlist.domain.usecase.ActivityUseCases
import com.kanlist.domain.usecase.DailyGoalUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkDetailViewModel @Inject constructor(
    private val getMediaDetailsUseCase: GetMediaDetailsUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val getRelationsUseCase: GetRelationsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getCharactersUseCase: GetCharactersUseCase,
    private val getStaffUseCase: GetStaffUseCase,
    private val getExternalLinksUseCase: GetExternalLinksUseCase,
    private val translateSynopsisUseCase: TranslateSynopsisUseCase,
    private val getUserMediaUseCase: GetUserMediaUseCase,
    private val updateEntryUseCase: UpdateLibraryEntryUseCase,
    private val removeFromLibraryUseCase: RemoveFromLibraryUseCase,
    private val incrementProgressUseCase: IncrementProgressUseCase,
    private val markChapterReadUseCase: MarkChapterReadUseCase,
    private val getChapterProgressUseCase: GetChapterProgressUseCase,
    private val collectionUseCases: CollectionUseCases,
    private val activityUseCases: ActivityUseCases,
    private val dailyGoalUseCases: DailyGoalUseCases
) : ViewModel() {

    private val _media = MutableStateFlow<Result<Media>>(Result.failure(Exception("Not loaded")))
    val media = _media.distinctUntilChanged()

    private val _userMedia = MutableStateFlow<UserMedia?>(null)
    val userMedia = _userMedia.distinctUntilChanged()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes = _episodes.distinctUntilChanged()

    private val _chapters = MutableStateFlow<ChapterList?>(null)
    val chapters = _chapters.distinctUntilChanged()

    private val _chapterProgress = MutableStateFlow<Set<Double>>(emptySet())
    val chapterProgress = _chapterProgress.distinctUntilChanged()

    private val _relations = MutableStateFlow<List<com.kanlist.domain.model.MediaRelation>>(emptyList())
    val relations = _relations.distinctUntilChanged()

    private val _recommendations = MutableStateFlow<List<com.kanlist.domain.model.MediaRecommendation>>(emptyList())
    val recommendations = _recommendations.distinctUntilChanged()

    private val _characters = MutableStateFlow<List<com.kanlist.domain.model.MediaCharacter>>(emptyList())
    val characters = _characters.distinctUntilChanged()

    private val _staff = MutableStateFlow<List<com.kanlist.domain.model.MediaStaff>>(emptyList())
    val staff = _staff.distinctUntilChanged()

    private val _externalLinks = MutableStateFlow<List<com.kanlist.domain.model.MediaExternalLink>>(emptyList())
    val externalLinks = _externalLinks.distinctUntilChanged()

    private val _translatedSynopsis = MutableStateFlow<String?>(null)
    val translatedSynopsis = _translatedSynopsis.distinctUntilChanged()

    private val _showTranslated = MutableStateFlow(false)
    val showTranslated = _showTranslated.distinctUntilChanged()

    private val _selectedChapterLanguages = MutableStateFlow<List<ChapterLanguage>>(listOf(ChapterLanguage.PT_BR, ChapterLanguage.EN))
    val selectedChapterLanguages = _selectedChapterLanguages.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _collections = MutableStateFlow<List<com.kanlist.domain.model.Collection>>(emptyList())
    val collections = _collections.distinctUntilChanged()

    private var mediaId: String = ""

    fun load(mediaId: String) {
        this.mediaId = mediaId
        viewModelScope.launch {
            _isLoading.value = true
            loadMediaDetails()
            loadUserMedia()
            loadEpisodesOrChapters()
            loadRelations()
            loadRecommendations()
            loadCharacters()
            loadStaff()
            loadExternalLinks()
            loadCollections()
            _isLoading.value = false
        }
    }

    private fun loadMediaDetails() {
        _media.value = getMediaDetailsUseCase(mediaId)
        _media.value.onSuccess { media ->
            // Load translated synopsis if needed
            if (media.description != null) {
                translateSynopsis(media)
            }
        }
    }

    private fun translateSynopsis(media: Media) {
        viewModelScope.launch {
            val result = translateSynopsisUseCase(media)
            result.onSuccess { translated ->
                _translatedSynopsis.value = translated
            }
        }
    }

    private fun loadUserMedia() {
        getUserMediaUseCase.getByMediaId(mediaId).onSuccess { userMedia ->
            _userMedia.value = userMedia
            if (userMedia != null && userMedia.media?.type in MediaType.readTypes) {
                loadChapterProgress(userMedia.id)
            }
        }
    }

    private fun loadEpisodesOrChapters() {
        _media.value.getOrNull()?.let { media ->
            if (media.type in MediaType.watchTypes) {
                loadEpisodes(media)
            } else {
                loadChapters(media)
            }
        }
    }

    private fun loadEpisodes(media: Media) {
        viewModelScope.launch {
            val source = media.source
            getEpisodesUseCase(media.id, source).onSuccess { eps ->
                _episodes.value = eps
            }
        }
    }

    private fun loadChapters(media: Media) {
        viewModelScope.launch {
            val source = if (media.idMangaDex != null) MediaSource.MANGDEX else media.source
            getChaptersUseCase(media.id, _selectedChapterLanguages.value, source).onSuccess { chapterList ->
                _chapters.value = chapterList
            }
        }
    }

    private fun loadChapterProgress(userMediaId: String) {
        viewModelScope.launch {
            val result = getChapterProgressUseCase(userMediaId)
            result.onSuccess { progress ->
                _chapterProgress.value = progress.map { it.chapterNumber }.toSet()
            }
        }
    }

    private fun loadRelations() {
        viewModelScope.launch {
            getRelationsUseCase(mediaId).onSuccess { rels ->
                _relations.value = rels
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            getRecommendationsUseCase(mediaId).onSuccess { recs ->
                _recommendations.value = recs
            }
        }
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            getCharactersUseCase(mediaId).onSuccess { chars ->
                _characters.value = chars
            }
        }
    }

    private fun loadStaff() {
        viewModelScope.launch {
            getStaffUseCase(mediaId).onSuccess { staffList ->
                _staff.value = staffList
            }
        }
    }

    private fun loadExternalLinks() {
        viewModelScope.launch {
            getExternalLinksUseCase(mediaId).onSuccess { links ->
                _externalLinks.value = links
            }
        }
    }

    private fun loadCollections() {
        viewModelScope.launch {
            collectionUseCases.getAll().onSuccess { colls ->
                _collections.value = colls
            }
        }
    }

    // User actions
    fun updateProgress(progress: Int, status: MediaStatus? = null) {
        _userMedia.value?.let { userMedia ->
            val newStatus = status ?: userMedia.status
            val newProgress = progress.coerceAtLeast(0).coerceAtMost(userMedia.media?.totalEpisodesOrChapters ?: Int.MAX_VALUE)
            
            viewModelScope.launch {
                updateEntryUseCase.updateProgress(userMedia.id, newProgress, newStatus)
                _userMedia.value = userMedia.copy(progress = newProgress, status = newStatus)
                
                // Add activity
                val entry = com.kanlist.domain.model.ActivityEntry(
                    userMediaId = userMedia.id,
                    mediaId = mediaId,
                    type = if (userMedia.media?.type in MediaType.watchTypes) 
                        com.kanlist.domain.model.ActivityType.EPISODE_WATCHED 
                    else 
                        com.kanlist.domain.model.ActivityType.CHAPTER_READ,
                    progress = newProgress,
                    previousProgress = userMedia.progress
                )
                activityUseCases.addEntry(entry)
                dailyGoalUseCases.increment(java.time.LocalDate.now())
            }
        }
    }

    fun incrementProgress() {
        _userMedia.value?.let { userMedia ->
            val newProgress = userMedia.progress + 1
            updateProgress(newProgress)
        }
    }

    fun updateScore(score: Float) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.updateScore(userMedia.id, score)
                _userMedia.value = userMedia.copy(score = score)
            }
        }
    }

    fun updateNotes(notes: String) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.updateNotes(userMedia.id, notes)
                _userMedia.value = userMedia.copy(notes = notes)
            }
        }
    }

    fun toggleFavorite() {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.toggleFavorite(userMedia.id)
                _userMedia.value = userMedia.copy(isFavorite = !userMedia.isFavorite)
            }
        }
    }

    fun addToCollection(collectionId: String) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.addToCollection(userMedia.id, collectionId)
            }
        }
    }

    fun removeFromCollection(collectionId: String) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.removeFromCollection(userMedia.id, collectionId)
            }
        }
    }

    fun removeFromLibrary() {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                removeFromLibraryUseCase(userMedia.id)
                _userMedia.value = null
            }
        }
    }

    fun resetProgress() {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                updateEntryUseCase.resetProgress(userMedia.id)
                _userMedia.value = userMedia.copy(progress = 0, status = MediaStatus.PLANNING)
            }
        }
    }

    // Chapter actions
    fun markChapterRead(chapterId: String, chapterNumber: Double) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                markChapterReadUseCase(userMedia.id, chapterId, chapterNumber).onSuccess { progress ->
                    _chapterProgress.value = _chapterProgress.value + chapterNumber
                    // Update user media progress
                    val readCount = _chapterProgress.value.size
                    _userMedia.value = _userMedia.value?.copy(progress = readCount)
                    
                    // Add activity
                    val entry = com.kanlist.domain.model.ActivityEntry(
                        userMediaId = userMedia.id,
                        mediaId = mediaId,
                        type = com.kanlist.domain.model.ActivityType.CHAPTER_READ,
                        progress = readCount,
                        previousProgress = userMedia.progress
                    )
                    activityUseCases.addEntry(entry)
                    dailyGoalUseCases.increment(java.time.LocalDate.now())
                }
            }
        }
    }

    fun markChapterUnread(chapterId: String) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                markChapterReadUseCase.markUnread(userMedia.id, chapterId)
                _chapterProgress.value = _chapterProgress.value - _chapters.value?.chapters.firstOrNull { it.id == chapterId }?.chapterNumber ?: 0.0
                val readCount = _chapterProgress.value.size
                _userMedia.value = _userMedia.value?.copy(progress = readCount)
            }
        }
    }

    fun markChaptersUpTo(chapterNumber: Double) {
        _userMedia.value?.let { userMedia ->
            viewModelScope.launch {
                markChapterReadUseCase.markUpTo(userMedia.id, chapterNumber).onSuccess { marked ->
                    val newProgress = userMedia.progress + marked
                    _userMedia.value = userMedia.copy(progress = newProgress)
                    _chapterProgress.value = _chapters.value?.chapters
                        .filter { it.chapterNumber <= chapterNumber }
                        .map { it.chapterNumber }
                        .toSet() ?: emptySet()
                    
                    // Add activity
                    val entry = com.kanlist.domain.model.ActivityEntry(
                        userMediaId = userMedia.id,
                        mediaId = mediaId,
                        type = com.kanlist.domain.model.ActivityType.CHAPTER_READ,
                        progress = newProgress,
                        previousProgress = userMedia.progress
                    )
                    activityUseCases.addEntry(entry)
                    dailyGoalUseCases.increment(java.time.LocalDate.now())
                }
            }
        }
    }

    fun toggleTranslation() {
        _showTranslated.value = !_showTranslated.value
    }

    fun updateChapterLanguages(languages: List<ChapterLanguage>) {
        _selectedChapterLanguages.value = languages
        _media.value.getOrNull()?.let { media ->
            loadChapters(media)
        }
    }

    fun refresh() {
        load(mediaId)
    }
}