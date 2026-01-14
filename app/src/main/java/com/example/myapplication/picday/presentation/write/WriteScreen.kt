package com.example.myapplication.picday.presentation.write

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
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.presentation.component.CircularPhotoPlaceholder
import com.example.myapplication.picday.presentation.component.WriteTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    selectedDate: LocalDate,
    items: List<Diary>,
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosAdded(uris.map { it.toString() })
        }
    }

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
            Spacer(modifier = Modifier.height(24.dp))

            if (coverPhotoUri != null) {
                CoverPhoto(uri = coverPhotoUri)
            } else {
                CircularPhotoPlaceholder()
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditMode) {
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
                            val existingUris = writeState.photoItems.map { it.uri }.toSet()
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

                val visiblePhotoItems = writeState.photoItems.filter { it.state != WritePhotoState.DELETE }
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

                WriteEditContent(
                    title = writeState.title,
                    content = writeState.content,
                    onTitleChange = onTitleChange,
                    onContentChange = onContentChange
                )
            } else {
                WriteViewContent(
                    items = items,
                    onAddClick = onAddClick,
                    onEditClick = { diary -> onEditClick(diary.id) }
                )
            }
        }
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
            modifier = Modifier
                .size(24.dp)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White
            )
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
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { stream ->
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
