package com.picday.diary.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.picday.diary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Context 확장 프로퍼티를 사용하여 앱 전체에서 사용할 DataStore 인스턴스를 생성합니다.
// "settings"라는 이름으로 파일에 저장됩니다.
internal val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * [SettingsRepository] 인터페이스의 구현체입니다.
 * DataStore를 사용하여 앱의 설정 값들을 로컬에 저장하고 관리합니다.
 */
class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    /**
     * DataStore에서 데이터를 읽을 때 발생할 수 있는 IO 예외를 처리하는 확장 함수입니다.
     * 예외 발생 시, 비어있는 Preferences 객체를 방출하여 Flow가 중단되는 것을 방지합니다.
     */
    private fun Flow<Preferences>.handleReadExceptions(): Flow<Preferences> {
        return catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    // 캘린더 배경 URI에 대한 설정 값 스트림
    override val calendarBackgroundUri: Flow<String?> = context.dataStore.data
        .handleReadExceptions()
        .map { preferences ->
            preferences[KEY_CALENDAR_BACKGROUND]
        }

    /**
     * 캘린더의 배경 이미지 URI를 DataStore에 저장합니다.
     * @param uri 저장할 이미지 URI. null일 경우 해당 설정을 제거합니다.
     */
    override suspend fun setCalendarBackgroundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(KEY_CALENDAR_BACKGROUND)
            } else {
                preferences[KEY_CALENDAR_BACKGROUND] = uri
            }
        }
    }

    companion object {
        // DataStore의 키들을 상수로 관리하여 오타를 방지하고 일관성을 유지합니다.
        private val KEY_CALENDAR_BACKGROUND = stringPreferencesKey("calendar_background_uri")
    }
}
