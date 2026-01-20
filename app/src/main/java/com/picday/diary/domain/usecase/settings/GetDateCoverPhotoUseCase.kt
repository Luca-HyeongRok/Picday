package com.picday.diary.domain.usecase.settings

import com.picday.diary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDateCoverPhotoUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(date: LocalDate): Flow<String?> {
        return repository.getDateCoverPhotoUri(date)
    }
}
