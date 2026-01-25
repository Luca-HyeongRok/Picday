package com.picday.diary.presentation.diary

import app.cash.turbine.test
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import com.picday.diary.domain.usecase.diary.GetDiariesByDateRangeUseCase
import com.picday.diary.domain.usecase.diary.GetDiariesByDateUseCase
import com.picday.diary.domain.usecase.diary.GetPhotosUseCase
import com.picday.diary.domain.usecase.diary.HasAnyRecordUseCase
import com.picday.diary.domain.usecase.settings.GetDateCoverPhotoUseCase
import com.picday.diary.domain.usecase.settings.SetDateCoverPhotoUseCase
import com.picday.diary.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.picday.diary.domain.updater.CalendarWidgetUpdater
import java.time.LocalDate

/**
 * DiaryViewModel의 조회 및 집계 로직을 검증하는 단위 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var diaryRepository: FakeDiaryRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var widgetUpdater: FakeCalendarWidgetUpdater
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        diaryRepository = FakeDiaryRepository()
        settingsRepository = FakeSettingsRepository()
        widgetUpdater = FakeCalendarWidgetUpdater()
        
        // ViewModel init 블록에서 updateUiForDate(now)가 호출됨
        viewModel = DiaryViewModel(
            getDiariesByDate = GetDiariesByDateUseCase(diaryRepository),
            getDiariesByDateRange = GetDiariesByDateRangeUseCase(diaryRepository),
            getPhotos = GetPhotosUseCase(diaryRepository),
            hasAnyRecordUseCase = HasAnyRecordUseCase(diaryRepository),
            getDateCoverPhoto = GetDateCoverPhotoUseCase(settingsRepository),
            setDateCoverPhoto = SetDateCoverPhotoUseCase(settingsRepository),
            widgetUpdater = widgetUpdater
        )
    }

    /**
     * 특정 날짜에 여러 개의 기록이 있을 때, UI 상태에 모두 로드되는지 확인
     */
    @Test
    fun `선택된 날짜의 기록들이 정상적으로 로드되어야 한다`() = runTest {
        // Given: 특정 날짜에 2개의 일기 저장
        val date = LocalDate.of(2024, 1, 1)
        diaryRepository.addDiaryForDate(date, "Diary 1", "Content 1")
        diaryRepository.addDiaryForDate(date, "Diary 2", "Content 2")

        // When: 날짜 선택
        viewModel.onDateSelected(date)

        // Then: uiItems에 2개의 항목이 있어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(date, state.selectedDate)
            assertEquals(2, state.uiItems.size)
            assertEquals("Diary 1", state.uiItems[0].title)
            assertEquals("Diary 2", state.uiItems[1].title)
        }
    }

    /**
     * 특정 날짜의 모든 일기에 포함된 사진들이 중복 없이 집계되는지 확인
     */
    @Test
    fun `날짜별 전체 사진이 중복 없이 집계되어야 한다`() = runTest {
        // Given: 한 날짜의 여러 일기에 걸쳐 사진 존재
        val date = LocalDate.of(2024, 1, 1)
        diaryRepository.addDiaryForDate(date, "D1", "C1", listOf("uri1", "uri2"))
        diaryRepository.addDiaryForDate(date, "D2", "C2", listOf("uri2", "uri3")) // uri2 중복

        // When: 날짜 선택
        viewModel.onDateSelected(date)

        // Then: 집계된 전체 사진은 [uri1, uri2, uri3] 3개여야 함
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != date) {
                state = awaitItem()
            }
            assertEquals(3, state.allPhotosForDate.size)
            assertTrue(state.allPhotosForDate.containsAll(listOf("uri1", "uri2", "uri3")))
        }
    }

    /**
     * 사용자가 수동으로 설정한 대표 사진이 있으면, 집계 리스트의 처음에 위치해야 함
     */
    @Test
    fun `저장된 대표 사진이 집계 리스트의 맨 앞에 위치해야 한다`() = runTest {
        // Given: 사진들이 있고, 그 중 하나를 대표 사진으로 설정
        val date = LocalDate.of(2024, 1, 1)
        diaryRepository.addDiaryForDate(date, "D1", "C1", listOf("uri1", "uri2"))
        settingsRepository.setDateCoverPhotoUri(date, "uri2")

        // When: 날짜 선택
        viewModel.onDateSelected(date)

        // Then: allPhotosForDate의 첫 번째는 uri2여야 함
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != date || state.allPhotosForDate.isEmpty()) {
                state = awaitItem()
            }
            advanceUntilIdle()
            assertEquals("uri1", state.allPhotosForDate.firstOrNull())
            assertEquals("uri2", viewModel.coverPhotoByDate.value[date])
        }
    }

    /**
     * 기록이나 사진이 없는 날짜를 조회해도 빈 상태로 정상 동작하는지 확인
     */
    @Test
    fun `기록이 없는 날짜 조회 시 빈 리스트를 반환해야 한다`() = runTest {
        // When: 데이터가 없는 날짜 선택
        val emptyDate = LocalDate.of(2000, 1, 1)
        viewModel.onDateSelected(emptyDate)

        // Then: 어떤 예외도 없이 빈 상태가 유지되어야 함
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(emptyDate, state.selectedDate)
            assertTrue(state.uiItems.isEmpty())
            assertTrue(state.allPhotosForDate.isEmpty())
        }
    }

    @Test
    fun `hasAnyRecord는 기록 존재 여부를 반환해야 한다`() = runTest {
        val dateWithRecord = LocalDate.of(2024, 2, 1)
        val emptyDate = LocalDate.of(2024, 2, 2)
        diaryRepository.addDiaryForDate(dateWithRecord, "Title", "Content")

        assertTrue(viewModel.hasAnyRecord(dateWithRecord))
        assertFalse(viewModel.hasAnyRecord(emptyDate))
    }

    @Test
    fun `대표 사진 저장 시 coverPhotoByDate가 갱신되어야 한다`() = runTest {
        val date = LocalDate.of(2024, 3, 1)
        viewModel.saveDateCoverPhoto(date, "uri_saved")
        advanceUntilIdle()

        assertEquals("uri_saved", viewModel.coverPhotoByDate.value[date])
    }

    @Test
    fun `preloadCoverPhotos는 저장된 대표 사진과 최신 사진을 우선 반영해야 한다`() = runTest {
        val dateWithSavedCover = LocalDate.of(2024, 4, 1)
        val dateWithDiary = LocalDate.of(2024, 4, 2)

        settingsRepository.setDateCoverPhotoUri(dateWithSavedCover, "uri_saved")
        diaryRepository.addDiaryForDate(dateWithDiary, "Title", "Content", listOf("uri1", "uri2"))

        viewModel.preloadCoverPhotos(listOf(dateWithSavedCover, dateWithDiary))

        viewModel.coverPhotoByDate.test {
            var map = awaitItem()
            while (!map.containsKey(dateWithSavedCover) || !map.containsKey(dateWithDiary)) {
                map = awaitItem()
            }

            assertEquals("uri_saved", map[dateWithSavedCover])
            assertNull(map[dateWithDiary])
        }
    }

    @Test
    fun `moveDateBy(1) 호출 시 selectedDate가 +1일 증가`() = runTest {
        val before = viewModel.uiState.value.selectedDate
        viewModel.moveDateBy(1)

        val expected = before.plusDays(1)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != expected) {
                state = awaitItem()
            }
            assertEquals(expected, state.selectedDate)
        }
    }

    @Test
    fun `moveDateBy(-1) 호출 시 selectedDate가 -1일 감소`() = runTest {
        val before = viewModel.uiState.value.selectedDate
        viewModel.moveDateBy(-1)

        val expected = before.minusDays(1)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != expected) {
                state = awaitItem()
            }
            assertEquals(expected, state.selectedDate)
        }
    }

    @Test
    fun `해당 날짜에 다이어리가 없으면 uiItems는 emptyList다`() = runTest {
        val target = LocalDate.of(2024, 7, 1)
        viewModel.onDateSelected(target)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != target) {
                state = awaitItem()
            }
            assertEquals(target, state.selectedDate)
            assertTrue(state.uiItems.isEmpty())
        }
    }

    @Test
    fun `selectDate 호출 시 selectedDate가 전달된 날짜로 변경된다`() = runTest {
        val target = LocalDate.of(2024, 8, 1)
        viewModel.onDateSelected(target)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedDate != target) {
                state = awaitItem()
            }
            assertEquals(target, state.selectedDate)
        }
    }
    @Test
    fun `대표 배경 저장 시 위젯 갱신 로직이 호출되어야 한다`() = runTest {
        // Given
        val date = LocalDate.of(2024, 5, 1)
        val uri = "new_cover_uri"

        // When
        viewModel.saveDateCoverPhoto(date, uri)

        // Then
        assertEquals(1, widgetUpdater.updateCallCount)
    }

    private class FakeCalendarWidgetUpdater : CalendarWidgetUpdater {
        var updateCallCount = 0
        override fun updateAll() {
            updateCallCount++
        }
    }
}
