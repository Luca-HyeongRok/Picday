package com.example.myapplication.picday.presentation.write

import androidx.lifecycle.ViewModel
import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.photo.WritePhotoState
import com.example.myapplication.picday.presentation.write.state.WriteState
import com.example.myapplication.picday.presentation.write.state.WriteUiMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
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
        viewModelScope.launch(Dispatchers.IO) {
            val diary = repository.getDiaryById(diaryId) ?: return@launch
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
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChanged(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun onPhotosAdded(uris: List<String>) {
        if (uris.isEmpty()) return
        
        _uiState.update { state ->
            // 현재 활성화된(삭제되지 않은) 사진들의 URI 집합
            val existingUris = state.photoItems
                .filter { it.state != WritePhotoState.DELETE }
                .map { it.uri }
                .toSet()

            // 중복되지 않은 신규 URI만 필터링 (입력 리스트 자체 내 중복도 제거)
            val newUniqueItems = uris.distinct()
                .filter { it !in existingUris }
                .map { uri ->
                    WritePhotoItem(
                        id = java.util.UUID.randomUUID().toString(),
                        uri = uri,
                        state = WritePhotoState.NEW
                    )
                }

            if (newUniqueItems.isEmpty()) {
                state
            } else {
                // 새로 추가한 사진들이 앞에 오도록 하여 자동으로 대표 사진(첫 번째)으로 지정
                state.copy(photoItems = newUniqueItems + state.photoItems)
            }
        }
    }

    fun onPhotoClicked(photoId: String) {
        _uiState.update { state ->
            val items = state.photoItems.toMutableList()
            val index = items.indexOfFirst { it.id == photoId }
            if (index > 0) {
                // 선택한 사진을 리스트의 맨 앞으로 이동시켜 대표 사진으로 설정
                val item = items.removeAt(index)
                items.add(0, item)
                state.copy(photoItems = items)
            } else {
                state
            }
        }
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

        viewModelScope.launch(Dispatchers.IO) {
            when (state.uiMode) {
                WriteUiMode.ADD -> {
                    repository.addDiaryForDate(date, normalizedTitle, state.content, retainedUris)
                    resetForView()
                }
                WriteUiMode.EDIT -> {
                    val targetId = state.editingDiaryId ?: return@launch
                    repository.updateDiary(targetId, normalizedTitle, state.content)
                    repository.replacePhotos(targetId, retainedUris)
                    resetForView()
                }
                WriteUiMode.VIEW -> Unit
            }
        }
    }

    fun onDelete(diaryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDiary(diaryId)
            resetForView()
        }
    }

    fun getCoverPhotoUri(): String? {
        return _uiState.value.photoItems.firstOrNull { item ->
            item.state == WritePhotoState.KEEP || item.state == WritePhotoState.NEW
        }?.uri
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
