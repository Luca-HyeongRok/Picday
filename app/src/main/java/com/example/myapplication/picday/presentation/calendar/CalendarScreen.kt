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
import java.time.LocalDate

import androidx.compose.ui.graphics.Brush

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    // ViewModel에서 상태 수집
    val uiState by viewModel.uiState.collectAsState()

    // 캘린더 전체를 화면 중앙에 배치하는 컨테이너
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            // 헤더와 캘린더 간 간격을 적당히 유지
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            /* -----------------------------
             * 월 이동 헤더 (중앙 집중형)
             * ----------------------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onPreviousMonthClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${uiState.currentYearMonth.year} ${uiState.currentYearMonth.month.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { viewModel.onNextMonthClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            /* -----------------------------
             * 캘린더 그리드 컨테이너
             * ----------------------------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // 플로팅 컨테이너 느낌을 위한 큰 라운드
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                // 하단으로 갈수록 밀도감 증가 (빈 공간 시각적 보완)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {

                    // 요일 헤더 (축약 표기)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 날짜 그리드 (7열 고정)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                        // 캘린더 박스 안에서 고정 높이 유지
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(uiState.calendarDays) { day ->
                            DayCell(
                                day = day,
                                isSelected = day.date == uiState.selectedDate,
                                onClick = { 
                                    viewModel.onDateSelected(it)
                                    onDateSelected(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val isToday = day.isToday

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(
                enabled = day.isCurrentMonth,
                onClick = { onClick(day.date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.isCurrentMonth) {

            // 배경 스타일 처리
            when {
                // 선택된 날짜: 솔리드 원 + 그림자
                isSelected -> {
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp
                    ) {}
                }

                // 오늘 날짜 (선택 안 된 상태): 테두리 링
                isToday -> {
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {}
                }
            }

            // 날짜 숫자 표시
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
