package com.picday.diary.presentation.main

data class NavResult(
    val backStack: List<MainDestination>,
    val effects: List<MainNavEffect> = emptyList()
)
