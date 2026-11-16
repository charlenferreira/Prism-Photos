package com.charlenferreira.prism.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.charlenferreira.prism.data.model.ExifMetadata
import com.charlenferreira.prism.data.model.PhotoItem
import com.charlenferreira.prism.data.repository.PhotoRepository
import com.charlenferreira.prism.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data object PermissionRequest : GalleryUiState
    data class Empty(
        val isRawFilterActive: Boolean,
        val isPartialAccess: Boolean
    ) : GalleryUiState

    data class Success(
        val photos: List<PhotoItem>,
        val totalCount: Int,
        val rawCount: Int,
        val isRawFilterActive: Boolean,
        val isPartialAccess: Boolean
    ) : GalleryUiState

    data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _allMedia = MutableStateFlow<List<PhotoItem>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _hasPermission = MutableStateFlow(false)
    private val _isPartialAccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _rawFilterEnabled = MutableStateFlow(false)

    private val _selectedExif = MutableStateFlow<ExifMetadata?>(null)
    val selectedExif: StateFlow<ExifMetadata?> = _selectedExif.asStateFlow()

    private val _isLoadingExif = MutableStateFlow(false)
    val isLoadingExif: StateFlow<Boolean> = _isLoadingExif.asStateFlow()

    val rawFilterEnabled: StateFlow<Boolean> = _rawFilterEnabled.asStateFlow()

    val gridColumns: StateFlow<Int> = settingsRepository.gridColumnsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsRepository.DEFAULT_GRID_COLUMNS
    )

    private data class MediaLoadSnapshot(
        val media: List<PhotoItem>,
        val isLoading: Boolean,
        val hasPermission: Boolean,
        val isPartialAccess: Boolean,
        val errorMessage: String?
    )

    private val loadSnapshot = combine(
        _allMedia,
        _isLoading,
        _hasPermission,
        _isPartialAccess,
        _errorMessage
    ) { media, loading, hasPermission, isPartial, error ->
        MediaLoadSnapshot(
            media = media,
            isLoading = loading,
            hasPermission = hasPermission,
            isPartialAccess = isPartial,
            errorMessage = error
        )
    }

    val uiState: StateFlow<GalleryUiState> = combine(
        loadSnapshot,
        _rawFilterEnabled
    ) { snapshot, rawOnly ->
        when {
            !snapshot.hasPermission && !snapshot.isPartialAccess -> GalleryUiState.PermissionRequest
            snapshot.isLoading -> GalleryUiState.Loading
            snapshot.errorMessage != null -> GalleryUiState.Error(snapshot.errorMessage)
            else -> {
                val filtered = if (rawOnly) {
                    snapshot.media.filter { it.isRaw }
                } else {
                    snapshot.media
                }
                val rawTotal = snapshot.media.count { it.isRaw }
                if (filtered.isEmpty()) {
                    GalleryUiState.Empty(
                        isRawFilterActive = rawOnly,
                        isPartialAccess = snapshot.isPartialAccess
                    )
                } else {
                    GalleryUiState.Success(
                        photos = filtered,
                        totalCount = snapshot.media.size,
                        rawCount = rawTotal,
                        isRawFilterActive = rawOnly,
                        isPartialAccess = snapshot.isPartialAccess
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GalleryUiState.Loading
    )

    fun onPermissionStatusUpdated(hasFullAccess: Boolean, hasPartialAccess: Boolean) {
        val hadAccess = _hasPermission.value || _isPartialAccess.value
        val hasAnyAccess = hasFullAccess || hasPartialAccess
        _hasPermission.value = hasFullAccess
        _isPartialAccess.value = !hasFullAccess && hasPartialAccess

        if (hasAnyAccess && (!hadAccess || _allMedia.value.isEmpty())) {
            refreshMedia()
        } else if (!hasAnyAccess) {
            _isLoading.value = false
            _allMedia.value = emptyList()
        }
    }

    fun refreshMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching {
                photoRepository.loadMedia()
            }.onSuccess { media ->
                _allMedia.value = media
                _isLoading.value = false
            }.onFailure { throwable ->
                _errorMessage.value = throwable.localizedMessage ?: "Erro desconhecido ao ler mídia."
                _isLoading.value = false
            }
        }
    }

    fun toggleRawFilter() {
        _rawFilterEnabled.value = !_rawFilterEnabled.value
    }

    fun setGridColumns(columns: Int) {
        val clamped = columns.coerceIn(
            SettingsRepository.MIN_COLUMNS,
            SettingsRepository.MAX_COLUMNS
        )
        viewModelScope.launch {
            settingsRepository.setGridColumns(clamped)
        }
    }

    fun toggleGridDensity() {
        val nextColumns = if (gridColumns.value <= SettingsRepository.DEFAULT_GRID_COLUMNS) {
            SettingsRepository.MAX_COLUMNS
        } else {
            SettingsRepository.DEFAULT_GRID_COLUMNS
        }
        setGridColumns(nextColumns)
    }

    fun findPhotoIndex(photoId: Long): Int {
        val currentList = when (val state = uiState.value) {
            is GalleryUiState.Success -> state.photos
            else -> _allMedia.value
        }
        return currentList.indexOfFirst { it.id == photoId }.coerceAtLeast(0)
    }

    fun currentDisplayedPhotos(): List<PhotoItem> {
        return when (val state = uiState.value) {
            is GalleryUiState.Success -> state.photos
            else -> if (_rawFilterEnabled.value) {
                _allMedia.value.filter { it.isRaw }
            } else {
                _allMedia.value
            }
        }
    }

    fun loadExif(photo: PhotoItem) {
        viewModelScope.launch {
            _isLoadingExif.value = true
            _selectedExif.value = null
            _selectedExif.value = photoRepository.readExifMetadata(photo)
            _isLoadingExif.value = false
        }
    }

    fun clearExif() {
        _selectedExif.value = null
        _isLoadingExif.value = false
    }

    class Factory(
        private val photoRepository: PhotoRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(photoRepository, settingsRepository) as T
        }
    }
}
