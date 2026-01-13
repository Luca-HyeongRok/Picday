package com.example.myapplication.picday.presentation.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    onSaveComplete: () -> Unit,
    onCalendarClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var editingDiaryId by rememberSaveable(selectedDate) { mutableStateOf<String?>(null) }
    var title by rememberSaveable(selectedDate) { mutableStateOf("") }
    var content by rememberSaveable(selectedDate) { mutableStateOf("") }

    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    val items = uiState.items
    val shouldEdit = mode == WriteMode.ADD || editingDiaryId != null
    val editingDiary = items.firstOrNull { it.id == editingDiaryId }

    LaunchedEffect(mode, editingDiaryId) {
        if (mode == WriteMode.ADD) {
            title = ""
            content = ""
        } else if (editingDiaryId == null) {
            title = ""
            content = ""
        } else {
            title = editingDiary?.title.orEmpty()
            content = editingDiary?.previewContent.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val titleModifier = if (mode == WriteMode.VIEW) {
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onCalendarClick)
        } else {
            Modifier.fillMaxWidth()
        }
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일의 기록",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = titleModifier
        )

        if (shouldEdit) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("제목 (선택)") },
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("본문 (필수)") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val normalizedTitle = title.trim().ifBlank { null }
                    if (mode == WriteMode.ADD) {
                        viewModel.addDiaryForDate(
                            date = selectedDate,
                            title = normalizedTitle,
                            content = content
                        )
                    } else {
                        val targetId = editingDiaryId
                        if (targetId != null) {
                            viewModel.updateDiary(
                                diaryId = targetId,
                                title = normalizedTitle,
                                content = content
                            )
                        }
                    }
                    onSaveComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = content.isNotBlank()
            ) {
                Text("저장")
            }
        } else {
            if (items.isEmpty()) {
                Text(
                    text = "기록이 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
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
            }
        }
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("수정")
        }
    }
}
