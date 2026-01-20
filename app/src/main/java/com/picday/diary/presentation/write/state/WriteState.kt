package com.picday.diary.presentation.write.state

import com.picday.diary.presentation.write.photo.WritePhotoItem

data class WriteState(
    val uiMode: WriteUiMode = WriteUiMode.VIEW,
    val editingDiaryId: String? = null,
    val title: String = "",
    val content: String = "",
    val photoItems: List<WritePhotoItem> = emptyList(),
    val pendingReleaseUris: List<String> = emptyList(),
    val baselineKey: String? = null,
    val baselineTitle: String = "",
    val baselineContent: String = "",
    val baselinePhotoUris: List<String> = emptyList(),
    val isDirty: Boolean = false
)
