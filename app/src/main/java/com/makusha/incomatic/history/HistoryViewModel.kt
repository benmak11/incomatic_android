package com.makusha.incomatic.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makusha.incomatic.net.ApiClient
import com.makusha.incomatic.net.dto.SavedCalculationDetail
import com.makusha.incomatic.net.dto.SavedCalculationSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the list of saved calculations + load/delete actions for the History tab.
 * No separate HistoryService wrapper is needed here (unlike iOS's
 * CalculationHistoryService) — the session token attaches globally via
 * ApiClient's interceptor (see AccountManager), so this can call
 * SalaryCalculatorService directly, same as CalculatorViewModel/EquityViewModel.
 */
class HistoryViewModel : ViewModel() {
    private val api = ApiClient.salaryCalculatorService

    private val _sessions = MutableStateFlow<List<SavedCalculationSummary>>(emptyList())
    val sessions: StateFlow<List<SavedCalculationSummary>> = _sessions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** No-op when signed out — the endpoint 401s anonymous requests anyway. */
    fun load(signedIn: Boolean) {
        if (!signedIn) {
            _sessions.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching { api.listCalculations(limit = 50) }
                .onSuccess { _sessions.value = it.items }
                .onFailure { _errorMessage.value = it.message ?: "Couldn't load history" }
            _isLoading.value = false
        }
    }

    fun delete(id: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { api.deleteCalculation(id) }
                .onSuccess {
                    _sessions.update { it.filterNot { s -> s.id == id } }
                    onDone()
                }
                .onFailure { _errorMessage.value = it.message ?: "Couldn't delete this calculation" }
        }
    }

    suspend fun detail(id: String): SavedCalculationDetail = api.getCalculation(id)

    fun clearForSignOut() {
        _sessions.value = emptyList()
        _errorMessage.value = null
    }
}
