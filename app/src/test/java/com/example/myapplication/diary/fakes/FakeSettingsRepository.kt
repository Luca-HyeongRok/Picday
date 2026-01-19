package com.picday.diary.fakes

import com.picday.diary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeSettingsRepository : SettingsRepository {
    private val _calendarBackgroundUri = MutableStateFlow<String?>(null)
    override val calendarBackgroundUri: Flow<String?> = _calendarBackgroundUri

    private val _dateCoverPhotos = MutableStateFlow<Map<LocalDate, String?>>(emptyMap())

    override suspend fun setCalendarBackgroundUri(uri: String?) {
        _calendarBackgroundUri.value = uri
    }

    override fun getDateCoverPhotoUri(date: LocalDate): Flow<String?> {
        return _dateCoverPhotos.map { it[date] }
    }

    override suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?) {
        _dateCoverPhotos.value = _dateCoverPhotos.value + (date to uri)
    }
}
