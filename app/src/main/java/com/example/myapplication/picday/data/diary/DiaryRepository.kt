package com.example.myapplication.picday.data.diary

import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

interface DiaryRepository {
    fun getDiaries(date: LocalDate): List<Diary>
    fun addDiary(diary: Diary)
    fun updateDiary(diaryId: String, title: String?, content: String): Boolean
}
