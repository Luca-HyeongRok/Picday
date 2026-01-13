package com.example.myapplication.picday.presentation.diary

import androidx.lifecycle.ViewModel
import com.example.myapplication.picday.data.diary.DiaryRepository
import com.example.myapplication.picday.data.diary.InMemoryDiaryRepository
import com.example.myapplication.picday.domain.diary.Diary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class DiaryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    private val repository: DiaryRepository = InMemoryDiaryRepository(seedData())

    init {
        updateUiForDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        updateUiForDate(date)
    }

    fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        val newItem = Diary(
            id = System.currentTimeMillis().toString(),
            date = date,
            title = title,
            previewContent = content
        )
        repository.addDiary(newItem)
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
        return repository.getDiaries(date).isNotEmpty()
    }

    private fun updateUiForDate(date: LocalDate) {
        val items = repository.getDiaries(date)
        _uiState.update {
            it.copy(
                selectedDate = date,
                items = items
            )
        }
    }
}

private fun seedData(): List<Diary> {
    return listOf(
        Diary(
            id = "1",
            date = LocalDate.of(2023, 10, 5),
            title = "카페 방문",
            previewContent = "도심에 있는 아늑한 카페를 발견했다. 라떼 아트가 훌륭했고 책 읽기 딱 좋은 분위기였다."
        ),
        Diary(
            id = "2",
            date = LocalDate.of(2023, 10, 4),
            title = "프로젝트 마감",
            previewContent = "드디어 회사 프로젝트를 끝냈다. 스트레스도 많았지만 보람찬 경험이었다. 이제 좀 쉬자!"
        ),
        Diary(
            id = "3",
            date = LocalDate.of(2023, 10, 1),
            title = "가을 등산",
            previewContent = "친구들과 함께 등산을 다녀왔다. 정상에서 본 풍경은 정말 숨이 멎을 듯 아름다웠다. 다리는 아프지만 가치가 있었다."
        ),
        Diary(
            id = "4",
            date = LocalDate.of(2023, 9, 28),
            title = "비 오는 날",
            previewContent = "하루 종일 비가 내렸다. 집에서 영화를 보고 쿠키를 구우며 시간을 보냈다."
        )
    )
}
