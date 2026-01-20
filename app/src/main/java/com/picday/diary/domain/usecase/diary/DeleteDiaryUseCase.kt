package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.repository.DiaryRepository
import javax.inject.Inject

class DeleteDiaryUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    operator fun invoke(diaryId: String) {
        repository.deleteDiary(diaryId)
    }
}
