package com.picday.diary.data.diary.repository

import androidx.room.withTransaction
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.database.PicDayDatabase
import com.picday.diary.data.diary.entity.DiaryPhotoEntity
import com.picday.diary.data.diary.entity.toDomain
import com.picday.diary.data.diary.entity.toEntity
import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.map

class RoomDiaryRepository(
    private val database: PicDayDatabase,
    private val diaryDao: DiaryDao,
    private val diaryPhotoDao: DiaryPhotoDao
) : DiaryRepository {
    override suspend fun getByDate(date: LocalDate): List<Diary> {
        return diaryDao.getByDate(date.toEpochDay()).map { it.toDomain() }
    }

    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Diary>> {
        return diaryDao.getByDateRangeFlow(startDate.toEpochDay(), endDate.toEpochDay())
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return diaryPhotoDao.getByDiaryId(diaryId).map { it.toDomain() }
    }

    override suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaryDao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay())
            .map { it.toDomain() }
    }

    override suspend fun getDiaryById(diaryId: String): Diary? {
        return diaryDao.getById(diaryId)?.toDomain()
    }

    override suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        addDiaryForDate(date, title, content, emptyList())
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
        val baseTime = System.currentTimeMillis()
        val photos = photoUris.mapIndexed { index, uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diary.id,
                uri = uri,
                createdAt = baseTime + index
            )
        }
        database.withTransaction {
            diaryDao.insert(diary.toEntity())
            if (photos.isNotEmpty()) {
                diaryPhotoDao.insertAll(photos)
            }
        }
    }

    override suspend fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        return diaryDao.updateDiary(diaryId, title, content) > 0
    }

    override suspend fun hasAnyRecord(date: LocalDate): Boolean {
        return diaryDao.existsByDate(date.toEpochDay())
    }

    override suspend fun replacePhotos(diaryId: String, photoUris: List<String>) {
        val baseTime = System.currentTimeMillis()
        val photos = photoUris.mapIndexed { index, uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diaryId,
                uri = uri,
                createdAt = baseTime + index
            )
        }
        database.withTransaction {
            diaryPhotoDao.deleteByDiaryId(diaryId)
            if (photos.isNotEmpty()) {
                diaryPhotoDao.insertAll(photos)
            }
        }
    }

    override suspend fun deleteDiary(diaryId: String) {
        // Delete diary and related photos atomically.
        database.withTransaction {
            diaryPhotoDao.deleteByDiaryId(diaryId)
            diaryDao.deleteById(diaryId)
        }
    }
}
