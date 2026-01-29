package com.picday.diary.data.diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.entity.DiaryEntity
import com.picday.diary.data.diary.entity.DiaryPhotoEntity

@Database(
    entities = [DiaryEntity::class, DiaryPhotoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PicDayDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryPhotoDao(): DiaryPhotoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE diary ADD COLUMN coverPhotoUri TEXT")
            }
        }
    }
}
