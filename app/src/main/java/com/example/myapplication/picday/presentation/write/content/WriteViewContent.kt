package com.example.myapplication.picday.presentation.write.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.picday.presentation.component.DiaryRecordCard
import com.example.myapplication.picday.presentation.diary.DiaryUiItem
import com.example.myapplication.picday.presentation.theme.AppShapes
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.state.WriteUiMode

@Composable
fun ColumnScope.WriteContent(
    uiMode: WriteUiMode,
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    items: List<DiaryUiItem>,
    title: String,
    content: String,
    photoItems: List<WritePhotoItem>,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onPageSelected: (Int) -> Unit = {},
    onPhotoClick: (String) -> Unit = {}
) {
    if (uiMode == WriteUiMode.VIEW) {
        WriteViewContent(
            coverPhotoUri = coverPhotoUri,
            viewModePhotoUris = viewModePhotoUris,
            items = items,
            onAddClick = onAddClick,
            onEditClick = { diary -> onEditClick(diary.id) },
            onPageSelected = onPageSelected
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
            onPhotoRemoved = onPhotoRemoved,
            onPhotoClick = onPhotoClick
        )
    }
}

@Composable
fun ColumnScope.WriteViewContent(
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    items: List<DiaryUiItem>,
    onAddClick: () -> Unit,
    onEditClick: (DiaryUiItem) -> Unit,
    onPageSelected: (Int) -> Unit = {}
) {
    Spacer(modifier = Modifier.height(16.dp))

    // 1. Highlighted Cover Area
    if (viewModePhotoUris.size > 1) {
        // Multi-photo pager logic could be here, but for now we follow the "redesign tone"
        // which emphasizes the square cover photo or a clean highlight.
        WriteCoverPhoto(
             coverPhotoUri = coverPhotoUri ?: viewModePhotoUris.firstOrNull(),
             onClick = {}
        )
    } else {
        WriteCoverPhoto(
            coverPhotoUri = coverPhotoUri,
            onClick = {}
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // 2. Records Section avec design consistent
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "이 날의 기록이 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items) { item ->
                DiaryRecordCard(
                    date = item.date,
                    title = item.title,
                    previewContent = item.previewContent,
                    coverPhotoUri = item.coverPhotoUri,
                    onClick = null, // In view mode, clicking the card might not do anything or open full view
                    onEditClick = { onEditClick(item) },
                    showEditIcon = true
                )
            }
        }
    }

    // 3. Plus Button for adding more records
    Button(
        onClick = onAddClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 4.dp),
        shape = AppShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("기록 추가하기", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}
