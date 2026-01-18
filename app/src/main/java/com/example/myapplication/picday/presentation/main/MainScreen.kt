package com.example.myapplication.picday.presentation.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.calendar.CalendarScreen
import com.example.myapplication.picday.presentation.diary.DiaryRoot
import com.example.myapplication.picday.presentation.diary.DiaryRootScreen
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.navigation.Screen
import com.example.myapplication.picday.presentation.navigation.WriteMode
import com.example.myapplication.picday.presentation.theme.AppColors
import com.example.myapplication.picday.presentation.theme.AppShapes
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val sharedViewModel: SharedViewModel = viewModel()
    
    val backStack = rememberNavBackStack(MainDestination.Calendar)
    val currentDestination = backStack.last()
    val isWriteMode = currentDestination is MainDestination.Write
    val selectedDate by sharedViewModel.selectedDate.collectAsState()

    fun popToRoot() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun popOne() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun switchBottomTab(target: MainDestination) {
        popToRoot()
        if (backStack.last() != target) {
            backStack.add(target)
        }
    }

    fun createWriteDestination(
        date: LocalDate,
        mode: WriteMode,
        editDiaryId: String? = null
    ): MainDestination.Write {
        return MainDestination.Write(
            date = date.toString(),
            mode = mode.name,
            editDiaryId = editDiaryId
        )
    }

    fun navigateToWrite(date: LocalDate, mode: WriteMode, editDiaryId: String? = null) {
        backStack.add(createWriteDestination(date, mode, editDiaryId))
    }

    fun onBottomTabClick(target: MainDestination) = switchBottomTab(target)
    fun onCalendarDateSelected(date: LocalDate, mode: WriteMode) = navigateToWrite(date, mode)
    fun onDiaryWriteClick(date: LocalDate, mode: WriteMode) = navigateToWrite(date, mode)
    fun onDiaryEditClick(date: LocalDate, editDiaryId: String) =
        navigateToWrite(date, WriteMode.VIEW, editDiaryId)
    fun onWriteAddClick(date: LocalDate) = navigateToWrite(date, WriteMode.ADD)
    fun onWriteBack() = popOne()
    fun onWriteSaveComplete() = popOne()
    fun onWriteDelete() = popOne()

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
                                    onBottomTabClick(MainDestination.Calendar)
                                }
                            )

                            // Spacer for Center Fab
                            Box(modifier = Modifier.width(64.dp))

                            // Right Tab: Diary
                            BottomNavItem(
                                screen = Screen.Diary,
                                isSelected = currentDestination is MainDestination.Diary,
                                onClick = {
                                    onBottomTabClick(MainDestination.Diary)
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
                                onWriteAddClick(selectedDate)
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
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = { popOne() },
            entryProvider = entryProvider {
                entry<MainDestination.Calendar> {
                    val diaryViewModel: DiaryViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()

                    CalendarScreen(
                        onDateSelected = { date ->
                            sharedViewModel.updateSelectedDate(date)

                            val mode = if (diaryViewModel.hasAnyRecord(date)) {
                                WriteMode.VIEW
                            } else {
                                WriteMode.ADD
                            }

                            onCalendarDateSelected(date, mode)
                        }
                    )
                }

                entry<MainDestination.Diary> {
                    DiaryRoot(
                        screen = DiaryRootScreen.DIARY,
                        selectedDate = selectedDate,
                        onWriteClick = { date, mode ->
                            onDiaryWriteClick(date, mode)
                        },
                        onEditClick = { diaryId ->
                            onDiaryEditClick(selectedDate, diaryId)
                        }
                    )
                }

                entry<MainDestination.Write> { destination ->
                    val date = runCatching { LocalDate.parse(destination.date) }
                        .getOrElse { selectedDate }
                    val mode = runCatching { WriteMode.valueOf(destination.mode) }
                        .getOrElse { WriteMode.ADD }
                    val writeViewModel: com.example.myapplication.picday.presentation.write.WriteViewModel =
                        androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()

                    DiaryRoot(
                        screen = DiaryRootScreen.WRITE,
                        selectedDate = date,
                        writeMode = mode,
                        editDiaryId = destination.editDiaryId,
                        onBack = { onWriteBack() },
                        onSaveComplete = { onWriteSaveComplete() },
                        onDelete = {
                            writeViewModel.onDelete(it)
                            onWriteDelete()
                        }
                    )
                }
            }
        )
    }
}

@Serializable
private sealed interface MainDestination : NavKey {
    @Serializable
    data object Calendar : MainDestination

    @Serializable
    data object Diary : MainDestination

    @Serializable
    data class Write(
        val date: String,
        val mode: String,
        val editDiaryId: String? = null
    ) : MainDestination
}

@Composable
private fun BottomNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Icon Logic for styling
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
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
