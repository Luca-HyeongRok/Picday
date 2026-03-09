package com.picday.diary.domain.usecase.expense

import com.picday.diary.domain.expense.Expense
import com.picday.diary.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiaryExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    // 다이어리별 지출 목록 스트림을 반환한다.
    operator fun invoke(diaryId: String): Flow<List<Expense>> {
        return expenseRepository.getExpensesByDiaryId(diaryId)
    }
}
