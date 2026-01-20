package com.picday.diary

import android.os.Bundle
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.picday.diary.presentation.main.MainScreen
import com.picday.diary.presentation.theme.PicDayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            seedSampleImagesIfNeeded()
        }
        setContent {
            PicDayTheme() {
                MainScreen()
            }
        }
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
                resolver.openOutputStream(uri)?.use { output ->
                    resources.openRawResource(resId).use { input ->
                        input.copyTo(output)
                    }
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
