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
        val next = reduceMainNav(backStack, MainNavEvent.BottomTabClick(MainDestination.Diary))

        // Then
        assertEquals(listOf(MainDestination.Diary), next)
    }

    @Test
    fun `Write ADD 진입 시 backStack에 push 된다`() {
        // Given
        val date = LocalDate.of(2025, 1, 1)
        val backStack = listOf<MainDestination>(MainDestination.Calendar)

        // When
        val next = reduceMainNav(backStack, MainNavEvent.WriteAddClick(date))

        // Then
        assertEquals(
            listOf(
                MainDestination.Calendar,
                MainDestination.Write(date.toString(), WriteMode.ADD.name, null)
            ),
            next
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
        val next = reduceMainNav(backStack, MainNavEvent.WriteBack)

        // Then
        assertEquals(listOf(MainDestination.Calendar), next)
    }
}
