package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.domain.diary.deriveCoverPhotoUri
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.time.LocalDate

@HiltViewModel
class DiaryViewModel @Inject constructor(
    // Repository 생성 책임은 외부로 분리한다.
    private val repository: DiaryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    private val _coverPhotoByDate = MutableStateFlow<Map<LocalDate, String?>>(emptyMap())
    val coverPhotoByDate: StateFlow<Map<LocalDate, String?>> = _coverPhotoByDate.asStateFlow()

    init {
        updateUiForDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        updateUiForDate(date)
    }

    fun hasAnyRecord(date: LocalDate): Boolean {
        return repository.hasAnyRecord(date)
    }

    fun preloadCoverPhotos(dates: List<LocalDate>) {
        if (dates.isEmpty()) return
        val startDate = dates.minOrNull() ?: return
        val endDate = dates.maxOrNull() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            // 초기 렌더링에 우선순위를 주기 위해 아주 약간 지연
            kotlinx.coroutines.delay(100)
            
            // 한 번의 쿼리로 해당 기간의 모든 일기 획득
            val allDiariesInRange = repository.getDiariesByDateRange(startDate, endDate)
            
            // 날짜별로 그룹화
            val diariesByDate = allDiariesInRange.groupBy { it.date }
            
            val coverMap = mutableMapOf<LocalDate, String?>()
            
            // 각 날짜별 최신 일기의 커버 사진 획득
            dates.forEach { date ->
                val latestDiary = diariesByDate[date]?.maxByOrNull { it.createdAt }
                if (latestDiary != null) {
                    val photos = repository.getPhotos(latestDiary.id)
                    coverMap[date] = deriveCoverPhotoUri(photos)
                } else {
                    coverMap[date] = null
                }
            }

            _coverPhotoByDate.update { current ->
                current + coverMap
            }
        }
    }

    private var updateJob: Job? = null

    private fun updateUiForDate(date: LocalDate) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            val items = repository.getByDate(date)
                .sortedBy { it.createdAt }
            var coverForDate: String? = null
            val lastIndex = items.lastIndex
            val uiItems = items.mapIndexed { index, diary ->
                val photos = repository.getPhotos(diary.id)
                val photoUris = photos.map { it.uri }
                val coverPhotoUri = deriveCoverPhotoUri(photos)
                if (index == lastIndex) {
                    coverForDate = coverPhotoUri
                }
                DiaryUiItem(
                    id = diary.id,
                    date = diary.date,
                    title = diary.title,
                    previewContent = diary.previewContent,
                    coverPhotoUri = coverPhotoUri,
                    photoUris = photoUris
                )
            }
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    items = items,
                    uiItems = uiItems
                )
            }
            _coverPhotoByDate.update { current ->
                current + mapOf(date to coverForDate)
            }
        }
    }
}
