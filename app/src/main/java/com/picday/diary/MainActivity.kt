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
import android.util.Log
import androidx.activity.ComponentActivity
import com.picday.diary.widget.CalendarWidgetProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkUri by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("WidgetClick", "MainActivity onCreate intent=$intent")
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            seedSampleImagesIfNeeded()
        }
        CalendarWidgetProvider.updateAll(this)

        handleIntent(intent)

        setContent {
            PicDayTheme {
                MainScreen(deepLinkUri = deepLinkUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("WidgetClick", "MainActivity onNewIntent intent=$intent")
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.d("WidgetClick", "MainActivity handleIntent intent=$intent")
        // 위젯을 통해 전달된 기존 Extra 기반의 날짜 처리
        val startDateString = intent.getStringExtra("start_date")
        if (startDateString != null) {
            // 기존 위젯 진입점을 새로운 딥링크 URI 형식으로 변환합니다.
            // timestamp를 추가하여 동일한 딥링크가 반복 호출되어도 `LaunchedEffect`가 트리거되도록 합니다.
            this.deepLinkUri = "app://picday.co/diary/$startDateString#${System.nanoTime()}"
            return
        }

        // 표준 딥링크 (intent.data) 처리
        val deepLinkData = intent.data ?: return
        // timestamp를 추가하여 동일한 딥링크가 반복 호출되어도 `LaunchedEffect`가 트리거되도록 합니다.
        this.deepLinkUri = deepLinkData.toString() + "#${System.nanoTime()}"
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
