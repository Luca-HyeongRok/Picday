package com.picday.diary.presentation.expense

import com.picday.diary.domain.expense.Expense

sealed interface ExpenseIntent {
    data class LoadExpenses(val diaryId: String) : ExpenseIntent
    data class AddExpense(val expense: Expense) : ExpenseIntent
    data class DeleteExpense(val expense: Expense) : ExpenseIntent
}
