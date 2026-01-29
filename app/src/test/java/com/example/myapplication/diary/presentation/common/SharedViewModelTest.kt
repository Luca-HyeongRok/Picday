package com.example.myapplication.diary.presentation.common

import com.picday.diary.presentation.common.SharedViewModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SharedViewModelTest {

    @Test
    fun `updateSelectedDate updates selectedDate state`() {
        val viewModel = SharedViewModel()
        val date = LocalDate.of(2026, 1, 15)

        viewModel.updateSelectedDate(date)

        assertEquals(date, viewModel.selectedDate.value)
    }
}
