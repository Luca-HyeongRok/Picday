package com.picday.diary.di

import com.picday.diary.domain.updater.CalendarWidgetUpdater
import com.picday.diary.widget.CalendarWidgetUpdaterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindCalendarWidgetUpdater(
        impl: CalendarWidgetUpdaterImpl
    ): CalendarWidgetUpdater
}
