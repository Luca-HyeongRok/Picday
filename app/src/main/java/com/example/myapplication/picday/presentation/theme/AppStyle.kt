package com.example.myapplication.picday.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    // Backgrounds
    val BackgroundLight = Color(0xFFF8FAFC)
    val BackgroundDark = Color(0xFF0F172A)
    
    // Surface
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1E293B)
    
    // Brand
    val Primary = Color(0xFF334155) // Slate
    val NavTint = Color(0xFFE2E8F0)
    
    // Text
    val TextMainLight = Color(0xFF1E293B)
    val TextSubLight = Color(0xFF94A3B8)
    val TextMainDark = Color(0xFFF1F5F9)
    val TextSubDark = Color(0xFF64748B)
    
    // UI Elements
    val DividerLight = Color(0xFFF1F5F9)
    val ShadowColor = Color(0xFF000000).copy(alpha = 0.08f)
    
    // Redesign Specific (Write/Edit Consistency)
    val WriteBackground = Color(0xFFF8F9FB)
}

object AppShapes {
    val Card = RoundedCornerShape(24.dp)
    val CardLg = RoundedCornerShape(32.dp)
    val Thumbnail = RoundedCornerShape(16.dp)
    val Button = RoundedCornerShape(100.dp) // Pill
    val Input = RoundedCornerShape(16.dp)
    val BottomNav = RoundedCornerShape(100.dp)
}

object AppElevations {
    val Low = 2.dp
    val Medium = 8.dp
    val High = 24.dp
}
