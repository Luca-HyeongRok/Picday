package com.example.myapplication.picday.domain.repository

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

interface DiaryRepository {
    fun getByDate(date: LocalDate): List<Diary>
    fun addDiaryForDate(date: LocalDate, title: String?, content: String)
    fun updateDiary(diaryId: String, title: String?, content: String): Boolean
    fun hasAnyRecord(date: LocalDate): Boolean
}