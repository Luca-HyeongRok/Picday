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
import java.time.LocalDate
import java.time.YearMonth

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

    /**
     * 특정 날짜의 커버 사진 URI를 Flow 형태로 관찰합니다.
     * @param date 조회할 날짜
     * @return 해당 날짜의 커버 사진 URI를 방출하는 Flow
     */
    override fun getDateCoverPhotoUri(date: LocalDate): Flow<String?> {
        val key = dateCoverPhotoKey(date)
        return context.dataStore.data
            .handleReadExceptions()
            .map { it[key] }
    }

    /**
     * 특정 날짜의 커버 사진 URI를 DataStore에 저장합니다.
     * @param date 설정할 날짜
     * @param uri 저장할 이미지 URI. null일 경우 해당 설정을 제거합니다.
     */
    override suspend fun setDateCoverPhotoUri(date: LocalDate, uri: String?) {
        val key = dateCoverPhotoKey(date)
        context.dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(key)
            } else {
                preferences[key] = uri
            }
        }
    }

    /**
     * 특정 월의 모든 커버 사진들을 관찰합니다.
     *
     * @param yearMonth 조회할 연월
     * @return Map<날짜, 커버 사진 URI> 형태의 데이터를 방출하는 Flow
     *
     * @warning
     * 현재 구현은 DataStore의 모든 키를 읽어와 필터링하므로,
     * 저장된 날짜별 커버 사진이 매우 많아질 경우 성능 저하가 발생할 수 있습니다.
     * 데이터가 많아지는 경우, 이 데이터를 Room DB로 이전하거나 JSON 형태로 묶어 저장하는 등
     * 다른 접근 방식을 고려해야 합니다.
     */
    override fun observeMonthlyCoverPhotos(yearMonth: YearMonth): Flow<Map<LocalDate, String>> {
        val prefix = DATE_COVER_PHOTO_KEY_PREFIX + yearMonth.toString()
        return context.dataStore.data
            .handleReadExceptions()
            .map { preferences ->
                preferences.asMap().keys
                    .filter { it.name.startsWith(prefix) }
                    .mapNotNull { key ->
                        val dateString = key.name.removePrefix(DATE_COVER_PHOTO_KEY_PREFIX)
                        try {
                            val date = LocalDate.parse(dateString)
                            val uri = preferences[key] as? String
                            if (uri != null) date to uri else null
                        } catch (e: Exception) {
                            // 날짜 파싱 실패 시 해당 키는 무시
                            null
                        }
                    }
                    .toMap()
            }
    }

    companion object {
        // DataStore의 키들을 상수로 관리하여 오타를 방지하고 일관성을 유지합니다.
        private val KEY_CALENDAR_BACKGROUND = stringPreferencesKey("calendar_background_uri")
        private const val DATE_COVER_PHOTO_KEY_PREFIX = "cover_"

        /**
         * 날짜에 해당하는 커버 사진의 DataStore 키를 생성합니다.
         * @param date 키를 생성할 날짜
         * @return "cover_YYYY-MM-DD" 형태의 Preferences.Key
         */
        private fun dateCoverPhotoKey(date: LocalDate): Preferences.Key<String> {
            return stringPreferencesKey(DATE_COVER_PHOTO_KEY_PREFIX + date.toString())
        }
    }
}
