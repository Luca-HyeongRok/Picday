package com.example.myapplication.picday.presentation.diary.write

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.presentation.component.CircularPhotoPlaceholder
import com.example.myapplication.picday.presentation.component.WriteTopBar
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    viewModel: DiaryViewModel,
    selectedDate: LocalDate,
    mode: WriteMode,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val writeState by viewModel.writeState.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    val items = uiState.items
    val isEditMode = writeState.uiMode != WriteUiMode.VIEW
    val editingDiary = items.firstOrNull { it.id == writeState.editingDiaryId }

    // 날짜 변경 시 ViewModel 동기화
    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    // 편집 대상 변경 시 입력값 세팅
    LaunchedEffect(selectedDate, mode) {
        // 화면 진입 시 모드를 ViewModel로 위임
        viewModel.setWriteMode(mode)
        title = ""
        content = ""
    }

    LaunchedEffect(writeState.editingDiaryId, writeState.uiMode) {
        if (writeState.uiMode == WriteUiMode.ADD) {
            title = ""
            content = ""
        } else if (writeState.uiMode == WriteUiMode.EDIT && editingDiary != null) {
            title = editingDiary.title.orEmpty()
            content = editingDiary.previewContent
        }
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = onBack,
                onSave = {
                    val normalizedTitle = title.trim().ifBlank { null }
                    // 저장 로직은 ViewModel에서 처리
                    viewModel.onSaveClicked(
                        date = selectedDate,
                        title = normalizedTitle,
                        content = content
                    )
                    onSaveComplete()
                },
                canSave = isEditMode && content.isNotBlank()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            CircularPhotoPlaceholder()

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditMode) {
                WriteEditContent(
                    title = title,
                    content = content,
                    onTitleChange = { title = it },
                    onContentChange = { content = it }
                )
            } else {
                WriteViewContent(
                    items = items,
                    onAddClick = {
                        viewModel.onAddClicked()
                    },
                    onEditClick = { diary ->
                        viewModel.onEditClicked(diary.id)
                    }
                )
            }
        }
    }
}
