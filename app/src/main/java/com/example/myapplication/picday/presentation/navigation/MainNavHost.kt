package com.example.myapplication.picday.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.calendar.CalendarScreen
import com.example.myapplication.picday.presentation.diary.DiaryScreen
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.diary.DiaryWriteScreen
import java.time.LocalDate

@Composable
fun MainNavHost(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    innerPadding: PaddingValues
) {
    val selectedDate by sharedViewModel.selectedDate.collectAsState()
    val diaryViewModel: DiaryViewModel = viewModel()

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
                viewModel = diaryViewModel,
                selectedDate = selectedDate,
                onWriteClick = { date ->
                    navController.navigate(Screen.DiaryWrite.createRoute(date))
                }
            )
        }
        composable(
            route = Screen.DiaryWrite.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    defaultValue = LocalDate.now().toString()
                }
            )
        ) { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = LocalDate.parse(dateArg)
            DiaryWriteScreen(
                viewModel = diaryViewModel,
                selectedDate = date,
                onSaveComplete = { navController.popBackStack() }
            )
        }
    }
}
