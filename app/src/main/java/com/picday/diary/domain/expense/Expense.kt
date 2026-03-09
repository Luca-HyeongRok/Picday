package com.picday.diary.domain.expense

data class Expense(
    val id: Long = 0L,
    val diaryId: String,
    val title: String,
    val amount: Int,
    val category: String? = null,
    val receiptImagePath: String? = null
)
