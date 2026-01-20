package com.picday.diary.presentation.write

import com.picday.diary.presentation.write.photo.WritePhotoState
import com.picday.diary.presentation.write.state.WriteState
import com.picday.diary.presentation.write.state.WriteUiMode

internal fun computeWriteIsDirty(state: WriteState): Boolean {
    if (state.uiMode == WriteUiMode.VIEW) return false
    if (state.baselineKey == null) return false

    val currentPhotoUris = state.photoItems
        .filter { it.state != WritePhotoState.DELETE }
        .map { it.uri }

    return state.title != state.baselineTitle ||
        state.content != state.baselineContent ||
        currentPhotoUris != state.baselinePhotoUris
}
