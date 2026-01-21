package com.picday.diary.widget

import android.content.Context
import com.picday.diary.domain.updater.CalendarWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CalendarWidgetUpdaterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarWidgetUpdater {
    override fun updateAll() {
        CalendarWidgetProvider.updateAll(context)
    }
}
