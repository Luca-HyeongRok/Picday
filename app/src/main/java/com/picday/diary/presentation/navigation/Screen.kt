package com.picday.diary.presentation.navigation

sealed class Screen(val meta: ScreenMeta) {
    val title: String get() = meta.title
    val icon get() = meta.icon

    object Calendar : Screen(ScreenMetaRegistry.Calendar)
    object Diary : Screen(ScreenMetaRegistry.Diary)
    object Write : Screen(ScreenMetaRegistry.Write)
}
