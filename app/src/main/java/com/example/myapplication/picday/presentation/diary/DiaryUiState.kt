package com.example.myapplication.picday.presentation.diary

data class DiaryUiState(
    val diaryList: List<DiaryItem> = emptyList(),
    val isLoading: Boolean = false
)

data class DiaryItem(
    val id: String,
    val date: String,
    val title: String,
    val previewContent: String
)
