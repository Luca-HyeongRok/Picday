package com.example.myapplication.picday.presentation.diary.write

import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.example.myapplication.picday.presentation.component.CircularPhotoPlaceholder
import com.example.myapplication.picday.presentation.component.WriteTopBar
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    selectedDate: LocalDate,
    mode: WriteMode,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
    val viewModel: DiaryViewModel = hiltViewModel(activity)
    val uiState by viewModel.uiState.collectAsState()
    val writeState by viewModel.writeState.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    val photoUris = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            photoUris.addAll(uris.map { it.toString() })
        }
    }

    val items = uiState.items
    val isEditMode = writeState.uiMode != WriteUiMode.VIEW
    val editingDiary = items.firstOrNull { it.id == writeState.editingDiaryId }

    // 날짜 변경 시 ViewModel 동기화
    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    // 편집 대상 변경 시 입력값 세팅
    LaunchedEffect(selectedDate, mode) {
        // 화면 진입 시 모드를 ViewModel로 위임
        viewModel.setWriteMode(mode)
        title = ""
        content = ""
        photoUris.clear()
    }

    LaunchedEffect(writeState.editingDiaryId, writeState.uiMode) {
        if (writeState.uiMode == WriteUiMode.ADD) {
            title = ""
            content = ""
        } else if (writeState.uiMode == WriteUiMode.EDIT && editingDiary != null) {
            title = editingDiary.title.orEmpty()
            content = editingDiary.previewContent
        }
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = onBack,
                onSave = {
                    val normalizedTitle = title.trim().ifBlank { null }
                    // 저장 로직은 ViewModel에서 처리
                    viewModel.onSaveClicked(
                        date = selectedDate,
                        title = normalizedTitle,
                        content = content,
                        photoUris = photoUris.toList()
                    )
                    onSaveComplete()
                },
                canSave = isEditMode && content.isNotBlank()
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

            CircularPhotoPlaceholder()

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

                if (photoUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        items(photoUris, key = { it }) { uri ->
                            PhotoThumbnail(
                                uri = uri,
                                onRemove = { photoUris.remove(uri) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                WriteEditContent(
                    title = title,
                    content = content,
                    onTitleChange = { title = it },
                    onContentChange = { content = it }
                )
            } else {
                WriteViewContent(
                    items = items,
                    onAddClick = {
                        viewModel.onAddClicked()
                    },
                    onEditClick = { diary ->
                        viewModel.onEditClicked(diary.id)
                    }
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
