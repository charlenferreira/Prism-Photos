package com.charlenferreira.prism.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.charlenferreira.prism.data.repository.SettingsRepository
import com.charlenferreira.prism.data.repository.UpdateInfo
import com.charlenferreira.prism.data.repository.UpdateRepository
import com.charlenferreira.prism.ui.theme.PrismTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    val theme: StateFlow<PrismTheme> = settingsRepository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrismTheme.DEEP_TECH_BLUE
    )

    val gridColumns: StateFlow<Int> = settingsRepository.gridColumnsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsRepository.DEFAULT_GRID_COLUMNS
    )

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateStatusMessage = MutableStateFlow<String?>(null)
    val updateStatusMessage: StateFlow<String?> = _updateStatusMessage.asStateFlow()

    init {
        checkForUpdates(manual = false)
    }

    fun selectTheme(prismTheme: PrismTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(prismTheme)
        }
    }

    fun selectGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsRepository.setGridColumns(columns)
        }
    }

    fun checkForUpdates(manual: Boolean = true) {
        if (_isCheckingUpdate.value) return
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            if (manual) {
                _updateStatusMessage.value = null
            }
            updateRepository.checkForUpdate()
                .onSuccess { info ->
                    _updateInfo.value = info
                    if (manual && info == null) {
                        _updateStatusMessage.value = "up_to_date"
                    }
                }
                .onFailure {
                    if (manual) {
                        _updateStatusMessage.value = "error"
                    }
                }
            _isCheckingUpdate.value = false
        }
    }

    fun clearStatusMessage() {
        _updateStatusMessage.value = null
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val updateRepository: UpdateRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, updateRepository) as T
        }
    }
}
