package com.example.myapplication.picday.presentation.main

import com.example.myapplication.picday.presentation.navigation.WriteMode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MainNavReducerTest {

    @Test
    fun `bottom tab 전환 시 root만 유지된다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Calendar,
            MainDestination.Write("2025-01-01", WriteMode.ADD.name, null)
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.BottomTabClick(MainDestination.Diary))

        // Then
        assertEquals(listOf(MainDestination.Diary), result.backStack)
    }

    @Test
    fun `Write ADD 진입 시 backStack에 push 된다`() {
        // Given
        val date = LocalDate.of(2025, 1, 1)
        val backStack = listOf<MainDestination>(MainDestination.Calendar)

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteAddClick(date))

        // Then
        assertEquals(
            listOf(
                MainDestination.Calendar,
                MainDestination.Write(date.toString(), WriteMode.ADD.name, null)
            ),
            result.backStack
        )
    }

    @Test
    fun `Back 이벤트 시 pop 된다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Calendar,
            MainDestination.Diary
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteBack)

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.backStack)
    }

    @Test
    fun `DiaryEditClick 시 Write VIEW로 진입한다`() {
        // Given
        val date = LocalDate.of(2025, 2, 1)
        val editDiaryId = "edit-123"
        val backStack = listOf<MainDestination>(MainDestination.Diary)

        // When
        val result = reduceMainNav(
            backStack,
            MainNavEvent.DiaryEditClick(date, editDiaryId)
        )

        // Then
        assertEquals(
            listOf(
                MainDestination.Diary,
                MainDestination.Write(date.toString(), WriteMode.VIEW.name, editDiaryId)
            ),
            result.backStack
        )
    }

    @Test
    fun `WriteSaveComplete 시 pop 된다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Calendar,
            MainDestination.Write("2025-01-01", WriteMode.ADD.name, null)
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteSaveComplete)

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.backStack)
    }

    @Test
    fun `WriteDeleteComplete 시 pop 된다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Calendar,
            MainDestination.Write("2025-01-01", WriteMode.ADD.name, null)
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteDeleteComplete)

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.backStack)
    }

    @Test
    fun `root 상태에서 Back 이벤트는 유지된다`() {
        // Given
        val backStack = listOf<MainDestination>(MainDestination.Calendar)

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteBack)

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.backStack)
    }

    @Test
    fun `동일 bottom tab 클릭 시 변화가 없다`() {
        // Given
        val backStack = listOf<MainDestination>(MainDestination.Calendar)

        // When
        val result = reduceMainNav(backStack, MainNavEvent.BottomTabClick(MainDestination.Calendar))

        // Then
        assertEquals(listOf(MainDestination.Calendar), result.backStack)
    }

    @Test
    fun `DiaryEditClick 발생 시 ConsumeEditDiary effect를 반환한다`() {
        // Given
        val date = LocalDate.of(2025, 3, 1)
        val editDiaryId = "edit-123"
        val backStack = listOf<MainDestination>(MainDestination.Diary)

        // When
        val result = reduceMainNav(
            backStack,
            MainNavEvent.DiaryEditClick(date, editDiaryId)
        )

        // Then
        assertEquals(
            listOf(
                MainDestination.Diary,
                MainDestination.Write(date.toString(), WriteMode.VIEW.name, editDiaryId)
            ),
            result.backStack
        )
        assertEquals(
            listOf(MainNavEffect.ConsumeEditDiary(editDiaryId)),
            result.effects
        )
    }

    @Test
    fun `WriteBack 이벤트는 PopOne effect를 반환한다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Calendar,
            MainDestination.Write("2025-01-01", WriteMode.ADD.name, null)
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteBack)

        // Then
        assertEquals(backStack, result.backStack)
        assertEquals(listOf(MainNavEffect.PopOne), result.effects)
    }

    @Test
    fun `WriteSaveComplete 이벤트는 PopOne effect를 반환한다`() {
        // Given
        val backStack = listOf<MainDestination>(
            MainDestination.Diary,
            MainDestination.Write("2025-01-02", WriteMode.ADD.name, null)
        )

        // When
        val result = reduceMainNav(backStack, MainNavEvent.WriteSaveComplete)

        // Then
        assertEquals(listOf(MainNavEffect.PopOne), result.effects)
    }

    @Test
    fun `BottomTabClick 동일 탭은 effect가 없다`() {
        // Given
        val backStack = listOf<MainDestination>(MainDestination.Calendar)

        // When
        val result = reduceMainNav(
            backStack,
            MainNavEvent.BottomTabClick(MainDestination.Calendar)
        )

        // Then
        assertEquals(emptyList<MainNavEffect>(), result.effects)
    }
}
