package com.example.myapplication.diary.presentation.calendar

import app.cash.turbine.test
import com.picday.diary.domain.usecase.calendar.ObserveMonthlyDiariesUseCase
import com.picday.diary.domain.usecase.settings.ObserveCalendarBackgroundUseCase
import com.picday.diary.domain.usecase.settings.SetCalendarBackgroundUseCase
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import com.picday.diary.presentation.calendar.CalendarViewModel
import com.picday.diary.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var diaryRepository: FakeDiaryRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository()
        diaryRepository = FakeDiaryRepository()
        viewModel = CalendarViewModel(
            observeCalendarBackground = ObserveCalendarBackgroundUseCase(settingsRepository),
            setCalendarBackground = SetCalendarBackgroundUseCase(settingsRepository),
            observeMonthlyDiaries = ObserveMonthlyDiariesUseCase(
                diaryRepository = diaryRepository,
                settingsRepository = settingsRepository
            )
        )
    }

    @Test
    fun `이전 달 이동 시 월이 감소한다`() {
        val initial = viewModel.uiState.value.currentYearMonth
        viewModel.onPreviousMonthClick()
        assertEquals(initial.minusMonths(1), viewModel.uiState.value.currentYearMonth)
    }

    @Test
    fun `다음 달 이동 시 월이 증가한다`() {
        val initial = viewModel.uiState.value.currentYearMonth
        viewModel.onNextMonthClick()
        assertEquals(initial.plusMonths(1), viewModel.uiState.value.currentYearMonth)
    }

    @Test
    fun `날짜 선택 시 selectedDate가 갱신된다`() {
        val date = LocalDate.of(2024, 6, 1)
        viewModel.onDateSelected(date)
        assertEquals(date, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `배경 설정 시 backgroundUri가 갱신된다`() = runTest {
        viewModel.backgroundUri.test {
            assertEquals(null, awaitItem())
            viewModel.setBackgroundUri("uri_bg")
            assertEquals("uri_bg", awaitItem())
        }
    }

    @Test
    fun `2026년 1월 신정과 일요일은 휴일로 표시된다`() {
        moveToMonth(YearMonth.of(2026, 1))

        val days = viewModel.uiState.value.calendarDays
        val newYearDay = days.first { it.date == LocalDate.of(2026, 1, 1) }
        assertEquals(true, newYearDay.isHoliday)
        assertEquals("신정", newYearDay.holidayName)

        val sunday = days.first { it.date.dayOfWeek.value == 7 }
        assertEquals(true, sunday.isHoliday)
    }

    @Test
    fun `2025년 12월의 평일은 휴일이 아니다`() {
        moveToMonth(YearMonth.of(2025, 12))

        val days = viewModel.uiState.value.calendarDays
        val nonSunday = days.first { it.date.year == 2025 && it.date.monthValue == 12 && it.date.dayOfWeek.value != 7 }
        assertEquals(false, nonSunday.isHoliday)
        assertEquals(null, nonSunday.holidayName)
    }

    @Test
    fun `2026년 각 월의 휴일 계산이 수행된다`() {
        val holidayMonths = setOf(1, 2, 3, 5, 6, 8, 9, 10, 12)
        for (month in 1..12) {
            moveToMonth(YearMonth.of(2026, month))
            val days = viewModel.uiState.value.calendarDays
            assertNotNull(days)
            if (month in holidayMonths) {
                val hasHoliday = days.any { it.holidayName != null }
                assertEquals(true, hasHoliday)
            }
        }
    }

    @Test
    fun `월별 대표 사진 스트림이 coverPhotos에 반영된다`() = runTest {
        val date = LocalDate.of(2026, 1, 3)
        diaryRepository.addDiary(
            com.picday.diary.domain.diary.Diary(
                id = "cover_diary",
                date = date,
                title = "t",
                content = "c",
                createdAt = 1L,
                coverPhotoUri = "cover_uri"
            )
        )

        advanceUntilIdle()
        assertEquals("cover_uri", viewModel.uiState.value.coverPhotos[date])
    }

    private fun moveToMonth(target: YearMonth) {
        var current = viewModel.uiState.value.currentYearMonth
        while (current < target) {
            viewModel.onNextMonthClick()
            current = viewModel.uiState.value.currentYearMonth
        }
        while (current > target) {
            viewModel.onPreviousMonthClick()
            current = viewModel.uiState.value.currentYearMonth
        }
    }
}
