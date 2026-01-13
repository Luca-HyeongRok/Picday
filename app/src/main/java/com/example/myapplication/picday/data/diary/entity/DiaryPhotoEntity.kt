package com.example.myapplication.picday.data.diary.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_photo")
data class DiaryPhotoEntity(
    @PrimaryKey val id: String,
    val diaryId: String,
    val uri: String,
    val createdAt: Long
)
