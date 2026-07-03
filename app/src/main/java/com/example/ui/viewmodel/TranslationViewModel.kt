package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.TranslationHistory
import com.example.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface TranslationUiState {
    object Idle : TranslationUiState
    data class Processing(val message: String, val progress: Float) : TranslationUiState
    data class Success(val history: TranslationHistory) : TranslationUiState
    data class Error(val message: String) : TranslationUiState
}

sealed interface ExportUiState {
    object Idle : ExportUiState
    data class Exporting(val message: String, val progress: Float) : ExportUiState
    data class Success(val videoPath: String, val srtPath: String?) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TranslationRepository
    val historyList: StateFlow<List<TranslationHistory>>

    private val _translationState = MutableStateFlow<TranslationUiState>(TranslationUiState.Idle)
    val translationState: StateFlow<TranslationUiState> = _translationState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _selectedHistoryItem = MutableStateFlow<TranslationHistory?>(null)
    val selectedHistoryItem: StateFlow<TranslationHistory?> = _selectedHistoryItem.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TranslationRepository(application, database.translationHistoryDao())
        historyList = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun startTranslation(videoUri: String, videoTitle: String) {
        viewModelScope.launch {
            _translationState.value = TranslationUiState.Processing("Khởi tạo dịch thuật...", 0.0f)
            try {
                val history = repository.translateVideo(videoUri, videoTitle) { message, progress ->
                    _translationState.value = TranslationUiState.Processing(message, progress)
                }
                _translationState.value = TranslationUiState.Success(history)
                _selectedHistoryItem.value = history
            } catch (e: Exception) {
                _translationState.value = TranslationUiState.Error(e.message ?: "Đã xảy ra lỗi không xác định")
            }
        }
    }

    fun resetTranslationState() {
        _translationState.value = TranslationUiState.Idle
    }

    fun selectHistoryItem(item: TranslationHistory?) {
        _selectedHistoryItem.value = item
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
            if (_selectedHistoryItem.value?.id == id) {
                _selectedHistoryItem.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _selectedHistoryItem.value = null
        }
    }

    fun exportVideo(history: TranslationHistory) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Exporting("Bắt đầu xuất video...", 0.0f)
            try {
                val (videoFile, srtFile) = repository.exportTranslatedVideo(history) { message, progress ->
                    _exportState.value = ExportUiState.Exporting(message, progress)
                }
                _exportState.value = ExportUiState.Success(videoFile.absolutePath, srtFile?.absolutePath)
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(e.message ?: "Lỗi khi xuất video")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }
}
