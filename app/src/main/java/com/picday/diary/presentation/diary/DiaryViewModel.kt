package com.picday.diary.presentation.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.deriveCoverPhotoUri
import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.collections.get

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
            delay(100)
            
            val allDiariesInRange = repository.getDiariesByDateRange(startDate, endDate)
            val diariesByDate = allDiariesInRange.groupBy { it.date }
            val coverMap = mutableMapOf<LocalDate, String?>()
            
            dates.forEach { date ->
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
            val domainItems = repository.getByDate(date)
                .sortedBy { it.createdAt }

            val uiItems = fetchUiItems(domainItems)
            val sortedPhotos = computeSortedPhotos(date, uiItems)
            val coverForDate = sortedPhotos.firstOrNull()

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
            val photos = repository.getPhotos(diary.id)
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

    private suspend fun computeSortedPhotos(date: LocalDate, uiItems: List<DiaryUiItem>): List<String> {
        val allPhotos = uiItems.flatMap { it.photoUris }.distinct()
        val savedCover = settingsRepository.getDateCoverPhotoUri(date).firstOrNull()

        return if (savedCover != null && allPhotos.contains(savedCover)) {
            listOf(savedCover) + (allPhotos - savedCover)
        } else {
            allPhotos
        }
    }

    suspend fun saveDateCoverPhoto(date: LocalDate, uri: String) {
        withContext(Dispatchers.IO) {
            settingsRepository.setDateCoverPhotoUri(date, uri)
            _uiState.update { current ->
                current.copy(coverPhotoByDate = current.coverPhotoByDate + (date to uri))
            }
        }
    }
}
