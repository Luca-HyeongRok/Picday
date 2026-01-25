package com.picday.diary.presentation.navigation

import android.net.Uri
import com.picday.diary.core.navigation.WriteMode
import java.time.LocalDate

sealed class Screen(val meta: ScreenMeta) {
    val route: String get() = meta.route
    val title: String get() = meta.title
    val icon get() = meta.icon
    val deepLink get() = meta.deepLink

    object Calendar : Screen(ScreenMetaRegistry.Calendar)
    object Diary : Screen(ScreenMetaRegistry.Diary)
    object Write : Screen(ScreenMetaRegistry.Write) {
        fun createRoute(date: LocalDate, mode: WriteMode): String {
            return "write/${Uri.encode(date.toString())}/${mode.name}"
        }
    }
}
