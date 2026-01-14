package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val items: List<Diary> = emptyList(),
    val uiItems: List<DiaryUiItem> = emptyList(),
    val isLoading: Boolean = false
)
