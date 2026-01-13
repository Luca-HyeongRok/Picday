package com.example.myapplication.picday.data.diary

import com.example.myapplication.picday.domain.diary.DayDiary
import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

class InMemoryDiaryRepository(
    seedData: List<Diary> = emptyList()
) : DiaryRepository {
    private val diaryByDate: MutableMap<LocalDate, DayDiary> = mutableMapOf()

    init {
        seedData.forEach { addDiary(it) }
    }

    override fun getDayDiary(date: LocalDate): DayDiary {
        return diaryByDate[date] ?: DayDiary(null, emptyList())
    }

    override fun addDiary(diary: Diary) {
        val day = diaryByDate[diary.date] ?: DayDiary(null, emptyList())
        val updatedDay = if (day.representative == null) {
            day.copy(representative = diary)
        } else {
            day.copy(recent = listOf(diary) + day.recent)
        }
        diaryByDate[diary.date] = updatedDay
    }
}
