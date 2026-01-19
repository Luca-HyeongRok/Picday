package com.picday.diary.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import java.time.LocalDate

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "Calendar", Icons.Outlined.CalendarToday)
    object Diary : Screen("diary", "Diary", Icons.Outlined.Edit)
    object Write : Screen("write/{date}/{mode}", "Write", Icons.Outlined.Edit) {
        fun createRoute(date: LocalDate, mode: WriteMode): String {
            return "write/${Uri.encode(date.toString())}/${mode.name}"
        }
    }
}
