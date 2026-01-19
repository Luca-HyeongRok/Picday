package com.picday.diary.fakes

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import java.util.UUID

class FakeDiaryRepository : DiaryRepository {
    private val diaries = mutableListOf<Diary>()
    private val photos = mutableListOf<DiaryPhoto>()

    override fun getByDate(date: LocalDate): List<Diary> {
        return diaries.filter { it.date == date }
    }

    override fun getDiaryById(diaryId: String): Diary? {
        return diaries.find { it.id == diaryId }
    }

    override fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        diaries.add(Diary(UUID.randomUUID().toString(), date, title, content, System.currentTimeMillis()))
    }

    override fun addDiaryForDate(date: LocalDate, title: String?, content: String, photoUris: List<String>) {
        val diaryId = UUID.randomUUID().toString()
        diaries.add(Diary(diaryId, date, title, content, System.currentTimeMillis()))
        photoUris.forEach { uri ->
            photos.add(DiaryPhoto(UUID.randomUUID().toString(), diaryId, uri, System.currentTimeMillis()))
        }
    }

    override fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        val index = diaries.indexOfFirst { it.id == diaryId }
        if (index != -1) {
            val old = diaries[index]
            diaries[index] = old.copy(title = title, content = content)
            return true
        }
        return false
    }

    override fun hasAnyRecord(date: LocalDate): Boolean {
        return diaries.any { it.date == date }
    }

    override fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return photos.filter { it.diaryId == diaryId }
    }

    override fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaries.filter { it.date in startDate..endDate }
    }

    override fun replacePhotos(diaryId: String, photoUris: List<String>) {
        photos.removeAll { it.diaryId == diaryId }
        photoUris.forEach { uri ->
            photos.add(DiaryPhoto(UUID.randomUUID().toString(), diaryId, uri, System.currentTimeMillis()))
        }
    }

    override fun deleteDiary(diaryId: String) {
        diaries.removeAll { it.id == diaryId }
        photos.removeAll { it.diaryId == diaryId }
    }
}
