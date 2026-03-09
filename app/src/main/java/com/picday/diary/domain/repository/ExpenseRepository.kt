package com.picday.diary.domain.repository

import com.picday.diary.domain.expense.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    // 지출 항목을 저장한다.
    suspend fun addExpense(expense: Expense)

    // 지출 항목을 삭제한다.
    suspend fun deleteExpense(expense: Expense)

    // 특정 다이어리에 연결된 지출 목록을 구독한다.
    fun getExpensesByDiaryId(diaryId: String): Flow<List<Expense>>

    // 특정 다이어리의 지출 총액을 구독한다.
    fun getDiaryExpenseTotal(diaryId: String): Flow<Long>
}
