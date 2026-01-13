package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val representative: Diary? = null,
    val recentItems: List<Diary> = emptyList(),
    val isLoading: Boolean = false
)
