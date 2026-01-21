package com.picday.diary.domain.usecase.calendar

import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveMonthlyDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        // 1. 효율적으로 DataStore에서 월간 대표사진 스트림을 가져옴
        val savedCoversFlow = settingsRepository.observeMonthlyCoverPhotos(yearMonth)

        // 2. DB의 다이어리 스트림과 DataStore의 대표사진 스트림을 결합
        return combine(
            diaryRepository.getDiariesStream(start, end),
            savedCoversFlow
        ) { diaries, savedCovers ->
            val diariesByDate = diaries.groupBy { it.date }
            val coverPhotoMap = mutableMapOf<LocalDate, String>()

            // 1순위: DataStore에 저장된 대표사진을 먼저 적용
            coverPhotoMap.putAll(savedCovers)

            // 2순위: DataStore에 대표사진이 없는 날짜에 대해 Fallback 적용
            diariesByDate.forEach { (date, dailyDiaries) ->
                if (coverPhotoMap.containsKey(date)) return@forEach

                // 해당 날짜의 다이어리 중 사진이 있는 마지막 다이어리의 마지막 사진을 대표사진으로 사용
                val fallbackUri = dailyDiaries.asReversed()
                    .firstNotNullOfOrNull { diary ->
                        diaryRepository.getPhotos(diary.id).lastOrNull()?.uri
                    }
                
                if (fallbackUri != null) {
                    coverPhotoMap[date] = fallbackUri
                }
            }
            coverPhotoMap
        }
    }
}
