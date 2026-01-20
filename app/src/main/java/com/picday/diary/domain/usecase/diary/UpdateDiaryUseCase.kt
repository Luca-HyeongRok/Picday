package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.repository.DiaryRepository
import javax.inject.Inject

class UpdateDiaryUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    operator fun invoke(diaryId: String, title: String?, content: String): Boolean {
        return repository.updateDiary(diaryId, title, content)
    }
}
