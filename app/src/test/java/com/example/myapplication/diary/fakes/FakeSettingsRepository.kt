package com.picday.diary.fakes

import com.picday.diary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.YearMonth

class FakeSettingsRepository : SettingsRepository {
    private val _calendarBackgroundUri = MutableStateFlow<String?>(null)
    override val calendarBackgroundUri: Flow<String?> = _calendarBackgroundUri

    override suspend fun setCalendarBackgroundUri(uri: String?) {
        _calendarBackgroundUri.value = uri
    }
}
