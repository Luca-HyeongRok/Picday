package com.example.myapplication.diary.domain.usecase.settings

import app.cash.turbine.test
import com.picday.diary.domain.usecase.settings.GetDateCoverPhotoUseCase
import com.picday.diary.domain.usecase.settings.ObserveCalendarBackgroundUseCase
import com.picday.diary.domain.usecase.settings.SetCalendarBackgroundUseCase
import com.picday.diary.domain.usecase.settings.SetDateCoverPhotoUseCase
import com.picday.diary.fakes.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCaseTest {

    private lateinit var repository: FakeSettingsRepository
    private lateinit var observeCalendarBackground: ObserveCalendarBackgroundUseCase
    private lateinit var setCalendarBackground: SetCalendarBackgroundUseCase
    private lateinit var getDateCoverPhoto: GetDateCoverPhotoUseCase
    private lateinit var setDateCoverPhoto: SetDateCoverPhotoUseCase

    @Before
    fun setUp() {
        repository = FakeSettingsRepository()
        observeCalendarBackground = ObserveCalendarBackgroundUseCase(repository)
        setCalendarBackground = SetCalendarBackgroundUseCase(repository)
        getDateCoverPhoto = GetDateCoverPhotoUseCase(repository)
        setDateCoverPhoto = SetDateCoverPhotoUseCase(repository)
    }

    @Test
    fun `캘린더 배경 설정이 flow에 반영되어야 한다`() = runTest {
        observeCalendarBackground().test {
            assertEquals(null, awaitItem())
            setCalendarBackground("uri_bg")
            assertEquals("uri_bg", awaitItem())
        }
    }

    @Test
    fun `날짜별 대표 사진 설정이 flow에 반영되어야 한다`() = runTest {
        val date = LocalDate.of(2024, 5, 1)
        getDateCoverPhoto(date).test {
            assertEquals(null, awaitItem())
            setDateCoverPhoto(date, "uri_cover")
            assertEquals("uri_cover", awaitItem())
        }
    }
}
