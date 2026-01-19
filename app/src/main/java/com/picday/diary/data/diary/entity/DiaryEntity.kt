package com.picday.diary.data.diary.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.picday.diary.domain.diary.Diary
import java.time.LocalDate

@Entity(tableName = "diary")
data class DiaryEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val title: String?,
    val content: String,
    val createdAt: Long
)

// LocalDate는 타임존 영향을 받지 않는 epochDay(Long)로 저장한다.
fun DiaryEntity.toDomain(): Diary {
    return Diary(
        id = id,
        date = LocalDate.ofEpochDay(dateEpochDay),
        title = title,
        content = content,
        createdAt = createdAt
    )
}

fun Diary.toEntity(): DiaryEntity {
    return DiaryEntity(
        id = id,
        dateEpochDay = date.toEpochDay(),
        title = title,
        content = content,
        createdAt = createdAt
    )
}
