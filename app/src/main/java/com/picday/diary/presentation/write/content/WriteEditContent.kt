package com.picday.diary.presentation.write.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.picday.diary.BuildConfig
import com.picday.diary.R
import com.picday.diary.presentation.write.photo.WritePhotoItem
import com.picday.diary.presentation.write.photo.WritePhotoState
import androidx.compose.foundation.BorderStroke

@Composable
fun ColumnScope.WriteEditContent(
    coverPhotoUri: String?,
    photoItems: List<WritePhotoItem>,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onPhotoClick: (String) -> Unit = {},
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // 1. Cover Photo Area
        item {
            WriteCoverPhoto(
                coverPhotoUri = coverPhotoUri,
                onClick = {}
            )
        }

        // 2. Action Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        2.dp,
                        RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.05f)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCameraClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF333333)
                            )
                            Text(
                                "카메라",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            )
                        }
                    }
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                    TextButton(
                        onClick = onGalleryClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF333333)
                            )
                            Text(
                                "갤러리",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Thumbnails
        item {
            val visiblePhotoItems = photoItems.filter { it.state != WritePhotoState.DELETE }
            if (visiblePhotoItems.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(visiblePhotoItems, key = { it.id }) { item ->
                        PhotoThumbnail(
                            uri = item.uri,
                            onRemove = { onPhotoRemoved(item.id) },
                            onClick = { onPhotoClick(item.id) }
                        )
                    }
                }
            }
        }

        // 4. Input Fields
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = {
                        Text(
                            "제목",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.4f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                TextField(
                    value = content,
                    onValueChange = onContentChange,
                    placeholder = {
                        Text(
                            "오늘의 기록",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.4f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        if (BuildConfig.DEBUG) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = {
                    val existingUris = photoItems
                        .filter { it.state != WritePhotoState.DELETE }
                        .map { it.uri }
                        .toSet()
                            val sampleRes = listOf(
                                R.drawable.sample_photo_1,
                                R.drawable.sample_photo_2,
                                R.drawable.sample_photo_3,
                                R.drawable.sample_photo_4
                            )
                            val next = sampleRes
                                .map { "android.resource://${context.packageName}/$it" }
                                .firstOrNull { it !in existingUris }
                            next?.let { onPhotosAdded(listOf(it)) }
                        },
                        shape = RoundedCornerShape(100),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            "샘플 사진 추가 (DEBUG)",
                            style = TextStyle(fontSize = 9.sp, color = Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(uri: String, onRemove: () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.TopEnd
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Box(modifier = Modifier.size(18.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, contentDescription = "사진 삭제", tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
internal fun WriteCoverPhoto(
    coverPhotoUri: String?,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                    )
                )
            )
            .clickable { onClick() }
            .then(
                if (coverPhotoUri == null) {
                    Modifier.border(BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)), RoundedCornerShape(32.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (coverPhotoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coverPhotoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Text(
                    text = "사진 추가",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
