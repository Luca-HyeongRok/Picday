package com.picday.diary.presentation.main

import androidx.compose.animation.*
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.picday.diary.presentation.calendar.CalendarScreen
import com.picday.diary.presentation.common.SharedViewModel
import com.picday.diary.presentation.diary.DiaryRoot
import com.picday.diary.presentation.diary.DiaryRootScreen
import com.picday.diary.presentation.diary.DiaryViewModel
import com.picday.diary.presentation.navigation.Screen
import com.picday.diary.presentation.navigation.WriteMode
import com.picday.diary.presentation.theme.AppColors
import com.picday.diary.presentation.theme.AppShapes
import java.time.LocalDate

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val sharedViewModel: SharedViewModel = viewModel()
    
    val backStack = rememberNavBackStack(MainDestination.Calendar)
    val currentDestination = backStack.last()
    val isWriteMode = currentDestination is MainDestination.Write
    val selectedDate by sharedViewModel.selectedDate.collectAsState()
    var pendingEffects by remember { mutableStateOf<List<MainNavEffect>>(emptyList()) }
    var pendingEffectsVersion by remember { mutableStateOf(0) }
    var pendingEditDiaryId by remember { mutableStateOf<String?>(null) }

    fun dispatchNav(event: MainNavEvent) {
        val current = backStack.asMainDestinations()
        val result = reduceMainNav(current, event)
        backStack.clear()
        backStack.addAll(result.backStack)
        pendingEffects = result.effects
        pendingEffectsVersion += 1
    }

    LaunchedEffect(pendingEffectsVersion) {
        if (pendingEffects.isEmpty()) return@LaunchedEffect

        pendingEffects.forEach { effect ->
            when (effect) {
                MainNavEffect.PopOne -> {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                MainNavEffect.PopToRoot -> {
                    while (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                is MainNavEffect.ConsumeEditDiary -> {
                    pendingEditDiaryId = effect.diaryId
                }
            }
        }
        pendingEffects = emptyList()
    }

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
                                    dispatchNav(MainNavEvent.BottomTabClick(MainDestination.Calendar))
                                }
                            )

                            // Spacer for Center Fab
                            Box(modifier = Modifier.width(64.dp))

                            // Right Tab: Diary
                            BottomNavItem(
                                screen = Screen.Diary,
                                isSelected = currentDestination is MainDestination.Diary,
                                onClick = {
                                    dispatchNav(MainNavEvent.BottomTabClick(MainDestination.Diary))
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
                                dispatchNav(MainNavEvent.WriteAddClick(selectedDate))
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
            onBack = { dispatchNav(MainNavEvent.WriteBack) },
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

                            dispatchNav(MainNavEvent.CalendarDateSelected(date, mode))
                        }
                    )
                }

                entry<MainDestination.Diary> {
                    DiaryRoot(
                        screen = DiaryRootScreen.DIARY,
                        selectedDate = selectedDate,
                        onWriteClick = { date, mode ->
                            dispatchNav(MainNavEvent.DiaryWriteClick(date, mode))
                        },
                        onEditClick = { diaryId ->
                            dispatchNav(MainNavEvent.DiaryEditClick(selectedDate, diaryId))
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
                    var pendingDelete by remember { mutableStateOf(false) }

                    LaunchedEffect(pendingEditDiaryId) {
                        val diaryId = pendingEditDiaryId ?: return@LaunchedEffect
                        writeViewModel.onEditClicked(diaryId)
                        pendingEditDiaryId = null
                    }

                    LaunchedEffect(pendingDelete, writeState.uiMode, writeState.editingDiaryId) {
                        if (
                            pendingDelete &&
                            writeState.uiMode == com.picday.diary.presentation.write.state.WriteUiMode.VIEW &&
                            writeState.editingDiaryId == null
                        ) {
                            pendingDelete = false
                            dispatchNav(MainNavEvent.WriteDeleteComplete)
                        }
                    }

                    DiaryRoot(
                        screen = DiaryRootScreen.WRITE,
                        selectedDate = date,
                        writeMode = mode,
                        editDiaryId = null,
                        onBack = { dispatchNav(MainNavEvent.WriteBack) },
                        onSaveComplete = { dispatchNav(MainNavEvent.WriteSaveComplete) },
                        onDelete = {
                            pendingDelete = true
                            writeViewModel.onDelete(it)
                        }
                    )
                }
            }
        )
    }
}

private fun List<NavKey>.asMainDestinations(): List<MainDestination> {
    return map { key ->
        require(key is MainDestination) {
            "MainScreen only supports MainDestination. Found: ${key::class.qualifiedName}"
        }
        key
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
