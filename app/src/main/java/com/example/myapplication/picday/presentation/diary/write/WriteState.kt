package com.example.myapplication.picday.presentation.diary.write

data class WriteState(
    val uiMode: WriteUiMode = WriteUiMode.VIEW,
    val editingDiaryId: String? = null,
    val title: String = "",
    val content: String = "",
    val photoItems: List<WritePhotoItem> = emptyList()
)
