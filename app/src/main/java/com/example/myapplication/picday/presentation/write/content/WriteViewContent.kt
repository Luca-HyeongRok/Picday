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
import com.example.myapplication.picday.presentation.component.DiaryRecordCard
import com.example.myapplication.picday.presentation.diary.DiaryUiItem
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.state.WriteUiMode
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

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
    onPageSelected: (Int) -> Unit = {}
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
            onPhotoRemoved = onPhotoRemoved
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
    Spacer(modifier = Modifier.height(24.dp))

    if (viewModePhotoUris.size > 1) {
        WritePhotoPager(
            photoUris = viewModePhotoUris,
            onPageSelected = onPageSelected
        )
    } else {
        WriteCoverPhoto(coverPhotoUri = coverPhotoUri)
    }

    Spacer(modifier = Modifier.height(32.dp))

    if (items.isEmpty()) {
        Text(
            text = "이 날의 기록이 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("오늘의 첫 번째 기록 추가하기", style = MaterialTheme.typography.titleSmall)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                DiaryRecordCard(
                    date = item.date,
                    title = item.title,
                    previewContent = item.previewContent,
                    coverPhotoUri = item.coverPhotoUri,
                    onClick = null,
                    onEditClick = { onEditClick(item) },
                    placeholderAlphaEmpty = 0.12f,
                    placeholderAlphaLoading = 0.2f
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("기록 추가하기", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun WritePhotoPager(
    photoUris: List<String>,
    onPageSelected: (Int) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { photoUris.size })

    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        onPageSelected(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp), // 기존 CoverPhoto(220dp)보다 약간 더 키워서 존재감 부여
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 16.dp, // 페이지 간 간격
            beyondViewportPageCount = 1
        ) { page ->
            val uri = photoUris[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp) // 좌우 여백을 주어 다음 사진이 살짝 보이게 하거나 깔끔하게 중앙 배치
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "사진 ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
