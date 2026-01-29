package com.picday.diary.data.diary.repository

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

class InMemoryDiaryRepository(
    seedData: List<Diary> = seedDiaryData()
) : DiaryRepository {
    private val diaryByDate: MutableMap<LocalDate, MutableList<Diary>> = mutableMapOf()
    private val photosByDiaryId: MutableMap<String, MutableList<DiaryPhoto>> = mutableMapOf()

    // Flow를 통해 변경 사항을 알리기 위한 StateFlow
    private val _allDiariesFlow = MutableStateFlow<List<Diary>>(emptyList())

    init {
        seedData.forEach { addDiaryInternal(it) }
        _allDiariesFlow.value = currentDiariesSnapshot()
    }

    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> {
        return _allDiariesFlow.map { allDiaries ->
            allDiaries.filter { it.date in startDate..endDate }
        }
    }

    override suspend fun getByDate(date: LocalDate): List<Diary> {
        return diaryByDate[date]?.toList() ?: emptyList()
    }

    override suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaryByDate.entries
            .filter { (date, _) -> !date.isBefore(startDate) && !date.isAfter(endDate) }
            .flatMap { it.value }
            .toList()
    }

    override suspend fun getDiaryById(diaryId: String): Diary? {
        return _allDiariesFlow.value.find { it.id == diaryId }
    }

    override suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        val diary = Diary(
            id = System.currentTimeMillis().toString(),
            date = date,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        addDiaryInternal(diary)
        notifyDiariesChanged()
    }

    override suspend fun addDiaryForDate(
        date: LocalDate,
        title: String?,
        content: String,
        photoUris: List<String>
    ) {
        val diary = Diary(
            id = System.currentTimeMillis().toString(),
            date = date,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        addDiaryInternal(diary)
        if (photoUris.isNotEmpty()) {
            val photos = photoUris.map { uri ->
                DiaryPhoto(
                    id = System.nanoTime().toString(),
                    diaryId = diary.id,
                    uri = uri,
                    createdAt = System.currentTimeMillis()
                )
            }
            photosByDiaryId.getOrPut(diary.id) { mutableListOf() }.addAll(photos)
        }
        notifyDiariesChanged()
    }

    override suspend fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        for ((date, diaries) in diaryByDate) {
            val index = diaries.indexOfFirst { it.id == diaryId }
            if (index >= 0) {
                val current = diaries[index]
                diaries[index] = current.copy(title = title, content = content)
                notifyDiariesChanged()
                return true
            }
        }
        return false
    }

    override suspend fun hasAnyRecord(date: LocalDate): Boolean {
        return diaryByDate[date]?.isNotEmpty() == true
    }

    override suspend fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return photosByDiaryId[diaryId]?.toList() ?: emptyList()
    }

    override suspend fun replacePhotos(diaryId: String, photoUris: List<String>) {
        photosByDiaryId[diaryId] = photoUris.map { uri ->
            DiaryPhoto(
                id = System.nanoTime().toString(),
                diaryId = diaryId,
                uri = uri,
                createdAt = System.currentTimeMillis()
            )
        }.toMutableList()
        notifyDiariesChanged()
    }

    override suspend fun deleteDiary(diaryId: String) {
        val iterator = diaryByDate.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val diariesList = entry.value

            val removed = diariesList.removeIf { it.id == diaryId }
            if (removed) {
                if (diariesList.isEmpty()) {
                    iterator.remove()
                }
                break
            }
        }
        photosByDiaryId.remove(diaryId) // 관련 사진 데이터도 제거
        notifyDiariesChanged()
    }

    // --- Cover Photo Methods ---
    override fun getDateCoverPhotoUri(date: LocalDate): Flow<String?> {
        return _allDiariesFlow.map { allDiaries ->
            allDiaries.filter { it.date == date }
                .firstNotNullOfOrNull { it.coverPhotoUri }
        }
    }

    override suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?) {
        val diariesOnDate = diaryByDate[date] ?: mutableListOf()
        if (diariesOnDate.isNotEmpty()) {
            // 가장 최근에 생성된 다이어리를 커버로 설정할 대상으로 지정
            val targetDiary = diariesOnDate.maxByOrNull { it.createdAt }

            diariesOnDate.replaceAll { diary ->
                if (diary.id == targetDiary?.id) {
                    diary.copy(coverPhotoUri = uri)
                } else {
                    diary.copy(coverPhotoUri = null) // 다른 다이어리는 커버 해제
                }
            }
            notifyDiariesChanged()
        }
    }

    override fun observeMonthlyCoverPhotos(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        return _allDiariesFlow.map { allDiaries ->
            allDiaries
                .filter { it.date.year == yearMonth.year && it.date.month == yearMonth.month }
                .filter { it.coverPhotoUri != null }
                .associate { it.date to it.coverPhotoUri!! }
        }
    }

    // --- Internal Helpers ---
    private fun addDiaryInternal(diary: Diary) {
        val day = diaryByDate.getOrPut(diary.date) { mutableListOf() }
        day.add(diary)
    }

    private fun currentDiariesSnapshot(): List<Diary> {
        return diaryByDate.values.flatten().toList()
    }

    private fun notifyDiariesChanged() {
        _allDiariesFlow.value = currentDiariesSnapshot()
    }
}
