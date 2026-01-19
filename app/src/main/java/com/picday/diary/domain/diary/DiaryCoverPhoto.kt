package com.picday.diary.domain.diary

fun deriveCoverPhotoUri(photos: List<DiaryPhoto>): String? {
    return photos.firstOrNull()?.uri
}
