package com.picday.diary.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.picday.diary.presentation.calendar.CalendarScreen
import com.picday.diary.presentation.common.SharedViewModel
import com.picday.diary.presentation.diary.DiaryRoot
import com.picday.diary.presentation.diary.DiaryRootScreen
import com.picday.diary.presentation.diary.DiaryViewModel
import com.picday.diary.presentation.write.WriteViewModel
import java.time.LocalDate

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
        modifier = Modifier // padding(innerPadding) 제거하여 풀스크린 구현
    ) {

        /* --------------------------------
         * Calendar
         * -------------------------------- */
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

                    navController.navigate(
                        Screen.Write.createRoute(date, mode)
                    )
                }
            )
        }

        /* --------------------------------
         * Diary
         * -------------------------------- */
        composable(Screen.Diary.route) {
            DiaryRoot(
                screen = DiaryRootScreen.DIARY,
                selectedDate = selectedDate,
                onWriteClick = { date, mode ->
                    navController.navigate(
                        Screen.Write.createRoute(date, mode)
                    )
                },
                onEditClick = { diaryId ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("editDiaryId", diaryId)

                    navController.navigate(
                        Screen.Write.createRoute(selectedDate, WriteMode.VIEW)
                    )
                }
            )
        }

        /* --------------------------------
         * Write
         * -------------------------------- */
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

            val writeViewModel: WriteViewModel = hiltViewModel()

            val dateArg =
                backStackEntry.arguments?.getString("date")
                    ?: Uri.encode(LocalDate.now().toString())

            val date = LocalDate.parse(Uri.decode(dateArg))

            val modeArg =
                backStackEntry.arguments?.getString("mode")
                    ?: WriteMode.ADD.name

            val mode = WriteMode.valueOf(modeArg)

            val previousEntry = navController.previousBackStackEntry

            /**
             * EDIT 진입 처리
             * - savedStateHandle은 previousEntry 기준
             * - LaunchedEffect(Unit)으로 1회 소비
             */
            LaunchedEffect(Unit) {
                val handle = previousEntry?.savedStateHandle
                val editDiaryId = handle?.get<String>("editDiaryId")

                if (editDiaryId != null) {
                    writeViewModel.onEditClicked(editDiaryId)
                    handle.remove<String>("editDiaryId")
                }
            }

            DiaryRoot(
                screen = DiaryRootScreen.WRITE,
                selectedDate = date,
                writeMode = mode,
                onBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() },
                onDelete = {
                    writeViewModel.onDelete(it)
                    navController.popBackStack()
                }
            )
        }
    }
}
