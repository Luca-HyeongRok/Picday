package com.example.myapplication.picday.data.diary.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.picday.data.diary.entity.DiaryEntity

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt ASC")
    suspend fun getByDate(dateEpochDay: Long): List<DiaryEntity>

    @Query("SELECT * FROM diary WHERE dateEpochDay >= :start AND dateEpochDay <= :end ORDER BY dateEpochDay ASC, createdAt ASC")
    suspend fun getByDateRange(start: Long, end: Long): List<DiaryEntity>

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(entity: DiaryEntity)


    @Update
    suspend fun update(entity: DiaryEntity)

    @Query("UPDATE diary SET title = :title, content = :content WHERE id = :id")
    suspend fun updateDiary(id: String, title: String?, content: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM diary WHERE dateEpochDay = :dateEpochDay)")
    suspend fun existsByDate(dateEpochDay: Long): Boolean

    @Query("DELETE FROM diary WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
