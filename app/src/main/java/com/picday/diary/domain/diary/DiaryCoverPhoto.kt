package com.picday.diary.domain.diary

fun deriveCoverPhotoUri(photos: List<DiaryPhoto>): String? {
    // 저장된 대표사진이 없을 때 사용하는 fallback 헬퍼.
    return photos.lastOrNull()?.uri
}
