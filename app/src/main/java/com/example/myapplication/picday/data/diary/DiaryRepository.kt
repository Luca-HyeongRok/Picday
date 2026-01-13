package com.example.myapplication.picday.data.diary

import com.example.myapplication.picday.domain.diary.DayDiary
import com.example.myapplication.picday.domain.diary.Diary
import java.time.LocalDate

interface DiaryRepository {
    fun getDayDiary(date: LocalDate): DayDiary
    fun addDiary(diary: Diary)
}
