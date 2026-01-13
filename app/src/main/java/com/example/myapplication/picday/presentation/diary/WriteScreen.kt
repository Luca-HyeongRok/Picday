package com.example.myapplication.picday.presentation.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@Composable
fun WriteScreen(
    viewModel: DiaryViewModel = viewModel(),
    selectedDate: LocalDate,
    mode: WriteMode,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var editingDiaryId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    val items = uiState.items
    val isEditMode = mode == WriteMode.ADD || editingDiaryId != null
    val editingDiary = items.firstOrNull { it.id == editingDiaryId }

    // 날짜 변경 시 ViewModel 동기화
    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    // 모드/수정 대상 변경 시 입력값 세팅
    LaunchedEffect(mode, editingDiaryId) {
        when {
            mode == WriteMode.ADD -> {
                title = ""
                content = ""
            }
            editingDiaryId != null -> {
                title = editingDiary?.title.orEmpty()
                content = editingDiary?.previewContent.orEmpty()
            }
        }
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                date = selectedDate,
                onBack = onBack,
                onSave = {
                    val normalizedTitle = title.trim().ifBlank { null }

                    if (editingDiaryId == null) {
                        viewModel.addDiaryForDate(
                            date = selectedDate,
                            title = normalizedTitle,
                            content = content
                        )
                    } else {
                        viewModel.updateDiary(
                            diaryId = editingDiaryId!!,
                            title = normalizedTitle,
                            content = content
                        )
                    }
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

            // 원형 사진 Placeholder
            CircularPhotoPlaceholder()

            Spacer(modifier = Modifier.height(32.dp))

            if (isEditMode) {
                //  ADD / EDIT 모드
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("제목 (선택)") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = { Text("오늘의 기록") },
                    placeholder = { Text("오늘의 기억을 남겨보세요") }
                )
            } else {
                //  VIEW 모드 – 기록 리스트
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        DiaryDetailCard(
                            item = item,
                            onEditClick = { editingDiaryId = item.id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { editingDiaryId = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("이 날의 기록 추가하기")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteTopBar(
    date: LocalDate,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "${date.monthValue}월 ${date.dayOfMonth}일",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Text("저장")
            }
        }
    )
}

@Composable
private fun CircularPhotoPlaceholder() {
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                CircleShape
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                // TODO: 사진 선택 (Activity Result API)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DiaryDetailCard(
    item: Diary,
    onEditClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val displayTitle = item.title?.takeIf { it.isNotBlank() }

        if (displayTitle != null) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "${item.date.monthValue}월 ${item.date.dayOfMonth}일의 기록",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = item.previewContent,
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("수정")
        }
    }
}
