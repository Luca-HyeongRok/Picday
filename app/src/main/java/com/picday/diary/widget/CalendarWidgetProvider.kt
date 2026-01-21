package com.picday.diary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
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

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        scope.cancel()
    }

    private suspend fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val today = LocalDate.now()
        // Format example: 9.21 / Tue
        val formattedDate = today.format(DateTimeFormatter.ofPattern("M.d / E", Locale.ENGLISH))

        // Get Photo URI from DataStore
        val dateKey = stringPreferencesKey("cover_${today}")
        val coverUriString = try {
            context.dataStore.data
                .map { preferences -> preferences[dateKey] }
                .first()
        } catch (e: Exception) {
            null
        }

        val views = RemoteViews(context.packageName, R.layout.widget_calendar_cover)
        views.setTextViewText(R.id.widget_date, formattedDate)

        // Load image or placeholder
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

        // Setup click intent to launch MainActivity
        val intent = Intent(context, MainActivity::class.java)
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
                    .allowHardware(false) // RemoteViews requires Software Bitmaps
                    .size(600) // Limit size to avoid excessive memory usage in widget
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
