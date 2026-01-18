package com.example.myapplication.picday.presentation.main

import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

sealed interface MainNavEvent {
    data class BottomTabClick(val target: MainDestination) : MainNavEvent
    data class CalendarDateSelected(val date: LocalDate, val mode: WriteMode) : MainNavEvent
    data class DiaryWriteClick(val date: LocalDate, val mode: WriteMode) : MainNavEvent
    data class DiaryEditClick(val date: LocalDate, val editDiaryId: String) : MainNavEvent
    data class WriteAddClick(val date: LocalDate) : MainNavEvent
    data object WriteBack : MainNavEvent
    data object WriteSaveComplete : MainNavEvent
    data object WriteDeleteComplete : MainNavEvent
}

fun reduceMainNav(
    backStack: List<MainDestination>,
    event: MainNavEvent
): List<MainDestination> {
    if (backStack.isEmpty()) return backStack

    return when (event) {
        is MainNavEvent.BottomTabClick -> switchBottomTab(backStack, event.target)
        is MainNavEvent.CalendarDateSelected ->
            pushWrite(backStack, event.date, event.mode, editDiaryId = null)
        is MainNavEvent.DiaryWriteClick ->
            pushWrite(backStack, event.date, event.mode, editDiaryId = null)
        is MainNavEvent.DiaryEditClick ->
            pushWrite(backStack, event.date, WriteMode.VIEW, editDiaryId = event.editDiaryId)
        is MainNavEvent.WriteAddClick ->
            pushWrite(backStack, event.date, WriteMode.ADD, editDiaryId = null)
        MainNavEvent.WriteBack -> popOne(backStack)
        MainNavEvent.WriteSaveComplete -> popOne(backStack)
        MainNavEvent.WriteDeleteComplete -> popOne(backStack)
    }
}

private fun switchBottomTab(
    backStack: List<MainDestination>,
    target: MainDestination
): List<MainDestination> {
    val root = listOf(backStack.first())
    return if (root.last() == target) root else root + target
}

private fun popOne(backStack: List<MainDestination>): List<MainDestination> {
    return if (backStack.size > 1) backStack.dropLast(1) else backStack
}

private fun pushWrite(
    backStack: List<MainDestination>,
    date: LocalDate,
    mode: WriteMode,
    editDiaryId: String?
): List<MainDestination> {
    return backStack + MainDestination.Write(
        date = date.toString(),
        mode = mode.name,
        editDiaryId = editDiaryId
    )
}
