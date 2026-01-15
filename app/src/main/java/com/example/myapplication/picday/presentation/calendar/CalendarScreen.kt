package com.example.myapplication.picday.presentation.calendar

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
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    diaryViewModel: DiaryViewModel = hiltViewModel(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundUri by viewModel.backgroundUri.collectAsState()
    val coverPhotoByDate by diaryViewModel.coverPhotoByDate.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setBackgroundUri(uri.toString())
        }
    }

    var showBackgroundPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(uiState.calendarDays) {
        diaryViewModel.preloadCoverPhotos(uiState.calendarDays.map { it.date })
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Image
        if (backgroundUri != null) {
            CalendarBackgroundImage(uri = backgroundUri!!)
        }

        // Main Unified Calendar Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /* ---------- Unified Header (Inside Card) ---------- */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::onPreviousMonthClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "이전 달",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "${uiState.currentYearMonth.year} ${uiState.currentYearMonth.month.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )

                    IconButton(onClick = viewModel::onNextMonthClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "다음 달",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- Calendar Grid ---------- */
                Column {
                    // 요일
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(
                            items = uiState.calendarDays,
                            key = { it.date.toString() } // 고유 키 추가로 성능 향상
                        ) { day ->
                            val coverUri = remember(coverPhotoByDate, day.date) { 
                                coverPhotoByDate[day.date] 
                            }
                            DayCell(
                                day = day,
                                coverPhotoUri = coverUri,
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

        // Background Change Button (Top Right)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            IconButton(
                onClick = { showBackgroundPicker = true },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "배경 변경",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showBackgroundPicker) {
            BackgroundPickerSheet(
                sheetState = sheetState,
                onDismiss = { showBackgroundPicker = false },
                onSelectSample = { resId, context ->
                    val uri = "android.resource://${context.packageName}/$resId"
                    viewModel.setBackgroundUri(uri)
                    showBackgroundPicker = false
                },
                onSelectGallery = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    showBackgroundPicker = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackgroundPickerSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectSample: (Int, android.content.Context) -> Unit,
    onSelectGallery: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "배경화면 설정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            val samples = listOf(
                R.drawable.sample_photo_1,
                R.drawable.sample_photo_2,
                R.drawable.sample_photo_3,
                R.drawable.sample_photo_4,
                R.drawable.sample_photo_5,
                R.drawable.sample_photo_6
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(samples) { resId ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectSample(resId, context) }
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSelectGallery,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Image, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("갤러리에서 선택하기")
            }
        }
    }
}

@Composable
private fun CalendarBackgroundImage(uri: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .size(1200) // 배경화면은 FHD급 이하로 충분함
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 투명도가 있는 검은 색을 덮어 가독성 확보 (Scrim) - 더욱 어둡게 조정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
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
            kotlinx.coroutines.delay(150)
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
            // 오늘 날짜 기초 배경 (사진 없을 때만 살짝)
            if (day.isToday && coverPhotoUri == null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.05f)
                ) {}
            }

            // 대표 사진
            if (coverPhotoUri != null) {
                DayCoverPhoto(uri = coverPhotoUri)
            }

            // 오늘 날짜 테두리 (진한 회색으로 사진을 감싸도록 나중에 배치)
            if (day.isToday) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.5.dp, Color(0xFF333333))
                ) {}
            }

            // 클릭 피드백 (일시적)
            if (isPressed) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.1f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                ) {}
            }

            // 날짜 숫자
            Text(
                text = day.date.dayOfMonth.toString(),
                style = if (day.isToday) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                color = when {
                    coverPhotoUri != null -> Color.White
                    day.isToday -> Color.Black // 진한 검은색
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Normal
            )
        }
    }
}

/* ---------- Photo ---------- */

@Composable
private fun DayCoverPhoto(uri: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .size(160) // 썸네일은 작게 로드하여 메모리 절약
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
