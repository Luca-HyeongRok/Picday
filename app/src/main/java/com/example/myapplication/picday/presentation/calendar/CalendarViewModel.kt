package com.example.myapplication.picday.presentation.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth


import androidx.lifecycle.viewModelScope
import com.example.myapplication.picday.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // 초기 상태는 ViewModel에서 명시적으로 생성
    private val _uiState = MutableStateFlow(
        CalendarUiState(
            currentYearMonth = YearMonth.now(),
            selectedDate = LocalDate.now()
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val backgroundUri: StateFlow<String?> = settingsRepository.calendarBackgroundUri
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        loadCalendarDays()
    }

    /**
     * 현재 월 기준으로 캘린더에 표시할 날짜 목록 생성
     * - 일요일 시작
     * - 항상 6주(42일) 고정
     */
    private fun loadCalendarDays() {
        val currentMonth = _uiState.value.currentYearMonth
        val firstDayOfMonth = currentMonth.atDay(1)

        // DayOfWeek: Monday=1 ... Sunday=7
        // 일요일 시작 기준으로 앞쪽 패딩 계산
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value
        val paddingDays =
            if (firstDayOfWeekValue == 7) 0 else firstDayOfWeekValue

        // 캘린더 그리드의 시작 날짜
        var currentDay = firstDayOfMonth.minusDays(paddingDays.toLong())

        val today = LocalDate.now()
        val days = mutableListOf<CalendarDay>()

        // 6주(42일) 고정 생성
        repeat(42) {
            days.add(
                CalendarDay(
                    date = currentDay,
                    isCurrentMonth = currentDay.month == currentMonth.month,
                    isToday = currentDay == today
                )
            )
            currentDay = currentDay.plusDays(1)
        }

        _uiState.update {
            it.copy(calendarDays = days)
        }
    }

    /** 이전 달로 이동 */
    fun onPreviousMonthClick() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1))
        }
        loadCalendarDays()
    }

    /** 다음 달로 이동 */
    fun onNextMonthClick() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1))
        }
        loadCalendarDays()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = date)
        }
    }

    fun setBackgroundUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setCalendarBackgroundUri(uri)
        }
    }
}
