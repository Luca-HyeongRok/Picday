package com.example.myapplication.picday.presentation.diary

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class DiaryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { 
            it.copy(
                diaryList = listOf(
                    DiaryItem(
                        id = "1",
                        date = "2023.10.05",
                        title = "카페 방문",
                        previewContent = "도심에 있는 아늑한 카페를 발견했다. 라떼 아트가 훌륭했고 책 읽기 딱 좋은 분위기였다."
                    ),
                    DiaryItem(
                        id = "2",
                        date = "2023.10.04",
                        title = "프로젝트 마감",
                        previewContent = "드디어 회사 프로젝트를 끝냈다. 스트레스도 많았지만 보람찬 경험이었다. 이제 좀 쉬자!"
                    ),
                    DiaryItem(
                        id = "3",
                        date = "2023.10.01",
                        title = "가을 등산",
                        previewContent = "친구들과 함께 등산을 다녀왔다. 정상에서 본 풍경은 정말 숨이 멎을 듯 아름다웠다. 다리는 아프지만 가치가 있었다."
                    ),
                    DiaryItem(
                        id = "4",
                        date = "2023.09.28",
                        title = "비 오는 날",
                        previewContent = "하루 종일 비가 내렸다. 집에서 영화를 보고 쿠키를 구우며 시간을 보냈다."
                    )
                )
            ) 
        }
    }
    fun addDiaryForDate(date: LocalDate) {
        val formattedDate = "%04d.%02d.%02d".format(date.year, date.monthValue, date.dayOfMonth)
        val newItem = DiaryItem(
            id = System.currentTimeMillis().toString(),
            date = formattedDate,
            title = "새로운 기록",
            previewContent = "이 날의 추가적인 기록입니다. 정말 멋진 하루였어요."
        )
        _uiState.update { 
            it.copy(diaryList = listOf(newItem) + it.diaryList)
        }
    }
}
