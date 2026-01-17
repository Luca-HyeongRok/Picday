package com.example.myapplication.picday.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.picday.presentation.diary.DiaryUiItem
import java.time.LocalDate

@Composable
fun DiaryItemCard(
    item: DiaryUiItem,
    onClick: (() -> Unit)? = null,
    onEditClick: () -> Unit = {},
    showEditIcon: Boolean = true,
    showThumbnail: Boolean = true,
    thumbnailSize: Dp = 72.dp,
    thumbnailEndPadding: Dp = 16.dp,
    contentPaddingVertical: Dp = 16.dp,
    showDivider: Boolean = false,
    placeholderAlphaEmpty: Float = 0.16f,
    placeholderAlphaLoading: Float = 0.22f
) {
    DiaryRecordCard(
        date = item.date,
        title = item.title,
        previewContent = item.previewContent,
        coverPhotoUri = item.coverPhotoUri,
        onClick = onClick,
        onEditClick = onEditClick,
        showEditIcon = showEditIcon,
        showThumbnail = showThumbnail,
        thumbnailSize = thumbnailSize,
        thumbnailEndPadding = thumbnailEndPadding,
        contentPaddingVertical = contentPaddingVertical,
        showDivider = showDivider,
        placeholderAlphaEmpty = placeholderAlphaEmpty,
        placeholderAlphaLoading = placeholderAlphaLoading
    )
}

@Composable
fun DiaryRecordCard(
    date: LocalDate,
    title: String?,
    previewContent: String,
    coverPhotoUri: String?,
    onClick: (() -> Unit)?,
    onEditClick: () -> Unit,
    showEditIcon: Boolean = true,
    showThumbnail: Boolean = true,
    thumbnailSize: Dp = 72.dp,
    thumbnailEndPadding: Dp = 16.dp,
    contentPaddingVertical: Dp = 16.dp,
    showDivider: Boolean = false,
    placeholderAlphaEmpty: Float = 0.16f,
    placeholderAlphaLoading: Float = 0.22f
) {
    val content: @Composable () -> Unit = {
        Column {
            Box {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = contentPaddingVertical)
                        .padding(end = 32.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showThumbnail) {
                        CoverPhotoOrIcon(
                            uri = coverPhotoUri,
                            size = thumbnailSize,
                            endPadding = thumbnailEndPadding,
                            placeholderAlphaEmpty = placeholderAlphaEmpty,
                            placeholderAlphaLoading = placeholderAlphaLoading
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val displayTitle = title?.takeIf { it.isNotBlank() }
                        if (displayTitle != null) {
                            Text(
                                text = displayTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "${date.monthValue}월 ${date.dayOfMonth}일의 기록",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = previewContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showEditIcon) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "기록 수정",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (onClick == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp
        ) {
            content()
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp
        ) {
            content()
        }
    }
}

@Composable
private fun CoverPhotoOrIcon(
    uri: String?,
    size: Dp,
    endPadding: Dp,
    placeholderAlphaEmpty: Float,
    placeholderAlphaLoading: Float
) {
    val shape = RoundedCornerShape(12.dp)
    
    if (uri == null) {
        Box(
            modifier = Modifier
                .padding(end = endPadding)
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = placeholderAlphaEmpty))
        )
        return
    }

    if (uri != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .padding(end = endPadding)
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .padding(end = endPadding)
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = placeholderAlphaLoading))
        )
    }
}
