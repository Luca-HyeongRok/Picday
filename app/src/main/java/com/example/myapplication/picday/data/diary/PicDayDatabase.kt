package com.example.myapplication.picday.data.diary

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DiaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PicDayDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
