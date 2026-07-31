package com.makusha.incomatic.equity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.makusha.incomatic.AppConfig
import com.makusha.incomatic.data.GrantsStore
import com.makusha.incomatic.net.dto.RsuGrant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns the on-device RSU grants (see data/GrantsStore.kt — local-only until
 * Phase 4's auth unlocks /v1/grants sync). AndroidViewModel so Application
 * (leak-safe) is available for GrantsStore, rather than threading a raw
 * Context through from a Composable.
 */
class EquityViewModel(application: Application) : AndroidViewModel(application) {
    private val store = GrantsStore(application)

    val grants: StateFlow<List<RsuGrant>> = store.grants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    /** Grant-derived RSU value vesting in the current tax year. */
    val vestingThisYear: Double
        get() = VestMath.value(AppConfig.TAX_YEAR, grants.value)

    /** Distinct tickers/companies for the Equity card subline ("AAPL, RDDT"). */
    val tickerSummary: String
        get() = grants.value.map { it.ticker ?: it.company ?: "—" }.distinct().joinToString(", ")

    fun save(grant: RsuGrant) {
        viewModelScope.launch { store.save(grant) }
    }

    fun delete(id: String) {
        viewModelScope.launch { store.delete(id) }
    }
}
