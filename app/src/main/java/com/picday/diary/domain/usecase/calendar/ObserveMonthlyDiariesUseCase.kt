package com.picday.diary.domain.usecase.calendar

import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMonthlyDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        return diaryRepository.getDiariesStream(start, end).map { diaries ->
            // 날짜별로 다이어리를 그룹화
            val diariesByDate = diaries.groupBy { it.date }
            
            val coverPhotoMap = mutableMapOf<LocalDate, String>()

            diariesByDate.forEach { (date, dailyDiaries) ->
                // 해당 날짜의 다이어리들 중 사진이 있는 첫 번째 것을 찾음
                // (최신순 정렬 등을 가정하거나, 단순히 첫 번째 발견되는 사진 사용)
                for (diary in dailyDiaries) {
                    val photos = diaryRepository.getPhotosSuspend(diary.id)
                    if (photos.isNotEmpty()) {
                        coverPhotoMap[date] = photos.first().uri
                        break // 해당 날짜의 대표 사진을 찾았으면 중단
                    }
                }
            }
            coverPhotoMap
        }
    }
}
