package com.example.myapplication.picday.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.picday.domain.repository.DiaryRepository
import com.example.myapplication.picday.data.diary.database.PicDayDatabase
import com.example.myapplication.picday.data.diary.repository.RoomDiaryRepository
import com.example.myapplication.picday.data.diary.dao.DiaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiaryModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PicDayDatabase {
        return Room.databaseBuilder(
            context,
            PicDayDatabase::class.java,
            "picday.db"
        ).build()
    }

    @Provides
    fun provideDiaryDao(database: PicDayDatabase): DiaryDao {
        return database.diaryDao()
    }

    @Provides
    fun provideDiaryRepository(diaryDao: DiaryDao): DiaryRepository {
        return RoomDiaryRepository(diaryDao)
    }
}