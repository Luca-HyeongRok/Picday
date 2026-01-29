package com.picday.diary.widget

import android.content.Context
import androidx.room.Room
import com.picday.diary.data.diary.database.PicDayDatabase
import com.picday.diary.data.diary.repository.RoomDiaryRepository
import com.picday.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WidgetRepositoryProvider {
    private val lock = Any()
    @Volatile private var database: PicDayDatabase? = null
    @Volatile private var diaryRepository: DiaryRepository? = null

    suspend fun getDiaryRepository(context: Context): DiaryRepository = withContext(Dispatchers.IO) {
        diaryRepository ?: synchronized(lock) {
            diaryRepository ?: run {
                val appContext = context.applicationContext
                val db = database ?: Room.databaseBuilder(appContext, PicDayDatabase::class.java, "picday.db")
                    .addMigrations(PicDayDatabase.MIGRATION_1_2)
                    .build()
                    .also { database = it }
                RoomDiaryRepository(db, db.diaryDao(), db.diaryPhotoDao())
                    .also { diaryRepository = it }
            }
        }
    }

}
