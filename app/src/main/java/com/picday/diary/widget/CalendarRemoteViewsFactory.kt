package com.picday.diary.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Binder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.room.Room
import androidx.datastore.preferences.core.stringPreferencesKey
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import com.picday.diary.R
import com.picday.diary.data.diary.database.PicDayDatabase
import com.picday.diary.data.diary.repository.RoomDiaryRepository
import com.picday.diary.data.repository.dataStore
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

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private lateinit var diaryRepository: DiaryRepository
    private val imageLoader = ImageLoader(context)

    // 캘린더에 표시될 날짜 목록 (6주, 42개)
    private var calendarDays: List<LocalDate?> = emptyList()
    // 날짜별 사진 비트맵 맵 (미리 로드하여 사용)
    private var photoBitmaps: Map<LocalDate, Bitmap> = emptyMap()
    private var currentMonth: YearMonth = YearMonth.now()
    private var widgetYear: Int = currentMonth.year
    private var widgetMonth: Int = currentMonth.monthValue

    override fun onCreate() {
        // 위젯은 Hilt DI를 직접 사용할 수 없으므로, Repository를 수동으로 생성합니다.
        val db = Room.databaseBuilder(context, PicDayDatabase::class.java, "picday.db").build()
        diaryRepository = RoomDiaryRepository(db, db.diaryDao(), db.diaryPhotoDao())
        widgetYear = intent.getIntExtra("year", currentMonth.year)
        widgetMonth = intent.getIntExtra("month", currentMonth.monthValue)
    }

    override fun onDataSetChanged() {
        // 이 메소드는 동기적으로 실행되어야 하며, 시간이 오래 걸리는 작업은 여기서 수행합니다.
        val identity = Binder.clearCallingIdentity()
        try {
            runBlocking {
                widgetYear = intent.getIntExtra("year", YearMonth.now().year)
                widgetMonth = intent.getIntExtra("month", YearMonth.now().monthValue)
                currentMonth = YearMonth.of(widgetYear, widgetMonth)
                populateCalendarDays()
                fetchDiaryPhotosAndBitmaps()
            }
        } finally {
            Binder.restoreCallingIdentity(identity)
        }
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
        val diariesInMonth = diaryRepository.getDiariesStream(startDate, endDate).first()

        val preferences = try {
            context.dataStore.data.first()
        } catch (e: Exception) {
            Log.w("CalendarRemoteViewsFactory", "DataStore read failure (widget)", e)
            null
        }

        val photosByDate = diariesInMonth
            .groupBy { it.date }
            .mapNotNull { (date, diaries) ->
                val representativeDiary = diaries.maxByOrNull { it.createdAt } ?: return@mapNotNull null
                val coverKey = stringPreferencesKey("cover_$date")
                val coverUri = preferences?.get(coverKey)
                val fallbackUri = if (coverUri == null) {
                    diaryRepository.getPhotosSuspend(representativeDiary.id).firstOrNull()?.uri
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
                .size(Size(100, 100)) // 위젯 썸네일 크기 조절
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.image as? BitmapImage)?.bitmap?.let {
                    bitmaps[date] = it.toCircularBitmap()
                }
            }
        }
        photoBitmaps = bitmaps
    }

    override fun getViewAt(position: Int): RemoteViews {
        val date = calendarDays.getOrNull(position)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_calendar_day)

        if (date == null) {
            // 현재 월에 속하지 않는 날짜는 빈 뷰를 반환
            remoteViews.setTextViewText(R.id.day_text, "")
            remoteViews.setViewVisibility(R.id.day_cell_root, View.INVISIBLE)
            return remoteViews
        }

        remoteViews.setViewVisibility(R.id.day_cell_root, View.VISIBLE)
        remoteViews.setTextViewText(R.id.day_text, date.dayOfMonth.toString())

        // 오늘 날짜 강조
        if (date.isEqual(LocalDate.now())) {
            remoteViews.setInt(R.id.day_text, "setBackgroundResource", R.drawable.widget_today_background)
            remoteViews.setTextColor(R.id.day_text, Color.BLACK)
        } else {
            remoteViews.setInt(R.id.day_text, "setBackgroundResource", android.R.color.transparent)
            // 테마에 맞는 기본 텍스트 색상 적용 필요
            remoteViews.setTextColor(R.id.day_text, Color.GRAY)
        }

        // 사진이 있는 경우 썸네일 표시
        val photoBitmap = photoBitmaps[date]
        if (photoBitmap != null) {
            remoteViews.setImageViewBitmap(R.id.photo_thumbnail, photoBitmap)
            remoteViews.setViewVisibility(R.id.photo_thumbnail, View.VISIBLE)
            // 사진이 있으면 날짜 텍스트를 흰색으로 변경하여 가독성 확보
            remoteViews.setTextColor(R.id.day_text, Color.WHITE)
        } else {
            remoteViews.setViewVisibility(R.id.photo_thumbnail, View.GONE)
        }

        // 날짜 클릭 시 앱 실행을 위한 Intent 설정
        val fillInIntent = Intent().apply {
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
