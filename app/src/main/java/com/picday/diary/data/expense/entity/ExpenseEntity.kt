package com.picday.diary.data.expense.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.picday.diary.data.diary.entity.DiaryEntity
import com.picday.diary.domain.expense.Expense

@Entity(
    tableName = "expense",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["diaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["diaryId"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    // DiaryEntity.id가 String 타입이라 FK도 String으로 맞춘다.
    val diaryId: String,
    val title: String,
    val amount: Int,
    val category: String?,
    val receiptImagePath: String?,
    val createdAt: Long
)

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        diaryId = diaryId,
        title = title,
        amount = amount,
        category = category,
        receiptImagePath = receiptImagePath
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        diaryId = diaryId,
        title = title,
        amount = amount,
        category = category,
        receiptImagePath = receiptImagePath,
        createdAt = System.currentTimeMillis()
    )
}
