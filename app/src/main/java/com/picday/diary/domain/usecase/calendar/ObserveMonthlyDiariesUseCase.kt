package com.picday.diary.domain.usecase.calendar

import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ObserveMonthlyDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        return diaryRepository.getDiariesStream(start, end).map { diaries ->
            // 날짜별로 다이어리를 그룹화
            val diariesByDate = diaries.groupBy { it.date }
            
            val coverPhotoMap = mutableMapOf<LocalDate, String>()

            diariesByDate.forEach { (date, dailyDiaries) ->
                // 편집 종료 시 저장된 대표사진을 우선 사용하고 없을 때만 사진 목록으로 fallback.
                val savedCover = settingsRepository.getDateCoverPhotoUri(date).firstOrNull()
                if (savedCover != null) {
                    coverPhotoMap[date] = savedCover
                    return@forEach
                }
                // 해당 날짜의 다이어리들 중 사진이 있는 첫 번째 것을 찾음
                // (최신순 정렬 등을 가정하거나, 단순히 첫 번째 발견되는 사진 사용)
                for (diary in dailyDiaries) {
                    val photos = diaryRepository.getPhotosSuspend(diary.id)
                    if (photos.isNotEmpty()) {
                        coverPhotoMap[date] = photos.last().uri
                        break // 해당 날짜의 대표 사진을 찾았으면 중단
                    }
                }
            }
            coverPhotoMap
        }
    }
}
