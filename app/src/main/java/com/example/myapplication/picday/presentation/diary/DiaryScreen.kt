package com.example.myapplication.picday.presentation.diary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    Text(
                        text = "이 날의 기록이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
            FloatingActionButton(onClick = { onWriteClick(uiState.selectedDate, WriteMode.ADD) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "기록 추가")
            }
        }
    }
}
