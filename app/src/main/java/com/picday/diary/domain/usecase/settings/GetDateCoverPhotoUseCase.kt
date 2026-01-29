package com.picday.diary.domain.usecase.settings // 패키지명은 일단 유지

import com.picday.diary.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetDateCoverPhotoUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository // SettingsRepository 대신 DiaryRepository 주입
) {
    operator fun invoke(date: LocalDate): Flow<String?> {
        return diaryRepository.getDateCoverPhotoUri(date)
    }
}
