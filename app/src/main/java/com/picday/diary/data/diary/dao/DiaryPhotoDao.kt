package com.picday.diary.data.diary.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.picday.diary.data.diary.entity.DiaryPhotoEntity

@Dao
interface DiaryPhotoDao {
    @Query("SELECT * FROM diary_photo WHERE diaryId = :diaryId ORDER BY createdAt ASC")
    suspend fun getByDiaryId(diaryId: String): List<DiaryPhotoEntity>

    @Insert
    suspend fun insertAll(photos: List<DiaryPhotoEntity>)

    @Query("DELETE FROM diary_photo WHERE diaryId = :diaryId")
    suspend fun deleteByDiaryId(diaryId: String)
}
