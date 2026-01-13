package com.example.myapplication.picday.presentation.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    date: LocalDate,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "${date.monthValue}월 ${date.dayOfMonth}일",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = canSave
            ) {
                Text("저장")
            }
        }
    )
}
