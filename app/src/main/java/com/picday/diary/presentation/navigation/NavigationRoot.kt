package com.picday.diary.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.picday.diary.presentation.common.SharedViewModel
import com.picday.diary.presentation.main.MainDestination

/**
 * 앱의 모든 네비게이션 진입점을 관리하는 단일 소스입니다.
 * 딥링크, 위젯, 일반 실행 등 모든 케이스를 처리하고 일관된 백스택을 보장합니다.
 *
 * @param deepLinkUri Activity로부터 전달받은 딥링크 URI 문자열. (예: "app://picday.co/diary/2026-01-22")
 * @param sharedViewModel 날짜 등 뷰모델 간 공유되는 데이터.
 * @param content 이 네비게이션 루트 하에서 표시될 메인 UI 컨텐츠.
 */
@Composable
fun NavigationRoot(
    deepLinkUri: String?,
    sharedViewModel: SharedViewModel,
    content: @Composable (backStack: List<MainDestination>, onNavigate: (MainNavEvent) -> Unit) -> Unit
) {
    // NavBackStack의 초기 상태를 정의하고 소유합니다.
    val backStack = rememberNavBackStack(MainDestination.Calendar)

    // 처리해야 할 부수 효과(PopOne, PopToRoot 등) 리스트
    var pendingEffects by remember { mutableStateOf<List<MainNavEffect>>(emptyList()) }
    // pendingEffects가 변경되었음을 감지하기 위한 버전 카운터
    var pendingEffectsVersion by remember { mutableStateOf(0) }

    // 모든 내비게이션 이벤트를 처리하고 백스택을 업데이트하는 함수
    val dispatchNav: (MainNavEvent) -> Unit = remember {
        { event ->
            // reduceMainNav 함수를 사용하여 현재 백스택과 이벤트를 기반으로 새로운 상태 계산
            val result = reduceMainNav(backStack.asMainDestinations(), event)
            // 발생한 부수 효과를 저장하고 버전 업데이트
            pendingEffects = result.effects
            pendingEffectsVersion += 1
        }
    }

    // 딥링크 URI가 변경될 때마다 실행되는 효과입니다.
    // 딥링크 처리 이벤트를 dispatchNav를 통해 보냅니다.
    LaunchedEffect(deepLinkUri) {
        dispatchNav(MainNavEvent.ProcessDeepLink(deepLinkUri))
    }

    // pendingEffects가 변경될 때마다 실행되는 효과입니다.
    // NavResult에서 반환된 부수 효과들을 처리합니다.
    LaunchedEffect(pendingEffectsVersion) {
        if (pendingEffects.isEmpty()) return@LaunchedEffect

        pendingEffects.forEach { effect ->
            when (effect) {
                is MainNavEffect.Navigate -> {
                    val destination = effect.route
                    require(destination is MainDestination) {
                        "NavigationRoot only supports MainDestination. Found: ${destination::class.qualifiedName}"
                    }
                    backStack.add(destination)
                }
                is MainNavEffect.ReplaceRoot -> {
                    val destination = effect.route
                    require(destination is MainDestination) {
                        "NavigationRoot only supports MainDestination. Found: ${destination::class.qualifiedName}"
                    }
                    backStack.clear()
                    backStack.add(destination)
                }
                MainNavEffect.Pop -> {
                    if (backStack.size > 1) { // 스택에 하나 이상의 항목이 있을 때만 팝
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                MainNavEffect.PopToRoot -> {
                    // 루트 외 모든 항목을 제거
                    while (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                is MainNavEffect.UpdateSelectedDate -> {
                    sharedViewModel.updateSelectedDate(effect.date)
                }
                is MainNavEffect.ConsumeEditDiary -> Unit
            }
        }
        pendingEffects = emptyList() // 처리 후 효과 리스트 비우기
    }

    // MainScreen에 변경 불가능한 백스택 뷰와 onNavigate 콜백을 전달합니다.
    content(backStack.asMainDestinations(), dispatchNav)
}

private fun List<NavKey>.asMainDestinations(): List<MainDestination> {
    return map { key ->
        require(key is MainDestination) {
            "NavigationRoot only supports MainDestination. Found: ${key::class.qualifiedName}"
        }
        key
    }
}
