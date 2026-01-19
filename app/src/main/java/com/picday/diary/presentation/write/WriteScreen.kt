package com.picday.diary.presentation.write

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.picday.diary.presentation.component.WriteTopBar
import com.picday.diary.presentation.diary.DiaryUiItem
import com.picday.diary.presentation.write.content.WriteContent
import com.picday.diary.presentation.write.state.WriteState
import com.picday.diary.presentation.write.state.WriteUiMode
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    selectedDate: LocalDate,
    items: List<DiaryUiItem>,
    writeState: WriteState,
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    currentPhotoIndex: Int = 0,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPageSelected: (Int) -> Unit = {},
    onPhotoClick: (String) -> Unit = {}
) {
    val isEditMode = writeState.uiMode != WriteUiMode.VIEW
    val isEditingExistingDiary = writeState.editingDiaryId != null

    // 삭제 확인 다이얼로그 표시 여부
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = {
                Text(
                    "정말 이 기록을 삭제하시겠습니까?\n" +
                            "삭제된 데이터는 복구할 수 없습니다."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        writeState.editingDiaryId?.let { diaryId ->
                            onDelete(diaryId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = onBack,
                onSave = onSave,
                canSave = isEditMode && writeState.content.isNotBlank(),
                onDelete = if (isEditingExistingDiary) {
                    { showDeleteDialog = true }
                } else {
                    null
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WriteContent(
                uiMode = writeState.uiMode,
                coverPhotoUri = coverPhotoUri,
                viewModePhotoUris = viewModePhotoUris,
                currentPhotoIndex = currentPhotoIndex,
                items = items,
                title = writeState.title,
                content = writeState.content,
                photoItems = writeState.photoItems,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onPhotosAdded = onPhotosAdded,
                onPhotoRemoved = onPhotoRemoved,
                onPageSelected = onPageSelected,
                onPhotoClick = onPhotoClick
            )
        }
    }
}
