package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.presentation.diary.write.WriteUiMode
import com.example.myapplication.picday.presentation.diary.write.WriteState
import com.example.myapplication.picday.presentation.navigation.WriteMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

@HiltViewModel
class DiaryViewModel @Inject constructor(
    // Repository 생성 책임은 외부로 분리한다.
    private val repository: DiaryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    private val _writeState = MutableStateFlow(WriteState())
    val writeState: StateFlow<WriteState> = _writeState.asStateFlow()

    init {
        updateUiForDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        updateUiForDate(date)
    }

    // WriteScreen 진입/모드 변경 시 ViewModel에서 모드 전환을 관리
    fun setWriteMode(mode: WriteMode) {
        _writeState.update {
            it.copy(
                uiMode = if (mode == WriteMode.ADD) WriteUiMode.ADD else WriteUiMode.VIEW,
                editingDiaryId = null
            )
        }
    }

    fun onAddClicked() {
        _writeState.update { it.copy(uiMode = WriteUiMode.ADD, editingDiaryId = null) }
    }

    fun onEditClicked(diaryId: String) {
        _writeState.update { it.copy(uiMode = WriteUiMode.EDIT, editingDiaryId = diaryId) }
    }

    fun onSaveClicked(date: LocalDate, title: String?, content: String) {
        when (_writeState.value.uiMode) {
            WriteUiMode.ADD -> addDiaryForDate(date, title, content)
            WriteUiMode.EDIT -> {
                val targetId = _writeState.value.editingDiaryId
                if (targetId != null) {
                    updateDiary(targetId, title, content)
                }
            }
            WriteUiMode.VIEW -> Unit
        }
    }

    fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        repository.addDiaryForDate(date, title, content)
        if (_uiState.value.selectedDate == date) {
            updateUiForDate(date)
        }
    }

    fun updateDiary(diaryId: String, title: String?, content: String) {
        if (repository.updateDiary(diaryId, title, content)) {
            updateUiForDate(_uiState.value.selectedDate)
        }
    }

    fun hasAnyRecord(date: LocalDate): Boolean {
        return repository.hasAnyRecord(date)
    }

    private fun updateUiForDate(date: LocalDate) {
        val items = repository.getByDate(date)
        _uiState.update {
            it.copy(
                selectedDate = date,
                items = items
            )
        }
    }
}
