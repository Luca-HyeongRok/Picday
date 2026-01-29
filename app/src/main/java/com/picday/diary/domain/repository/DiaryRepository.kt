package com.picday.diary.domain.repository

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface DiaryRepository {
    suspend fun getByDate(date: LocalDate): List<Diary>
    suspend fun getDiaryById(diaryId: String): Diary?
    suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String)
    suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String, photoUris: List<String>)
    suspend fun updateDiary(diaryId: String, title: String?, content: String): Boolean
    suspend fun hasAnyRecord(date: LocalDate): Boolean
    suspend fun getPhotos(diaryId: String): List<DiaryPhoto>
    suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> // 범위 쿼리 추가
    fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>>
    suspend fun replacePhotos(diaryId: String, photoUris: List<String>)
    suspend fun deleteDiary(diaryId: String)

    // Cover Photo methods moved from SettingsRepository
    fun getDateCoverPhotoUri(date: LocalDate): Flow<String?>
    suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?)
    fun observeMonthlyCoverPhotos(yearMonth: YearMonth): Flow<Map<LocalDate, String>>
}
