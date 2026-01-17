package com.example.myapplication.picday.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val calendarBackgroundUri: Flow<String?>
    suspend fun setCalendarBackgroundUri(uri: String?)
}
