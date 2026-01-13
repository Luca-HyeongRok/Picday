package com.example.myapplication.picday.presentation.write

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.picday.presentation.component.CircularPhotoPlaceholder
import com.example.myapplication.picday.presentation.component.WriteTopBar
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    viewModel: DiaryViewModel = viewModel(),
    selectedDate: LocalDate,
    mode: WriteMode,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var uiMode by rememberSaveable(selectedDate, mode) {
        mutableStateOf(if (mode == WriteMode.ADD) WriteUiMode.ADD else WriteUiMode.VIEW)
    }
    var editingDiaryId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    val items = uiState.items
    val isEditMode = uiMode != WriteUiMode.VIEW
    val editingDiary = items.firstOrNull { it.id == editingDiaryId }

    // 날짜 변경 시 ViewModel 동기화
    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    // 편집 대상 변경 시 입력값 세팅
    LaunchedEffect(selectedDate, mode) {
        uiMode = if (mode == WriteMode.ADD) WriteUiMode.ADD else WriteUiMode.VIEW
        editingDiaryId = null
        title = ""
        content = ""
    }

    LaunchedEffect(editingDiaryId, uiMode) {
        if (uiMode == WriteUiMode.ADD) {
            title = ""
            content = ""
        } else if (uiMode == WriteUiMode.EDIT && editingDiary != null) {
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

                    if (uiMode == WriteUiMode.ADD) {
                        viewModel.addDiaryForDate(
                            date = selectedDate,
                            title = normalizedTitle,
                            content = content
                        )
                    } else if (uiMode == WriteUiMode.EDIT && editingDiaryId != null) {
                        viewModel.updateDiary(
                            diaryId = editingDiaryId!!,
                            title = normalizedTitle,
                            content = content
                        )
                    }
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
                        uiMode = WriteUiMode.ADD
                        editingDiaryId = null
                    },
                    onEditClick = { diary ->
                        editingDiaryId = diary.id
                        uiMode = WriteUiMode.EDIT
                    }
                )
            }
        }
    }
}
