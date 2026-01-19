package com.picday.diary.domain.diary

data class DiaryPhoto(
    val id: String,
    val diaryId: String,
    val uri: String,
    val createdAt: Long
)
