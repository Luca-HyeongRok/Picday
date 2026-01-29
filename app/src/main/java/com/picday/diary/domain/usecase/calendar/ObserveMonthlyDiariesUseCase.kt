package com.picday.diary.domain.usecase.calendar

import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ObserveMonthlyDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository // 캘린더 배경 URI를 위해 여전히 필요
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        // 1. DiaryRepository에서 월간 대표사진 스트림을 가져옴 (이제 Room에서 가져옴)
        val monthlyCoverPhotosFlow = diaryRepository.observeMonthlyCoverPhotos(yearMonth)

        // 2. DB의 다이어리 스트림과 월간 대표사진 스트림을 결합
        return combine(
            diaryRepository.getDiariesStream(startDate, endDate),
            monthlyCoverPhotosFlow
        ) { diaries, savedCovers ->
            val diariesByDate = diaries.groupBy { it.date }
            val coverPhotoMap = mutableMapOf<LocalDate, String>()

            // 1순위: Room에 저장된 대표사진을 먼저 적용
            coverPhotoMap.putAll(savedCovers)

            // 2순위: 대표사진이 없는 날짜에 대해 Fallback 적용 (다이어리에 첨부된 첫 사진)
            diariesByDate.forEach { (date, dailyDiaries) ->
                if (coverPhotoMap.containsKey(date)) return@forEach

                // 해당 날짜의 다이어리 중 사진이 있는 마지막 다이어리의 마지막 사진을 대표사진으로 사용
                val fallbackUri = dailyDiaries.asReversed()
                    .firstNotNullOfOrNull { diary ->
                        // 여기서는 DiaryPhotoEntity가 아닌 Diary 모델의 coverPhotoUri를 참조해야 합니다.
                        // 하지만 이 usecase는 monthlyCoverPhotos를 통해 이미 커버사진을 받고 있으므로
                        // 여기서는 diaries의 photoUris를 사용하여 대체합니다.
                        // 이는 Diary 모델이 coverPhotoUri 필드를 가지고 있다면 더 깔끔하게 처리됩니다.
                        runBlocking { // 이 부분은 Flow에서 suspend 함수 호출 시 주의 필요. Flow의 map 내에서 suspend 호출은 권장되지 않음.
                            diaryRepository.getPhotos(diary.id).firstOrNull()?.uri
                        }
                    }
                
                if (fallbackUri != null) {
                    coverPhotoMap[date] = fallbackUri
                }
            }
            coverPhotoMap
        }
    }
}
