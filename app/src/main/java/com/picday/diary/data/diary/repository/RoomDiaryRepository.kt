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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.map

class RoomDiaryRepository(
    private val database: PicDayDatabase,
    private val diaryDao: DiaryDao,
    private val diaryPhotoDao: DiaryPhotoDao
) : DiaryRepository {
    override fun getByDate(date: LocalDate): List<Diary> = runBlocking(Dispatchers.IO) {
        diaryDao.getByDate(date.toEpochDay()).map { it.toDomain() }
    }

    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Diary>> {
        return diaryDao.getByDateRangeFlow(startDate.toEpochDay(), endDate.toEpochDay())
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getPhotosSuspend(diaryId: String): List<DiaryPhoto> {
        return diaryPhotoDao.getByDiaryId(diaryId).map { it.toDomain() }
    }

    override fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> =
        runBlocking(Dispatchers.IO) {
            diaryDao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay())
                .map { it.toDomain() }
        }

    override fun getDiaryById(diaryId: String): Diary? = runBlocking(Dispatchers.IO) {
        diaryDao.getById(diaryId)?.toDomain()
    }

    override fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        addDiaryForDate(date, title, content, emptyList())
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
        val baseTime = System.currentTimeMillis()
        val photos = photoUris.mapIndexed { index, uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diary.id,
                uri = uri,
                createdAt = baseTime + index
            )
        }
        runBlocking(Dispatchers.IO) {
            database.withTransaction {
                diaryDao.insert(diary.toEntity())
                if (photos.isNotEmpty()) {
                    diaryPhotoDao.insertAll(photos)
                }
            }
        }
    }

    override fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            diaryDao.updateDiary(diaryId, title, content) > 0
        }
    }

    override fun hasAnyRecord(date: LocalDate): Boolean = runBlocking(Dispatchers.IO) {
        diaryDao.existsByDate(date.toEpochDay())
    }

    override fun getPhotos(diaryId: String): List<DiaryPhoto> = runBlocking(Dispatchers.IO) {
        diaryPhotoDao.getByDiaryId(diaryId).map { it.toDomain() }
    }

    override fun replacePhotos(diaryId: String, photoUris: List<String>) {
        val baseTime = System.currentTimeMillis()
        val photos = photoUris.mapIndexed { index, uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diaryId,
                uri = uri,
                createdAt = baseTime + index
            )
        }
        runBlocking(Dispatchers.IO) {
            database.withTransaction {
                diaryPhotoDao.deleteByDiaryId(diaryId)
                if (photos.isNotEmpty()) {
                    diaryPhotoDao.insertAll(photos)
                }
            }
        }
    }

    override fun deleteDiary(diaryId: String) {
        runBlocking(Dispatchers.IO) {
            // Delete diary and related photos atomically.
            database.withTransaction {
                diaryPhotoDao.deleteByDiaryId(diaryId)
                diaryDao.deleteById(diaryId)
            }
        }
    }
}
