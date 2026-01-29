package com.picday.diary.presentation.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.deriveCoverPhotoUri
import com.picday.diary.domain.usecase.diary.GetDiariesByDateRangeUseCase
import com.picday.diary.domain.usecase.diary.GetDiariesByDateUseCase
import com.picday.diary.domain.usecase.diary.GetPhotosUseCase
import com.picday.diary.domain.usecase.diary.HasAnyRecordUseCase
import com.picday.diary.domain.usecase.settings.GetDateCoverPhotoUseCase
import com.picday.diary.domain.usecase.settings.SetDateCoverPhotoUseCase
import androidx.lifecycle.ViewModel
import com.picday.diary.domain.updater.CalendarWidgetUpdater
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiariesByDate: GetDiariesByDateUseCase,
    private val getDiariesByDateRange: GetDiariesByDateRangeUseCase,
    private val getPhotos: GetPhotosUseCase,
    private val hasAnyRecordUseCase: HasAnyRecordUseCase,
    private val getDateCoverPhoto: GetDateCoverPhotoUseCase,
    private val setDateCoverPhoto: SetDateCoverPhotoUseCase,
    private val widgetUpdater: CalendarWidgetUpdater
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

    fun moveDateBy(days: Long) {
        val next = _uiState.value.selectedDate.plusDays(days)
        updateUiForDate(next)
    }

    suspend fun hasAnyRecord(date: LocalDate): Boolean {
        return hasAnyRecordUseCase(date)
    }

    fun preloadCoverPhotos(dates: List<LocalDate>) {
        if (dates.isEmpty()) return
        val startDate = dates.minOrNull() ?: return
        val endDate = dates.maxOrNull() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            delay(100)
            
            val allDiariesInRange = getDiariesByDateRange(startDate, endDate)
            val diariesByDate = allDiariesInRange.groupBy { it.date }
            val coverMap = mutableMapOf<LocalDate, String?>()
            
            dates.forEach { date ->
                val savedCover = getDateCoverPhoto(date).firstOrNull()
                if (savedCover != null) {
                    coverMap[date] = savedCover
                } else {
                    coverMap[date] = null
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
            val domainItems = getDiariesByDate(date)
                .sortedBy { it.createdAt }

            val uiItems = fetchUiItems(domainItems)
            val savedCover = getDateCoverPhoto(date).firstOrNull()
            val sortedPhotos = computeSortedPhotos(uiItems, savedCover)
            val coverForDate = savedCover ?: sortedPhotos.firstOrNull()

            _uiState.update { current ->
                current.copy(
                    selectedDate = date,
                    uiItems = uiItems,
                    allPhotosForDate = sortedPhotos,
                    coverPhotoByDate = current.coverPhotoByDate + (date to coverForDate)
                )
            }
        }
    }

    private suspend fun fetchUiItems(items: List<Diary>): List<DiaryUiItem> {
        return items.map { diary ->
            val photos = getPhotos(diary.id)
            DiaryUiItem(
                id = diary.id,
                date = diary.date,
                title = diary.title,
                previewContent = diary.previewContent,
                coverPhotoUri = deriveCoverPhotoUri(photos),
                photoUris = photos.map { it.uri }
            )
        }
    }

    private fun computeSortedPhotos(uiItems: List<DiaryUiItem>, savedCover: String?): List<String> {
        val photos = uiItems.flatMap { it.photoUris }.distinct()
        if (savedCover.isNullOrBlank()) {
            return photos
        }

        // Ensure saved cover is first without duplication.
        val withoutCover = photos.filterNot { it == savedCover }
        return listOf(savedCover) + withoutCover
    }

    suspend fun saveDateCoverPhoto(date: LocalDate, uri: String?) {
        withContext(Dispatchers.IO) {
            setDateCoverPhoto(date, uri)
        }
        _uiState.update { current ->
            current.copy(coverPhotoByDate = current.coverPhotoByDate + (date to uri))
        }
        widgetUpdater.updateAll()
    }
}
