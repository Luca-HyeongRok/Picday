package com.example.myapplication.picday.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.calendar.CalendarScreen
import com.example.myapplication.picday.presentation.diary.DiaryScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    innerPadding: PaddingValues
) {
    val selectedDate by sharedViewModel.selectedDate.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Calendar.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onDateSelected = { date ->
                    sharedViewModel.updateSelectedDate(date)
                }
            )
        }
        composable(Screen.Diary.route) {
            DiaryScreen(
                selectedDate = selectedDate
            )
        }
    }
}
