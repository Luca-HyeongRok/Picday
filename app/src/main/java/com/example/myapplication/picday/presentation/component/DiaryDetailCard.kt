package com.example.myapplication.picday.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.picday.domain.diary.Diary

@Composable
fun DiaryDetailCard(
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
