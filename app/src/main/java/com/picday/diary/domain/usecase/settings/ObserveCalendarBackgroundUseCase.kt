package com.picday.diary.domain.usecase.settings

import com.picday.diary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCalendarBackgroundUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<String?> {
        return repository.calendarBackgroundUri
    }
}
