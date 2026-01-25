package com.example.myapplication.diary.presentation.navigation

import com.picday.diary.core.navigation.NavigationState
import com.picday.diary.core.navigation.WriteMode
import com.picday.diary.presentation.main.MainDestination
import com.picday.diary.presentation.navigation.MainNavEffect
import com.picday.diary.presentation.navigation.MainNavEvent
import com.picday.diary.presentation.navigation.reduceMainNav
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainNavGraphReducerTest {

    @Test
    fun `CalendarDateSelected는 Navigate와 UpdateSelectedDate를 생성한다`() = runTest {
        // Given
        val date = LocalDate.of(2025, 1, 1)
        val originalState = NavigationState(
            backStack = listOf(MainDestination.Calendar),
            selectedDate = null
        )
        val originalBackStack = originalState.backStack

        // When
        val result = reduceMainNav(
            originalState,
            MainNavEvent.CalendarDateSelected(date, WriteMode.VIEW)
        )

        // Then
        val expectedDestination = MainDestination.Write(date.toString(), WriteMode.VIEW.name, null)
        assertEquals(
            listOf(MainDestination.Calendar, expectedDestination),
            result.state.backStack
        )
        assertEquals(date, result.state.selectedDate)
        assertEquals(
            listOf(
                MainNavEffect.UpdateSelectedDate(date),
                MainNavEffect.Navigate(expectedDestination)
            ),
            result.effects
        )
        // 기존 상태 불변성 확인
        assertEquals(listOf(MainDestination.Calendar), originalState.backStack)
        assertEquals(originalBackStack, originalState.backStack)
    }

    @Test
    fun `BottomTabClick은 ReplaceRoot를 생성한다`() = runTest {
        // Given
        val date = LocalDate.of(2025, 2, 1)
        val originalState = NavigationState(
            backStack = listOf(
                MainDestination.Calendar,
                MainDestination.Write(date.toString(), WriteMode.ADD.name, null)
            ),
            selectedDate = date
        )

        // When
        val result = reduceMainNav(
            originalState,
            MainNavEvent.BottomTabClick(MainDestination.Diary)
        )

        // Then
        assertEquals(listOf(MainDestination.Diary), result.state.backStack)
        assertEquals(
            listOf(MainNavEffect.ReplaceRoot(MainDestination.Diary)),
            result.effects
        )
        assertEquals(date, result.state.selectedDate)
    }

    @Test
    fun `WriteBack은 Pop을 생성하고 스택을 줄인다`() = runTest {
        // Given
        val date = LocalDate.of(2025, 3, 1)
        val originalState = NavigationState(
            backStack = listOf(
                MainDestination.Calendar,
                MainDestination.Write(date.toString(), WriteMode.ADD.name, null)
            ),
            selectedDate = date
        )

        // When
        val result = reduceMainNav(originalState, MainNavEvent.WriteBack)

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.state.backStack)
        assertEquals(listOf(MainNavEffect.Pop), result.effects)
    }

    @Test
    fun `DiaryEditClick은 UpdateSelectedDate를 포함한다`() = runTest {
        // Given
        val date = LocalDate.of(2025, 4, 1)
        val editId = "edit-1"
        val originalState = NavigationState(
            backStack = listOf(MainDestination.Diary),
            selectedDate = null
        )

        // When
        val result = reduceMainNav(
            originalState,
            MainNavEvent.DiaryEditClick(date, editId)
        )

        // Then
        assertFalse(result.effects.none { it is MainNavEffect.UpdateSelectedDate })
        assertEquals(date, result.state.selectedDate)
    }
}
