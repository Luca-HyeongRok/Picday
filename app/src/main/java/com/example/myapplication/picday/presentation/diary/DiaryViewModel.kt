package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.presentation.diary.write.WriteUiMode
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

data class WriteState(
    val uiMode: WriteUiMode = WriteUiMode.VIEW,
    val editingDiaryId: String? = null
)

private fun seedData(): List<Diary> {
    return listOf(
        Diary(
            id = "1",
            date = LocalDate.of(2023, 10, 5),
            title = "카페 방문",
            content = "도심에 있는 아늑한 카페를 발견했다. 라떼 아트가 훌륭했고 책 읽기 딱 좋은 분위기였다.",
            createdAt = 1696494000000
        ),
        Diary(
            id = "2",
            date = LocalDate.of(2023, 10, 4),
            title = "프로젝트 마감",
            content = "드디어 회사 프로젝트를 끝냈다. 스트레스도 많았지만 보람찬 경험이었다. 이제 좀 쉬자!",
            createdAt = 1696407600000
        ),
        Diary(
            id = "3",
            date = LocalDate.of(2023, 10, 1),
            title = "가을 등산",
            content = "친구들과 함께 등산을 다녀왔다. 정상에서 본 풍경은 정말 숨이 멎을 듯 아름다웠다. 다리는 아프지만 가치가 있었다.",
            createdAt = 1696148400000
        ),
        Diary(
            id = "4",
            date = LocalDate.of(2023, 9, 28),
            title = "비 오는 날",
            content = "하루 종일 비가 내렸다. 집에서 영화를 보고 쿠키를 구우며 시간을 보냈다.",
            createdAt = 1695889200000
        )
    )
}
