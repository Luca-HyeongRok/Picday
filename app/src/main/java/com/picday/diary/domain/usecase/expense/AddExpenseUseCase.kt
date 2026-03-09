package com.picday.diary.domain.usecase.expense

import com.picday.diary.domain.expense.Expense
import com.picday.diary.domain.expense.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    // 지출 항목 추가를 도메인 규칙의 단일 진입점으로 제공한다.
    suspend operator fun invoke(expense: Expense) {
        expenseRepository.addExpense(expense)
    }
}
