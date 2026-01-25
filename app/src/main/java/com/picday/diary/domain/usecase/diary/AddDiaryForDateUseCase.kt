package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class AddDiaryForDateUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        title: String?,
        content: String,
        photoUris: List<String>? = null
    ) {
        if (photoUris == null) {
            repository.addDiaryForDate(date, title, content)
        } else {
            repository.addDiaryForDate(date, title, content, photoUris)
        }
    }
}
