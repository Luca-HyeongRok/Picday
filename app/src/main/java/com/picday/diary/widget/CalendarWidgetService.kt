package com.picday.diary.widget

import android.content.Intent
import android.widget.RemoteViewsService

class CalendarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        // Hilt를 사용하지 않으므로 Factory에 직접 Context와 Intent를 전달합니다.
        return CalendarRemoteViewsFactory(applicationContext, intent)
    }
}
