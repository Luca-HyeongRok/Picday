package com.example.myapplication.picday.presentation.write.state

import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem

data class WriteState(
    val uiMode: WriteUiMode = WriteUiMode.VIEW,
    val editingDiaryId: String? = null,
    val title: String = "",
    val content: String = "",
    val photoItems: List<WritePhotoItem> = emptyList()
)
