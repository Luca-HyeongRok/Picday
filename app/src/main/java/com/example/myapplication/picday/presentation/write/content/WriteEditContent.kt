package com.example.myapplication.picday.presentation.write.content

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import android.content.pm.PackageManager
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.picday.BuildConfig
import com.example.myapplication.picday.R
import com.example.myapplication.picday.presentation.write.photo.WritePhotoItem
import com.example.myapplication.picday.presentation.write.photo.WritePhotoState

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
    onPhotoClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    fun onImagesPicked(uris: List<android.net.Uri>) {
        if (uris.isEmpty()) return
        onPhotosAdded(uris.map { it.toString() })
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        onImagesPicked(uris)
    }

    // --- Camera Logic Start ---
    var tempPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val tempPhotoUri = tempPhotoUriString?.let { Uri.parse(it) }
    
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun createTempPictureUri(): Uri? {
        return runCatching {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("Pictures") ?: context.cacheDir
            val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { uri ->
                onPhotosAdded(listOf(uri.toString()))
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createTempPictureUri()?.let { uri ->
                tempPhotoUriString = uri.toString()
                takePictureLauncher.launch(uri)
            }
        } else {
            // 거부됨 - rationale은 이미 체크했으므로 여기서는 장기 거부 가능성 있음
            val activity = context as? ComponentActivity
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA)) {
                showSettingsDialog = true
            }
        }
    }

    fun handleCameraClick() {
        when {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                createTempPictureUri()?.let { uri ->
                    tempPhotoUriString = uri.toString()
                    takePictureLauncher.launch(uri)
                }
            }
            context is ComponentActivity && ActivityCompat.shouldShowRequestPermissionRationale(context, android.Manifest.permission.CAMERA) -> {
                showRationaleDialog = true
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("카메라 권한 필요") },
            text = { Text("다이어리에 바로 찍은 사진을 올리려면 카메라 권한이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) { Text("허용") }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) { Text("취소") }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("권한 설정 안내") },
            text = { Text("카메라 권한이 거부되어 있습니다. 설정 화면에서 권한을 허용해주세요.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("설정으로 이동") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("취소") }
            }
        )
    }
    // --- Camera Logic End ---

    Spacer(modifier = Modifier.height(24.dp))

    // Cover Photo Area
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        WriteCoverPhoto(coverPhotoUri = coverPhotoUri)
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Photo Buttons Area (Camera & Gallery)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Camera Button
        Button(
            onClick = { handleCameraClick() },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("카메라", style = MaterialTheme.typography.titleSmall)
        }

        // Gallery Button
        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("갤러리", style = MaterialTheme.typography.titleSmall)
        }
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
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp)
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
                .padding(top = 16.dp)
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

    Spacer(modifier = Modifier.height(24.dp))

    // Title Field
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("제목") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Content Field
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        label = { Text("오늘의 기록") },
        placeholder = { Text("오늘의 기억을 남겨보세요") },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
internal fun WriteCoverPhoto(coverPhotoUri: String?) {
    if (coverPhotoUri != null) {
        CoverPhoto(uri = coverPhotoUri)
    } else {
        // Rounded Placeholder
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add, // Or a Camera icon if available, sticking to Add/Edit vibe
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    uri: String,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.TopEnd
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
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
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
