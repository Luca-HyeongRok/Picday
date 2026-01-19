package com.picday.diary.presentation.diary

import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val uiItems: List<DiaryUiItem> = emptyList(),
    val allPhotosForDate: List<String> = emptyList(),
    val coverPhotoByDate: Map<LocalDate, String?> = emptyMap()
)
