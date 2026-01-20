package com.picday.diary.data.diary.repository

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import kotlin.collections.iterator

class InMemoryDiaryRepository(
    seedData: List<Diary> = seedDiaryData()
) : DiaryRepository {
    private val diaryByDate: MutableMap<LocalDate, MutableList<Diary>> = mutableMapOf()
    private val photosByDiaryId: MutableMap<String, MutableList<DiaryPhoto>> = mutableMapOf()

    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Diary>> {
        return kotlinx.coroutines.flow.flow {
            emit(getDiariesByDateRange(startDate, endDate))
        }
    }

    override suspend fun getPhotosSuspend(diaryId: String): List<DiaryPhoto> {
        return getPhotos(diaryId)
    }

    init {
        seedData.forEach { addDiaryInternal(it) }
    }

    override fun getByDate(date: LocalDate): List<Diary> {
        return diaryByDate[date]?.toList() ?: emptyList()
    }

    override fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaryByDate.entries
            .filter { (date, _) -> !date.isBefore(startDate) && !date.isAfter(endDate) }
            .flatMap { it.value }
            .toList()
    }

    override fun getDiaryById(diaryId: String): Diary? {
        for (diaries in diaryByDate.values) {
            diaries.firstOrNull { it.id == diaryId }?.let { return it }
        }
        return null
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

    override fun addDiaryForDate(
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

    override fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return photosByDiaryId[diaryId]?.toList() ?: emptyList()
    }

    override fun replacePhotos(diaryId: String, photoUris: List<String>) {
        val photos = photoUris.map { uri ->
            DiaryPhoto(
                id = System.nanoTime().toString(),
                diaryId = diaryId,
                uri = uri,
                createdAt = System.currentTimeMillis()
            )
        }
        photosByDiaryId[diaryId] = photos.toMutableList()
    }

    override fun deleteDiary(diaryId: String) {
        val iterator = diaryByDate.entries.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val diaries = entry.value

            val removed = diaries.removeIf { it.id == diaryId }
            if (removed) {
                if (diaries.isEmpty()) {
                    iterator.remove()
                }
                break
            }
        }

        // 관련 사진 데이터도 제거
        photosByDiaryId.remove(diaryId)
    }


    private fun addDiaryInternal(diary: Diary) {
        val day = diaryByDate.getOrPut(diary.date) { mutableListOf() }
        day.add(diary)
    }
}
