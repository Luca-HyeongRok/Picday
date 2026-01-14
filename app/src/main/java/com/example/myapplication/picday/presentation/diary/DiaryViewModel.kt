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

    init {
        updateUiForDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        updateUiForDate(date)
    }

    fun hasAnyRecord(date: LocalDate): Boolean {
        return repository.hasAnyRecord(date)
    }

    fun getDiaryUiItemForDate(date: LocalDate): DiaryUiItem? {
        val diary = repository.getByDate(date).lastOrNull() ?: return null
        val photos = repository.getPhotos(diary.id)
        return DiaryUiItem(
            id = diary.id,
            date = diary.date,
            title = diary.title,
            previewContent = diary.previewContent,
            coverPhotoUri = deriveCoverPhotoUri(photos)
        )
    }

    private fun updateUiForDate(date: LocalDate) {
        val items = repository.getByDate(date)
        val uiItems = items.map { diary ->
            val photos = repository.getPhotos(diary.id)
            DiaryUiItem(
                id = diary.id,
                date = diary.date,
                title = diary.title,
                previewContent = diary.previewContent,
                coverPhotoUri = deriveCoverPhotoUri(photos)
            )
        }
        _uiState.update {
            it.copy(
                selectedDate = date,
                items = items,
                uiItems = uiItems
            )
        }
    }
}
