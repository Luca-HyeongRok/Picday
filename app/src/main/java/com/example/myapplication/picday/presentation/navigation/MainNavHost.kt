package com.example.myapplication.picday.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.picday.presentation.common.SharedViewModel
import com.example.myapplication.picday.presentation.calendar.CalendarScreen
import com.example.myapplication.picday.presentation.diary.DiaryRoot
import com.example.myapplication.picday.presentation.diary.DiaryRootScreen
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

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
            val diaryViewModel: DiaryViewModel = hiltViewModel()
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
            DiaryRoot(
                screen = DiaryRootScreen.DIARY,
                selectedDate = selectedDate,
                onWriteClick = { date, mode -> navController.navigate(Screen.Write.createRoute(date, mode)) },
                onEditClick = { diaryId ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("editDiaryId", diaryId)
                    navController.navigate(Screen.Write.createRoute(selectedDate, WriteMode.VIEW))
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
            val previousEntry = navController.previousBackStackEntry
            val editDiaryId = previousEntry?.savedStateHandle?.get<String>("editDiaryId")
            LaunchedEffect(editDiaryId) {
                if (editDiaryId != null) {
                    previousEntry.savedStateHandle.remove<String>("editDiaryId")
                }
            }
            DiaryRoot(
                screen = DiaryRootScreen.WRITE,
                selectedDate = date,
                writeMode = mode,
                editDiaryId = editDiaryId,
                onBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() },
            )
        }
    }
}
