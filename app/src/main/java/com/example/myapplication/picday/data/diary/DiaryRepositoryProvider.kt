package com.example.myapplication.picday.data.diary

import android.content.Context
import androidx.room.Room

object DiaryRepositoryProvider {
    // DI 도입 전까지 Repository 생성 책임을 한곳에 모아둔다.
    fun provide(context: Context): DiaryRepository {
        val database = Room.databaseBuilder(
            context.applicationContext,
            PicDayDatabase::class.java,
            "picday.db"
        ).build()
        return RoomDiaryRepository(database.diaryDao())
    }
}
