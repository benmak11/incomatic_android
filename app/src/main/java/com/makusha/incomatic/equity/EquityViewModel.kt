package com.makusha.incomatic.equity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.net.ApiClient
import com.makusha.incomatic.net.dto.RsuGrant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the signed-in user's RSU grants via /v1/grants — no local storage.
 * Matches iOS's EquityStore: signed-out users have no grants at all (see
 * EquityCard.kt's signed-out state), so [load] just empties the list rather
 * than falling back to anything on-device.
 */
class EquityViewModel : ViewModel() {
    private val _grants = MutableStateFlow<List<RsuGrant>>(emptyList())
    val grants: StateFlow<List<RsuGrant>> = _grants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Grant-derived RSU value vesting in the current tax year. */
    val vestingThisYear: Double
        get() = VestMath.value(AppConfig.taxYear, grants.value)

    /** Distinct tickers/companies for the Equity card subline ("AAPL, RDDT"). */
    val tickerSummary: String
        get() = grants.value.map { it.ticker ?: it.company ?: "—" }.distinct().joinToString(", ")

    /** Call whenever sign-in state is known/changes — signed-out clears the list. */
    fun load(signedIn: Boolean) {
        if (!signedIn) {
            _grants.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _grants.value = ApiClient.salaryCalculatorService.listGrants()
            } catch (e: Exception) {
                _errorMessage.value = "Couldn't load grants"
            }
            _isLoading.value = false
        }
    }

    /** Create (null id) or update (existing id). Returns the saved grant, or null on failure. */
    suspend fun save(grant: RsuGrant): RsuGrant? {
        _errorMessage.value = null
        return try {
            val saved = if (grant.id == null) {
                ApiClient.salaryCalculatorService.createGrant(grant)
            } else {
                ApiClient.salaryCalculatorService.updateGrant(grant)
            }
            val current = _grants.value
            _grants.value = if (current.any { it.id == saved.id }) {
                current.map { if (it.id == saved.id) saved else it }
            } else {
                current + saved
            }
            saved
        } catch (e: Exception) {
            _errorMessage.value = "Couldn't save grant"
            null
        }
    }

    /** Optimistic removal; restores the grant and surfaces an error on failure. */
    fun delete(grant: RsuGrant) {
        val id = grant.id ?: return
        val current = _grants.value
        val idx = current.indexOfFirst { it.id == id }
        if (idx == -1) return
        _errorMessage.value = null
        _grants.value = current.toMutableList().apply { removeAt(idx) }
        viewModelScope.launch {
            try {
                ApiClient.salaryCalculatorService.deleteGrant(id)
            } catch (e: Exception) {
                _grants.value = _grants.value.toMutableList().apply { add(idx.coerceAtMost(size), grant) }
                _errorMessage.value = "Couldn't delete grant, restored"
            }
        }
    }
}
