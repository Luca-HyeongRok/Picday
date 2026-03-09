package com.picday.diary.domain.usecase.expense

import com.picday.diary.domain.expense.Expense
import com.picday.diary.domain.expense.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    // 지출 항목 삭제를 유스케이스 단위로 캡슐화한다.
    suspend operator fun invoke(expense: Expense) {
        expenseRepository.deleteExpense(expense)
    }
}
