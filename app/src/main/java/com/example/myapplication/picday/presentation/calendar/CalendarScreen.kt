package com.example.myapplication.picday.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel()
) {
    // ViewModel에서 상태 수집
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* -----------------------------
         * 월 이동 헤더 영역
         * ----------------------------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 이전 달 이동 버튼
            IconButton(onClick = { viewModel.onPreviousMonthClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달"
                )
            }

            // 현재 연/월 표시 (API 24 안전)
            Text(
                text = "${uiState.currentYearMonth.year}년 ${uiState.currentYearMonth.monthValue}월",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // 다음 달 이동 버튼
            IconButton(onClick = { viewModel.onNextMonthClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* -----------------------------
         * 캘린더 카드 영역
         * ----------------------------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                // 반투명 배경으로 카드 느낌 연출
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .padding(16.dp)
        ) {
            Column {

                // 요일 헤더 (일요일 시작)
                Row(modifier = Modifier.fillMaxWidth()) {
                    val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 날짜 그리드 (7열)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.calendarDays) { day ->
                        DayCell(day = day)
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(day: CalendarDay) {
    Box(
        modifier = Modifier
            // 날짜 셀은 정사각형
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                // 오늘 날짜는 강조 표시
                if (day.isToday) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            // 현재 달에 속한 날짜만 클릭 가능
            .clickable(enabled = day.isCurrentMonth) {
                // TODO: 날짜 선택 이벤트 처리
            },
        contentAlignment = Alignment.Center
    ) {

        // 현재 달 날짜만 숫자 표시
        if (day.isCurrentMonth) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (day.isToday)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
