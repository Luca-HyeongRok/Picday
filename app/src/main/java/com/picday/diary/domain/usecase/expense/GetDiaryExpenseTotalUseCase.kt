package com.picday.diary.domain.usecase.expense

import com.picday.diary.domain.expense.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiaryExpenseTotalUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    // 다이어리별 지출 총액 스트림을 반환한다.
    operator fun invoke(diaryId: String): Flow<Long> {
        return expenseRepository.getDiaryExpenseTotal(diaryId)
    }
}
