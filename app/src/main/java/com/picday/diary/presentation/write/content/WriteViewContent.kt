package com.picday.diary.presentation.write.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.picday.diary.presentation.component.DiaryRecordCard
import com.picday.diary.presentation.diary.DiaryUiItem
import com.picday.diary.presentation.theme.AppShapes
import com.picday.diary.presentation.write.photo.WritePhotoItem
import com.picday.diary.presentation.write.state.WriteUiMode

@Composable
fun ColumnScope.WriteContent(
    uiMode: WriteUiMode,
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    currentPhotoIndex: Int = 0,
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
            currentPhotoIndex = currentPhotoIndex,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.WriteViewContent(
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    currentPhotoIndex: Int = 0,
    items: List<DiaryUiItem>,
    onAddClick: () -> Unit,
    onEditClick: (DiaryUiItem) -> Unit,
    onPageSelected: (Int) -> Unit = {}
) {
    Spacer(modifier = Modifier.height(16.dp))

    // 1. Highlighted Cover Area
    if (viewModePhotoUris.isNotEmpty()) {
        val pageCount = viewModePhotoUris.size
        val safeInitialPage = currentPhotoIndex.coerceIn(0, pageCount - 1)
        val pagerState = rememberPagerState(
            initialPage = safeInitialPage,
            pageCount = { pageCount }
        )

        LaunchedEffect(pageCount, safeInitialPage) {
            if (pagerState.currentPage != safeInitialPage) {
                pagerState.scrollToPage(safeInitialPage)
            }
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onPageSelected(page)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            WriteCoverPhoto(
                coverPhotoUri = viewModePhotoUris[page],
                onClick = {}
            )
        }
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

    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    Spacer(modifier = Modifier.height(16.dp))
}
