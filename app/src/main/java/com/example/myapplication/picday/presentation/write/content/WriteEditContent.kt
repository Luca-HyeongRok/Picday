package com.example.myapplication.picday.presentation.write.content

import android.graphics.BitmapFactory
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.BuildConfig
import com.example.myapplication.picday.R
import com.example.myapplication.picday.presentation.component.CircularPhotoPlaceholder
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.photo.WritePhotoState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ColumnScope.WriteEditContent(
    coverPhotoUri: String?,
    photoItems: List<WritePhotoItem>,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosAdded(uris.map { it.toString() })
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    WriteCoverPhoto(coverPhotoUri = coverPhotoUri)

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedButton(
        onClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("사진 추가")
    }

    if (BuildConfig.DEBUG) {
        OutlinedButton(
            onClick = {
                val existingUris = photoItems.map { it.uri }.toSet()
                val nextSampleUri = listOf(
                    R.drawable.sample_photo_1,
                    R.drawable.sample_photo_2,
                    R.drawable.sample_photo_3,
                    R.drawable.sample_photo_4
                )
                    .map { resId ->
                        "android.resource://${context.packageName}/$resId"
                    }
                    .firstOrNull { it !in existingUris }
                if (nextSampleUri != null) {
                    onPhotosAdded(listOf(nextSampleUri))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("샘플 사진 추가 (DEBUG)")
        }
    }

    val visiblePhotoItems = photoItems.filter { it.state != WritePhotoState.DELETE }
    if (visiblePhotoItems.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(visiblePhotoItems, key = { it.id }) { item ->
                PhotoThumbnail(
                    uri = item.uri,
                    onRemove = { onPhotoRemoved(item.id) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("제목") },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        label = { Text("오늘의 기록") },
        placeholder = { Text("오늘의 기억을 남겨보세요") }
    )
}

@Composable
internal fun WriteCoverPhoto(coverPhotoUri: String?) {
    if (coverPhotoUri != null) {
        CoverPhoto(uri = coverPhotoUri)
    } else {
        CircularPhotoPlaceholder()
    }
}

@Composable
private fun PhotoThumbnail(
    uri: String,
    onRemove: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.TopEnd
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp) // 터치 영역
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp) // 보이는 동그라미
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "사진 삭제",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun CoverPhoto(uri: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))
                    ?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
