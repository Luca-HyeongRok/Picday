package com.picday.diary.domain.usecase.settings

import com.picday.diary.domain.repository.SettingsRepository
import java.time.LocalDate
import javax.inject.Inject

class SetDateCoverPhotoUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(date: LocalDate, uri: String?) {
        repository.setDateCoverPhotoUri(date, uri)
    }
}
