package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDiariesByDateRangeUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return repository.getDiariesByDateRange(startDate, endDate)
    }
}
