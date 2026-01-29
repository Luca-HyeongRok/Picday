package com.picday.diary.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingsRepository {
    val calendarBackgroundUri: Flow<String?>
    suspend fun setCalendarBackgroundUri(uri: String?)
}
