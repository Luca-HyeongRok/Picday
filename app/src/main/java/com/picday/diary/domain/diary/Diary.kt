package com.picday.diary.domain.diary

import java.time.LocalDate

data class Diary(
    val id: String,
    val date: LocalDate,
    val title: String?,
    val content: String,
    val createdAt: Long,
    val coverPhotoUri: String? = null
) {
    val previewContent: String
        get() = content
}
