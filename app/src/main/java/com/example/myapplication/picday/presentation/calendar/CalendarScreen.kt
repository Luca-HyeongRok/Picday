package com.example.myapplication.picday.presentation.calendar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.picday.R
import com.example.myapplication.picday.presentation.diary.DiaryViewModel
import com.example.myapplication.picday.presentation.theme.AppShapes
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
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (ignored: Exception) {}
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
        // 1. Dynamic Background
        if (backgroundUri != null) {
            CalendarBackgroundImage(uri = backgroundUri!!)
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

        // 2. Main Calendar Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentHeight()
                .animateContentSize(),
            shape = AppShapes.CardLg,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::onPreviousMonthClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.currentYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.currentYearMonth.year.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    IconButton(onClick = viewModel::onNextMonthClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weekday Labels
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    listOf("S","M","T","W","T","F","S").forEach {
                        Text(
                            text = it,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Days Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    items(uiState.calendarDays, key = { it.date.toString() }) { day ->
                        val coverUri = remember(coverPhotoByDate, day.date) { coverPhotoByDate[day.date] }
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

        // Background Picker Trigger
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(44.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
        ) {
            IconButton(onClick = { showBackgroundPicker = true }) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color.`White`.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
            }
        }

        if (showBackgroundPicker) {
            BackgroundPickerSheet(
                sheetState = sheetState,
                onDismiss = { showBackgroundPicker = false },
                onSelectSample = { resId, context ->
                    viewModel.setBackgroundUri("android.resource://${context.packageName}/$resId")
                    showBackgroundPicker = false
                },
                onSelectGallery = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(text = "배경화면 설정", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 20.dp))

            val samples = listOf(R.drawable.sample_photo_1, R.drawable.sample_photo_2, R.drawable.sample_photo_3, R.drawable.sample_photo_4, R.drawable.sample_photo_5, R.drawable.sample_photo_6)

            LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(220.dp)) {
                items(samples) { resId ->
                    Box(modifier = Modifier.aspectRatio(1f).clip(AppShapes.Thumbnail).clickable { onSelectSample(resId, context) }) {
                        Image(painter = androidx.compose.ui.res.painterResource(id = resId), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onSelectGallery, modifier = Modifier.fillMaxWidth().height(56.dp), shape = AppShapes.Button, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Default.Image, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("갤러리에서 선택하기", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CalendarBackgroundImage(uri: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
    }
}

@Composable
fun DayCell(day: CalendarDay, coverPhotoUri: String?, onClick: (LocalDate) -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isPressed) { if (isPressed) { delay(150); isPressed = false } }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(
                enabled = day.isCurrentMonth,
                onClick = { isPressed = true; onClick(day.date) },
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.isCurrentMonth) {
            // Circle Background for Today or Photos
            if (coverPhotoUri != null) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(coverPhotoUri).crossfade(true).size(160).build(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
            } else if (day.isToday) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)))
            }

            // Today Border
            if (day.isToday) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = Color.Transparent, border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {}
            }

            // Date Text
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 15.sp,
                color = when {
                    coverPhotoUri != null -> Color.White
                    day.isToday -> MaterialTheme.colorScheme.primary
                    day.isHoliday -> Color(0xFFEF4444)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
            )
            
            // Pressed Overlay
            if (isPressed) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f))) }
        }
    }
}
