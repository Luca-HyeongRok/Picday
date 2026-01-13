package com.example.myapplication.picday.data.diary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt ASC")
    suspend fun getByDate(dateEpochDay: Long): List<DiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiaryEntity)

    @Update
    suspend fun update(entity: DiaryEntity)

    @Query("UPDATE diary SET title = :title, content = :content WHERE id = :id")
    suspend fun updateDiary(id: String, title: String?, content: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM diary WHERE dateEpochDay = :dateEpochDay)")
    suspend fun existsByDate(dateEpochDay: Long): Boolean
}
