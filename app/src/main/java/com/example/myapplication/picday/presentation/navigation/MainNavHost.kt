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
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.calendar.CalendarScreen
import com.example.myapplication.picday.presentation.diary.DiaryScreen
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.diary.write.WriteScreen
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
                    val mode = if (diaryViewModel.hasAnyRecord(date)) {
                        WriteMode.VIEW
                    } else {
                        WriteMode.ADD
                    }
                    navController.navigate(Screen.Write.createRoute(date, mode))
                }
            )
        }
        composable(Screen.Diary.route) {
            DiaryScreen(
                viewModel = diaryViewModel,
                selectedDate = selectedDate,
                onWriteClick = { date, mode ->
                    navController.navigate(Screen.Write.createRoute(date, mode))
                }
            )
        }
        composable(
            route = Screen.Write.route,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    defaultValue = Uri.encode(LocalDate.now().toString())
                },
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = WriteMode.ADD.name
                }
            )
        ) { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date") ?: Uri.encode(LocalDate.now().toString())
            val date = LocalDate.parse(Uri.decode(dateArg))
            val modeArg = backStackEntry.arguments?.getString("mode") ?: WriteMode.ADD.name
            val mode = WriteMode.valueOf(modeArg)
            WriteScreen(
                viewModel = diaryViewModel,
                selectedDate = date,
                mode = mode,
                onBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() },
            )
        }
    }
}
