package com.picday.diary.domain.usecase.settings

import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class SetDateCoverPhotoUseCase @Inject constructor(
    private val repository: DiaryRepository
) {
    suspend operator fun invoke(date: LocalDate, uri: String?) {
        repository.setDateCoverPhotoUri(date, uri)
    }
}
