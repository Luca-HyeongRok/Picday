package com.picday.diary.presentation.expense

import com.picday.diary.domain.expense.Expense

data class ExpenseState(
    val expenses: List<Expense> = emptyList(),
    val totalExpense: Int = 0,
    val loading: Boolean = false
)
