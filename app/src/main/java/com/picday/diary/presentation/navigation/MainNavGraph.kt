package com.picday.diary.presentation.navigation

import android.net.Uri
import com.picday.diary.core.navigation.AppRoute
import com.picday.diary.core.navigation.NavEffect
import com.picday.diary.core.navigation.NavEvent
import com.picday.diary.core.navigation.NavigationState
import com.picday.diary.core.navigation.WriteMode
import com.picday.diary.presentation.main.MainDestination
import java.time.LocalDate

// Navigation 이벤트를 나타내는 sealed interface
sealed interface MainNavEvent : NavEvent {
    // 하단 탭 클릭 이벤트
    data class BottomTabClick(val target: MainDestination) : MainNavEvent
    // 캘린더 날짜 선택 이벤트
    data class CalendarDateSelected(val date: LocalDate, val mode: WriteMode) : MainNavEvent
    // 일기 작성 클릭 이벤트
    data class DiaryWriteClick(val date: LocalDate, val mode: WriteMode) : MainNavEvent
    // 일기 수정 클릭 이벤트
    data class DiaryEditClick(val date: LocalDate, val editDiaryId: String) : MainNavEvent
    // 일기 추가 클릭 이벤트
    data class WriteAddClick(val date: LocalDate) : MainNavEvent
    // 뒤로 가기 이벤트
    data object WriteBack : MainNavEvent
    // 일기 저장 완료 이벤트
    data object WriteSaveComplete : MainNavEvent
    // 일기 삭제 완료 이벤트
    data object WriteDeleteComplete : MainNavEvent
    // 딥링크 처리 이벤트
    data class ProcessDeepLink(val deepLinkUri: String?) : MainNavEvent
}

// Navigation 부수 효과를 나타내는 sealed interface
sealed interface MainNavEffect : NavEffect {
    // 지정한 화면으로 이동 (스택에 push)
    data class Navigate(val route: AppRoute) : MainNavEffect
    // 루트를 지정한 화면으로 교체
    data class ReplaceRoot(val route: AppRoute) : MainNavEffect
    // 현재 화면 하나를 스택에서 제거
    data object Pop : MainNavEffect
    // 스택의 최상위 화면까지 모두 제거
    data object PopToRoot : MainNavEffect
    // 딥링크로 선택된 날짜 동기화
    data class UpdateSelectedDate(val date: LocalDate) : MainNavEffect
    // 일기 수정 후 다시 돌아왔을 때 해당 일기 정보를 소비
    data class ConsumeEditDiary(val diaryId: String) : MainNavEffect
}

// NavigationReducer의 결과
data class NavResult(
    val state: NavigationState,
    val effects: List<MainNavEffect> = emptyList()
)

// Navigation 이벤트를 처리하여 새로운 백스택과 부수 효과를 반환하는 Reducer 함수
fun reduceMainNav(
    state: NavigationState,
    event: MainNavEvent,
): NavResult {
    val backStack = state.backStack.asMainDestinations()
    // 백스택이 비어있는 경우 기본 스택을 반환
    if (backStack.isEmpty()) return NavResult(state)

    return when (event) {
        is MainNavEvent.BottomTabClick -> {
            val nextBackStack = switchBottomTab(backStack, event.target)
            val nextState = state.copy(backStack = nextBackStack)
            val effects = if (nextBackStack == backStack) {
                emptyList()
            } else {
                listOf(MainNavEffect.ReplaceRoot(event.target))
            }
            NavResult(nextState, effects)
        }
        is MainNavEvent.CalendarDateSelected -> {
            val destination = MainDestination.Write(event.date.toString(), event.mode.name, null)
            val nextState = state.copy(
                backStack = pushWrite(backStack, event.date, event.mode, editDiaryId = null),
                selectedDate = event.date
            )
            NavResult(
                state = nextState,
                effects = listOf(
                    MainNavEffect.UpdateSelectedDate(event.date),
                    MainNavEffect.Navigate(destination)
                )
            )
        }
        is MainNavEvent.DiaryWriteClick -> {
            val destination = MainDestination.Write(event.date.toString(), event.mode.name, null)
            val nextState = state.copy(
                backStack = pushWrite(backStack, event.date, event.mode, editDiaryId = null),
                selectedDate = event.date
            )
            NavResult(
                state = nextState,
                effects = listOf(
                    MainNavEffect.UpdateSelectedDate(event.date),
                    MainNavEffect.Navigate(destination)
                )
            )
        }
        is MainNavEvent.DiaryEditClick -> {
            val destination = MainDestination.Write(event.date.toString(), WriteMode.VIEW.name, event.editDiaryId)
            val nextState = state.copy(
                backStack = pushWrite(
                    backStack,
                    event.date,
                    WriteMode.VIEW,
                    editDiaryId = event.editDiaryId
                ),
                selectedDate = event.date
            )
            NavResult(
                state = nextState,
                effects = listOf(
                    MainNavEffect.UpdateSelectedDate(event.date),
                    MainNavEffect.Navigate(destination),
                    MainNavEffect.ConsumeEditDiary(event.editDiaryId)
                )
            )
        }
        is MainNavEvent.WriteAddClick -> {
            val destination = MainDestination.Write(event.date.toString(), WriteMode.ADD.name, null)
            val nextState = state.copy(
                backStack = pushWrite(backStack, event.date, WriteMode.ADD, editDiaryId = null),
                selectedDate = event.date
            )
            NavResult(
                state = nextState,
                effects = listOf(
                    MainNavEffect.UpdateSelectedDate(event.date),
                    MainNavEffect.Navigate(destination)
                )
            )
        }
        MainNavEvent.WriteBack -> {
            val nextBackStack = if (backStack.size > 1) backStack.dropLast(1) else backStack
            val effects = if (backStack.size > 1) listOf(MainNavEffect.Pop) else emptyList()
            NavResult(state = state.copy(backStack = nextBackStack), effects = effects)
        }
        MainNavEvent.WriteSaveComplete -> {
            val nextBackStack = if (backStack.size > 1) backStack.dropLast(1) else backStack
            val effects = if (backStack.size > 1) listOf(MainNavEffect.Pop) else emptyList()
            NavResult(state = state.copy(backStack = nextBackStack), effects = effects)
        }
        MainNavEvent.WriteDeleteComplete -> {
            val nextBackStack = if (backStack.size > 1) backStack.dropLast(1) else backStack
            val effects = if (backStack.size > 1) listOf(MainNavEffect.Pop) else emptyList()
            NavResult(state = state.copy(backStack = nextBackStack), effects = effects)
        }
        is MainNavEvent.ProcessDeepLink -> {
            val deepLinkUri = event.deepLinkUri
            if (deepLinkUri == null) {
                // 딥링크가 없으면 현재 스택 유지
                return NavResult(state)
            }

            try {
                val uri = Uri.parse(deepLinkUri)
                if (uri.scheme == "app" && uri.host == "picday.co") {
                    val path = uri.path ?: return NavResult(state)

                    // "/diary/{yyyy-MM-dd}" 형태의 딥링크 처리
                    if (path.startsWith("/diary/")) {
                        val dateString = path.substringAfter("/diary/")
                        val date = LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        
                        // 딥링크 진입 시 항상 [Calendar, Diary] 스택으로 재구성
                        // Diary 화면에서 뒤로가기 시 Calendar 화면이 나타나도록 보장
                        val effects = listOf(
                            MainNavEffect.UpdateSelectedDate(date),
                            MainNavEffect.ReplaceRoot(MainDestination.Calendar),
                            MainNavEffect.Navigate(MainDestination.Diary)
                        )
                        return NavResult(
                            state.copy(
                                backStack = listOf(MainDestination.Calendar, MainDestination.Diary),
                                selectedDate = date
                            ),
                            effects
                        )
                    }
                }
            } catch (e: Exception) {
                // 잘못된 형식의 URI는 무시하고 현재 흐름 유지
                e.printStackTrace()
            }
            return NavResult(state) // 처리되지 않은 딥링크는 현재 스택 유지
        }
    }
}

private fun List<AppRoute>.asMainDestinations(): List<MainDestination> {
    return map { route ->
        require(route is MainDestination) {
            "MainNavGraph only supports MainDestination. Found: ${route::class.qualifiedName}"
        }
        route
    }
}

// 하단 탭 전환 로직
private fun switchBottomTab(
    backStack: List<MainDestination>,
    target: MainDestination
): List<MainDestination> {
    // 이미 해당 탭에 있는 경우 스택 변경 없음
    if (backStack.firstOrNull() == target) {
        return backStack
    }
    // 대상 탭으로 스택을 초기화
    return listOf(target)
}

// Write 화면으로 이동 로직
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
