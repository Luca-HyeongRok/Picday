package com.example.myapplication.picday.presentation.write

import androidx.lifecycle.ViewModel
import com.example.myapplication.picday.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WriteState())
    val uiState: StateFlow<WriteState> = _uiState.asStateFlow()

    fun setMode(uiMode: WriteUiMode) {
        when (uiMode) {
            WriteUiMode.ADD -> resetForAdd()
            WriteUiMode.VIEW -> resetForView()
            WriteUiMode.EDIT -> resetForView()
        }
    }

    fun onAddClicked() {
        resetForAdd()
    }

    fun onEditClicked(diaryId: String) {
        val diary = repository.getDiaryById(diaryId) ?: return
        val photos = repository.getPhotos(diaryId)
        _uiState.update {
            it.copy(
                uiMode = WriteUiMode.EDIT,
                editingDiaryId = diaryId,
                title = diary.title.orEmpty(),
                content = diary.content,
                photoItems = photos.map { photo ->
                    WritePhotoItem(
                        id = photo.id,
                        uri = photo.uri,
                        state = WritePhotoState.KEEP
                    )
                }
            )
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChanged(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun onPhotosAdded(uris: List<String>) {
        if (uris.isEmpty()) return
        val newItems = uris.map { uri ->
            WritePhotoItem(
                id = System.nanoTime().toString(),
                uri = uri,
                state = WritePhotoState.NEW
            )
        }
        _uiState.update { it.copy(photoItems = it.photoItems + newItems) }
    }

    fun onPhotoRemoved(photoId: String) {
        _uiState.update { current ->
            val updated = current.photoItems.map { item ->
                if (item.id == photoId) item.copy(state = WritePhotoState.DELETE) else item
            }
            current.copy(photoItems = updated)
        }
    }

    fun onSave(date: LocalDate) {
        val state = _uiState.value
        val normalizedTitle = state.title.trim().ifBlank { null }
        val retainedUris = state.photoItems
            .filter { it.state != WritePhotoState.DELETE }
            .map { it.uri }

        when (state.uiMode) {
            WriteUiMode.ADD -> {
                repository.addDiaryForDate(date, normalizedTitle, state.content, retainedUris)
                resetForView()
            }
            WriteUiMode.EDIT -> {
                val targetId = state.editingDiaryId ?: return
                repository.updateDiary(targetId, normalizedTitle, state.content)
                repository.replacePhotos(targetId, retainedUris)
                resetForView()
            }
            WriteUiMode.VIEW -> Unit
        }
    }

    private fun resetForAdd() {
        _uiState.update {
            it.copy(
                uiMode = WriteUiMode.ADD,
                editingDiaryId = null,
                title = "",
                content = "",
                photoItems = emptyList()
            )
        }
    }

    private fun resetForView() {
        _uiState.update {
            it.copy(
                uiMode = WriteUiMode.VIEW,
                editingDiaryId = null,
                title = "",
                content = "",
                photoItems = emptyList()
            )
        }
    }
}
