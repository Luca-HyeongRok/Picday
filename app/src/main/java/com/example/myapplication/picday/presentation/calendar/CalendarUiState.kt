package com.example.myapplication.picday.presentation.calendar

import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val currentYearMonth: YearMonth,
    val calendarDays: List<CalendarDay> = emptyList(),
    val isLoading: Boolean = false
)

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false
)

