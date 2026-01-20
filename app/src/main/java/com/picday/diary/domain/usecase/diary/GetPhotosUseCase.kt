package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.diary.DiaryPhoto
import com.picday.diary.domain.repository.DiaryRepository
import javax.inject.Inject

class GetPhotosUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    operator fun invoke(diaryId: String): List<DiaryPhoto> {
        return repository.getPhotos(diaryId)
    }
}
