package com.makusha.incomatic.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.makusha.incomatic.data.ReviewPrefs
import com.makusha.incomatic.net.ApiClient
import com.makusha.incomatic.net.dto.CalculateResponse
import com.makusha.incomatic.net.dto.UsState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val form: CalculatorState = CalculatorState(),
    val section: CalculatorSection = CalculatorSection.EARNINGS,
    val usStates: List<UsState> = emptyList(),
    val result: CalculateResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Single ViewModel for Phase 2 — deliberately not yet split into
 * MainViewModel/SalaryCalculatorViewModel per the architecture doc's full
 * §8; that split earns its keep once Account/History/Equity stores exist in
 * later phases. Anonymous-only for now (no Bearer token — auth is Phase 4).
 *
 * [AndroidViewModel] (not plain [ViewModel]) purely to get a [Context]-safe
 * `application` for [ReviewPrefs] — same pattern as [AccountManager]'s
 * `SessionStore(application)`.
 */
class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val api = ApiClient.salaryCalculatorService
    private val reviewPrefs = ReviewPrefs(application)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    // One-shot event: the ViewModel can't hold an Activity to launch the
    // native review flow itself, so it just signals "eligible now" and lets
    // IncomaticShell (which has Activity/Context via LocalContext) do it.
    private val _reviewPromptEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val reviewPromptEvent: SharedFlow<Unit> = _reviewPromptEvent.asSharedFlow()

    init {
        loadUsStates()
    }

    private fun loadUsStates() {
        viewModelScope.launch {
            runCatching { api.getUsStates() }
                .onSuccess { states -> _uiState.update { it.copy(usStates = states) } }
            // Silent on failure — the state picker just stays empty; the
            // user can still fill in everything else and Calculate will
            // surface a clear error if the backend is unreachable at all.
        }
    }

    fun updateForm(transform: (CalculatorState) -> CalculatorState) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    fun setSection(section: CalculatorSection) {
        _uiState.update { it.copy(section = section) }
    }

    fun calculate(grantsRsuValue: Double = 0.0, onDone: () -> Unit = {}) {
        val form = _uiState.value.form
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { api.calculate(form.toCalculateRequest(grantsRsuValue)) }
                .onSuccess { response ->
                    _uiState.update { it.copy(result = response, isLoading = false) }
                    if (reviewPrefs.recordSuccessfulCalculation()) {
                        _reviewPromptEvent.tryEmit(Unit)
                    }
                    onDone()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Couldn't reach the calculator backend.",
                        )
                    }
                }
        }
    }
}
