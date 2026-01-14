package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.domain.diary.deriveCoverPhotoUri
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
        val coverMap = dates.associateWith { date ->
            val diary = repository.getByDate(date)
                .maxByOrNull { it.createdAt }
                ?: return@associateWith null
            val photos = repository.getPhotos(diary.id)
            deriveCoverPhotoUri(photos)
        }
        _coverPhotoByDate.update { current ->
            current + coverMap
        }
    }

    private fun updateUiForDate(date: LocalDate) {
        val items = repository.getByDate(date)
            .sortedBy { it.createdAt }
        var coverForDate: String? = null
        val lastIndex = items.lastIndex
        val uiItems = items.mapIndexed { index, diary ->
            val photos = repository.getPhotos(diary.id)
            val coverPhotoUri = deriveCoverPhotoUri(photos)
            if (index == lastIndex) {
                coverForDate = coverPhotoUri
            }
            DiaryUiItem(
                id = diary.id,
                date = diary.date,
                title = diary.title,
                previewContent = diary.previewContent,
                coverPhotoUri = coverPhotoUri
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
