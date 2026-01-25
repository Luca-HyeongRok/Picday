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
import androidx.compose.runtime.remember
import com.picday.diary.core.navigation.WriteMode
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

    val lastDiaryItem = diaryState.uiItems.lastOrNull()
    val savedCoverUri = diaryState.coverPhotoByDate[selectedDate]
    
    // View 모드일 때 보여줄 사진 목록 (DataStore 순서 섞임 없이 시간순/원본순 유지)
    val viewModePhotoUris = when (writeState.uiMode) {
        WriteUiMode.VIEW -> diaryState.allPhotosForDate
        else -> emptyList()
    }
    
    // 초기 인덱스 계산: 저장된 커버 사진이 있다면 그 위치에서 시작
    val initialPhotoIndex = remember(viewModePhotoUris, savedCoverUri) {
         val index = viewModePhotoUris.indexOf(savedCoverUri)
         if (index >= 0) index else 0
    }

    var currentPhotoIndex by rememberSaveable(selectedDate, viewModePhotoUris) { 
        mutableStateOf(initialPhotoIndex) 
    }

    // Pager 등 외부 요인으로 초기값이 변경되어야 할 경우 동기화 (선택적)
    LaunchedEffect(initialPhotoIndex) {
        if (currentPhotoIndex == 0 && initialPhotoIndex > 0) {
            currentPhotoIndex = initialPhotoIndex
        }
    }

    val coverPhotoUri = when (writeState.uiMode) {
        WriteUiMode.VIEW -> savedCoverUri ?: lastDiaryItem?.coverPhotoUri
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
                    if (writeState.uiMode == WriteUiMode.VIEW) {
                        // 멈춘 상태에서 뒤로가기 시점에 현재 보고 있는 사진을 대표사진으로 저장
                        val currentUri = viewModePhotoUris.getOrNull(currentPhotoIndex)
                        if (currentUri != null) {
                            coroutineScope.launch {
                                diaryViewModel.saveDateCoverPhoto(selectedDate, currentUri)
                            }
                        }
                    }
                    onBack()
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
                        // 작성 완료 시점에도 캘린더 커버를 즉시 업데이트
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
