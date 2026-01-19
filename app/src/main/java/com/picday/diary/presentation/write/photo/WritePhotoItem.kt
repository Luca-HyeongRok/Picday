package com.picday.diary.presentation.write.photo

data class WritePhotoItem(
    val id: String,
    val uri: String,
    val state: WritePhotoState
)
