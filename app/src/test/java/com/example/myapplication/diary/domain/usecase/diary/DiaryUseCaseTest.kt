package com.example.myapplication.diary.domain.usecase.diary

import com.picday.diary.domain.usecase.diary.AddDiaryForDateUseCase
import com.picday.diary.domain.usecase.diary.DeleteDiaryUseCase
import com.picday.diary.domain.usecase.diary.GetDiariesByDateRangeUseCase
import com.picday.diary.domain.usecase.diary.GetDiariesByDateUseCase
import com.picday.diary.domain.usecase.diary.GetDiaryByIdUseCase
import com.picday.diary.domain.usecase.diary.GetPhotosUseCase
import com.picday.diary.domain.usecase.diary.HasAnyRecordUseCase
import com.picday.diary.domain.usecase.diary.ReplacePhotosUseCase
import com.picday.diary.domain.usecase.diary.UpdateDiaryUseCase
import com.picday.diary.fakes.FakeDiaryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DiaryUseCaseTest {

    private lateinit var repository: FakeDiaryRepository
    private lateinit var addDiaryForDate: AddDiaryForDateUseCase
    private lateinit var updateDiary: UpdateDiaryUseCase
    private lateinit var replacePhotos: ReplacePhotosUseCase
    private lateinit var deleteDiary: DeleteDiaryUseCase
    private lateinit var getDiaryById: GetDiaryByIdUseCase
    private lateinit var getPhotos: GetPhotosUseCase
    private lateinit var getDiariesByDate: GetDiariesByDateUseCase
    private lateinit var getDiariesByDateRange: GetDiariesByDateRangeUseCase
    private lateinit var hasAnyRecord: HasAnyRecordUseCase

    @Before
    fun setUp() {
        repository = FakeDiaryRepository()
        addDiaryForDate = AddDiaryForDateUseCase(repository)
        updateDiary = UpdateDiaryUseCase(repository)
        replacePhotos = ReplacePhotosUseCase(repository)
        deleteDiary = DeleteDiaryUseCase(repository)
        getDiaryById = GetDiaryByIdUseCase(repository)
        getPhotos = GetPhotosUseCase(repository)
        getDiariesByDate = GetDiariesByDateUseCase(repository)
        getDiariesByDateRange = GetDiariesByDateRangeUseCase(repository)
        hasAnyRecord = HasAnyRecordUseCase(repository)
    }

    @Test
    fun `사진 없이 추가하면 사진이 생성되지 않는다`() = runTest {
        val date = LocalDate.of(2024, 1, 1)
        addDiaryForDate(date, "Title", "Content")

        val diaries = getDiariesByDate(date)
        assertEquals(1, diaries.size)
        assertTrue(getPhotos(diaries[0].id).isEmpty())
    }

    @Test
    fun `사진 포함 추가 시 사진이 생성된다`() = runTest {
        val date = LocalDate.of(2024, 1, 2)
        addDiaryForDate(date, "Title", "Content", listOf("uri1", "uri2"))

        val diary = getDiariesByDate(date).first()
        val photos = getPhotos(diary.id)
        assertEquals(2, photos.size)
        assertEquals(listOf("uri1", "uri2"), photos.map { it.uri })
    }

    @Test
    fun `조회와 업데이트 삭제가 정상 동작해야 한다`() = runTest {
        val date = LocalDate.of(2024, 1, 3)
        addDiaryForDate(date, "Title", "Content", listOf("uri1"))
        val diaryId = getDiariesByDate(date).first().id

        assertNotNull(getDiaryById(diaryId))
        assertTrue(hasAnyRecord(date))

        assertTrue(updateDiary(diaryId, "New", "Updated"))
        assertEquals("Updated", getDiaryById(diaryId)?.content)

        replacePhotos(diaryId, listOf("uri2"))
        assertEquals(listOf("uri2"), getPhotos(diaryId).map { it.uri })

        deleteDiary(diaryId)
        assertNull(getDiaryById(diaryId))
        assertFalse(hasAnyRecord(date))
    }

    @Test
    fun `기간 조회는 범위 내 기록만 반환한다`() = runTest {
        val start = LocalDate.of(2024, 2, 1)
        val end = LocalDate.of(2024, 2, 3)
        addDiaryForDate(start, "D1", "C1")
        addDiaryForDate(end, "D2", "C2")
        addDiaryForDate(end.plusDays(1), "D3", "C3")

        val range = getDiariesByDateRange(start, end)
        assertEquals(2, range.size)
    }
}
