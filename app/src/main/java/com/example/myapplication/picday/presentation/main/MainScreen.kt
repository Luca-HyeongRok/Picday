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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.navigation.MainNavHost
import com.example.myapplication.picday.presentation.navigation.Screen
import com.example.myapplication.picday.presentation.navigation.WriteMode
import com.example.myapplication.picday.presentation.theme.AppColors
import com.example.myapplication.picday.presentation.theme.AppShapes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    val isWriteMode = currentRoute?.startsWith("write") == true
    val selectedDate by sharedViewModel.selectedDate.collectAsState()

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
                                isSelected = currentDestination?.hierarchy?.any { it.route == Screen.Calendar.route } == true,
                                onClick = {
                                    navController.navigate(Screen.Calendar.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // Spacer for Center Fab
                            Box(modifier = Modifier.width(64.dp))

                            // Right Tab: Diary
                            BottomNavItem(
                                screen = Screen.Diary,
                                isSelected = currentDestination?.hierarchy?.any { it.route == Screen.Diary.route } == true,
                                onClick = {
                                    navController.navigate(Screen.Diary.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
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
                                navController.navigate(
                                    Screen.Write.createRoute(selectedDate, WriteMode.ADD)
                                )
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
        MainNavHost(
            navController = navController,
            sharedViewModel = sharedViewModel,
            innerPadding = PaddingValues(0.dp)
        )
    }
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
