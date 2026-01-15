package com.example.myapplication.picday.presentation.main

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // 탭 간 데이터 공유를 위한 SharedViewModel
    val sharedViewModel: SharedViewModel = viewModel()

    val items = listOf(Screen.Calendar, Screen.Diary)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = Color.Transparent, // Scaffold 바탕 투명 처리 (배경화면 위해)
        bottomBar = {
            // 가독성을 위한 아주 은은한 하단 그라데이션 (Fog Scrim)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.5f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp, top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // 클릭 효과 제거
                                ) {
                                    if (!isSelected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                                .padding(horizontal = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) Color(0xFF2D2D2D) else Color(0xFF757575)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = screen.title,
                                fontSize = 12.sp, // 10sp -> 12sp로 상향
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF2D2D2D) else Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            sharedViewModel = sharedViewModel,
            innerPadding = innerPadding
        )
    }
}
