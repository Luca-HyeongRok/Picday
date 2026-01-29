package com.picday.diary.widget

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Binder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Size
import com.picday.diary.R
import com.picday.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * 캘린더 위젯의 GridView를 채우기 위한 RemoteViewsFactory 구현체.
 * 각 날짜 셀의 UI를 생성하고 데이터를 바인딩하는 역할을 합니다.
 */
class CalendarRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private lateinit var diaryRepository: DiaryRepository
    private val imageLoader = ImageLoader(context)

    // 캘린더에 표시될 날짜 목록 (6주, 42개)
    private var calendarDays: List<LocalDate?> = emptyList()
    // 날짜별 사진 비트맵 맵 (미리 로드하여 사용)
    private var photoBitmaps: Map<LocalDate, Bitmap> = emptyMap()
    private var currentMonth: YearMonth = YearMonth.now()

    override fun onCreate() {
        updateCurrentMonthFromIntent() // Call the new method
    }

    override fun onDataSetChanged() {
        updateCurrentMonthFromIntent() // Call the new method
        // 이 메소드는 동기적으로 실행되어야 하며, 시간이 오래 걸리는 작업은 여기서 수행합니다.
        val identity = Binder.clearCallingIdentity()
        try {
            runBlocking {
                diaryRepository = WidgetRepositoryProvider.getDiaryRepository(context)
                populateCalendarDays()
                fetchDiaryPhotosAndBitmaps()
            }
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
    }

    /**
     * Provider로부터 받은 Intent에서 월 정보를 파싱하여 currentMonth를 업데이트합니다.
     * 이 방법은 SharedPreferences를 다시 조회하는 대신, Provider가 전달한 정확한 상태를 사용하도록 보장합니다.
     */
    private fun updateCurrentMonthFromIntent() {
        currentMonth = intent.data?.lastPathSegment?.let { monthString ->
            runCatching { YearMonth.parse(monthString) }.getOrNull()
        } ?: YearMonth.now()
    }

    private suspend fun populateCalendarDays() {
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfCalendar = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        val days = mutableListOf<LocalDate?>()
        var currentDay = firstDayOfCalendar

        // 6주(42일) 동안의 날짜를 추가
        for (i in 0 until 42) {
            if (currentDay.month == currentMonth.month) {
                days.add(currentDay)
            } else {
                // 현재 월에 속하지 않는 날은 null 처리하여 비활성화
                days.add(null)
            }
            currentDay = currentDay.plusDays(1)
        }
        calendarDays = days
    }

    private suspend fun fetchDiaryPhotosAndBitmaps() {
        val startDate = currentMonth.atDay(1)
        val endDate = currentMonth.atEndOfMonth()
        val diariesInMonth = diaryRepository.getDiariesByDateRange(startDate, endDate)

        val photosByDate = diariesInMonth
            .groupBy { it.date }
            .mapNotNull { (date, diaries) ->
                // 이제 DiaryRepository에서 직접 커버 사진 URI를 가져옵니다.
                val coverUri = diaryRepository.getDateCoverPhotoUri(date).first() // Use diaryRepository

                // 커버 사진이 없을 경우, 해당 날짜의 첫 번째 사진을 fallback으로 사용합니다.
                val fallbackUri = if (coverUri == null) {
                    var firstUri: String? = null
                    for (diary in diaries) {
                        val uri = diaryRepository.getPhotos(diary.id).firstOrNull()?.uri
                        if (uri != null) {
                            firstUri = uri
                            break
                        }
                    }
                    firstUri
                } else {
                    null
                }
                val selectedUri = coverUri ?: fallbackUri
                selectedUri?.let { uri -> date to uri }
            }
            .toMap()

        val bitmaps = mutableMapOf<LocalDate, Bitmap>()
        for ((date, uri) in photosByDate) {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // 위젯에서는 하드웨어 비트맵 사용 불가
                .size(Size(120, 120)) // 썸네일 크기를 약간 키움
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.image as? BitmapImage)?.bitmap?.let {
                    // 원형 비트맵으로 변환하여 저장
                    bitmaps[date] = it.toCircularBitmap()
                }
                if (bitmaps[date] == null) {
                    Log.w("WidgetPhoto", "비트맵 변환 실패: date=$date, uri=$uri")
                }
            } else {
                Log.w("WidgetPhoto", "이미지 로드 실패: date=$date, uri=$uri")
            }
        }
        photoBitmaps = bitmaps
    }

    override fun getViewAt(position: Int): RemoteViews {
        val date = calendarDays.getOrNull(position)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_calendar_day)

        if (date == null) {
            // 현재 월에 속하지 않는 날짜는 보이지 않도록 처리하되, 클릭 루트는 유지
            remoteViews.setViewVisibility(R.id.day_cell_root, View.VISIBLE)
            remoteViews.setTextViewText(R.id.day_text, "")
            remoteViews.setTextViewText(R.id.day_text_on_photo, "")
            remoteViews.setViewVisibility(R.id.day_ring, View.GONE)
            remoteViews.setViewVisibility(R.id.photo_thumbnail, View.GONE)
            remoteViews.setViewVisibility(R.id.day_text_on_photo, View.GONE)
            remoteViews.setViewVisibility(R.id.day_text, View.GONE)
            remoteViews.setFloat(R.id.day_cell_root, "setAlpha", 0f)
            return remoteViews
        }

        remoteViews.setViewVisibility(R.id.day_cell_root, View.VISIBLE)
        remoteViews.setFloat(R.id.day_cell_root, "setAlpha", 1f)
        val dayText = date.dayOfMonth.toString()
        remoteViews.setTextViewText(R.id.day_text, dayText)
        remoteViews.setTextViewText(R.id.day_text_on_photo, dayText)

        // 오늘 날짜 강조 (링 표시)
        if (date.isEqual(LocalDate.now())) {
            remoteViews.setViewVisibility(R.id.day_ring, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.day_ring, View.GONE)
        }

        // 사진이 있는 경우 썸네일 표시
        val photoBitmap = photoBitmaps[date]
        if (photoBitmap != null) {
            remoteViews.setImageViewBitmap(R.id.photo_thumbnail, photoBitmap)
            remoteViews.setViewVisibility(R.id.photo_thumbnail, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.day_text_on_photo, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.day_text, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.photo_thumbnail, View.GONE)
            remoteViews.setViewVisibility(R.id.day_text_on_photo, View.GONE)
            remoteViews.setViewVisibility(R.id.day_text, View.VISIBLE)
            val textColor = when (date.dayOfWeek) {
                java.time.DayOfWeek.SUNDAY -> Color.parseColor("#E53935")
                java.time.DayOfWeek.SATURDAY -> Color.parseColor("#1E88E5")
                else -> Color.parseColor("#222222")
            }
            remoteViews.setTextColor(R.id.day_text, textColor)
        }

        // 날짜 클릭 시 앱 실행을 위한 Intent 설정
        val fillInIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("app://picday.co/diary/${date}")
            putExtra("start_date", date.toString())
        }
        remoteViews.setOnClickFillInIntent(R.id.day_cell_root, fillInIntent)

        return remoteViews
    }

    private fun Bitmap.toCircularBitmap(): Bitmap {
        val size = width.coerceAtMost(height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, size, size)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val sourceRect = Rect((width - size) / 2, (height - size) / 2, (width + size) / 2, (height + size) / 2)
        canvas.drawBitmap(this, sourceRect, rect, paint)

        return output
    }

    override fun getCount(): Int = calendarDays.size
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDestroy() { /* no-op */ }
}
