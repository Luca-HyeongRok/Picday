package com.example.myapplication.picday.domain.diary

import java.time.LocalDate

data class Diary(
    val id: String,
    val date: LocalDate,
    val title: String,
    val previewContent: String
)

data class DayDiary(
    val representative: Diary?,
    val recent: List<Diary>
)
