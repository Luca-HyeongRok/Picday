package com.picday.diary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.BitmapImage
import com.picday.diary.MainActivity
import com.picday.diary.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.picday.diary.data.repository.dataStore

class CalendarWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
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
        appWidgetId: Int
    ) {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)

        // 5x5 위젯에 충분한 크기인지 확인 (임계값은 기기 밀도에 따라 조정 필요)
        if (minWidth > 220 && minHeight > 220) {
            updateMainCalendarWidget(context, appWidgetManager, appWidgetId)
        } else {
            updateCoverWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private suspend fun updateMainCalendarWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_calendar_main)

        // 월/연도 텍스트 설정
        val month = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MMMM", Locale.KOREAN)
        views.setTextViewText(R.id.month_year_text, month.format(formatter))

        // GridView 어댑터 설정
        val intent = Intent(context, CalendarWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.calendar_grid, intent)

        // GridView 아이템 클릭 시 사용할 PendingIntent 템플릿 설정
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val clickPendingIntent = PendingIntent.getActivity(
            context, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("M.d / E", Locale.getDefault()))

        val dateKey = stringPreferencesKey("cover_${today}")
        val coverUriString = try {
            context.dataStore.data
                .map { preferences -> preferences[dateKey] }
                .first()
        } catch (e: Exception) {
            Log.w("CalendarWidget", "Failed to read cover photo from DataStore", e)
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
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, CalendarWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
