package com.example.myapplication.picday.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingsRepository {
    val calendarBackgroundUri: Flow<String?>
    suspend fun setCalendarBackgroundUri(uri: String?)
    
    fun getDateCoverPhotoUri(date: LocalDate): Flow<String?>
    suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?)
}
