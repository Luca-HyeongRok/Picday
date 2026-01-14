package com.example.myapplication.picday.presentation.diary

import java.time.LocalDate

data class DiaryUiItem(
    val id: String,
    val date: LocalDate,
    val title: String?,
    val coverPhotoUri: String?
)
