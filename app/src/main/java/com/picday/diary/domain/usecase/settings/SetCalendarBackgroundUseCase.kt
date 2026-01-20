package com.picday.diary.domain.usecase.settings

import com.picday.diary.domain.repository.SettingsRepository
import javax.inject.Inject

class SetCalendarBackgroundUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(uri: String?) {
        repository.setCalendarBackgroundUri(uri)
    }
}
