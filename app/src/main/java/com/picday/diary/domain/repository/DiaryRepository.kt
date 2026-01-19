package com.picday.diary.domain.repository

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import java.time.LocalDate

interface DiaryRepository {
    fun getByDate(date: LocalDate): List<Diary>
    fun getDiaryById(diaryId: String): Diary?
    fun addDiaryForDate(date: LocalDate, title: String?, content: String)
    fun addDiaryForDate(date: LocalDate, title: String?, content: String, photoUris: List<String>)
    fun updateDiary(diaryId: String, title: String?, content: String): Boolean
    fun hasAnyRecord(date: LocalDate): Boolean
    fun getPhotos(diaryId: String): List<DiaryPhoto>
    fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> // 범위 쿼리 추가
    fun replacePhotos(diaryId: String, photoUris: List<String>)
    fun deleteDiary(diaryId: String)
}
