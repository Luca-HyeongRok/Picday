package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<Diary> = emptyList(),
    val uiItems: List<DiaryUiItem> = emptyList(),
    val allPhotosForDate: List<String> = emptyList(),
    val initialPageIndex: Int = 0,
    val isLoading: Boolean = false,
    val coverPhotoByDate: Map<LocalDate, String?> = emptyMap()
)
