package com.makusha.incomatic.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reviewDataStore by preferencesDataStore(name = "review_prefs")

/**
 * Tracks progress toward the in-app rating prompt — a running count of
 * successful calculations plus a one-time "already prompted" flag, so the
 * native Play In-App Review flow is requested exactly once, the moment the
 * count reaches 3. No repository layer needed, same as [OnboardingPrefs].
 */
class ReviewPrefs(private val context: Context) {
    private val successfulCalculationCountKey = intPreferencesKey("successful_calculation_count")
    private val hasPromptedKey = booleanPreferencesKey("has_prompted_for_review")

    val successfulCalculationCount: Flow<Int> =
        context.reviewDataStore.data.map { it[successfulCalculationCountKey] ?: 0 }

    val hasPrompted: Flow<Boolean> =
        context.reviewDataStore.data.map { it[hasPromptedKey] ?: false }

    /**
     * Increments the successful-calculation count. If the new count is
     * exactly 3 and the "already prompted" flag isn't set yet, flips that
     * flag and returns true (eligible to prompt now) — fire-once, so this
     * never returns true again after the first time.
     */
    suspend fun recordSuccessfulCalculation(): Boolean {
        var eligible = false
        context.reviewDataStore.edit { prefs ->
            val newCount = (prefs[successfulCalculationCountKey] ?: 0) + 1
            prefs[successfulCalculationCountKey] = newCount
            val alreadyPrompted = prefs[hasPromptedKey] ?: false
            if (newCount == 3 && !alreadyPrompted) {
                prefs[hasPromptedKey] = true
                eligible = true
            }
        }
        return eligible
    }
}
