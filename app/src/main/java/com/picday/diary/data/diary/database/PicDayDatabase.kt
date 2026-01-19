package com.picday.diary.data.diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.entity.DiaryEntity
import com.picday.diary.data.diary.entity.DiaryPhotoEntity

@Database(
    entities = [DiaryEntity::class, DiaryPhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PicDayDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryPhotoDao(): DiaryPhotoDao
}
