package com.example.myapplication.picday.data.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

class InMemoryDiaryRepository(
    seedData: List<Diary> = emptyList()
) : DiaryRepository {
    private val diaryByDate: MutableMap<LocalDate, MutableList<Diary>> = mutableMapOf()

    init {
        seedData.forEach { addDiary(it) }
    }

    override fun getDiaries(date: LocalDate): List<Diary> {
        return diaryByDate[date]?.toList() ?: emptyList()
    }

    override fun addDiary(diary: Diary) {
        val day = diaryByDate.getOrPut(diary.date) { mutableListOf() }
        day.add(diary)
    }

    override fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        for ((date, diaries) in diaryByDate) {
            val index = diaries.indexOfFirst { it.id == diaryId }
            if (index >= 0) {
                val current = diaries[index]
                diaries[index] = current.copy(title = title, previewContent = content)
                return true
            }
        }
        return false
    }
}
