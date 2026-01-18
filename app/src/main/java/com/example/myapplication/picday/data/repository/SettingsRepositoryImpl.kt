package com.example.myapplication.picday.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import com.example.myapplication.picday.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val KEY_CALENDAR_BACKGROUND = stringPreferencesKey("calendar_background_uri")

    override val calendarBackgroundUri: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_CALENDAR_BACKGROUND]
        }

    override suspend fun setCalendarBackgroundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(KEY_CALENDAR_BACKGROUND)
            } else {
                preferences[KEY_CALENDAR_BACKGROUND] = uri
            }
        }
    }

    override fun getDateCoverPhotoUri(date: LocalDate): Flow<String?> {
        val key = stringPreferencesKey("cover_${date}")
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { it[key] }
    }

    override suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?) {
        val key = stringPreferencesKey("cover_${date}")
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(key)
            } else {
                preferences[key] = uri
            }
        }
    }
}
