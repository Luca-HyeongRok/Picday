package com.picday.diary.domain.usecase.diary

import com.picday.diary.domain.repository.DiaryRepository
import com.picday.diary.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 다이어리를 삭제하는 유즈케이스입니다.
 * Room DB의 다이어리 정보와 DataStore의 커버 사진 설정을 모두 삭제하여
 * 데이터 정합성을 보장합니다.
 */
class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * @param diaryId 삭제할 다이어리의 ID
     */
    suspend operator fun invoke(diaryId: String) {
        // 1. 삭제할 다이어리 정보를 먼저 조회하여 날짜를 얻어옵니다.
        val diaryToDelete = diaryRepository.getDiaryById(diaryId)

        if (diaryToDelete != null) {
            // 2. DataStore에서 해당 날짜의 커버 사진 설정을 제거합니다.
            settingsRepository.setDateCoverPhotoUri(diaryToDelete.date, null)

            // 3. Room DB에서 다이어리와 관련 사진들을 삭제합니다.
            diaryRepository.deleteDiary(diaryId)
        }
        // 만약 diaryToDelete가 null이라면, 이미 삭제되었거나 잘못된 ID이므로 아무 작업도 하지 않습니다.
    }
}
