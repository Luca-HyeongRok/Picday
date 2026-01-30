package com.picday.diary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.picday.diary.MainActivity
import com.picday.diary.R
import com.picday.diary.data.repository.dataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PREV_MONTH,
            ACTION_NEXT_MONTH -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val currentMonth = loadWidgetMonth(context, appWidgetId)
                        val newMonth = if (intent.action == ACTION_PREV_MONTH) {
                            currentMonth.minusMonths(1)
                        } else {
                            currentMonth.plusMonths(1)
                        }
                        saveWidgetMonth(context, appWidgetId, newMonth)

                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        updateAppWidget(context, appWidgetManager, appWidgetId, newMonth)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAll(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val pendingResult = goAsync()
        scope.launch {
            try {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scope.cancel()
    }

    private suspend fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        month: YearMonth? = null
    ) {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val layoutId = resolveLayout(appWidgetManager, appWidgetId, options)
        if (layoutId == R.layout.widget_calendar_cover) {
            updateCoverWidget(context, appWidgetManager, appWidgetId)
        } else {
            updateMainCalendarWidget(context, appWidgetManager, appWidgetId, layoutId, month)
        }
    }

    private suspend fun updateMainCalendarWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        layoutId: Int,
        specifiedMonth: YearMonth? = null
    ) {
        val views = RemoteViews(context.packageName, layoutId)

        // 월/연도 텍스트 설정 (전달된 specifiedMonth가 있으면 즉시 사용)
        val currentMonth = specifiedMonth ?: loadWidgetMonth(context, appWidgetId)
        val monthDate = currentMonth.atDay(1)
        val headerText = "${monthDate.year}년 ${monthDate.monthValue}월"
        views.setTextViewText(R.id.month_year_text, headerText)

        val backgroundKey = stringPreferencesKey("calendar_background_uri")
        val backgroundUriString = try {
            context.dataStore.data
                .map { preferences: Preferences -> preferences[backgroundKey] }
                .first()
        } catch (e: Exception) {
            Log.w("CalendarWidget", "Failed to read background photo from DataStore", e)
            null
        }

        val bitmap = if (backgroundUriString != null) {
            loadBitmap(context, backgroundUriString)
        } else {
            null
        }

        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_bg, bitmap)
        } else {
            views.setImageViewResource(R.id.widget_bg, R.drawable.sample_photo_1)
        }

        // GridView 어댑터 설정
        val intent = Intent(context, CalendarWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // 월 변경 시 RemoteViewsFactory가 캐싱되지 않도록 data에 월 정보를 포함
            data = Uri.parse("widget://calendar/$appWidgetId/${currentMonth}")
        }
        views.setRemoteAdapter(R.id.calendar_grid, intent)

        // 이전/다음 월 이동
        val prevMonthIntent = Intent(context, CalendarWidgetProvider::class.java).apply {
            action = ACTION_PREV_MONTH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val prevMonthPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            prevMonthIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_prev_month, prevMonthPendingIntent)

        val nextMonthIntent = Intent(context, CalendarWidgetProvider::class.java).apply {
            action = ACTION_NEXT_MONTH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val nextMonthPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId + 1,
            nextMonthIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_next_month, nextMonthPendingIntent)

        // GridView 아이템 클릭 시 사용할 PendingIntent 템플릿 설정
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val clickPendingIntent = PendingIntent.getActivity(
            context, appWidgetId, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.calendar_grid, clickPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.calendar_grid)
    }

    private suspend fun updateCoverWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val diaryRepository = WidgetRepositoryProvider.getDiaryRepository(context)

        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("M.d / E", Locale.getDefault()))

        val coverUriString = try {
            diaryRepository.getDateCoverPhotoUri(today).first()
        } catch (e: Exception) {
            Log.w("CalendarWidget", "Failed to read cover photo from Repository", e)
            null
        }

        val views = RemoteViews(context.packageName, R.layout.widget_calendar_cover)
        views.setTextViewText(R.id.widget_date, formattedDate)

        val bitmap = if (coverUriString != null) {
            loadBitmap(context, coverUriString)
        } else {
            null
        }

        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_bg, bitmap)
        } else {
            views.setImageViewResource(R.id.widget_bg, R.drawable.sample_photo_1)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            val deepLinkDate = today.toString()
            val deepLinkUriWithTimestamp = "app://picday.co/diary/$deepLinkDate#${System.nanoTime()}"
            data = Uri.parse(deepLinkUriWithTimestamp)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun loadBitmap(context: Context, uri: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .size(600)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    (result.image as? BitmapImage)?.bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        private const val ACTION_PREV_MONTH = "com.picday.diary.widget.ACTION_PREV_MONTH"
        private const val ACTION_NEXT_MONTH = "com.picday.diary.widget.ACTION_NEXT_MONTH"
        internal const val PREFS_NAME = "calendar_widget_state"
        internal const val WIDGET_MONTH_PREFIX = "widget_month_"
        private const val WIDGET_MONTH_FALLBACK_KEY = "widget_month_global"
        private const val MIN_SIZE_5X5_DP = 320

        // 이 함수는 주로 시스템 시간/날짜 변경과 같은 외부 이벤트에 반응하여 위젯의 내용을 새로 고쳐야 할 때 사용
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CalendarWidgetProvider::class.java)
            // 현재 홈 화면에 설치된 모든 해당 위젯의 ID 목록을 가져옴
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                // 앱이 실행 중이지 않아도 시스템이 위젯을 갱신할 수 있음
                context.sendBroadcast(intent)
            }
        }
    }

    private fun resolveMainLayout(options: Bundle): Int {
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        return if (minWidth >= MIN_SIZE_5X5_DP && minHeight >= MIN_SIZE_5X5_DP) {
            R.layout.widget_calendar_main_5x5
        } else {
            R.layout.widget_calendar_main
        }
    }

    private fun resolveLayout(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle
    ): Int {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (info?.initialLayout == R.layout.widget_calendar_cover) {
            return R.layout.widget_calendar_cover
        }
        return resolveMainLayout(options)
    }

    private fun loadWidgetMonth(context: Context, appWidgetId: Int): YearMonth {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = monthKey(appWidgetId)
        val stored = prefs.getString(key, null) ?: return YearMonth.now()
        return runCatching {
            YearMonth.parse(stored)
        }.getOrElse { YearMonth.now() }
    }

    private fun saveWidgetMonth(context: Context, appWidgetId: Int, month: YearMonth) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit(commit = true) {
            putString(monthKey(appWidgetId), month.toString())
        }
    }

    private fun monthKey(appWidgetId: Int): String {
        return if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            WIDGET_MONTH_FALLBACK_KEY
        } else {
            "$WIDGET_MONTH_PREFIX$appWidgetId"
        }
    }
}
