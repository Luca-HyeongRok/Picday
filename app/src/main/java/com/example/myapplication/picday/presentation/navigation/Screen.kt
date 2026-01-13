package com.example.myapplication.picday.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Diary : Screen("diary", "Diary", Icons.Default.Edit)
    object DiaryWrite : Screen("diary_write?date={date}", "DiaryWrite", Icons.Default.Edit) {
        fun createRoute(date: LocalDate): String {
            return "diary_write?date=$date"
        }
    }
}
