package com.example.myapplication.picday.presentation.write

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ColumnScope.WriteEditContent(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("제목") },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        label = { Text("오늘의 기록") },
        placeholder = { Text("오늘의 기억을 남겨보세요") }
    )
}
