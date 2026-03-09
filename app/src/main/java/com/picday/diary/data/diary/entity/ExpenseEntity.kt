package com.picday.diary.data.diary.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    // 기존 DiaryEntity.id가 String 타입이므로 FK 타입을 동일하게 맞춘다.
    val diaryId: String,
    val title: String,
    val amount: Int,
    val category: String?,
    val receiptImagePath: String?,
    val createdAt: Long
)
