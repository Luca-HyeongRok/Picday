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

/**
 * [DiaryRepository] 인터페이스의 Room 데이터베이스 구현체입니다.
 * Room DAO를 사용하여 데이터베이스와 상호작용하며, 도메인 모델과 데이터 모델 간의 변환을 처리합니다.
 */
class RoomDiaryRepository(
    private val database: PicDayDatabase, // Room 데이터베이스 인스턴스
    private val diaryDao: DiaryDao, // 다이어리 데이터 접근 객체
    private val diaryPhotoDao: DiaryPhotoDao // 다이어리 사진 데이터 접근 객체
) : DiaryRepository {
    /**
     * 특정 날짜에 해당하는 다이어리 목록을 조회합니다.
     * @param date 조회할 날짜
     * @return 해당 날짜의 다이어리 목록 (도메인 모델)
     */
    override suspend fun getByDate(date: LocalDate): List<Diary> {
        return diaryDao.getByDate(date.toEpochDay()).map { it.toDomain() }
    }

    /**
     * 특정 기간 내의 다이어리 목록을 Flow로 스트리밍합니다.
     * 데이터베이스 변경 시 자동으로 업데이트됩니다.
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 내 다이어리 목록의 Flow (도메인 모델)
     */
    override fun getDiariesStream(startDate: LocalDate, endDate: LocalDate): kotlinx.coroutines.flow.Flow<List<Diary>> {
        return diaryDao.getByDateRangeFlow(startDate.toEpochDay(), endDate.toEpochDay())
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    /**
     * 특정 다이어리 ID에 연결된 사진 목록을 조회합니다.
     * @param diaryId 다이어리 ID
     * @return 해당 다이어리의 사진 목록 (도메인 모델)
     */
    override suspend fun getPhotos(diaryId: String): List<DiaryPhoto> {
        return diaryPhotoDao.getByDiaryId(diaryId).map { it.toDomain() }
    }

    /**
     * 특정 날짜 범위에 해당하는 다이어리 목록을 조회합니다.
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 날짜 범위 내 다이어리 목록 (도메인 모델)
     */
    override suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaryDao.getByDateRange(startDate.toEpochDay(), endDate.toEpochDay())
            .map { it.toDomain() }
    }

    /**
     * 특정 다이어리 ID로 다이어리 하나를 조회합니다.
     * @param diaryId 조회할 다이어리 ID
     * @return 일치하는 다이어리 (도메인 모델) 또는 없으면 null
     */
    override suspend fun getDiaryById(diaryId: String): Diary? {
        return diaryDao.getById(diaryId)?.toDomain()
    }

    /**
     * 특정 날짜에 새 다이어리를 추가합니다 (사진 없음).
     * @param date 다이어리 날짜
     * @param title 다이어리 제목 (선택 사항)
     * @param content 다이어리 내용
     */
    override suspend fun addDiaryForDate(date: LocalDate, title: String?, content: String) {
        addDiaryForDate(date, title, content, emptyList())
    }

    /**
     * 특정 날짜에 새 다이어리를 추가하고 사진 URI 목록을 연결합니다.
     * 다이어리와 사진 추가는 단일 트랜잭션으로 처리됩니다.
     * @param date 다이어리 날짜
     * @param title 다이어리 제목 (선택 사항)
     * @param content 다이어리 내용
     * @param photoUris 연결할 사진 URI 목록
     */
    override suspend fun addDiaryForDate(
        date: LocalDate,
        title: String?,
        content: String,
        photoUris: List<String>
    ) {
        val diary = Diary(
            id = System.currentTimeMillis().toString(), // 고유 ID 생성
            date = date,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis()
        )
        val baseTime = System.currentTimeMillis()
        val photos = photoUris.mapIndexed { index, uri ->
            DiaryPhotoEntity(
                id = System.nanoTime().toString(), // 고유 ID 생성
                diaryId = diary.id,
                uri = uri,
                createdAt = baseTime + index // 순서 유지를 위한 생성 시간
            )
        }
        database.withTransaction { // 트랜잭션을 통해 원자적으로 처리
            diaryDao.insert(diary.toEntity()) // 다이어리 삽입
            if (photos.isNotEmpty()) {
                diaryPhotoDao.insertAll(photos) // 사진들 삽입
            }
        }
    }

    /**
     * 기존 다이어리의 제목과 내용을 업데이트합니다.
     * @param diaryId 업데이트할 다이어리 ID
     * @param title 새 제목 (선택 사항)
     * @param content 새 내용
     * @return 업데이트 성공 여부 (true: 성공, false: 실패)
     */
    override suspend fun updateDiary(diaryId: String, title: String?, content: String): Boolean {
        return diaryDao.updateDiary(diaryId, title, content) > 0 // 업데이트된 행의 수가 0보다 크면 성공
    }

    /**
     * 특정 날짜에 기록된 다이어리가 있는지 확인합니다.
     * @param date 확인할 날짜
     * @return 다이어리 기록이 있으면 true, 없으면 false
     */
    override suspend fun hasAnyRecord(date: LocalDate): Boolean {
        return diaryDao.existsByDate(date.toEpochDay())
    }

    /**
     * 특정 다이어리의 모든 사진을 새 사진 목록으로 교체합니다.
     * 기존 사진은 삭제되고 새 사진이 추가됩니다. 이 작업은 단일 트랜잭션으로 처리됩니다.
     * @param diaryId 사진을 교체할 다이어리 ID
     * @param photoUris 새 사진 URI 목록
     */
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
        database.withTransaction { // 트랜잭션을 통해 원자적으로 처리
            diaryPhotoDao.deleteByDiaryId(diaryId) // 기존 사진 삭제
            if (photos.isNotEmpty()) {
                diaryPhotoDao.insertAll(photos) // 새 사진 삽입
            }
        }
    }

    /**
     * 특정 다이어리를 삭제하고, 연결된 모든 사진도 함께 삭제.
     * 다이어리와 사진 삭제는 단일 트랜잭션으로 처리.
     * @param diaryId 삭제할 다이어리 ID
     */
    override suspend fun deleteDiary(diaryId: String) {
        // 다이어리와 관련 사진을 원자적으로 삭제
        database.withTransaction {
            diaryPhotoDao.deleteByDiaryId(diaryId) // 관련 사진 삭제
            diaryDao.deleteById(diaryId) // 다이어리 삭제
        }
    }
}

