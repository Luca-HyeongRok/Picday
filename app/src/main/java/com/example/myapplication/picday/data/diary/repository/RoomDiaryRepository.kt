package com.example.myapplication.picday.data.diary.repository

import androidx.room.withTransaction
import com.example.myapplication.picday.data.diary.dao.DiaryDao
import com.example.myapplication.picday.data.diary.dao.DiaryPhotoDao
import com.example.myapplication.picday.data.diary.database.PicDayDatabase
import com.example.myapplication.picday.data.diary.entity.DiaryPhotoEntity
import com.example.myapplication.picday.data.diary.entity.toDomain as photoToDomain
import com.example.myapplication.picday.data.diary.entity.toDomain
import com.example.myapplication.picday.data.diary.entity.toEntity
import com.example.myapplication.picday.domain.diary.Diary
import com.example.myapplication.picday.domain.diary.DiaryPhoto
import com.example.myapplication.picday.domain.repository.DiaryRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class RoomDiaryRepository(
    private val database: PicDayDatabase,
    private val diaryDao: DiaryDao,
    private val diaryPhotoDao: DiaryPhotoDao
) : DiaryRepository {
    override fun getByDate(date: LocalDate): List<Diary> = runBlocking(Dispatchers.IO) {
        diaryDao.getByDate(date.toEpochDay()).map { it.toDomain() }
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
        val photos = photoUris.map { uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diary.id,
                uri = uri,
                createdAt = System.currentTimeMillis()
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
        diaryPhotoDao.getByDiaryId(diaryId).map { it.photoToDomain() }
    }

    override fun replacePhotos(diaryId: String, photoUris: List<String>) {
        val photos = photoUris.map { uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(),
                diaryId = diaryId,
                uri = uri,
                createdAt = System.currentTimeMillis()
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
}
