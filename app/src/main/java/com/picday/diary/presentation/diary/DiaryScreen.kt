package com.picday.diary.presentation.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.picday.diary.core.navigation.WriteMode
import com.picday.diary.presentation.component.DiaryItemCard
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DiaryScreen(
    uiState: DiaryUiState,
    onWriteClick: (LocalDate, WriteMode) -> Unit = { _, _ -> },
    onEditClick: (String) -> Unit = {},
    onPreviousDate: () -> Unit = {},
    onNextDate: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Spacer(modifier = Modifier.height(32.dp))

        // Header Section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDate) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 날짜"
                    )
                }
                Text(
                    text = "${uiState.selectedDate.monthValue}월 ${uiState.selectedDate.dayOfMonth}일의 기록",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextDate) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 날짜"
                    )
                }
            }

            val dayOfWeek = uiState.selectedDate.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                Locale.KOREAN
            )
            Text(
                text = "$dayOfWeek, ${uiState.selectedDate.year}년",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.uiItems) { item ->
                DiaryItemCard(
                    item = item,
                    onEditClick = { onEditClick(item.id) },
                    showEditIcon = true
                )
            }
            
            if (uiState.uiItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "이 날의 기록이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
