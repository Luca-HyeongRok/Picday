package com.picday.diary.data.diary.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.picday.diary.data.diary.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expense WHERE diaryId = :diaryId ORDER BY createdAt ASC")
    fun getExpensesByDiaryId(diaryId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense WHERE diaryId = :diaryId")
    fun getTotalExpenseByDiary(diaryId: String): Flow<Int>
}
