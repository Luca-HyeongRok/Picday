package com.example.myapplication.picday.data.diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.picday.data.diary.dao.DiaryPhotoDao
import com.example.myapplication.picday.data.diary.entity.DiaryPhotoEntity
import com.example.myapplication.picday.data.diary.dao.DiaryDao
import com.example.myapplication.picday.data.diary.entity.DiaryEntity

@Database(
    entities = [DiaryEntity::class, DiaryPhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PicDayDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun diaryPhotoDao(): DiaryPhotoDao
}
