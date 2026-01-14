package com.example.myapplication.picday.domain.diary

fun deriveCoverPhotoUri(photos: List<DiaryPhoto>): String? {
    return photos.firstOrNull()?.uri
}
