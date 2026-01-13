package com.example.myapplication.picday.presentation.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.picday.presentation.component.DiaryItemCard
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@Composable
fun DiaryScreen(
    selectedDate: LocalDate = LocalDate.now(),
    onWriteClick: (LocalDate, WriteMode) -> Unit = { _, _ -> }
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
    val viewModel: DiaryViewModel = hiltViewModel(activity)
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(selectedDate) {
        viewModel.onDateSelected(selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // 1. [오늘의 기록] 섹션
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "${uiState.selectedDate.monthValue}월 ${uiState.selectedDate.dayOfMonth}일의 기록",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val latestRecord = uiState.items.lastOrNull()
            if (latestRecord != null) {
                // 오늘의 기록 표시 (마지막에 추가된 기록)
                DiaryItemCard(item = latestRecord)

                // 추가 기록 버튼 (작게 표시)
                OutlinedButton(
                    onClick = { onWriteClick(uiState.selectedDate, WriteMode.ADD) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("이 날의 기록 더 추가하기")
                }
            } else {
                // 기록 없음: 기록 추가 버튼 강조
                Button(
                    onClick = { onWriteClick(uiState.selectedDate, WriteMode.ADD) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // 높이를 주어 빈 공간 채움
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "오늘의 첫 번째 기록을 남겨보세요",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 2. [최근 기록] 섹션 (스크롤 가능 영역)
        val otherRecords = if (uiState.items.size > 1) {
            uiState.items.dropLast(1)
        } else {
            emptyList()
        }
        if (otherRecords.isNotEmpty()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "이 날의 기록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(otherRecords) { item ->
                        DiaryItemCard(item = item)
                    }
                }
            }
        } else {
             Spacer(modifier = Modifier.weight(1f))
        }
    }
}
