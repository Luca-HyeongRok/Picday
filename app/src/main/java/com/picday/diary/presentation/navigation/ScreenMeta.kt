package com.picday.diary.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.ui.graphics.vector.ImageVector

// UI/Navigation이 참고하는 정적 정의
sealed interface ScreenMeta {
    val route: String
    val title: String
    val icon: ImageVector
    val deepLink: String?
}

object ScreenMetaRegistry {
    object Calendar : ScreenMeta {
        override val route = "calendar"
        override val title = "Calendar"
        override val icon = Icons.Outlined.CalendarToday
        override val deepLink: String? = null
    }

    object Diary : ScreenMeta {
        override val route = "diary"
        override val title = "Diary"
        override val icon = Icons.Outlined.Edit
        override val deepLink: String? = null
    }

    object Write : ScreenMeta {
        override val route = "write/{date}/{mode}"
        override val title = "Write"
        override val icon = Icons.Outlined.Edit
        override val deepLink: String? = null
    }
}
