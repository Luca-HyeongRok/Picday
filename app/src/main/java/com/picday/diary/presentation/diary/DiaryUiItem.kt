package com.picday.diary.presentation.diary

import java.time.LocalDate

data class DiaryUiItem(
    val id: String,
    val date: LocalDate,
    val title: String?,
    val previewContent: String,
    val coverPhotoUri: String?,
    val photoUris: List<String> = emptyList()
)
