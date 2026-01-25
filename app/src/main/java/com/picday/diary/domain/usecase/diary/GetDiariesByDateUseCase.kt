package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDiariesByDateUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(date: LocalDate): List<Diary> {
        return repository.getByDate(date)
    }
}
