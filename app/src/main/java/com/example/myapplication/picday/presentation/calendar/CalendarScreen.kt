package com.example.myapplication.picday.presentation.calendar

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coverPhotoByDate by diaryViewModel.coverPhotoByDate.collectAsState()

    LaunchedEffect(uiState.calendarDays) {
        diaryViewModel.preloadCoverPhotos(uiState.calendarDays.map { it.date })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            /* ---------- Header ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::onPreviousMonthClick) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }

                Text(
                    text = "${uiState.currentYearMonth.year} ${uiState.currentYearMonth.month.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = viewModel::onNextMonthClick) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }

            /* ---------- Calendar ---------- */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {

                    // 요일
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(uiState.calendarDays) { day ->
                            DayCell(
                                day = day,
                                coverPhotoUri = coverPhotoByDate[day.date],
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

/* ---------- Day Cell ---------- */

@Composable
fun DayCell(
    day: CalendarDay,
    coverPhotoUri: String?,
    onClick: (LocalDate) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(
                enabled = day.isCurrentMonth,
                onClick = {
                    isPressed = true
                    onClick(day.date)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.isCurrentMonth) {

            // 오늘 날짜 표시 (연한 회색)
            if (day.isToday) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {}
            }

            // 클릭 피드백 (일시적)
            if (isPressed) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {}
            }

            // 대표 사진
            if (coverPhotoUri != null) {
                DayCoverPhoto(uri = coverPhotoUri)
            }

            // 날짜 숫자
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (coverPhotoUri != null) Color.White
                else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/* ---------- Photo ---------- */

@Composable
private fun DayCoverPhoto(uri: String) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver
                    .openInputStream(android.net.Uri.parse(uri))
                    ?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
