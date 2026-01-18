package com.example.myapplication.picday.presentation.main

sealed interface MainNavEffect {
    data object PopOne : MainNavEffect
    data object PopToRoot : MainNavEffect
    data class ConsumeEditDiary(val diaryId: String) : MainNavEffect
}
