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
): NavResult {
    if (backStack.isEmpty()) return NavResult(backStack)

    return when (event) {
        is MainNavEvent.BottomTabClick -> NavResult(switchBottomTab(backStack, event.target))
        is MainNavEvent.CalendarDateSelected ->
            NavResult(pushWrite(backStack, event.date, event.mode, editDiaryId = null))
        is MainNavEvent.DiaryWriteClick ->
            NavResult(pushWrite(backStack, event.date, event.mode, editDiaryId = null))
        is MainNavEvent.DiaryEditClick ->
            NavResult(
                backStack = pushWrite(
                    backStack,
                    event.date,
                    WriteMode.VIEW,
                    editDiaryId = event.editDiaryId
                ),
                effects = listOf(MainNavEffect.ConsumeEditDiary(event.editDiaryId))
            )
        is MainNavEvent.WriteAddClick ->
            NavResult(pushWrite(backStack, event.date, WriteMode.ADD, editDiaryId = null))
        MainNavEvent.WriteBack ->
            NavResult(backStack = backStack, effects = listOf(MainNavEffect.PopOne))
        MainNavEvent.WriteSaveComplete ->
            NavResult(backStack = backStack, effects = listOf(MainNavEffect.PopOne))
        MainNavEvent.WriteDeleteComplete ->
            NavResult(backStack = backStack, effects = listOf(MainNavEffect.PopOne))
    }
}

private fun switchBottomTab(
    backStack: List<MainDestination>,
    target: MainDestination
): List<MainDestination> {
    return if (backStack.first() == target) {
        backStack
    } else {
        listOf(target)
    }
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
