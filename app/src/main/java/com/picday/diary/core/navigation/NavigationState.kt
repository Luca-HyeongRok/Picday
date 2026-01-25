package com.picday.diary.core.navigation

import java.time.LocalDate

data class NavigationState(
    val backStack: List<AppRoute>,
    val selectedDate: LocalDate?
)
