package com.picday.diary.di

import android.content.Context
import androidx.room.Room
import com.picday.diary.data.diary.dao.DiaryDao
import com.picday.diary.data.diary.dao.DiaryPhotoDao
import com.picday.diary.data.diary.dao.ExpenseDao
import com.picday.diary.data.diary.database.PicDayDatabase
import com.picday.diary.data.diary.repository.RoomDiaryRepository
import com.picday.diary.domain.repository.DiaryRepository
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
        )
            .addMigrations(PicDayDatabase.MIGRATION_1_2, PicDayDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideDiaryDao(database: PicDayDatabase): DiaryDao {
        return database.diaryDao()
    }

    @Provides
    fun provideDiaryPhotoDao(database: PicDayDatabase): DiaryPhotoDao {
        return database.diaryPhotoDao()
    }

    @Provides
    fun provideExpenseDao(database: PicDayDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideDiaryRepository(
        database: PicDayDatabase,
        diaryDao: DiaryDao,
        diaryPhotoDao: DiaryPhotoDao
    ): DiaryRepository {
        return RoomDiaryRepository(database, diaryDao, diaryPhotoDao)
    }
}
