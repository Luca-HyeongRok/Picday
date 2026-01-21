package com.picday.diary.domain.usecase.calendar

import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMonthlyDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()

        val datesInMonth = buildList {
            var current = start
            while (!current.isAfter(end)) {
                add(current)
                current = current.plusDays(1)
            }
        }
        val savedCoversFlow = if (datesInMonth.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(
                datesInMonth.map { date ->
                    settingsRepository.getDateCoverPhotoUri(date).map { uri -> date to uri }
                }
            ) { pairs ->
                pairs.mapNotNull { (date, uri) -> uri?.let { date to it } }.toMap()
            }
        }

        return combine(
            diaryRepository.getDiariesStream(start, end),
            savedCoversFlow
        ) { diaries, savedCovers ->
            // 날짜별로 다이어리를 그룹화
            val diariesByDate = diaries.groupBy { it.date }

            val coverPhotoMap = mutableMapOf<LocalDate, String>()
            coverPhotoMap.putAll(savedCovers)

            diariesByDate.forEach { (date, dailyDiaries) ->
                if (coverPhotoMap.containsKey(date)) return@forEach
                // 편집 종료 시 저장된 대표사진을 우선 사용하고 없을 때만 사진 목록으로 fallback.
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
