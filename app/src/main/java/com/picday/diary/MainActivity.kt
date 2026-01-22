package com.picday.diary

import android.os.Bundle
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.picday.diary.presentation.main.MainScreen
import com.picday.diary.presentation.theme.PicDayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.activity.ComponentActivity
import com.picday.diary.widget.CalendarWidgetProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkEvent by mutableStateOf<DeepLinkEvent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            seedSampleImagesIfNeeded()
        }
        CalendarWidgetProvider.updateAll(this)

        handleIntent(intent)

        setContent {
            PicDayTheme() {
                MainScreen(deepLinkEvent = deepLinkEvent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val startDateString = intent.getStringExtra(EXTRA_START_DATE) ?: return
        
        try {
            val date = java.time.LocalDate.parse(startDateString)
            // Create a new event to trigger LaunchedEffect even if date is same
            deepLinkEvent = DeepLinkEvent(date)
        } catch (e: Exception) {
            // Ignore invalid date formats, fallback to default behavior (Calendar/Today)
            e.printStackTrace()
        }
    }
    
    // Wrapper to ensure unique events for LaunchedEffect
    data class DeepLinkEvent(
        val date: java.time.LocalDate,
        val timestamp: Long = System.nanoTime()
    )

    companion object {
        const val EXTRA_START_DATE = "start_date"
    }

    private fun seedSampleImagesIfNeeded() {
        val prefs = getSharedPreferences("debug_seed", MODE_PRIVATE)
        if (prefs.getBoolean("gallery_seeded_v1", false)) return

        lifecycleScope.launch(Dispatchers.IO) {
            val resolver = applicationContext.contentResolver
            val resIds = listOf(
                R.drawable.sample_photo_1,
                R.drawable.sample_photo_2,
                R.drawable.sample_photo_3,
                R.drawable.sample_photo_4,
                R.drawable.sample_photo_5,
                R.drawable.sample_photo_6
            )
            resIds.forEachIndexed { index, resId ->
                val displayName = "picday_sample_${index + 1}.jpg"
                if (imageExists(resolver, displayName)) return@forEachIndexed

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PicDay/")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@forEachIndexed
                val wroteBytes = resolver.openOutputStream(uri)?.use { output ->
                    resources.openRawResource(resId).use { input ->
                        input.copyTo(output)
                    }
                }
                if (wroteBytes == null) {
                    resolver.delete(uri, null, null)
                    return@forEachIndexed
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            }
            prefs.edit().putBoolean("gallery_seeded_v1", true).apply()
        }
    }

    private fun imageExists(resolver: android.content.ContentResolver, displayName: String): Boolean {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection: String
        val selectionArgs: Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME}=? AND ${MediaStore.Images.Media.RELATIVE_PATH}=?"
            selectionArgs = arrayOf(displayName, "Pictures/PicDay/")
        } else {
            selection = "${MediaStore.Images.Media.DISPLAY_NAME}=?"
            selectionArgs = arrayOf(displayName)
        }
        resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        ).use { cursor ->
            return cursor?.moveToFirst() == true
        }
    }
}
