package com.example.myapplication.picday.presentation.diary

import com.example.myapplication.picday.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

@HiltViewModel
class DiaryViewModel @Inject constructor(
    // Repository 생성 책임은 외부로 분리한다.
    private val repository: DiaryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        updateUiForDate(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        updateUiForDate(date)
    }

    fun hasAnyRecord(date: LocalDate): Boolean {
        return repository.hasAnyRecord(date)
    }

    private fun updateUiForDate(date: LocalDate) {
        val items = repository.getByDate(date)
        _uiState.update {
            it.copy(
                selectedDate = date,
                items = items
            )
        }
    }
}
