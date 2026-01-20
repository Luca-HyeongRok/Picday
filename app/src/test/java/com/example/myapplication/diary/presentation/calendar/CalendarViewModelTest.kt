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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

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
            observeMonthlyDiaries = ObserveMonthlyDiariesUseCase(diaryRepository)
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
}
