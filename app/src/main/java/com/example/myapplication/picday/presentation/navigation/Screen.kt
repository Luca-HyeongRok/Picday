package com.example.myapplication.picday.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import java.time.LocalDate

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Diary : Screen("diary", "Diary", Icons.Default.Edit)
    object Write : Screen("write/{date}/{mode}", "Write", Icons.Default.Edit) {
        fun createRoute(date: LocalDate, mode: WriteMode): String {
            return "write/${Uri.encode(date.toString())}/${mode.name}"
        }
    }
}
