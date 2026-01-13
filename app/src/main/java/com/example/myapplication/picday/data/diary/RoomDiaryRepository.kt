package com.example.myapplication.picday.data.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RoomDiaryRepository(
    private val diaryDao: DiaryDao
) : DiaryRepository {
    override fun getByDate(date: LocalDate): List<Diary> = runBlocking(Dispatchers.IO) {
        diaryDao.getByDate(date.toEpochDay()).map { it.toDomain() }
    }

    override fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        val diary = Diary(
            id = System.currentTimeMillis().toString(),
            date = date,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        runBlocking(Dispatchers.IO) {
            diaryDao.insert(diary.toEntity())
        }
    }

    override fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            diaryDao.updateDiary(diaryId, title, content) > 0
        }
    }

    override fun hasAnyRecord(date: LocalDate): Boolean = runBlocking(Dispatchers.IO) {
        diaryDao.existsByDate(date.toEpochDay())
    }
}
