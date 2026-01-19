package com.picday.diary.presentation.diary

import app.cash.turbine.test
import com.picday.diary.fakes.FakeDiaryRepository
import com.picday.diary.fakes.FakeSettingsRepository
import com.picday.diary.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        diaryRepository = FakeDiaryRepository()
        settingsRepository = FakeSettingsRepository()
        // ViewModel init 블록에서 updateUiForDate(now)가 호출됨
        viewModel = DiaryViewModel(diaryRepository, settingsRepository)
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
            val state = awaitItem()
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
            val state = awaitItem()
            assertEquals("uri2", state.allPhotosForDate.firstOrNull())
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
}
