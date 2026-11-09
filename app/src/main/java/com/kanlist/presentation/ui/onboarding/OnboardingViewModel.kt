package com.kanlist.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanlist.domain.usecase.PreferencesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesUseCases: PreferencesUseCases
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.distinctUntilChanged()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.distinctUntilChanged()

    fun setName(name: String) {
        _name.value = name.trim()
        _error.value = null
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        val name = _name.value
        if (name.isBlank()) {
            _error.value = "Por favor, insira seu nome ou apelido"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            preferencesUseCases.updateName(name).onSuccess {
                _isLoading.value = false
                onComplete()
            }.onFailure { error ->
                _isLoading.value = false
                _error.value = "Erro ao salvar: ${error.message}"
            }
        }
    }
}