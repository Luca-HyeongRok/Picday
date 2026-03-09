package com.picday.diary.data.expense.repository

import com.picday.diary.data.expense.dao.ExpenseDao
import com.picday.diary.data.expense.entity.toDomain
import com.picday.diary.data.expense.entity.toEntity
import com.picday.diary.domain.expense.Expense
import com.picday.diary.domain.expense.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }

    override fun getExpensesByDiaryId(diaryId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByDiaryId(diaryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDiaryExpenseTotal(diaryId: String): Flow<Long> {
        return expenseDao.getTotalExpenseByDiary(diaryId)
    }
}
