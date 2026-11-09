package com.kanlist.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.model.MediaType
import com.kanlist.domain.model.UserPreferences
import com.kanlist.domain.model.UserStats
import com.kanlist.domain.usecase.StatsUseCases
import com.kanlist.domain.usecase.PreferencesUseCases
import com.kanlist.domain.usecase.BackupUseCases
import com.kanlist.domain.usecase.DailyGoalUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val statsUseCases: StatsUseCases,
    private val preferencesUseCases: PreferencesUseCases,
    private val backupUseCases: BackupUseCases,
    private val dailyGoalUseCases: DailyGoalUseCases
) : ViewModel() {

    private val _stats = MutableStateFlow<UserStats>(UserStats())
    val stats = _stats.distinctUntilChanged()

    private val _preferences = MutableStateFlow<UserPreferences>(UserPreferences())
    val preferences = _preferences.distinctUntilChanged()

    private val _streak = MutableStateFlow(0)
    val streak = _streak.distinctUntilChanged()

    private val _weeklyActivity = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val weeklyActivity = _weeklyActivity.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.distinctUntilChanged()

    sealed interface BackupState {
        data class Exporting(val progress: Float) : BackupState
        data class Importing(val progress: Float) : BackupState
        data class Success(val message: String) : BackupState
        data class Error(val message: String) : BackupState
        object Idle : BackupState
    }

    init {
        loadData()
        observePreferences()
        observeStreak()
        observeWeeklyActivity()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _stats.value = statsUseCases.get().getOrDefault(UserStats())
            _preferences.value = preferencesUseCases.get().getOrDefault(UserPreferences())
            _isLoading.value = false
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesUseCases.observe().collect { prefs ->
                _preferences.value = prefs
            }
        }
    }

    private fun observeStreak() {
        viewModelScope.launch {
            dailyGoalUseCases.observeCurrentStreak().collect { streak ->
                _streak.value = streak
            }
        }
    }

    private fun observeWeeklyActivity() {
        viewModelScope.launch {
            // Would observe activity for last 14 days
            _weeklyActivity.value = emptyMap()
        }
    }

    // Preferences actions
    fun updateName(name: String) {
        viewModelScope.launch {
            preferencesUseCases.updateName(name)
        }
    }

    fun updateTheme(theme: com.kanlist.domain.model.ThemeMode) {
        viewModelScope.launch {
            preferencesUseCases.updateTheme(theme)
        }
    }

    fun updateTitleLanguage(language: com.kanlist.domain.model.TitleLanguage) {
        viewModelScope.launch {
            preferencesUseCases.updateTitleLanguage(language)
        }
    }

    fun updateDailyGoalTarget(target: Int) {
        viewModelScope.launch {
            preferencesUseCases.updateDailyGoalTarget(target)
        }
    }

    fun updateChapterLanguages(languages: List<com.kanlist.domain.model.ChapterLanguage>) {
        viewModelScope.launch {
            preferencesUseCases.updateChapterLanguages(languages)
        }
    }

    fun updateAutoTranslateSynopsis(enabled: Boolean) {
        viewModelScope.launch {
            preferencesUseCases.updateAutoTranslateSynopsis(enabled)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesUseCases.updateHapticFeedback(enabled)
        }
    }

    fun updateShowAdultContent(show: Boolean) {
        viewModelScope.launch {
            preferencesUseCases.updateShowAdultContent(show)
        }
    }

    fun updateEnabledSources(sources: List<com.kanlist.domain.model.MediaSource>) {
        viewModelScope.launch {
            preferencesUseCases.updateEnabledSources(sources)
        }
    }

    fun recalculateStats() {
        viewModelScope.launch {
            _isLoading.value = true
            _stats.value = statsUseCases.recalculate().getOrDefault(UserStats())
            _isLoading.value = false
        }
    }

    // Backup actions
    fun exportBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting(0f)
            val result = backupUseCases.export()
            result.onSuccess { json ->
                _backupState.value = BackupState.Success("Backup exportado! Copiado para a área de transferência.")
                // Copy to clipboard
                val clipboard = com.kanlist.KanlistApplication.instance?.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Kanlist Backup", json))
            }.onFailure { error ->
                _backupState.value = BackupState.Error("Erro ao exportar: ${error.message}")
            }
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Importing(0f)
            val result = backupUseCases.import(json)
            result.onSuccess {
                _backupState.value = BackupState.Success("Backup importado com sucesso! Reiniciando...")
                // Would need to restart app or reload data
            }.onFailure { error ->
                _backupState.value = BackupState.Error("Erro ao importar: ${error.message}")
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            backupUseCases.clearCache()
            _backupState.value = BackupState.Success("Cache limpo!")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _backupState.value = BackupState.Exporting(0f)
            backupUseCases.clearAllData()
            _backupState.value = BackupState.Success("Todos os dados foram apagados.")
        }
    }

    // Stats helpers
    fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        if (hours > 0) return "${hours}h ${mins}min"
        return "${mins}min"
    }

    fun getMediaTypeDistribution(): List<Pair<MediaType, Int>> {
        return _stats.value.mediaByType.toList().sortedByDescending { it.second }
    }

    fun getStatusDistribution(): List<Pair<com.kanlist.domain.model.MediaStatus, Int>> {
        return _stats.value.mediaByStatus.toList().sortedBy { it.key.order }
    }
}