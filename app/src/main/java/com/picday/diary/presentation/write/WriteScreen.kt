package com.picday.diary.presentation.write

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.picday.diary.presentation.component.WriteTopBar
import com.picday.diary.presentation.diary.DiaryUiItem
import com.picday.diary.presentation.write.content.WriteContent
import com.picday.diary.presentation.write.state.WriteState
import com.picday.diary.presentation.write.state.WriteUiMode
import java.io.File
import java.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    selectedDate: LocalDate,
    items: List<DiaryUiItem>,
    writeState: WriteState,
    coverPhotoUri: String?,
    viewModePhotoUris: List<String> = emptyList(),
    currentPhotoIndex: Int = 0,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPageSelected: (Int) -> Unit = {},
    onPhotoClick: (String) -> Unit = {}
) {
    val isEditMode = writeState.uiMode != WriteUiMode.VIEW
    val isEditingExistingDiary = writeState.editingDiaryId != null

    val context = LocalContext.current

    fun onImagesPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
        } catch (_: Exception) {}
        }
        onPhotosAdded(uris.map { it.toString() })
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        onImagesPicked(uris)
    }

    var tempPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }

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
            tempPhotoUriString
                ?.let(Uri::parse)
                ?.let { uri -> onPhotosAdded(listOf(uri.toString())) }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createTempPictureUri()?.let { uri ->
                tempPhotoUriString = uri.toString()
                takePictureLauncher.launch(uri)
            } ?: run {
                Toast.makeText(context, "임시 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
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
                } ?: run {
                    Toast.makeText(context, "임시 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
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

    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(writeState.uiMode) {
        if (writeState.uiMode == WriteUiMode.VIEW) {
            showUnsavedDialog = false
        }
    }

    fun handleBack() {
        if (writeState.isDirty) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            text = { Text("저장하지 않고 나가시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onBack()
                }) {
                    Text("나가기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("계속 작성")
                }
            }
        )
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

    // 삭제 확인 다이얼로그 표시 여부
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = {
                Text(
                    "정말 이 기록을 삭제하시겠습니까?\n" +
                            "삭제된 데이터는 복구할 수 없습니다."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        writeState.editingDiaryId?.let { diaryId ->
                            onDelete(diaryId)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = { handleBack() },
                onSave = onSave,
                canSave = isEditMode && writeState.content.isNotBlank(),
                onDelete = if (isEditingExistingDiary) {
                    { showDeleteDialog = true }
                } else {
                    null
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WriteContent(
                uiMode = writeState.uiMode,
                coverPhotoUri = coverPhotoUri,
                viewModePhotoUris = viewModePhotoUris,
                currentPhotoIndex = currentPhotoIndex,
                items = items,
                title = writeState.title,
                content = writeState.content,
                photoItems = writeState.photoItems,
                onAddClick = onAddClick,
                onEditClick = onEditClick,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onPhotosAdded = onPhotosAdded,
                onPhotoRemoved = onPhotoRemoved,
                onPageSelected = onPageSelected,
                onPhotoClick = onPhotoClick,
                onCameraClick = { handleCameraClick() },
                onGalleryClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
    }
}
