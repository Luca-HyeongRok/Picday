package com.example.myapplication.picday.presentation.write.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.presentation.component.DiaryRecordCard
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.state.WriteUiMode

@Composable
fun ColumnScope.WriteContent(
    uiMode: WriteUiMode,
    coverPhotoUri: String?,
    items: List<Diary>,
    title: String,
    content: String,
    photoItems: List<WritePhotoItem>,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit
) {
    if (uiMode == WriteUiMode.VIEW) {
        WriteViewContent(
            coverPhotoUri = coverPhotoUri,
            items = items,
            onAddClick = onAddClick,
            onEditClick = { diary -> onEditClick(diary.id) }
        )
    } else {
        WriteEditContent(
            coverPhotoUri = coverPhotoUri,
            photoItems = photoItems,
            title = title,
            content = content,
            onTitleChange = onTitleChange,
            onContentChange = onContentChange,
            onPhotosAdded = onPhotosAdded,
            onPhotoRemoved = onPhotoRemoved
        )
    }
}

@Composable
fun ColumnScope.WriteViewContent(
    coverPhotoUri: String?,
    items: List<Diary>,
    onAddClick: () -> Unit,
    onEditClick: (Diary) -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))

    WriteCoverPhoto(coverPhotoUri = coverPhotoUri)

    Spacer(modifier = Modifier.height(32.dp))

    if (items.isEmpty()) {
        Text(
            text = "이 날의 기록이 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("오늘의 첫 번째 기록 추가하기")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                DiaryRecordCard(
                    date = item.date,
                    title = item.title,
                    previewContent = item.previewContent,
                    coverPhotoUri = null,
                    onClick = {},
                    onEditClick = { onEditClick(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("기록 추가하기")
        }
    }
}
