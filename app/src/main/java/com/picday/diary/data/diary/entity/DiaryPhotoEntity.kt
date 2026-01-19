package com.picday.diary.data.diary.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.picday.diary.domain.diary.DiaryPhoto

@Entity(tableName = "diary_photo")
data class DiaryPhotoEntity(
    @PrimaryKey val id: String,
    val diaryId: String,
    val uri: String,
    val createdAt: Long
)

fun DiaryPhotoEntity.toDomain(): DiaryPhoto {
    return DiaryPhoto(
        id = id,
        diaryId = diaryId,
        uri = uri,
        createdAt = createdAt
    )
}

fun DiaryPhoto.toEntity(): DiaryPhotoEntity {
    return DiaryPhotoEntity(
        id = id,
        diaryId = diaryId,
        uri = uri,
        createdAt = createdAt
    )
}
