package com.example.myapplication.picday.presentation.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.picday.presentation.write.WriteScreen
import com.example.myapplication.picday.presentation.write.WriteViewModel
import com.example.myapplication.picday.presentation.write.state.WriteUiMode
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

enum class DiaryRootScreen {
    DIARY,
    WRITE
}

@Composable
fun DiaryRoot(
    screen: DiaryRootScreen,
    selectedDate: LocalDate,
    writeMode: WriteMode = WriteMode.VIEW,
    onWriteClick: (LocalDate, WriteMode) -> Unit = { _, _ -> },
    onEditClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {},
    editDiaryId: String? = null,
    onDelete: (String) -> Unit = {}
) {
    val diaryViewModel: DiaryViewModel = hiltViewModel()
    val writeViewModel: WriteViewModel = hiltViewModel()
    val diaryState by diaryViewModel.uiState.collectAsState()
    val writeState by writeViewModel.uiState.collectAsState()
    val coverPhotoUri = when (writeState.uiMode) {
        WriteUiMode.VIEW -> diaryState.uiItems.lastOrNull()?.coverPhotoUri
        else -> writeViewModel.getCoverPhotoUri()
    }

    LaunchedEffect(selectedDate) {
        diaryViewModel.onDateSelected(selectedDate)
    }

    LaunchedEffect(screen, writeMode, editDiaryId) {
        if (screen == DiaryRootScreen.WRITE) {
            if (editDiaryId != null) {
                writeViewModel.onEditClicked(editDiaryId)
                return@LaunchedEffect
            }
            val uiMode = when (writeMode) {
                WriteMode.ADD -> WriteUiMode.ADD
                WriteMode.VIEW -> WriteUiMode.VIEW
            }
            writeViewModel.setMode(uiMode)
        }
    }

    when (screen) {
        DiaryRootScreen.DIARY -> {
            DiaryScreen(
                uiState = diaryState,
                onWriteClick = onWriteClick,
                onEditClick = onEditClick
            )
        }
        DiaryRootScreen.WRITE -> {
            WriteScreen(
                selectedDate = selectedDate,
                items = diaryState.uiItems,
                writeState = writeState,
                coverPhotoUri = coverPhotoUri,
                onBack = onBack,
                onSave = {
                    writeViewModel.onSave(selectedDate)
                    onSaveComplete()
                },
                onAddClick = { writeViewModel.onAddClicked() },
                onEditClick = { diaryId -> writeViewModel.onEditClicked(diaryId) },
                onTitleChange = { title -> writeViewModel.onTitleChanged(title) },
                onContentChange = { content -> writeViewModel.onContentChanged(content) },
                onPhotosAdded = { uris -> writeViewModel.onPhotosAdded(uris) },
                onPhotoRemoved = { photoId -> writeViewModel.onPhotoRemoved(photoId) },
                onDelete = onDelete
            )
        }
    }
}
