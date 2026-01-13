package com.example.myapplication.picday.presentation.diary

import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val representative: DiaryItem? = null,
    val recentItems: List<DiaryItem> = emptyList(),
    val isLoading: Boolean = false
)

data class DiaryItem(
    val id: String,
    val date: String,
    val title: String,
    val previewContent: String
)
