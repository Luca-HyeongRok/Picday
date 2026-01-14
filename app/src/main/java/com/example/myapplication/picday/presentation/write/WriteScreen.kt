package com.example.myapplication.picday.presentation.write

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.presentation.component.WriteTopBar
import com.example.myapplication.picday.presentation.diary.DiaryUiItem
import com.example.myapplication.picday.presentation.write.content.WriteContent
import com.example.myapplication.picday.presentation.write.state.WriteState
import com.example.myapplication.picday.presentation.write.state.WriteUiMode
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    selectedDate: LocalDate,
    items: List<DiaryUiItem>,
    writeState: WriteState,
    coverPhotoUri: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit
) {
    val isEditMode = writeState.uiMode != WriteUiMode.VIEW

    Scaffold(
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = onBack,
                onSave = onSave,
                canSave = isEditMode && writeState.content.isNotBlank()
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
            WriteContent(
                uiMode = writeState.uiMode,
                coverPhotoUri = coverPhotoUri,
                items = items,
                title = writeState.title,
                content = writeState.content,
                photoItems = writeState.photoItems,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onPhotosAdded = onPhotosAdded,
                onPhotoRemoved = onPhotoRemoved
            )
        }
    }
}
