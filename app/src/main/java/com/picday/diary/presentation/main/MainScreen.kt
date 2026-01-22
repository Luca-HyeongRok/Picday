package com.picday.diary.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.picday.diary.presentation.calendar.CalendarScreen
import com.picday.diary.presentation.common.SharedViewModel
import com.picday.diary.presentation.diary.DiaryRoot
import com.picday.diary.presentation.diary.DiaryRootScreen
import com.picday.diary.presentation.diary.DiaryViewModel
import com.picday.diary.presentation.navigation.MainNavEvent
import com.picday.diary.presentation.navigation.NavigationRoot
import com.picday.diary.presentation.navigation.Screen
import com.picday.diary.presentation.navigation.WriteMode
import com.picday.diary.presentation.theme.AppColors
import com.picday.diary.presentation.theme.AppShapes
import java.time.LocalDate

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(deepLinkUri: String? = null) {
    val sharedViewModel: SharedViewModel = viewModel()

    NavigationRoot(
        deepLinkUri = deepLinkUri,
        sharedViewModel = sharedViewModel
    ) { backStack, onNavigate -> // NavigationRoot에서 제공하는 backStack과 onNavigate 콜백 사용
        val currentDestination = backStack.last()
        val isWriteMode = currentDestination is MainDestination.Write
        val selectedDate by sharedViewModel.selectedDate.collectAsState()
        // pendingEditDiaryId는 NavigationRoot에서 관리되므로 여기서는 필요 없습니다.

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(
                    visible = !isWriteMode,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 1. Navigation Shell (The background pill)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = AppShapes.BottomNav,
                                    ambientColor = AppColors.ShadowColor,
                                    spotColor = AppColors.ShadowColor
                                ),
                            shape = AppShapes.BottomNav,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Tab: Calendar
                                BottomNavItem(
                                    screen = Screen.Calendar,
                                    isSelected = currentDestination is MainDestination.Calendar,
                                    onClick = {
                                        onNavigate(MainNavEvent.BottomTabClick(MainDestination.Calendar))
                                    }
                                )

                                // Spacer for Center Fab
                                Box(modifier = Modifier.width(64.dp))

                                // Right Tab: Diary
                                BottomNavItem(
                                    screen = Screen.Diary,
                                    isSelected = currentDestination is MainDestination.Diary,
                                    onClick = {
                                        onNavigate(MainNavEvent.BottomTabClick(MainDestination.Diary))
                                    }
                                )
                            }
                        }

                        // 2. Floating Center "+" Button
                        Box(
                            modifier = Modifier
                                .offset(y = (-32).dp)
                                .size(64.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    onNavigate(MainNavEvent.WriteAddClick(selectedDate))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "기록 추가",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack, // NavigationRoot에서 받은 backStack 사용
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                onBack = { onNavigate(MainNavEvent.WriteBack) }, // 뒤로 가기 이벤트는 onNavigate로 전달
                entryProvider = entryProvider {
                    entry<MainDestination.Calendar> {
                        val diaryViewModel: DiaryViewModel = hiltViewModel()

                        CalendarScreen(
                            onDateSelected = { date ->
                                sharedViewModel.updateSelectedDate(date)

                                val mode = if (diaryViewModel.hasAnyRecord(date)) {
                                    WriteMode.VIEW
                                } else {
                                    WriteMode.ADD
                                }

                                onNavigate(MainNavEvent.CalendarDateSelected(date, mode))
                            }
                        )
                    }

                    entry<MainDestination.Diary> {
                        DiaryRoot(
                            screen = DiaryRootScreen.DIARY,
                            selectedDate = selectedDate,
                            onWriteClick = { date, mode ->
                                onNavigate(MainNavEvent.DiaryWriteClick(date, mode))
                            },
                            onEditClick = { diaryId ->
                                onNavigate(MainNavEvent.DiaryEditClick(selectedDate, diaryId))
                            }
                        )
                    }

                    entry<MainDestination.Write> { destination ->
                        val date = runCatching { LocalDate.parse(destination.date) }
                            .getOrElse { selectedDate }
                        val mode = runCatching { WriteMode.valueOf(destination.mode) }
                            .getOrElse { WriteMode.ADD }
                        val writeViewModel: com.picday.diary.presentation.write.WriteViewModel =
                            hiltViewModel()
                        val writeState by writeViewModel.uiState.collectAsState()
                        // pendingDelete와 관련된 로직은 WriteViewModel 내부 또는 상위에서 관리되어야 함
                        // MainScreen에서는 내비게이션 이벤트만 발생시킵니다.

                        // LaunchedEffect(pendingEditDiaryId)는 NavigationRoot에서 처리됩니다.

                        // LaunchedEffect(pendingDelete, writeState.uiMode, writeState.editingDiaryId)
                        // 이 로직은 MainScreen에서 직접 스택을 조작하지 않으므로 변경됩니다.
                        // WriteSaveComplete나 WriteDeleteComplete 이벤트만 onNavigate로 전달합니다.

                        DiaryRoot(
                            screen = DiaryRootScreen.WRITE,
                            selectedDate = date,
                            writeMode = mode,
                            editDiaryId = destination.editDiaryId,
                            onBack = { onNavigate(MainNavEvent.WriteBack) },
                            onSaveComplete = { onNavigate(MainNavEvent.WriteSaveComplete) },
                            onDelete = {
                                // 삭제 완료 이벤트만 onNavigate로 전달 (실제 삭제는 ViewModel에서)
                                onNavigate(MainNavEvent.WriteDeleteComplete)
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isDarkTheme = isSystemInDarkTheme()
    
    // Icon Logic for styling
    val color = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDarkTheme) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        }
    }
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    val iconSize = 24.dp

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = fontWeight,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}
