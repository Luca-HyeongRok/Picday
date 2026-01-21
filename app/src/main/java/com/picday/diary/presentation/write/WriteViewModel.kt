package com.picday.diary.presentation.write

import androidx.lifecycle.ViewModel
import com.picday.diary.domain.usecase.diary.AddDiaryForDateUseCase
import com.picday.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.picday.diary.domain.usecase.diary.GetDiaryByIdUseCase
import com.picday.diary.domain.usecase.diary.GetPhotosUseCase
import com.picday.diary.domain.usecase.diary.ReplacePhotosUseCase
import com.picday.diary.domain.usecase.diary.UpdateDiaryUseCase
import com.picday.diary.presentation.write.photo.WritePhotoItem
import com.picday.diary.presentation.write.photo.WritePhotoState
import com.picday.diary.presentation.write.state.WriteState
import com.picday.diary.presentation.write.state.WriteUiMode
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
import java.util.UUID

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val addDiaryForDate: AddDiaryForDateUseCase,
    private val updateDiary: UpdateDiaryUseCase,
    private val replacePhotos: ReplacePhotosUseCase,
    private val getDiaryById: GetDiaryByIdUseCase,
    private val getPhotos: GetPhotosUseCase,
    private val deleteDiary: DeleteDiaryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(WriteState())
    val uiState: StateFlow<WriteState> = _uiState.asStateFlow()

    private fun updateState(transform: (WriteState) -> WriteState) {
        _uiState.update { current ->
            val next = transform(current)
            next.copy(isDirty = computeWriteIsDirty(next))
        }
    }

    private fun buildBaselineKey(state: WriteState): String {
        return "${state.uiMode}:${state.editingDiaryId ?: "new"}"
    }

    private fun setBaseline(state: WriteState): WriteState {
        val baselinePhotoUris = state.photoItems
            .filter { it.state != WritePhotoState.DELETE }
            .map { it.uri }
        return state.copy(
            baselineKey = buildBaselineKey(state),
            baselineTitle = state.title,
            baselineContent = state.content,
            baselinePhotoUris = baselinePhotoUris
        )
    }

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
            val diary = getDiaryById(diaryId) ?: return@launch
            val photos = getPhotos(diaryId)
            updateState {
                setBaseline(
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
                )
            }
        }
    }

    fun onTitleChanged(title: String) {
        updateState { it.copy(title = title) }
    }

    fun onContentChanged(content: String) {
        updateState { it.copy(content = content) }
    }

    fun onPhotosAdded(uris: List<String>) {
        if (uris.isEmpty()) return
        updateState { state ->
            val newItems = createUniqueNewItems(uris, state.photoItems)
            if (newItems.isEmpty()) state
            else state.copy(photoItems = newItems + state.photoItems)
        }
    }

    fun onPhotoClicked(photoId: String) {
        updateState { state ->
            state.copy(photoItems = reorderWithPriority(photoId, state.photoItems))
        }
    }

    fun onPhotoRemoved(photoId: String) {
        updateState { current ->
            val removedItem = current.photoItems.firstOrNull { it.id == photoId }
            val updated = current.photoItems.map { item ->
                if (item.id == photoId) item.copy(state = WritePhotoState.DELETE) else item
            }
            val pendingRelease = if (removedItem != null && removedItem.uri.startsWith("content://")) {
                if (removedItem.uri in current.pendingReleaseUris) current.pendingReleaseUris
                else current.pendingReleaseUris + removedItem.uri
            } else {
                current.pendingReleaseUris
            }
            current.copy(photoItems = updated, pendingReleaseUris = pendingRelease)
        }
    }

    fun onSave(date: LocalDate, onReleasePersistableUris: (List<String>) -> Unit) {
        val state = _uiState.value
        val normalizedTitle = state.title.trim().ifBlank { null }
        val retainedUris = state.photoItems
            .filter { it.state != WritePhotoState.DELETE }
            .map { it.uri }
        val releaseUris = state.pendingReleaseUris
            .filter { it.startsWith("content://") }
            .filterNot { it in retainedUris }

        viewModelScope.launch(Dispatchers.IO) {
            when (state.uiMode) {
                WriteUiMode.ADD -> {
                    addDiaryForDate(date, normalizedTitle, state.content, retainedUris)
                    if (releaseUris.isNotEmpty()) {
                        onReleasePersistableUris(releaseUris)
                    }
                    resetForView()
                }
                WriteUiMode.EDIT -> {
                    val targetId = state.editingDiaryId ?: return@launch
                    updateDiary(targetId, normalizedTitle, state.content)
                    replacePhotos(targetId, retainedUris)
                    if (releaseUris.isNotEmpty()) {
                        onReleasePersistableUris(releaseUris)
                    }
                    resetForView()
                }
                WriteUiMode.VIEW -> Unit
            }
        }
    }

    fun onDelete(diaryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteDiary(diaryId)
            resetForView()
        }
    }

    fun getCoverPhotoUri(): String? {
        return _uiState.value.photoItems.firstOrNull { item ->
            item.state == WritePhotoState.KEEP || item.state == WritePhotoState.NEW
        }?.uri
    }

    fun getRepresentativePhotoUriForExit(): String? {
        return _uiState.value.photoItems
            .filter { item -> item.state == WritePhotoState.KEEP || item.state == WritePhotoState.NEW }
            .map { it.uri }
            .lastOrNull()
    }

    private fun resetForAdd() {
        updateState {
            setBaseline(
                it.copy(
                uiMode = WriteUiMode.ADD,
                editingDiaryId = null,
                title = "",
                content = "",
                photoItems = emptyList(),
                pendingReleaseUris = emptyList(),
                baselineKey = null,
                baselineTitle = "",
                baselineContent = "",
                baselinePhotoUris = emptyList()
                )
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
                photoItems = emptyList(),
                pendingReleaseUris = emptyList(),
                baselineKey = null,
                baselineTitle = "",
                baselineContent = "",
                baselinePhotoUris = emptyList(),
                isDirty = false
            )
        }
    }

    private fun createUniqueNewItems(uris: List<String>, currentItems: List<WritePhotoItem>): List<WritePhotoItem> {
        val existingUris = currentItems
            .filter { it.state != WritePhotoState.DELETE }
            .map { it.uri }
            .toSet()

        return uris.distinct()
            .filter { it !in existingUris }
            .map { uri ->
                WritePhotoItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    state = WritePhotoState.NEW
                )
            }
    }

    private fun reorderWithPriority(targetId: String, currentItems: List<WritePhotoItem>): List<WritePhotoItem> {
        val items = currentItems.toMutableList()
        val index = items.indexOfFirst { it.id == targetId }
        return if (index > 0) {
            val item = items.removeAt(index)
            items.add(0, item)
            items
        } else {
            currentItems
        }
    }
}
