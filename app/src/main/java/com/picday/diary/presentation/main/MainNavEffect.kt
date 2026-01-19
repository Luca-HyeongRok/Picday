package com.picday.diary.presentation.main

sealed interface MainNavEffect {
    data object PopOne : MainNavEffect
    data object PopToRoot : MainNavEffect
    data class ConsumeEditDiary(val diaryId: String) : MainNavEffect
}
