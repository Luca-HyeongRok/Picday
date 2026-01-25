package com.picday.diary.presentation.main

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import com.picday.diary.core.navigation.AppRoute

@Serializable
sealed interface MainDestination : NavKey, AppRoute {
    @Serializable
    data object Calendar : MainDestination

    @Serializable
    data object Diary : MainDestination

    @Serializable
    data class Write(
        val date: String,
        val mode: String,
        val editDiaryId: String? = null
    ) : MainDestination
}
