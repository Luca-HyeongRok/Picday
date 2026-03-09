package com.picday.diary.presentation.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picday.diary.domain.usecase.expense.AddExpenseUseCase
import com.picday.diary.domain.usecase.expense.DeleteExpenseUseCase
import com.picday.diary.domain.usecase.expense.GetDiaryExpenseTotalUseCase
import com.picday.diary.domain.usecase.expense.GetDiaryExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getDiaryExpensesUseCase: GetDiaryExpensesUseCase,
    private val getDiaryExpenseTotalUseCase: GetDiaryExpenseTotalUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseState())
    val state: StateFlow<ExpenseState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var currentDiaryId: String? = null

    // 화면에서 들어오는 Intent를 단일 진입점으로 처리한다.
    fun processIntent(intent: ExpenseIntent) {
        when (intent) {
            is ExpenseIntent.LoadExpenses -> handleLoadExpenses(intent.diaryId)
            is ExpenseIntent.AddExpense -> handleAddExpense(intent)
            is ExpenseIntent.DeleteExpense -> handleDeleteExpense(intent)
        }
    }

    private fun handleLoadExpenses(diaryId: String) {
        currentDiaryId = diaryId
        observeJob?.cancel()
        _state.update { it.copy(loading = true, errorMessage = null) }

        observeJob = viewModelScope.launch {
            // 목록/총액 Flow를 결합해 하나의 상태로 동기화한다.
            combine(
                getDiaryExpensesUseCase(diaryId),
                getDiaryExpenseTotalUseCase(diaryId)
            ) { expenses, total ->
                expenses to total
            }
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _state.update { current ->
                        current.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "지출 내역을 불러오지 못했습니다."
                        )
                    }
                }
                .collect { (expenses, total) ->
                    _state.update { current ->
                        current.copy(
                            expenses = expenses,
                            totalExpense = total.toSafeInt(),
                            loading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun handleAddExpense(intent: ExpenseIntent.AddExpense) {
        viewModelScope.launch {
            try {
                addExpenseUseCase(intent.expense)
                // 목록은 LoadExpenses에서 구독한 Flow가 자동 갱신한다.
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { current ->
                    current.copy(errorMessage = e.message ?: "지출 추가에 실패했습니다.")
                }
            }
        }
    }

    private fun handleDeleteExpense(intent: ExpenseIntent.DeleteExpense) {
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(intent.expense)
                // 목록은 LoadExpenses에서 구독한 Flow가 자동 갱신한다.
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { current ->
                    current.copy(errorMessage = e.message ?: "지출 삭제에 실패했습니다.")
                }
            }
        }
    }

    // Long 총액을 Int 상태로 안전하게 변환한다.
    private fun Long.toSafeInt(): Int {
        return when {
            this > Int.MAX_VALUE.toLong() -> Int.MAX_VALUE
            this < Int.MIN_VALUE.toLong() -> Int.MIN_VALUE
            else -> this.toInt()
        }
    }
}
