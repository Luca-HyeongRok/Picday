package com.example.myapplication.picday.presentation.diary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.presentation.component.DiaryItemCard
import com.example.myapplication.picday.presentation.navigation.WriteMode
import java.time.LocalDate

@Composable
fun DiaryScreen(
    uiState: DiaryUiState,
    onWriteClick: (LocalDate, WriteMode) -> Unit = { _, _ -> },
    onEditClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = "${uiState.selectedDate.monthValue}월 ${uiState.selectedDate.dayOfMonth}일의 기록",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.uiItems) { item ->
                DiaryItemCard(
                    item = item,
                    onClick = { onWriteClick(uiState.selectedDate, WriteMode.VIEW) },
                    onEditClick = { onEditClick(item.id) }
                )
            }
            if (uiState.uiItems.isEmpty()) {
                item {
                    Button(
                        onClick = { onWriteClick(uiState.selectedDate, WriteMode.ADD) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "오늘의 첫 번째 기록을 남겨보세요",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                item {
                    OutlinedButton(
                        onClick = { onWriteClick(uiState.selectedDate, WriteMode.ADD) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("이 날의 기록 더 추가하기")
                    }
                }
            }
        }
    }
}
