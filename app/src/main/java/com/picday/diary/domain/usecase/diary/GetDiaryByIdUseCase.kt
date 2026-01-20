package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.diary.Diary
import com.picday.diary.domain.repository.DiaryRepository
import javax.inject.Inject

class GetDiaryByIdUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    operator fun invoke(diaryId: String): Diary? {
        return repository.getDiaryById(diaryId)
    }
}
