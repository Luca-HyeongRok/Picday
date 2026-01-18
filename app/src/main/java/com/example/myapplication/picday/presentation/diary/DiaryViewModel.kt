package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.diary.deriveCoverPhotoUri
import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.time.LocalDate

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    
    val coverPhotoByDate: StateFlow<Map<LocalDate, String?>> = _uiState.asStateFlow()
        .map { it.coverPhotoByDate }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value.coverPhotoByDate)

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
                // 먼저 저장된 대표 사진이 있는지 확인
                val savedCover = settingsRepository.getDateCoverPhotoUri(date).firstOrNull()
                if (savedCover != null) {
                    coverMap[date] = savedCover
                } else {
                    val latestDiary = diariesByDate[date]?.maxByOrNull { it.createdAt }
                    if (latestDiary != null) {
                        val photos = repository.getPhotos(latestDiary.id)
                        coverMap[date] = deriveCoverPhotoUri(photos)
                    } else {
                        coverMap[date] = null
                    }
                }
            }

            _uiState.update { current ->
                current.copy(coverPhotoByDate = current.coverPhotoByDate + coverMap)
            }
        }
    }

    private var updateJob: Job? = null

    private fun updateUiForDate(date: LocalDate) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            val items = repository.getByDate(date)
                .sortedBy { it.createdAt }
            
            // 전수 사진 수집 (기록 생성 순 -> 사진 생성 순)
            val allPhotos = items.flatMap { diary ->
                repository.getPhotos(diary.id).map { it.uri }
            }.distinct()

            // 저장된 대표 사진 가져오기
            val savedCover = settingsRepository.getDateCoverPhotoUri(date).firstOrNull()
            
            // 정렬 및 대표 사진 처리
            val sortedPhotos = if (savedCover != null && allPhotos.contains(savedCover)) {
                // 대표 사진을 가장 앞으로
                listOf(savedCover) + (allPhotos - savedCover)
            } else {
                allPhotos
            }

            var coverForDate: String? = sortedPhotos.firstOrNull()
            
            val uiItems = items.mapIndexed { index, diary ->
                val photos = repository.getPhotos(diary.id)
                val photoUris = photos.map { it.uri }
                val coverPhotoUri = deriveCoverPhotoUri(photos)
                
                DiaryUiItem(
                    id = diary.id,
                    date = diary.date,
                    title = diary.title,
                    previewContent = diary.previewContent,
                    coverPhotoUri = coverPhotoUri,
                    photoUris = photoUris
                )
            }
            _uiState.update { current ->
                current.copy(
                    selectedDate = date,
                    items = items,
                    uiItems = uiItems,
                    allPhotosForDate = sortedPhotos,
                    initialPageIndex = 0, // 위에서 이미 front로 옮겼으므로 항상 0
                    coverPhotoByDate = current.coverPhotoByDate + (date to coverForDate)
                )
            }
        }
    }

    suspend fun saveDateCoverPhoto(date: LocalDate, uri: String) {
        withContext(Dispatchers.IO) {
            settingsRepository.setDateCoverPhotoUri(date, uri)
            // 즉시 UI 반영을 위해 uiState 내의 coverPhotoByDate 업데이트
            _uiState.update { current ->
                current.copy(coverPhotoByDate = current.coverPhotoByDate + (date to uri))
            }
        }
    }
}
