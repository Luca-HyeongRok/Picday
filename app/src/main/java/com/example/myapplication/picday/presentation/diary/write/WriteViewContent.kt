package com.example.myapplication.picday.presentation.diary.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.presentation.component.DiaryDetailCard

@Composable
fun ColumnScope.WriteViewContent(
    items: List<Diary>,
    onAddClick: () -> Unit,
    onEditClick: (Diary) -> Unit
) {
    if (items.isEmpty()) {
        Text(
            text = "이 날의 기록이 없습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("오늘의 첫 번째 기록 추가하기")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                DiaryDetailCard(
                    item = item,
                    onEditClick = { onEditClick(item) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("기록 추가하기")
        }
    }
}
