package com.picday.diary.fakes

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class FakeDiaryRepository : DiaryRepository {
    private val diaries = mutableListOf<Diary>()
    private val photos = mutableListOf<DiaryPhoto>()

    // 변경사항을 관찰할 수 있도록 StateFlow를 사용합니다.
    private val diariesFlow = MutableStateFlow<List<Diary>>(emptyList())

    override suspend fun getByDate(date: LocalDate): List<Diary> {
        return diaries.filter { it.date == date }
    }

    override suspend fun getDiaryById(diaryId: String): Diary? {
        return diaries.find { it.id == diaryId }
    }

    override suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        val newDiary = Diary(UUID.randomUUID().toString(), date, title, content, System.currentTimeMillis())
        diaries.add(newDiary)
        notifyDiaryChanged()
    }

    override suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String, photoUris: List<String>) {
        val diaryId = UUID.randomUUID().toString()
        val newDiary = Diary(diaryId, date, title, content, System.currentTimeMillis())
        diaries.add(newDiary)
        photoUris.forEach { uri ->
            photos.add(DiaryPhoto(UUID.randomUUID().toString(), diaryId, uri, System.currentTimeMillis()))
        }
        notifyDiaryChanged()
    }

    override suspend fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        val index = diaries.indexOfFirst { it.id == diaryId }
        if (index != -1) {
            val old = diaries[index]
            diaries[index] = old.copy(title = title, content = content)
            notifyDiaryChanged()
            return true
        }
        return false
    }

    override suspend fun hasAnyRecord(date: LocalDate): Boolean {
        return diaries.any { it.date == date }
    }

    override suspend fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return photos.filter { it.diaryId == diaryId }
    }

    override suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaries.filter { it.date in startDate..endDate }
    }

    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> {
        // 실제 구현과 유사하게, diariesFlow에서 날짜 범위를 필터링하여 반환합니다.
        return diariesFlow.map { list ->
            list.filter { it.date in startDate..endDate }
        }
    }

    override suspend fun replacePhotos(diaryId: String, photoUris: List<String>) {
        photos.removeAll { it.diaryId == diaryId }
        photoUris.forEach { uri ->
            photos.add(DiaryPhoto(UUID.randomUUID().toString(), diaryId, uri, System.currentTimeMillis()))
        }
        notifyDiaryChanged()
    }

    override suspend fun deleteDiary(diaryId: String) {
        diaries.removeAll { it.id == diaryId }
        photos.removeAll { it.diaryId == diaryId }
        notifyDiaryChanged()
    }

    // --- Cover Photo Methods ---

    override fun getDateCoverPhotoUri(date: LocalDate): Flow<String?> {
        return diariesFlow.map { list ->
            list.filter { it.date == date }
                .firstNotNullOfOrNull { it.coverPhotoUri }
        }
    }

    override suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?) {
        val diariesOnDate = diaries.filter { it.date == date }
        if (diariesOnDate.isNotEmpty()) {
            val targetDiary = diariesOnDate.maxByOrNull { it.createdAt } ?: return

            // 기존 커버 사진 초기화
            diariesOnDate.forEach { diary ->
                val index = diaries.indexOfFirst { it.id == diary.id }
                if (index != -1 && diaries[index].coverPhotoUri != null) {
                    diaries[index] = diaries[index].copy(coverPhotoUri = null)
                }
            }
            // 새 커버 사진 설정
            if (uri != null) {
                val index = diaries.indexOfFirst { it.id == targetDiary.id }
                if (index != -1) {
                    diaries[index] = diaries[index].copy(coverPhotoUri = uri)
                }
            }
            notifyDiaryChanged()
        }
    }

    override fun observeMonthlyCoverPhotos(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        return diariesFlow.map { list ->
            list.filter { it.date.year == yearMonth.year && it.date.month == yearMonth.month }
                .mapNotNull { diary ->
                    diary.coverPhotoUri?.let { uri -> diary.date to uri }
                }
                .toMap()
        }
    }

    private fun notifyDiaryChanged() {
        diariesFlow.value = diaries.toList()
    }

    // 테스트에서 직접 데이터를 추가하기 위한 public 함수
    fun addDiary(diary: Diary) {
        diaries.add(diary)
        notifyDiaryChanged()
    }
}
