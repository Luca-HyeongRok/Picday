package com.example.myapplication.picday.data.diary.repository

import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.domain.repository.DiaryRepository
import java.time.LocalDate
import kotlin.collections.iterator

class InMemoryDiaryRepository(
    seedData: List<Diary> = seedDiaryData()
) : DiaryRepository {
    private val diaryByDate: MutableMap<LocalDate, MutableList<Diary>> = mutableMapOf()

    init {
        seedData.forEach { addDiaryInternal(it) }
    }

    override fun getByDate(date: LocalDate): List<Diary> {
        return diaryByDate[date]?.toList() ?: emptyList()
    }

    override fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        val diary = Diary(
            id = System.currentTimeMillis().toString(),
            date = date,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        addDiaryInternal(diary)
    }

    override fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        for ((date, diaries) in diaryByDate) {
            val index = diaries.indexOfFirst { it.id == diaryId }
            if (index >= 0) {
                val current = diaries[index]
                diaries[index] = current.copy(title = title, content = content)
                return true
            }
        }
        return false
    }

    override fun hasAnyRecord(date: LocalDate): Boolean {
        return diaryByDate[date]?.isNotEmpty() == true
    }

    private fun addDiaryInternal(diary: Diary) {
        val day = diaryByDate.getOrPut(diary.date) { mutableListOf() }
        day.add(diary)
    }
}
