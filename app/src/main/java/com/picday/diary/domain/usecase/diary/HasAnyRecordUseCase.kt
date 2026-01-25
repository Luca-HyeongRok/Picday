package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class HasAnyRecordUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(date: LocalDate): Boolean {
        return repository.hasAnyRecord(date)
    }
}
