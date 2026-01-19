package com.picday.diary.presentation.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth


import androidx.lifecycle.viewModelScope
import com.picday.diary.domain.repository.SettingsRepository
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
     * - 주(Week) 단위로 필요한 만큼만 행 생성
     */
    private fun loadCalendarDays() {
        val currentMonth = _uiState.value.currentYearMonth
        val firstDayOfMonth = currentMonth.atDay(1)

        // 일요일 시작 기준으로 앞쪽 패딩 계산
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value
        val paddingDays = if (firstDayOfWeekValue == 7) 0 else firstDayOfWeekValue

        // 총 필요한 날짜 수 계산 (첫날 패딩 + 해당 월 일 수)
        val totalDaysInView = paddingDays + currentMonth.lengthOfMonth()
        // 필요한 주(Rows) 수 계산 (올림 처리)
        val rows = if (totalDaysInView % 7 == 0) totalDaysInView / 7 else (totalDaysInView / 7) + 1
        val totalDaysToGenerate = rows * 7

        var currentDay = firstDayOfMonth.minusDays(paddingDays.toLong())
        val today = LocalDate.now()
        val days = mutableListOf<CalendarDay>()

        repeat(totalDaysToGenerate) {
            val holidayName = getHolidayName(currentDay)
            days.add(
                CalendarDay(
                    date = currentDay,
                    isCurrentMonth = currentDay.month == currentMonth.month,
                    isToday = currentDay == today,
                    isHoliday = holidayName != null || currentDay.dayOfWeek.value == 7, // 일요일도 휴일 처리
                    holidayName = holidayName
                )
            )
            currentDay = currentDay.plusDays(1)
        }

        _uiState.update {
            it.copy(calendarDays = days)
        }
    }

    /** 2026년 주요 공휴일 데이터 */
    private fun getHolidayName(date: LocalDate): String? {
        if (date.year != 2026) return null
        return when (date.monthValue) {
            1 -> if (date.dayOfMonth == 1) "신정" else null
            2 -> when (date.dayOfMonth) {
                16 -> "설날 연휴"
                17 -> "설날"
                18 -> "설날 연휴"
                else -> null
            }
            3 -> when (date.dayOfMonth) {
                1 -> "삼일절"
                2 -> "대체공휴일"
                else -> null
            }
            5 -> when (date.dayOfMonth) {
                5 -> "어린이날"
                24 -> "부처님오신날"
                25 -> "대체공휴일"
                else -> null
            }
            6 -> if (date.dayOfMonth == 6) "현충일" else null
            8 -> if (date.dayOfMonth == 15) "광복절" else null
            9 -> when (date.dayOfMonth) {
                24 -> "추석 연휴"
                25 -> "추석"
                26 -> "추석 연휴"
                else -> null
            }
            10 -> when (date.dayOfMonth) {
                3 -> "개천절"
                5 -> "대체공휴일"
                9 -> "한글날"
                else -> null
            }
            12 -> if (date.dayOfMonth == 25) "성탄절" else null
            else -> null
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
