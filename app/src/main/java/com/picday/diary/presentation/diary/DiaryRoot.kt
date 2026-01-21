package com.picday.diary.presentation.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.picday.diary.presentation.navigation.WriteMode
import com.picday.diary.presentation.write.WriteScreen
import com.picday.diary.presentation.write.WriteViewModel
import com.picday.diary.presentation.write.state.WriteUiMode
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val diaryState by diaryViewModel.uiState.collectAsState()
    val writeState by writeViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var currentPhotoIndex by rememberSaveable(selectedDate) { mutableStateOf(0) }

    val lastDiaryItem = diaryState.uiItems.lastOrNull()
    val coverPhotoUri = when (writeState.uiMode) {
        WriteUiMode.VIEW -> diaryState.allPhotosForDate.firstOrNull() ?: lastDiaryItem?.coverPhotoUri
        else -> writeViewModel.getCoverPhotoUri()
    }
    val viewModePhotoUris = when (writeState.uiMode) {
        WriteUiMode.VIEW -> diaryState.allPhotosForDate
        else -> emptyList()
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
                onEditClick = onEditClick,
                onPreviousDate = { diaryViewModel.moveDateBy(-1) },
                onNextDate = { diaryViewModel.moveDateBy(1) }
            )
        }
        DiaryRootScreen.WRITE -> {
            WriteScreen(
                selectedDate = selectedDate,
                items = diaryState.uiItems,
                writeState = writeState,
                coverPhotoUri = coverPhotoUri,
                viewModePhotoUris = viewModePhotoUris,
                currentPhotoIndex = currentPhotoIndex,
                onBack = {
                    val representativeUri = if (writeState.uiMode == WriteUiMode.VIEW) {
                        viewModePhotoUris.lastOrNull()
                    } else {
                        writeViewModel.getRepresentativePhotoUriForExit()
                    }
                    coroutineScope.launch {
                        diaryViewModel.saveDateCoverPhoto(selectedDate, representativeUri)
                        onBack()
                    }
                },
                onSave = {
                    val representativeUri = writeViewModel.getRepresentativePhotoUriForExit()
                    writeViewModel.onSave(selectedDate) { uris ->
                        uris.forEach { uri ->
                            try {
                                if (uri.startsWith("content://")) {
                                    context.contentResolver.releasePersistableUriPermission(
                                        Uri.parse(uri),
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                            } catch (ignored: Exception) {}
                        }
                    }
                    coroutineScope.launch {
                        diaryViewModel.saveDateCoverPhoto(selectedDate, representativeUri)
                    }
                    onSaveComplete()
                },
                onAddClick = { writeViewModel.onAddClicked() },
                onEditClick = { diaryId -> writeViewModel.onEditClicked(diaryId) },
                onTitleChange = { title -> writeViewModel.onTitleChanged(title) },
                onContentChange = { content -> writeViewModel.onContentChanged(content) },
                onPhotosAdded = { uris -> writeViewModel.onPhotosAdded(uris) },
                onPhotoRemoved = { photoId -> writeViewModel.onPhotoRemoved(photoId) },
                onDelete = onDelete,
                onPageSelected = { index -> currentPhotoIndex = index },
                onPhotoClick = { photoId -> writeViewModel.onPhotoClicked(photoId) }
            )
        }
    }
}
