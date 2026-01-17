package com.example.myapplication.picday.presentation.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.myapplication.picday.R
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import kotlinx.coroutines.delay
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

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // 앱 재시작시에도 접근 권한을 유지하기 위해 지속 가능한 권한 요청
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // 권한 획득 실패 시 로그 출력 등 추가 처리가 가능하지만, 우선 그대로 진행
            }
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
                .wrapContentHeight()
                .animateContentSize(), // 크기 변화 시 부드러운 애니메이션
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
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
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        items(
                            items = uiState.calendarDays,
                            key = { it.date.toString() }
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
                },
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.isCurrentMonth) {
            // 오늘 날짜 기초 배경
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

            // 오늘 날짜 테두리
            if (day.isToday) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.5.dp, Color(0xFF333333))
                ) {}
            }

            // 클릭 피드백
            if (isPressed) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.1f)
                ) {}
            }

            // 날짜 숫자 (항상 정중앙 유지)
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = if (day.isToday) 17.sp else 16.sp,
                color = when {
                    coverPhotoUri != null -> Color.White
                    day.isToday -> Color.Black
                    day.isHoliday -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (day.isToday) FontWeight.Black else FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // 공휴일 이름 (중앙 기준 12dp 오프셋으로 수동 조정)
            if (day.holidayName != null) {
                Text(
                    text = day.holidayName,
                    fontSize = 6.sp, // 긴 글자(대체공휴일 등) 잘림 방지를 위해 축소
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    letterSpacing = (-0.2).sp, // 자간을 미세하게 좁혀 가독성 확보
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 12.dp)
                        .padding(horizontal = 2.dp)
                )
            }
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
