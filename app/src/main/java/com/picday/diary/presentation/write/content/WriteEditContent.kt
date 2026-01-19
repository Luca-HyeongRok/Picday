package com.picday.diary.presentation.write.content

import android.Manifest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
    onPhotoClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    fun onImagesPicked(uris: List<Uri>) {
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
            val activity = context as? ComponentActivity
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                showSettingsDialog = true
            }
        }
    }

    fun handleCameraClick() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                createTempPictureUri()?.let { uri ->
                    tempPhotoUriString = uri.toString()
                    takePictureLauncher.launch(uri)
                }
            }
            context is ComponentActivity && ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA) -> {
                showRationaleDialog = true
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    // 1. Cover Photo Area
    WriteCoverPhoto(
        coverPhotoUri = coverPhotoUri,
        onClick = { handleCameraClick() }
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 2. Action Bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { handleCameraClick() },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF333333))
                    Text("카메라", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333)))
                }
            }
            VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
            TextButton(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF333333))
                    Text("갤러리", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333)))
                }
            }
        }
    }

    // 3. Thumbnails
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
    } else {
        Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Input Fields
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("제목", color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.4f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF546089)
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            singleLine = true
        )

        TextField(
            value = content,
            onValueChange = onContentChange,
            placeholder = { Text("오늘의 기록", color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.4f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF546089)
            ),
            textStyle = TextStyle(fontSize = 16.sp)
        )
    }

    if (BuildConfig.DEBUG) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = {
                    val existingUris = photoItems.map { it.uri }.toSet()
                    val sampleRes = listOf(R.drawable.sample_photo_1, R.drawable.sample_photo_2, R.drawable.sample_photo_3, R.drawable.sample_photo_4)
                    val next = sampleRes.map { "android.resource://${context.packageName}/$it" }.firstOrNull { it !in existingUris }
                    next?.let { onPhotosAdded(listOf(it)) }
                },
                shape = RoundedCornerShape(100),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("샘플 사진 추가 (DEBUG)", style = TextStyle(fontSize = 9.sp, color = Color.LightGray))
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
